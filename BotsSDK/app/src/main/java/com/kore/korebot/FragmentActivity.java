package com.kore.korebot;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.kore.korebot.customtemplates.LinkTemplateHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import kore.botssdk.fragment.botchat.BotChatFragment;
import kore.botssdk.models.BrandingModel;
import kore.botssdk.net.RestResponse;
import kore.botssdk.net.SDKConfig;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.LangUtils;

public class FragmentActivity extends AppCompatActivity {
    BotChatFragment botChatFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        //Can set Language for Bot SDK
        LangUtils.setAppLanguages(this, LangUtils.LANG_EN);

//        Can set your customized Header view in the chat window by using this method. By extending BaseHeaderFragment. Can find examples under fragments package
//        SDKConfig.addCustomHeaderFragment(new CustomHeaderFragment());

//        Can set your customized Content view in the chat window by using this method. By extending BaseContentFragment. Can find examples under fragments package
//        SDKConfig.addCustomContentFragment(new CustomContentFragment());

//        Can set your customized Footer view in the chat window by using this method. By extending BaseFooterFragment. Can find examples under fragments package
//        SDKConfig.addCustomFooterFragment(new CustomFooterFragment());

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

        //Set jwtServerUrl, This value is mandatory
        String jwtServerUrl = "PLEASE_ENTER_JWT_SERVER_URL";

        //Set isWebHook
        SDKConfig.isWebHook(false);

        //Initialize the bot with bot config
        //You can pass client id and client secret as empty when you pass jwt token
        SDKConfig.initialize(botId, botName, clientId, clientSecret, identity, jwtToken, serverUrl, brandingUrl, jwtServerUrl);

        //You can pass the custom data to the bot by using this method. Can get sample format from the mentioned method
        SDKConfig.setCustomData(getCustomData());

        //You can set query parameters to the socket url by using this method. Can get sample format from the mentioned method
        SDKConfig.setQueryParams(getQueryParams());

        //Inject the custom template like below
        SDKConfig.setCustomTemplateViewHolder("link", LinkTemplateHolder.class);

        //Flag to show the bot icon beside the bot response
        SDKConfig.setIsShowIcon(true);

        //Flag to show the bot icon in top position or bottom of the bot response
        SDKConfig.setIsShowIconTop(false);

        //Flag to show timestamp of each bot and user messages
        SDKConfig.setIsTimeStampsRequired(true);

        //Flag to show bot header or hide the header
        SDKConfig.setIsShowHeader(true);

        //Set local branding model by overriding the branding api response
        SDKConfig.setLocalBranding(true, getLocalBrandingModel());

        SDKConfiguration.OverrideKoreConfig.showAttachment = true;
        SDKConfiguration.OverrideKoreConfig.showASRMicroPhone = true;
        SDKConfiguration.OverrideKoreConfig.showTextToSpeech = true;

        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        //Add Bot Content Fragment
        botChatFragment = new BotChatFragment();
        fragmentTransaction.add(R.id.flChatBot, botChatFragment).commit();
    }

    @SuppressLint("UnknownNullness")
    public HashMap<String, Object> getQueryParams() {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("q1", true);
        queryParams.put("q2", 4);
        queryParams.put("q3", "connect");
        return queryParams;
    }

    @SuppressLint("UnknownNullness")
    public RestResponse.BotCustomData getCustomData() {
        RestResponse.BotCustomData customData = new RestResponse.BotCustomData();
        customData.put("name", "Kore Bot");
        customData.put("emailId", "emailId");
        customData.put("mobile", "mobile");
        customData.put("accountId", "accountId");
        customData.put("timeZoneOffset", -330);
        customData.put("UserTimeInGMT", TimeZone.getDefault().getID() + " " + Locale.getDefault().getISO3Language());
        return customData;
    }

    private BrandingModel getLocalBrandingModel() {
        BrandingModel brandingModel = new BrandingModel();
        brandingModel.setBotchatBgColor("#F3F5F8");
        brandingModel.setBotchatTextColor("#202124");
        brandingModel.setButtonActiveBgColor("#0D6EFD");
        brandingModel.setButtonActiveTextColor("#ffffff");
        brandingModel.setButtonInactiveBgColor("#0659d2");
        brandingModel.setButtonInactiveTextColor("#ffffff");
        brandingModel.setUserchatBgColor("#0D6EFD");
        brandingModel.setUserchatTextColor("#ffffff");
        brandingModel.setWidgetHeaderColor("#0D6EFD");
        brandingModel.setWidgetFooterColor("#FFFFFF");
        brandingModel.setWidgetFooterBorderColor("#E4E5E7");
        brandingModel.setWidgetFooterHintColor("#262626");
        brandingModel.setWidgetFooterHintText("Type your message...");
        brandingModel.setWidgetTextColor("#ffffff");
        brandingModel.setWidgetBorderColor("#E4E5E7");
        brandingModel.setButtonBorderColor("#0D6EFD");
        brandingModel.setWidgetBodyColor("#ffffff");
        brandingModel.setBotName("Bot Name");
        brandingModel.setChatBubbleStyle("square");
        return brandingModel;
    }
}
