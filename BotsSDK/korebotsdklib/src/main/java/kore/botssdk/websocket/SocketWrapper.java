package kore.botssdk.websocket;

import android.content.Context;
import android.net.Network;
import android.os.Handler;
import android.util.Log;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.commons.lang3.StringUtils;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kore.botssdk.event.KoreEventCenter;
import kore.botssdk.io.crossbar.autobahn.websocket.WebSocketConnection;
import kore.botssdk.io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import kore.botssdk.io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import kore.botssdk.io.crossbar.autobahn.websocket.interfaces.IWebSocket;
import kore.botssdk.io.crossbar.autobahn.websocket.types.WebSocketOptions;
import kore.botssdk.models.BotInfoModel;
import kore.botssdk.models.BotSocketOptions;
import kore.botssdk.net.RestBuilder;
import kore.botssdk.net.RestResponse;
import kore.botssdk.net.RestResponse.BotAuthorization;
import kore.botssdk.net.RestResponse.JWTTokenResponse;
import kore.botssdk.net.RestResponse.RTMUrl;
import kore.botssdk.utils.Constants;
import kore.botssdk.utils.Utils;
import retrofit2.Call;
import retrofit2.Response;

import static kore.botssdk.utils.Utils.accessTokenHeader;

/**
 * Created by Ramachandra Pradeep on 6/1/2016.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public final class SocketWrapper /*extends BaseSpiceManager*/ {

    private final String LOG_TAG = SocketWrapper.class.getSimpleName();

    public static SocketWrapper pKorePresenceInstance;
    private SocketConnectionListener socketConnectionListener = null;

//    private final WebSocketConnection mConnection = new WebSocketConnection();
    private final IWebSocket mConnection = new WebSocketConnection();
    private static Timer timer = new Timer();

    public boolean ismIsReconnectionAttemptNeeded() {
        return mIsReconnectionAttemptNeeded;
    }

    public void shouldAttemptToReconnect(boolean mIsReconnectionAttemptNeeded) {
        this.mIsReconnectionAttemptNeeded = mIsReconnectionAttemptNeeded;
    }

    private boolean mIsReconnectionAttemptNeeded = true;
    private boolean isConnecting = false;

//    private String url;
//    private URI uri;
//    private boolean mTLSEnabled = false;

    private HashMap<String, Object> optParameterBotInfo;
    private String accessToken;
    private String userAccessToken = null;

    private String anonymousUserAccessToken = null;
    private String JWTToken;
    private String uuId;
    private String auth;
    private String botUserId;
    private BotInfoModel botInfoModel;
    private BotSocketOptions options;

    private Context mContext;
    /**
     * initial reconnection delay 1 Sec
     */
    private int mReconnectDelay = 1000;

    /**
     * initial reconnection count
     */
    private int mReconnectionCount = 0;

    /**
     * Restricting outside object creation
     */
    private SocketWrapper(Context mContext) {
        /*start(mContext);*/
        this.mContext = mContext;
        KoreEventCenter.register(this);
    }

    public String getAccessToken(){
        return auth;
    }

    /**
     * The global default SocketWrapper instance
     */
    public static SocketWrapper getInstance(Context mContext) {
        if (pKorePresenceInstance == null) {
//            synchronized (SocketWrapper.class) {
//                if(pKorePresenceInstance == null) {
                    pKorePresenceInstance = new SocketWrapper(mContext);
//                }
//            }
        }
        return pKorePresenceInstance;
    }

    /**
     * To prevent cloning
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new CloneNotSupportedException("Clone not supported");
    }

    /**
     * Method to invoke connection for authenticated user
     *
     * @param accessToken   : AccessToken of the loged user.
     */
    public void connect(String accessToken,final BotInfoModel botInfoModel,SocketConnectionListener socketConnectionListener) {
        this.botInfoModel= botInfoModel;
        this.socketConnectionListener = socketConnectionListener;
        this.accessToken = accessToken;
        optParameterBotInfo = new HashMap<>();
        optParameterBotInfo.put(Constants.BOT_INFO, botInfoModel);

        //If spiceManager is not started then start it
        /*if (!isConnected()) {
            start(mContext);
        }*/


        rtmUrlRequest(accessToken, null, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RestResponse.RTMUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(RestResponse.RTMUrl response) {
                        try {
                            connectToSocket(response.getUrl(), false);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {

                    }
                    @Override
                    public void onComplete() {

                    }
                });

/*
        RestRequest<RestResponse.RTMUrl> request = new RestRequest<RestResponse.RTMUrl>(RestResponse.RTMUrl.class, null, accessToken) {
            @Override
            public RestResponse.RTMUrl loadDataFromNetwork() throws Exception {
                RestResponse.JWTTokenResponse jwtToken = getService().getJWTToken(accessTokenHeader());
                HashMap<String, Object> hsh = new HashMap<>();
                hsh.put(Constants.KEY_ASSERTION, jwtToken.getJwt());
                hsh.put(Constants.BOT_INFO, botInfoModel);

                RestResponse.BotAuthorization jwtGrant = getService().jwtGrant(hsh);
                auth = jwtGrant.getAuthorization().getAccessToken();
                this.accessToken = jwtGrant.getAuthorization().getAccessToken();
                botUserId = jwtGrant.getUserInfo().getUserId();

                RestResponse.RTMUrl rtmUrl = getService().getRtmUrl(accessTokenHeader(jwtGrant.getAuthorization().getAccessToken()), optParameterBotInfo);
                return rtmUrl;
            }
        };*/

        /*getSpiceManager().execute(request, new RequestListener<RestResponse.RTMUrl>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            @Override
            public void onRequestSuccess(RestResponse.RTMUrl response) {
                try {
                    connectToSocket(response.getUrl(), false);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });*/

    }

    private Observable<RestResponse.RTMUrl> rtmUrlRequest(final String _accessToken, final String jwtToken, final boolean isReconnect) {
        return Observable.create(new ObservableOnSubscribe<RTMUrl>() {
            @Override
            public void subscribe(ObservableEmitter<RTMUrl> emitter) throws Exception {
                try{
                    String access_token = Utils.accessTokenHeader(_accessToken);
                    HashMap<String, Object> hsh = new HashMap<>();
                    if(jwtToken == null) {
                        Call<JWTTokenResponse> _jwtToken = RestBuilder.getRestAPI().getJWTToken(access_token);
                        Response<JWTTokenResponse> rBody = _jwtToken.execute();
                        JWTTokenResponse jwtToken = rBody.body();
                        hsh.put(Constants.KEY_ASSERTION, jwtToken.getJwt());
                    }else {
                        hsh.put(Constants.KEY_ASSERTION, jwtToken);
                    }
                    hsh.put(Constants.BOT_INFO, botInfoModel);


                    HashMap<String, Object> hsh1 = new HashMap<>();
                    hsh1.put(Constants.BOT_INFO, botInfoModel);

                    Call<BotAuthorization> _jwtGrant = RestBuilder.getRestAPI().jwtGrant(hsh);
                    Response<BotAuthorization> rBody1 = _jwtGrant.execute();
                    BotAuthorization jwtGrant = rBody1.body();


                    auth = jwtGrant.getAuthorization().getAccessToken();
                    botUserId = jwtGrant.getUserInfo().getUserId();
                    accessToken = jwtGrant.getAuthorization().getAccessToken();
                    anonymousUserAccessToken = jwtGrant.getAuthorization().getAccessToken();

                    Call<RTMUrl> _rtmUrl;
                    if(isReconnect){
                        _rtmUrl = RestBuilder.getRestAPI().getRtmUrl(accessTokenHeader(jwtGrant.getAuthorization().getAccessToken()), hsh1, true);
                    }else{
                        _rtmUrl = RestBuilder.getRestAPI().getRtmUrl(accessTokenHeader(jwtGrant.getAuthorization().getAccessToken()), hsh1);
                    }

                    Response<RTMUrl> rBody2 = _rtmUrl.execute();
                    RTMUrl rtmUrl = rBody2.body();

                    emitter.onNext(rtmUrl);
                    emitter.onComplete();


                }catch(Exception e){
                    emitter.onError(e);
                }
            }
        });

    }

    public void ConnectAnonymousForKora(final String userAccessToken, final String sJwtGrant, BotInfoModel botInfoModel, final String uuId,SocketConnectionListener socketConnectionListener){
        this.userAccessToken = userAccessToken;
        this.botInfoModel = botInfoModel;
        connectAnonymous(sJwtGrant,botInfoModel,uuId,socketConnectionListener);
    }

    /**
     * Method to invoke connection for anonymous
     *
     * These keys are generated from bot admin console
     */
    public void connectAnonymous(final String sJwtGrant, final BotInfoModel botInfoModel, final String uuId,SocketConnectionListener socketConnectionListener) {
        this.socketConnectionListener = socketConnectionListener;
        this.accessToken = null;
        this.JWTToken = sJwtGrant;
        this.uuId = uuId;
        this.botInfoModel = botInfoModel;
        this.options = null;
        //If spiceManager is not started then start it
        /*if (!isConnected()) {
            start(mContext);
        }*/

        rtmUrlRequest(accessToken, sJwtGrant, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RestResponse.RTMUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(RestResponse.RTMUrl response) {
                        try {
                            connectToSocket(response.getUrl(), false);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    @Override
                    public void onComplete() {

                    }
                });




       /* RestRequest<RestResponse.RTMUrl> request = new RestRequest<RestResponse.RTMUrl>(RestResponse.RTMUrl.class, null, null) {
            @Override
            public RestResponse.RTMUrl loadDataFromNetwork() throws Exception {
                setPriority(PRIORITY_HIGH);
                HashMap<String, Object> hsh = new HashMap<>();
                hsh.put(Constants.KEY_ASSERTION, sJwtGrant);
                hsh.put(Constants.BOT_INFO, botInfoModel);


                RestResponse.BotAuthorization jwtGrant = getService().jwtGrant(hsh);

                HashMap<String, Object> hsh1 = new HashMap<>();
                hsh1.put(Constants.BOT_INFO, botInfoModel);

                botUserId = jwtGrant.getUserInfo().getUserId();
                this.accessToken = jwtGrant.getAuthorization().getAccessToken();
                anonymousUserAccessToken = jwtGrant.getAuthorization().getAccessToken();
                auth = jwtGrant.getAuthorization().getAccessToken();

                RestResponse.RTMUrl rtmUrl = getService().getRtmUrl(accessTokenHeader(jwtGrant.getAuthorization().getAccessToken()), hsh1);
                return rtmUrl;
            }
        };*/

       /* getSpiceManager().execute(request, new RequestListener<RestResponse.RTMUrl>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            @Override
            public void onRequestSuccess(RestResponse.RTMUrl response) {
                try {
                    connectToSocket(response.getUrl(),false);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    /**
     * Method to invoke connection for anonymous
     *
     * These keys are generated from bot admin console
     */
    public void connectAnonymousWithOptions(final String sJwtGrant, final BotInfoModel botInfoModel, final String uuId,
                                            SocketConnectionListener socketConnectionListener, BotSocketOptions options) {
        this.socketConnectionListener = socketConnectionListener;
        this.accessToken = null;
        this.JWTToken = sJwtGrant;
        this.uuId = uuId;
        this.botInfoModel = botInfoModel;
        this.options = options;
        //If spiceManager is not started then start it
        /*if (!isConnected()) {
            start(mContext);
        }*/
        rtmUrlRequest(accessToken, sJwtGrant, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RestResponse.RTMUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(RestResponse.RTMUrl response) {
                        try {
                            connectToSocket(response.getUrl(), false);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    @Override
                    public void onComplete() {

                    }
                });


        /*
        RestRequest<RestResponse.RTMUrl> request = new RestRequest<RestResponse.RTMUrl>(RestResponse.RTMUrl.class, null, null) {
            @Override
            public RestResponse.RTMUrl loadDataFromNetwork() throws Exception {
                HashMap<String, Object> hsh = new HashMap<>();
                hsh.put(Constants.KEY_ASSERTION, sJwtGrant);
                hsh.put(Constants.BOT_INFO, botInfoModel);


                RestResponse.BotAuthorization jwtGrant = getService().jwtGrant(hsh);

                HashMap<String, Object> hsh1 = new HashMap<>();
                hsh1.put(Constants.BOT_INFO, botInfoModel);

                botUserId = jwtGrant.getUserInfo().getUserId();
                this.accessToken = jwtGrant.getAuthorization().getAccessToken();
                anonymousUserAccessToken = jwtGrant.getAuthorization().getAccessToken();
                auth = jwtGrant.getAuthorization().getAccessToken();

                RestResponse.RTMUrl rtmUrl = getService().getRtmUrl(accessTokenHeader(jwtGrant.getAuthorization().getAccessToken()), hsh1);
                return rtmUrl;
            }
        };

        getSpiceManager().execute(request, new RequestListener<RestResponse.RTMUrl>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            @Override
            public void onRequestSuccess(RestResponse.RTMUrl response) {
                try {
                    connectToSocket(response.getUrl(),false);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    /**
     * To connect through socket
     *
     * @param url : to connect the socket to
     * @throws URISyntaxException
     */
    private void connectToSocket(String url, final boolean isReconnectionAttaempt) throws URISyntaxException {
        if((isConnecting || isConnected())) return;
        isConnecting = true;
        if (url != null) {
            if(options != null){
                url = options.replaceOptions(url,options);
            }
//            this.url = url;
//            this.uri = new URI(url);
            WebSocketOptions connectOptions = new WebSocketOptions();
            connectOptions.setReconnectInterval(0);
            try {
                mConnection.connect(url, new  WebSocketConnectionHandler() {
                    @Override
                    public void onOpen() {
                        Log.d(LOG_TAG, "Connection Open.");
                        if (socketConnectionListener != null) {
                            socketConnectionListener.onOpen(isReconnectionAttaempt);
                        }
                        isConnecting = false;
                        startSendingPong();
                        mReconnectionCount = 1;
                        mReconnectDelay = 1000;
                    }

                    @Override
                    public void onClose(int code, String reason) {
                        Log.d(LOG_TAG, "Connection Lost.");
                        if (socketConnectionListener != null) {
                            socketConnectionListener.onClose(code, reason);
                        }
                        if(timer != null){
                            timer.cancel();
                            timer = null;
                        }
                        isConnecting = false;
                        if (isConnected()) {
                           // stop();
                        }
                        reconnectAttempt();
                    }

                    @Override
                    public void onMessage(String payload) {
//                        Log.d(LOG_TAG, "onTextMessage payload :" + payload);
                        if (socketConnectionListener != null) {
                            socketConnectionListener.onTextMessage(payload);
                        }
                    }

                    /*@Override
                    public void onRawTextMessage(byte[] payload) {
//                        Log.d(LOG_TAG, "onRawTextMessage payload:" + payload);
                        if (socketConnectionListener != null) {
                            socketConnectionListener.onRawTextMessage(payload);
                        }
                    }

                    @Override
                    public void onBinaryMessage(byte[] payload) {
//                        Log.d(LOG_TAG, "onBinaryMessage payload: " + payload);
                        if (socketConnectionListener != null) {
                            socketConnectionListener.onBinaryMessage(payload);
                        }
                    }*/
                });
            } catch (WebSocketException e) {
                isConnecting = false;
                if(e.getMessage() != null && e.getMessage().equals("already connected")){
                    if (socketConnectionListener != null) {
                        socketConnectionListener.onOpen(isReconnectionAttaempt);
                    }
                    startSendingPong();
                    mReconnectionCount = 1;
                    mReconnectDelay = 1000;
                }
                e.printStackTrace();
            }
        }
    }
    private void startSendingPong(){
        TimerTask tTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if ( mConnection.isConnected()) {
                        mConnection.sendPing("pong from the client".getBytes());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

       try {
           if(timer == null)timer = new Timer();
           timer.scheduleAtFixedRate(tTask, 1000L, 30000L);
       }catch(Exception e){
           e.printStackTrace();
       }
    }

    /**
     *  Reconnect to socket
     */
    private void reconnect() {
        if (accessToken != null) {
            //Reconnection for valid credential
            reconnectForAuthenticUser();
        } else {
            //Reconnection for anonymous
            reconnectForAnonymousUser();
        }
    }

    /**
     * Reconnection for authentic user
     */
    private void reconnectForAuthenticUser() {
        Log.i(LOG_TAG, "Connection lost. Reconnecting....");

        //If spiceManager is not started then start it
        if (!isConnected()) {
            //start(mContext);
        }

        rtmUrlRequest(accessToken, null, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RestResponse.RTMUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(RestResponse.RTMUrl response) {
                        try {
                            connectToSocket(response.getUrl().concat("&isReconnect=true"),true);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    @Override
                    public void onComplete() {

                    }
                });



       /* RestRequest<RestResponse.RTMUrl> request = new RestRequest<RestResponse.RTMUrl>(RestResponse.RTMUrl.class, null, accessToken) {
            @Override
            public RestResponse.RTMUrl loadDataFromNetwork() throws Exception {
                RestResponse.JWTTokenResponse jwtToken = getService().getJWTToken(accessTokenHeader());
                HashMap<String, Object> hsh = new HashMap<>(1);
                hsh.put(Constants.KEY_ASSERTION, jwtToken.getJwt());
                RestResponse.BotAuthorization jwtGrant = getService().jwtGrant(hsh);

                RestResponse.RTMUrl rtmUrl = getService().getRtmUrl(accessTokenHeader(jwtGrant.getAuthorization().getAccessToken()), optParameterBotInfo, true);
                return rtmUrl;
            }
        };

        getSpiceManager().execute(request, new RequestListener<RestResponse.RTMUrl>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            @Override
            public void onRequestSuccess(RestResponse.RTMUrl response) {
                try {
                    connectToSocket(response.getUrl().concat("&isReconnect=true"),true);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    /**
     * Reconnection for anonymous user
     */
    private void reconnectForAnonymousUser() {

        Log.i(LOG_TAG, "Connection lost. Reconnecting....");

        //If spiceManager is not started then start it
        /*if (!isConnected()) {
            start(mContext);
        }*/

        rtmUrlRequest(accessToken, JWTToken, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RestResponse.RTMUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(RestResponse.RTMUrl response) {
                        try {
                            connectToSocket(response.getUrl().concat("&isReconnect=true"),true);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    @Override
                    public void onComplete() {

                    }
                });


      /*  RestRequest<RestResponse.RTMUrl> request = new RestRequest<RestResponse.RTMUrl>(RestResponse.RTMUrl.class, null, null) {
            @Override
            public RestResponse.RTMUrl loadDataFromNetwork() throws Exception {

                HashMap<String, Object> hsh = new HashMap<>();
                hsh.put(Constants.KEY_ASSERTION, JWTToken);

                hsh.put(Constants.BOT_INFO, botInfoModel);


                RestResponse.BotAuthorization jwtGrant = getService().jwtGrant(hsh);

                HashMap<String, Object> hsh1 = new HashMap<>();
                hsh1.put(Constants.BOT_INFO, botInfoModel);

                this.accessToken = jwtGrant.getAuthorization().getAccessToken();
                anonymousUserAccessToken = jwtGrant.getAuthorization().getAccessToken();
                auth = jwtGrant.getAuthorization().getAccessToken();
                botUserId = jwtGrant.getUserInfo().getUserId();
                RestResponse.RTMUrl rtmUrl = getService().getRtmUrl(accessTokenHeader(accessToken), hsh1, true);
                return rtmUrl;
            }
        };


        getSpiceManager().execute(request, new RequestListener<RestResponse.RTMUrl>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            @Override
            public void onRequestSuccess(RestResponse.RTMUrl response) {
                try {
                    connectToSocket(response.getUrl().concat("&isReconnect=true"),true);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    public void onEvent(String token){
        JWTToken = token;
        reconnect();
    }

    /**
     * @param msg : The message object
     * @return Was it able to successfully send the message.
     */
    public boolean sendMessage(String msg) {
        if (mConnection != null && mConnection.isConnected()) {
            mConnection.sendMessage(msg);
            return true;
        } else {
            if(userAccessToken != null && socketConnectionListener != null){
                socketConnectionListener.refreshJwtToken();
            }else {
                reconnect();
            }
            Log.e(LOG_TAG, "Connection is not present. Reconnecting...");
            return false;
        }
    }

   /*
     * Method to Reconnection attempt based on incremental delay
     *
     * @reurn
     */
    private void reconnectAttempt() {
//        mIsImmediateFetchActionNeeded = true;
        mReconnectDelay = getReconnectDelay();
        try {
            final Handler _handler = new Handler();
            Runnable r = new Runnable() {

                @Override
                public void run() {

                    Log.d(LOG_TAG, "Entered into reconnection post delayed " + mReconnectDelay);
                    if (mIsReconnectionAttemptNeeded && !isConnected()) {
                        reconnect();
//                        Toast.makeText(mContext,"SocketDisConnected",Toast.LENGTH_SHORT).show();
                        mReconnectDelay = getReconnectDelay();
                        _handler.postDelayed(this, mReconnectDelay);
                        Log.d(LOG_TAG, "#### trying to reconnect");
                    }

                }
            };
                _handler.postDelayed(r, mReconnectDelay);
        } catch (Exception e) {
            Log.d(LOG_TAG, ":: The Exception is " + e.toString());
        }
    }

    /**
     * The reconnection attempt delay(incremental delay)
     *
     * @return
     */
    private int getReconnectDelay() {
        mReconnectionCount++;
        Log.d(LOG_TAG, "Reconnection count " + mReconnectionCount);
        if (mReconnectionCount > 6) mReconnectionCount = 1;
        Random rint = new Random();
        return (rint.nextInt(5) + 1) * mReconnectionCount * 1000;
    }
    /**
     * For disconnecting user's presence
     * Call this method when the user logged out
     */
    public void disConnect() {
        mIsReconnectionAttemptNeeded = false;
        if (mConnection != null && mConnection.isConnected()) {
            try {
                mConnection.sendClose();
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception while disconnection");
            }
            Log.d(LOG_TAG, "DisConnected successfully");
        } else {
            Log.d(LOG_TAG, "Cannot disconnect.._client is null");
        }
        /*if (isConnected()) {
            stop();
        }*/
    }

    public String getAnonymousUserAccessToken() {
        return anonymousUserAccessToken;
    }

    /**
     * Determine the TLS enability of the url.
     * @param url url to connect to (is generally either ws:// or wss://)
     */
   /* private void determineTLSEnability(String url) {
        mTLSEnabled = url.startsWith(Constants.SECURE_WEBSOCKET_PREFIX);
    }*/

    /**
     * To determine wither socket is connected or not
     * @return boolean indicating the connection presence.
     */
    public boolean isConnected() {
        if (mConnection != null && mConnection.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public String getBotUserId() {
        return botUserId;
    }

    public void setBotUserId(String botUserId) {
        this.botUserId = botUserId;
    }
}