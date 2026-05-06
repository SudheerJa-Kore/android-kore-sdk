package kore.botssdk.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotButtonModel;
import kore.botssdk.models.BotResponse;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.StringUtils;

public class ButtonLinkTemplateAdapter extends RecyclerView.Adapter<ButtonLinkTemplateAdapter.ViewHolder> {
    private final ArrayList<BotButtonModel> buttons;
    private final boolean isEnabled;
    private ComposeFooterInterface composeFooterInterface;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    String textColor;

    public ButtonLinkTemplateAdapter(Context context, ArrayList<BotButtonModel> buttons, boolean isEnabled) {
        this.buttons = buttons;
        this.isEnabled = isEnabled;
        SharedPreferences sharedPreferences = context.getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);

        textColor = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.white));
        textColor = sharedPreferences.getString(BotResponse.BUTTON_ACTIVE_TXT_COLOR, textColor);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_button_link_item_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BotButtonModel buttonTemplate = getItem(position);
        if (buttonTemplate == null) return;
        holder.button.setText(buttonTemplate.getTitle());
        holder.ivDeepLink.setVisibility(View.GONE);

        holder.button.setTextColor(Color.parseColor(textColor));

        holder.button.setOnClickListener(v -> {
            if (composeFooterInterface != null && invokeGenericWebViewInterface != null && isEnabled) {
                if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(buttonTemplate.getType())) {

                    if (!StringUtils.isNullOrEmpty(buttonTemplate.getUrl())) {
                        if (buttonTemplate.isSamePageNavigation())
                            composeFooterInterface.onDeepLinkClicked(buttonTemplate.getUrl());
                        else
                            invokeGenericWebViewInterface.invokeGenericWebView(buttonTemplate.getUrl());
                    }
                } else if (BundleConstants.BUTTON_TYPE_URL.equalsIgnoreCase(buttonTemplate.getType())) {
                    invokeGenericWebViewInterface.invokeGenericWebView(buttonTemplate.getUrl());
                } else if (BundleConstants.BUTTON_TYPE_USER_INTENT.equalsIgnoreCase(buttonTemplate.getType())) {
                    invokeGenericWebViewInterface.handleUserActions(buttonTemplate.getAction(), buttonTemplate.getCustomData());
                } else {
                    String title = buttonTemplate.getTitle();
                    String payload = buttonTemplate.getPayload();
                    composeFooterInterface.onSendClick(title, payload, false);
                }
            }
        });

        holder.bot_options_more.setOnClickListener(v -> {
            if (composeFooterInterface != null && invokeGenericWebViewInterface != null && isEnabled) {
                if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(buttonTemplate.getType())) {

                    if (!StringUtils.isNullOrEmpty(buttonTemplate.getUrl())) {
                        if (buttonTemplate.isSamePageNavigation())
                            composeFooterInterface.onDeepLinkClicked(buttonTemplate.getUrl());
                        else
                            invokeGenericWebViewInterface.invokeGenericWebView(buttonTemplate.getUrl());
                    }
                } else if (BundleConstants.BUTTON_TYPE_URL.equalsIgnoreCase(buttonTemplate.getType())) {
                    invokeGenericWebViewInterface.invokeGenericWebView(buttonTemplate.getUrl());
                } else if (BundleConstants.BUTTON_TYPE_USER_INTENT.equalsIgnoreCase(buttonTemplate.getType())) {
                    invokeGenericWebViewInterface.handleUserActions(buttonTemplate.getAction(), buttonTemplate.getCustomData());
                } else {
                    String title = buttonTemplate.getTitle();
                    String payload = buttonTemplate.getPayload();
                    composeFooterInterface.onSendClick(title, payload, false);
                }
            }
        });
    }

    private BotButtonModel getItem(int position) {
        return buttons != null ? buttons.get(position) : null;
    }

    @Override
    public int getItemCount() {
        return buttons.size();
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView button;
        ImageView ivDeepLink;
        LinearLayout bot_options_more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.more_txt_view);
            ivDeepLink = itemView.findViewById(R.id.ivDeepLink);
            bot_options_more = itemView.findViewById(R.id.bot_options_more);
            button.setTextColor(Color.parseColor("#2e6fc5"));
        }
    }
}
