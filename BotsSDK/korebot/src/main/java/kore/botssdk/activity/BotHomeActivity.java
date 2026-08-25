package kore.botssdk.activity;

import static kore.botssdk.utils.BundleConstants.CAPTURE_IMAGE_BUNDLED_PERMISSION_REQUEST;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.UUID;

import kore.botssdk.R;
import kore.botssdk.listener.BotSocketConnectionManager;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.BundleUtils;
import kore.botssdk.utils.KaPermissionsHelper;
import kore.botssdk.utils.StringUtils;

/**
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class
BotHomeActivity extends BotAppCompactActivity {
    private Button launchBotBtn;
    EditText etIdentity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bot_home_activity_layout);

        findViews();
        setListeners();
    }

    private void findViews() {

        launchBotBtn = findViewById(R.id.launchBotBtn);
        etIdentity = findViewById(R.id.etIdentity);
        launchBotBtn.setText(getResources().getString(R.string.get_started));
        etIdentity.setText(SDKConfiguration.Client.identity);
        if (StringUtils.isNotEmpty(etIdentity.getText().toString()))
            etIdentity.setSelection(etIdentity.getText().toString().length());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (KaPermissionsHelper.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                Log.e("Has Permission", "true");
            } else {
                KaPermissionsHelper.requestForPermission(this, CAPTURE_IMAGE_BUNDLED_PERMISSION_REQUEST,
                        Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setListeners() {
        launchBotBtn.setOnClickListener(launchBotBtnOnClickListener);
    }

    /**
     * START of : Listeners
     */
    final View.OnClickListener launchBotBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isOnline()) {
                if (!StringUtils.isNullOrEmpty(etIdentity.getText().toString())) {
                    if (StringUtils.isValidEmail(etIdentity.getText().toString())) {
                        if (!SDKConfiguration.Client.isWebHook) {
                            SDKConfiguration.Client.identity = UUID.randomUUID().toString();
                            BotSocketConnectionManager.getInstance().startAndInitiateConnectionWithConfig(getApplicationContext(), null);
                            launchBotChatActivity();
                        } else {
                            launchBotChatActivity();
                        }
                    } else
                        Toast.makeText(BotHomeActivity.this, "Please enter a valid Email.", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(BotHomeActivity.this, "Please enter your Email.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BotHomeActivity.this, "No internet connectivity", Toast.LENGTH_SHORT).show();
            }
        }
    };


    /**
     * Launching BotHomeActivity where user can interact with bot
     */
    void launchBotChatActivity() {
        Intent intent = new Intent(getApplicationContext(), BotChatActivity.class);
        Bundle bundle = new Bundle();
        //This should not be null
        bundle.putBoolean(BundleUtils.SHOW_PROFILE_PIC, false);
        bundle.putString(BundleUtils.BOT_NAME_INITIALS, SDKConfiguration.Client.bot_name.charAt(0) + "");
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    protected boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAPTURE_IMAGE_BUNDLED_PERMISSION_REQUEST) {
            if (KaPermissionsHelper.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                Log.e("Has Permission", "true");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
