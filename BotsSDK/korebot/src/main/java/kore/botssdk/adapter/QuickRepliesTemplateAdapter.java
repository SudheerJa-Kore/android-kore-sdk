package kore.botssdk.adapter;

import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.listener.ChatContentStateListener;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.QuickRepliesPayloadModel;
import kore.botssdk.models.QuickReplyTemplate;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.viewholders.QuickReplyViewHolder;

public class QuickRepliesTemplateAdapter extends RecyclerView.Adapter<QuickReplyViewHolder> {

    private ArrayList<QuickReplyTemplate> quickReplyTemplateArrayList;
    final Context context;
    private final RecyclerView parentRecyclerView;
    private ComposeFooterInterface composeFooterInterface;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    private ChatContentStateListener contentStateListener;
    private String msgId;
    private int selectedIndex = -1;
    private boolean isEnabled = true;

    private final String leftTint;
    private final String botTextColor;
    private final String rightTint;
    private final String userTextColor;

    public QuickRepliesTemplateAdapter(Context context, RecyclerView parentRecyclerView, boolean isEnabled) {
        this.context = context;
        this.parentRecyclerView = parentRecyclerView;
        this.isEnabled = isEnabled;

        SharedPreferences prefs = context.getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
        leftTint = prefs.getString(BotResponse.BUBBLE_LEFT_BG_COLOR, "#E0F2E9");
        botTextColor = prefs.getString(BotResponse.BUBBLE_LEFT_TEXT_COLOR, "#000000");
        rightTint = prefs.getString(BotResponse.BUBBLE_RIGHT_BG_COLOR, "#7B2D8E");
        userTextColor = prefs.getString(BotResponse.BUBBLE_RIGHT_TEXT_COLOR, "#FFFFFF");
    }

    @NonNull
    @Override
    public QuickReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = View.inflate(context, R.layout.quick_replies_item_cell, null);
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
            if (!isEnabled) return;
            int clickedPosition = holder.getBindingAdapterPosition();
            if (clickedPosition == RecyclerView.NO_POSITION) return;

            selectedIndex = clickedPosition;
            if (contentStateListener != null && msgId != null) {
                contentStateListener.onSaveState(msgId, selectedIndex, BotResponse.SELECTED_ITEM);
            }
            notifyDataSetChanged();

            QuickReplyTemplate quickReplyTemplate1 = quickReplyTemplateArrayList.get(clickedPosition);
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

            if (composeFooterInterface != null && BundleConstants.BUTTON_TYPE_POSTBACK.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                composeFooterInterface.onSendClick(quickReplyTemplate1.getTitle(), quickReplyPayload, false);
            } else if (invokeGenericWebViewInterface != null && BundleConstants.BUTTON_TYPE_USER_INTENT.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                invokeGenericWebViewInterface.invokeGenericWebView(BundleConstants.BUTTON_TYPE_USER_INTENT);
            } else if (composeFooterInterface != null && BundleConstants.BUTTON_TYPE_TEXT.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                composeFooterInterface.onSendClick(quickReplyTemplate1.getTitle(), quickReplyPayload, false);
            } else if (invokeGenericWebViewInterface != null && BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(quickReplyTemplate1.getContent_type())) {
                invokeGenericWebViewInterface.invokeGenericWebView(quickReplyPayload);
            } else if (composeFooterInterface != null) {
                composeFooterInterface.onSendClick(quickReplyTemplate1.getTitle(), quickReplyPayload, false);
            }
        });
    }

    private void applyButtonStyle(QuickReplyViewHolder holder, boolean selected) {
        GradientDrawable bgDrawable;
        if (holder.getQuickReplyButton().getBackground() instanceof GradientDrawable) {
            bgDrawable = (GradientDrawable) holder.getQuickReplyButton().getBackground().mutate();
        } else {
            bgDrawable = new GradientDrawable();
            holder.getQuickReplyButton().setBackground(bgDrawable);
        }
        bgDrawable.setCornerRadius(5 * dp1);
        bgDrawable.setShape(GradientDrawable.RECTANGLE);

        if (selected) {
            bgDrawable.setColor(Color.parseColor(userTextColor));
            bgDrawable.setStroke((int) (1.5 * dp1), Color.parseColor("#D0D5DD"));
            holder.getQuickReplyTitle().setTextColor(Color.BLACK);
        } else {
            bgDrawable.setColor(Color.TRANSPARENT);
            bgDrawable.setStroke((int) (1.5 * dp1), Color.parseColor(leftTint));
            holder.getQuickReplyTitle().setTextColor(Color.parseColor(botTextColor));
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
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setContentStateListener(ChatContentStateListener contentStateListener) {
        this.contentStateListener = contentStateListener;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
