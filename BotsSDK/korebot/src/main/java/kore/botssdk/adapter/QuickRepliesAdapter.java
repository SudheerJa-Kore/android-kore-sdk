package kore.botssdk.adapter;

import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.QuickRepliesPayloadModel;
import kore.botssdk.models.QuickReplyTemplate;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.viewholders.QuickReplyViewHolder;

/**
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class QuickRepliesAdapter extends RecyclerView.Adapter<QuickReplyViewHolder> {

    ArrayList<QuickReplyTemplate> quickReplyTemplateArrayList;
    final Context context;
    final RecyclerView parentRecyclerView;
    ComposeFooterInterface composeFooterInterface;
    InvokeGenericWebViewInterface invokeGenericWebViewInterface;

    private final String leftTint;
    private final String botTextColor;
    private final String rightTint;
    private final String userTextColor;
    private int selectedIndex = -1;

    public QuickRepliesAdapter(Context context, RecyclerView parentRecyclerView) {
        this.context = context;
        this.parentRecyclerView = parentRecyclerView;

        SharedPreferences prefs = context.getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
        leftTint = prefs.getString(BotResponse.BUBBLE_LEFT_BG_COLOR, "#E0F2E9");
        botTextColor = prefs.getString(BotResponse.BUBBLE_LEFT_TEXT_COLOR, "#000000");
        rightTint = prefs.getString(BotResponse.BUBBLE_RIGHT_BG_COLOR, "#7B2D8E");
        userTextColor = prefs.getString(BotResponse.BUBBLE_RIGHT_TEXT_COLOR, "#FFFFFF");
    }

    @NonNull
    @Override
    public QuickReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = View.inflate(context, R.layout.quick_reply_item_layout, null);
        return new QuickReplyViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickReplyViewHolder holder, int position) {
        QuickReplyTemplate quickReplyTemplate = quickReplyTemplateArrayList.get(position);

        if (quickReplyTemplate.getImage_url() != null && !quickReplyTemplate.getImage_url().isEmpty()) {
            Glide.with(context)
                    .load(quickReplyTemplate.getImage_url())
                    .into(holder.getQuickReplyImage());
            holder.getQuickReplyImage().setVisibility(View.VISIBLE);
        } else {
            holder.getQuickReplyImage().setVisibility(View.GONE);
        }

        holder.getQuickReplyTitle().setText(quickReplyTemplate.getTitle());
        applyButtonStyle(holder, position == selectedIndex);

        holder.getQuickReplyRoot().setOnClickListener(v -> {
            int position1 = parentRecyclerView.getChildAdapterPosition(v);
            if (position1 == RecyclerView.NO_POSITION) return;

            selectedIndex = position1;
            notifyDataSetChanged();

            if (composeFooterInterface != null && invokeGenericWebViewInterface != null) {
                QuickReplyTemplate quickReplyTemplate1 = quickReplyTemplateArrayList.get(position1);

                String quickReplyPayload;
                try {
                    quickReplyPayload = (String) quickReplyTemplate1.getPayload();
                } catch (Exception e) {
                    try {
                        QuickRepliesPayloadModel quickRepliesPayloadModel = (QuickRepliesPayloadModel) quickReplyTemplate1.getPayload();
                        quickReplyPayload = quickRepliesPayloadModel.getName();
                    } catch (Exception exception) {
                        quickReplyPayload = "";
                    }
                }

                if (BundleConstants.BUTTON_TYPE_POSTBACK.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                    composeFooterInterface.onSendClick(quickReplyTemplate1.getTitle(), quickReplyPayload, false);
                } else if (BundleConstants.BUTTON_TYPE_USER_INTENT.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                    invokeGenericWebViewInterface.invokeGenericWebView(BundleConstants.BUTTON_TYPE_USER_INTENT);
                } else if (BundleConstants.BUTTON_TYPE_TEXT.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                    composeFooterInterface.onSendClick(quickReplyTemplate1.getTitle(), quickReplyPayload, false);
                } else if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                    invokeGenericWebViewInterface.invokeGenericWebView(quickReplyPayload);
                } else {
                    composeFooterInterface.onSendClick(quickReplyTemplate1.getTitle(), quickReplyPayload, false);
                }
            }
        });
    }

    private void applyButtonStyle(QuickReplyViewHolder holder, boolean selected) {
        LinearLayout buttonView = holder.getQuickReplyButton();
        GradientDrawable bgDrawable;
        if (buttonView.getBackground() instanceof GradientDrawable) {
            bgDrawable = (GradientDrawable) buttonView.getBackground().mutate();
        } else {
            bgDrawable = new GradientDrawable();
            buttonView.setBackground(bgDrawable);
        }
        bgDrawable.setCornerRadius(5 * dp1);
        bgDrawable.setShape(GradientDrawable.RECTANGLE);

        if (selected) {
            bgDrawable.setColor(Color.parseColor(userTextColor));
            bgDrawable.setStroke((int) (1.5 * dp1), Color.parseColor("#D0D5DD"));
            holder.getQuickReplyTitle().setTextColor(Color.BLACK);
        } else {
            bgDrawable.setColor(Color.parseColor(rightTint));
            bgDrawable.setStroke((int) (1.5 * dp1), Color.parseColor(rightTint));
            holder.getQuickReplyTitle().setTextColor(Color.parseColor(userTextColor));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (quickReplyTemplateArrayList == null) {
            return 0;
        } else {
            return quickReplyTemplateArrayList.size();
        }
    }

    public void setQuickReplyTemplateArrayList(ArrayList<QuickReplyTemplate> quickReplyTemplateArrayList) {
        this.quickReplyTemplateArrayList = quickReplyTemplateArrayList;
        this.selectedIndex = -1;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }
}
