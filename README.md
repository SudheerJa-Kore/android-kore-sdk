# Kore.ai SDK
Kore.ai offers Bots SDKs as a set of platform-specific client libraries that provide a quick and convenient way to integrate Kore Bots chat capability into custom applications.

With just few lines of code, you can embed our Kore.ai chat widget into your applications to enable end-users to interact with your applications using Natural Language. For more information, refer to https://developer.kore.com/docs/bots/kore-web-sdk/

# Kore.ai Android SDK for developers

Kore.ai SDK for Android enables you to talk to Kore.ai bots over a web socket. This repo also comes with the code for sample application that developers can modify according to their Bot configuration.

# Setting up

### Prerequisites
* Service to generate JWT (JSON Web Tokens)- this service will be used in the assertion function injected to obtain the connection.
* SDK app credentials 
* Login to the Bots platform
	* Navigate to the Bot builder
	* Search and click on the bot 
	* Enable *Web / Mobile Client* channel against the bot as shown in the screen below.
		
	![Add bot to Web/Mobile Client channel](https://github.com/Koredotcom/android-kore-sdk/blob/master/channels.png)
	
	* create new or use existing SDK app to obtain client id and client secret
	
	![Obtain Client id and Client secret](https://github.com/Koredotcom/android-kore-sdk/blob/master/web-mobile-client-channel.png)

## Instructions

### Configuration changes
* Setting up clientId, clientSecret, botId, botName and identity in SDKConfiguration.java

Client id - Copy this id from Bot Builder SDK Settings ex. cs-5250bdc9-6bfe-5ece-92c9-ab54aa2d4285
 ```
 public static final String client_id = "PLEASE_ENTER_CLIENT_ID";
 ```

Client secret - copy this value from Bot Builder SDK Settings ex. Wibn3ULagYyq0J10LCndswYycHGLuIWbwHvTRSfLwhs=
 ```
public static final String client_secret = "PLEASE_ENTER_CLIENT_SECRET";
 ```

User identity - this should represent the subject for JWT token that could be an email or phone number in case of known user. In case of anonymous user, this can be a randomly generated unique id.
 ```
public static final String identity = "PLEASE_ENTER_IDENTITY";
 ```

Bot name - copy this value from Bot Builder -> Channels -> Web/Mobile SDK config  ex. "Demo Bot"
 ```
public static final String bot_name = "PLEASE_ENTER_BOT_NAME";
 ```

Bot Id - copy this value from Bot Builder -> Channels -> Web/Mobile SDK config  ex. st-acecd91f-b009-5f3f-9c15-7249186d827d
 ```
public static final String bot_id = "PLEASE_ENTER_BOT_ID"; 
 ```

Server URL - replace it with your server URL, if required
 ```
public static final String KORE_BOT_SERVER_URL = "PLEASE_ENTER_KORE_BOT_SERVER_URL";
 ```

Anonymous user - if not anonymous, assign same identity (such as email or phone number) while making a connection
 ```
public static final boolean IS_ANONYMOUS_USER = true; 
 ```

Speech server URL
 ```
public static final String SPEECH_SERVER_BASE_URL = "wss://speech.kore.ai/speechcntxt/ws/speech";
 ```

JWT Server URL - specify the server URL for JWT token generation. This token is used to authorize the SDK client. Refer to documentation on how to setup the JWT server for token generation - e.g. https://jwt-token-server.example.com/
 ```
public static final String JWT_SERVER_URL = "PLEASE_ENTER_JWT_SERVER_URL";

```

### Running the Demo app
*	Download or clone the repository.
*	Import the project.
*	Run the app.
  
# How to integrate BotSdk with UI through gradle implementation

1. Add below snippet in project/build.gradle
   
```
maven { url 'https://www.jitpack.io' }
```
2. Add below snippet in app/build.gradle under dependencies
```
implementation 'com.github.Koredotcom:android-kore-sdk:0.0.5'
```
3. You can initialize the bot by providing the bot config like below. You can pass Jwt Token as empty so that we generate token in the SDK. If token is passed we will use it to establish bot connection.
```
//If token is empty sdk token generation will happen. if not empty we will use this token for bot connection.
String jwtToken = "";

//Set clientId, If jwtToken is empty this value is mandatory
String clientId = "PLEASE_ENTER_CLIENT_ID";

//Set clientSecret, If jwtToken is empty this value is mandatory
String clientSecret = "PLEASE_ENTER_CLIENT_SECRET";

//Set botId, This value is mandatory
String botId = "PLEASE_ENTER_BOT_ID";

//Set identity, This value is mandatory
String identity = "PLEASE_ENTER_IDENTITY";

//Set botName, This value is mandatory
String botName = "PLEASE_ENTER_BOT_NAME";

//Set serverUrl, This value is mandatory
String serverUrl = "PLEASE_ENTER_SERVER_URL";

//Set brandingUrl, This value is mandatory
String brandingUrl = "PLEASE_ENTER_BRANDING_SERVER_URL";

//Set Server url
SDKConfig.setServerUrl(serverUrl);
//Set Branding url
SDKConfig.setBrandingUrl(brandingUrl);

//Set isWebHook
SDKConfig.isWebHook(false);

//Initialize the bot with bot config
//You can pass client id and client secret as empty when you pass jwt token
SDKConfig.initialize(botId, botName,clientId , clientSecret, identity, jwtToken);

//Inject the custom template like below
SDKConfig.setCustomTemplateViewHolder("link", LinkTemplateHolder.class);

//Flag to show the bot icon beside the bot response
SDKConfiguration.BubbleColors.showIcon = true;

//Flag to show the bot icon in top position or bottom of the bot response
SDKConfiguration.OverrideKoreConfig.showIconTop = true;

//Flag to show timestamp of each bot and user messages
SDKConfiguration.setTimeStampsRequired(true);
```
4. You can navigate to the bot chat window through Intent as below snippet
```
Intent intent = new Intent(getApplicationContext(), BotChatActivity.class);
Bundle bundle = new Bundle();
//This should not be null
bundle.putBoolean(BundleUtils.SHOW_PROFILE_PIC, false);
bundle.putString(BundleUtils.BOT_NAME_INITIALS,"B");
intent.putExtras(bundle);
startActivity(intent);
```
# 5. You can have your customized templates without touching the SDK code can check it in below provided branch "LinkTemplateView"
   
  https://github.com/Koredotcom/android-kore-sdk/tree/master_gradle

# How to integrate BotSdk withoutUI through gradle implementation

1. Add below snippet in project/build.gradle
   
```
maven { url 'https://www.jitpack.io' }
```
2. Add below snippet in app/build.gradle under dependencies
```
implementation 'com.github.Koredotcom.android-kore-sdk:korebotsdklib:0.0.5'
```
3. You can change the bot config like below
```
SDKConfiguration.Client.bot_id = "PLEASE_ENTER_BOT_ID";
SDKConfiguration.Client.bot_name = "PLEASE_ENTER_BOT_NAME";
SDKConfiguration.Client.client_id = "PLEASE_ENTER_CLIENT_ID";
SDKConfiguration.Client.client_secret = "PLEASE_ENTER_CLIENT_SECRET";
SDKConfiguration.Client.identity = "PLEASE_ENTER_IDENTITY";

 public static boolean isWebHook = false;
```
4. You can intialize the Bot like below
   
```
//Initiating BotClient
BotClient botClient = new BotClient(this);

//Local library to generate JWT token can be replaced as per requirement(Can be client side API call)
String jwt = botClient.generateJWT(SDKConfiguration.Client.identity,SDKConfiguration.Client.client_secret,SDKConfiguration.Client.client_id,SDKConfiguration.Server.IS_ANONYMOUS_USER);

//Initiating bot connection once connected callbacks will be fired on respective actions
botClient.connectAsAnonymousUser(jwt,SDKConfiguration.Client.bot_name,SDKConfiguration.Client.bot_id, MainActivity.this);
```
5. Where the Bot initiated, that class should implement "SocketConnectionListener" to receive the bot response as below
```
public class MainActivity extends AppCompatActivity implements SocketConnectionListener
```
6. Application class can send message to bot as below
```
botClient.sendMessage("Your message to bot");
```
7. Application class can receive message at Override method "onTextMessage(String payload)" as String as below
```
@Override
public void onTextMessage(String payload) {
Log.e("onTextMessage payload", payload);
}
```

## Integrating into your app with library code base
Copy "korebotsdklib" module available in this repository into your application and follow the below steps

1. Create BotClient object providing context
```
BotClient botClient = new BotClient(this);
```
#### 2. Implement SocketConnectionListener to receive callback
```
SocketConnectionListener socketConnectionListener = new SocketConnectionListener() {
    @Override
    public void onOpen() {
    }
    @Override
    public void onClose(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification code, String reason) {
    }
    @Override
    public void onTextMessage(String payload) {
    }
    @Override
    public void onRawTextMessage(byte[] payload) {
    }
    @Override
    public void onBinaryMessage(byte[] payload) {
    }
};
```
#### 3. Initialize RTM client
```
botClient.connectAsAnonymousUser(jwt,
            SDKConfiguration.Config.demo_client_id,chatBot,taskBotId, BotChatActivity.this);

```
#### 4. JWT generation
    a. You need to have secure token service hosted in your environment which returns the JWT token.
    b. Generate JWT in your enviornment.

NOTE: Please refer about JWT signing and verification at - https://developer.kore.com/docs/bots/kore-web-sdk/

#### 5. Connect with JWT
    private void getJWTToken(){
        String id;
        if(SDKConfiguration.Config.IS_ANONYMOUS_USER){
            id = UUID.randomUUID().toString();
        }else{
            id = SDKConfiguration.Config.identity;
        }

        JWTGrantRequest request = new JWTGrantRequest(SDKConfiguration.Config.demo_client_id,
                SDKConfiguration.Config.clientSecret, id,SDKConfiguration.Config.IS_ANONYMOUS_USER);
        spiceManagerForJWT.execute(request, new RequestListener<RestResponse.JWTTokenResponse>() {
            @Override
            public void onRequestFailure(SpiceException e) {

            }

            @Override
            public void onRequestSuccess(RestResponse.JWTTokenResponse jwt) {
                botClient.connectAsAnonymousUser(jwt.getJwt(),
                        SDKConfiguration.Config.demo_client_id,chatBot,taskBotId, BotChatActivity.this);
            }
        });
    }

#### 6. Send message
```
botClient.sendMessage("Tweet hello")
```
#### 7. Listen to events in socketConnectionListener
```
@Override
public void onOpen() {
}
@Override
public void onClose(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification code, String reason) {
}
@Override
public void onTextMessage(String payload) {
}
@Override
public void onRawTextMessage(byte[] payload) {
}
@Override
public void onBinaryMessage(byte[] payload) {
}
```

#### 8. Subscribe to push notifications
```
PushNotificationRegistrar pushNotificationRegistrar =  new PushNotification(requestListener);
pushNotificationRegistrar.registerPushNotification(Context context, String userId, String accessToken);
```
#### 9. Unsubscribe to push notifications
```
PushNotificationRegistrar pushNotificationRegistrar =  new PushNotification(requestListener);
pushNotificationRegistrar.unsubscribePushNotification(Context context, String accessToken);
```
#### 10. Disconnect
----
Invoke to disconnect previous socket connection upon closing Activity/Fragment or upon destroying view.
botconnector.disconnect();
```
```
# How to enable API based (webhook channel) message communication
----
#### 1. Enable the webhook channel by following the below link
	https://developer.kore.ai/docs/bots/channel-enablement/adding-webhook-channel/
	
#### 2. You can initialize the bot by providing the bot config like below. You can pass Jwt Token as empty so that we generate token in the SDK. If token is passed we will use it to establish bot connection.
```
//If token is empty sdk token generation will happen. if not empty we will use this token for bot connection.
String jwtToken = "";

//Set clientId, If jwtToken is empty this value is mandatory
String clientId = "PLEASE_ENTER_CLIENT_ID";

//Set clientSecret, If jwtToken is empty this value is mandatory
String clientSecret = "PLEASE_ENTER_CLIENT_SECRET";

//Set botId, This value is mandatory
String botId = "PLEASE_ENTER_BOT_ID";

//Set identity, This value is mandatory
String identity = "PLEASE_ENTER_IDENTITY";

//Set botName, This value is mandatory
String botName = "PLEASE_ENTER_BOT_NAME";

//Set serverUrl, This value is mandatory
String serverUrl = "PLEASE_ENTER_SERVER_URL";

//Set brandingUrl, This value is mandatory
String brandingUrl = "PLEASE_ENTER_BRANDING_URL";

//Set Server url
SDKConfig.setServerUrl(serverUrl);
//Set Branding url
SDKConfig.setBrandingUrl(brandingUrl);

//Set isWebHook
SDKConfig.isWebHook(true);

//Initialize the bot with bot config
//You can pass client id and client secret as empty when you pass jwt token
SDKConfig.initialize(botId, botName,clientId , clientSecret, identity, jwtToken);

//Inject the custom template like below
SDKConfig.setCustomTemplateViewHolder("link", LinkTemplateHolder.class);
```
#### 3. You can navigate to the bot chat window through Intent as below snippet
```
Intent intent = new Intent(getApplicationContext(), BotChatActivity.class);
Bundle bundle = new Bundle();
//This should not be null
bundle.putBoolean(BundleUtils.SHOW_PROFILE_PIC, false);
bundle.putString(BundleUtils.BOT_NAME_INITIALS,"B");
intent.putExtras(bundle);
startActivity(intent);

License
Copyright © Kore.ai, Inc. MIT License; see LICENSE for further details.
