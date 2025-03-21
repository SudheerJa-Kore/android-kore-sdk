package kore.botssdk.listener;

import java.util.ArrayList;
import java.util.HashMap;

import kore.botssdk.models.FormActionTemplate;

@SuppressWarnings("UnKnownNullness")
public interface ComposeFooterInterface {
    /**
     * @param message : Title and payload, Both are same
     */
    void onSendClick(String message, boolean isFromUtterance);

    /**
     * @param message : Title of the button
     * @param payload : Payload to be send
     */
    void onSendClick(String message, String payload, boolean isFromUtterance);

    void onSendClick(String message, ArrayList<HashMap<String, String>> attachments, boolean isFromUtterance);

    void onFormActionButtonClicked(FormActionTemplate fTemplate);

    void copyMessageToComposer(String text, boolean isForOnboard);

    void sendImage(String fP, String fN, String fPT);

    void externalReadWritePermission(String fileUrl);

    void onDeepLinkClicked(String url);
}