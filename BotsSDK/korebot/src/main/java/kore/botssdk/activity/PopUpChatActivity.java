package kore.botssdk.activity;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import kore.botssdk.R;
import kore.botssdk.net.SDKConfiguration;

/**
 * Presents the existing chat UI as a bottom-aligned popup while leaving the parent app visible.
 *
 * <p>All chat behavior remains in {@link BotChatActivity}; this subclass only controls the
 * presentation of its window.
 */
public class PopUpChatActivity extends BotChatActivity {

    private static final float BACKGROUND_DIM_AMOUNT = 0.25f;
    private static final float TOP_CORNER_RADIUS_DP = 18f;

    private View popupLoadingOverlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setStatusBarColor(Color.TRANSPARENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.dimAmount = BACKGROUND_DIM_AMOUNT;
        attributes.gravity = Gravity.BOTTOM;
        window.setAttributes(attributes);

        View popupRoot = findViewById(R.id.base_frame);
        if (popupRoot != null) {
            float cornerRadius =
                    TOP_CORNER_RADIUS_DP * getResources().getDisplayMetrics().density;
            popupRoot.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                @SuppressWarnings("deprecation")
                public void getOutline(View view, Outline outline) {
                    Path outlinePath = new Path();
                    float[] cornerRadii = {
                            cornerRadius, cornerRadius,
                            cornerRadius, cornerRadius,
                            0f, 0f,
                            0f, 0f
                    };
                    outlinePath.addRoundRect(
                            0f,
                            0f,
                            view.getWidth(),
                            view.getHeight(),
                            cornerRadii,
                            Path.Direction.CW
                    );
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        outline.setPath(outlinePath);
                    } else {
                        outline.setConvexPath(outlinePath);
                    }
                }
            });
            popupRoot.setClipToOutline(true);
        }
    }

    @Override
    protected void changeStatusBarColor(String color) {
        // The popup starts below the host app's status bar, so the SDK's Android 15+
        // status-bar spacer would appear as an extra colored header inside the sheet.
        View statusBarBackground = findViewById(R.id.status_bar_bg);
        if (statusBarBackground != null) {
            statusBarBackground.setVisibility(View.GONE);
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    public void onStart() {
        super.onStart();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float popupHeightRatio =
                SDKConfiguration.OverrideKoreConfig.getResolvedChatScreenPercentage() / 100f;
        getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (displayMetrics.heightPixels * popupHeightRatio)
        );
        getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    void showProgressDialogue() {
        ViewGroup popupRoot = findViewById(R.id.base_frame);
        if (popupRoot == null) {
            super.showProgressDialogue();
            return;
        }

        FrameLayout loadingOverlay = new FrameLayout(this);
        loadingOverlay.setClickable(true);
        loadingOverlay.setFocusable(true);
        loadingOverlay.setBackgroundColor(Color.WHITE);

        View loadingView =
                LayoutInflater.from(this).inflate(R.layout.progress_bar_dialog, loadingOverlay, false);
        TextView loadingText = loadingView.findViewById(R.id.syncDialogPreparingDeviceTextView);
        loadingText.setText(getPreferredString(R.string.loading_data));
        FrameLayout.LayoutParams loadingViewParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        loadingOverlay.addView(loadingView, loadingViewParams);

        RelativeLayout.LayoutParams overlayParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        popupRoot.addView(loadingOverlay, overlayParams);
        loadingOverlay.bringToFront();
        popupLoadingOverlay = loadingOverlay;
    }

    @Override
    void closeProgressDialogue() {
        if (popupLoadingOverlay == null) {
            super.closeProgressDialogue();
            return;
        }

        ViewGroup parent = (ViewGroup) popupLoadingOverlay.getParent();
        if (parent != null) {
            parent.removeView(popupLoadingOverlay);
        }
        popupLoadingOverlay = null;
    }
}
