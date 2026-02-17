package kore.botssdk.fileupload.core;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.net.ssl.HttpsURLConnection;

import kore.botssdk.fileupload.configurations.Constants;
import kore.botssdk.fileupload.configurations.FileUploadEndPoints;
import kore.botssdk.fileupload.listeners.ChunkUploadListener;
import kore.botssdk.fileupload.ssl.KoreHttpsUrlConnectionBuilder;
import kore.botssdk.utils.LogUtils;


public class UploadExecutor implements Runnable {

    public final String LOG_TAG = "UploadExecutor";

    /*Intent params*/
    private final String fileName;
    private final String accessToken;
    private final String fileToken;
    private final String userOrTeamId;
    byte[] dataToSet;
    int chunkNo;
    private final Context context;
    ChunkUploadListener mListener;
    private final String host;
    private final boolean isAnonymousUser;
    private final boolean isWebhook;
    private final String botId;

    private static final String BOUNDARY = "*****";
    private static final String LINE_FEED = "\r\n";
    private static final String CHARSET = "UTF-8";

    public UploadExecutor(Context context, String fileName, String fileToken, String accessToken, String userOrTeamId, byte[] dataToPost,
                          int chunkNo, ChunkUploadListener listener, String host, boolean isAnonymousUser, boolean isWebHook, String botId) {
        this.fileName = fileName;
        this.fileToken = fileToken;
        this.accessToken = accessToken;
        this.userOrTeamId = userOrTeamId;
        this.dataToSet = dataToPost;
        this.chunkNo = chunkNo;
        this.mListener = listener;
        this.context = context;
        this.host = host;
        this.isAnonymousUser = isAnonymousUser;
        this.isWebhook = isWebHook;
        this.botId = botId;
    }

    @Override
    public void run() {
        StringBuilder serverResponse;
        HttpsURLConnection httpsURLConnection = null;
        BufferedReader bufferedReader = null;
        DataOutputStream dataOutputStream = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        try {

            LogUtils.d(LOG_TAG, "About to send chunks" + chunkNo + "for file" + fileName);
            String FULL_URL = host + String.format(FileUploadEndPoints.WEBHOOK_ANONYMOUS_CHUNK_UPLOAD, botId, "ivr", fileToken);

            if (isAnonymousUser) {
                if (!isWebhook)
                    FULL_URL = host + String.format(FileUploadEndPoints.ANONYMOUS_CHUNK_UPLOAD, userOrTeamId, fileToken);
            } else {
                if (!isWebhook)
                    FULL_URL = host + String.format(FileUploadEndPoints.CHUNK_UPLOAD, userOrTeamId, fileToken);
            }

            KoreHttpsUrlConnectionBuilder koreHttpsUrlConnectionBuilder = new KoreHttpsUrlConnectionBuilder(context, FULL_URL);
            httpsURLConnection = koreHttpsUrlConnectionBuilder.getHttpsURLConnection();
            httpsURLConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setUseCaches(false);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestProperty("User-Agent", Constants.getUserAgent());
            httpsURLConnection.setRequestProperty("Authorization", accessToken);
            httpsURLConnection.setRequestProperty("Cache-Control", "no-cache");
            httpsURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            httpsURLConnection.setReadTimeout(Constants.CONNECTION_READ_TIMEOUT);
            outputStream = httpsURLConnection.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            addFormField(dataOutputStream, "chunkNo", String.valueOf(chunkNo));

            // Add fileToken as a string part
            addFormField(dataOutputStream, "fileToken", fileToken);

            // Add chunk as a file part (byte array)
            addFilePart(dataOutputStream, dataToSet, fileName);

            // End of multipart/form-data
            dataOutputStream.writeBytes("--" + BOUNDARY + "--" + LINE_FEED);
            dataOutputStream.flush();

            //Real upload starting here -->>
            inputStream = httpsURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            serverResponse = new StringBuilder();
            for (int c = bufferedReader.read(); c != -1; c = bufferedReader.read()) {
                serverResponse.append((char) c);
            }
            LogUtils.d(LOG_TAG, "Got serverResponse for chunk upload" + serverResponse);
            int statusCode = httpsURLConnection.getResponseCode();
            LogUtils.e(LOG_TAG, "status code for chunks" + chunkNo + "is" + statusCode);

            String chunkNo;

            if (statusCode == 200) {
                JSONObject jsonObject = new JSONObject(serverResponse.toString());

                jsonObject.get("chunkNo");
                chunkNo = (String) jsonObject.get("chunkNo");
                if (mListener != null)
                    mListener.notifyChunkUploadCompleted(chunkNo, fileName);
                LogUtils.e(LOG_TAG, "Response for chunk ::::" + chunkNo + "for file" + fileName);

            } else {
                if (mListener != null)
                    mListener.notifyChunkUploadCompleted(null, fileName);
                throw new Exception("Response code not 200");
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Exception in uploading chunk " + e);
            if (mListener != null)
                mListener.notifyChunkUploadCompleted(chunkNo + "", fileName);
            LogUtils.e(LOG_TAG, "Failed to post message for chunk no:: " + this.chunkNo);
        } finally {
            try {
                if (httpsURLConnection != null)
                    httpsURLConnection.disconnect();
                if (inputStream != null)
                    inputStream.close();
                if (inputStreamReader != null)
                    inputStreamReader.close();
                if (outputStream != null)
                    outputStream.close();
                if (bufferedReader != null)
                    bufferedReader.close();
                if (dataOutputStream != null)
                    dataOutputStream.close();
            } catch (IOException e) {
                LogUtils.e(LOG_TAG, "Exception in uploading chunk " + e);
            }
        }
    }

    private void addFormField(DataOutputStream dataOutputStream, String name, String value) throws IOException {
        dataOutputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED);
        dataOutputStream.writeBytes("Content-Type: text/plain; charset=" + CHARSET + LINE_FEED);
        dataOutputStream.writeBytes(LINE_FEED);
        dataOutputStream.writeBytes(value + LINE_FEED);
        dataOutputStream.flush();
    }

    private void addFilePart(DataOutputStream dataOutputStream, byte[] uploadFileBytes, String fileName) throws IOException {
        dataOutputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + "chunk" + "\"; filename=\"" + fileName + "\"" + LINE_FEED);
        dataOutputStream.writeBytes("Content-Type: application/octet-stream" + LINE_FEED);
        dataOutputStream.writeBytes(LINE_FEED);

        dataOutputStream.write(uploadFileBytes);
        dataOutputStream.writeBytes(LINE_FEED);
        dataOutputStream.flush();
    }
}