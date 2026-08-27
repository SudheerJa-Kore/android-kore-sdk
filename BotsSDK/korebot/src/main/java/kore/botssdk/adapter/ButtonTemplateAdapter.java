package kore.botssdk.adapter;

import static android.content.Context.MODE_PRIVATE;
import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.listener.ChatContentStateListener;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotButtonModel;
import kore.botssdk.models.BotResponse;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.StringUtils;

public class ButtonTemplateAdapter extends RecyclerView.Adapter<ButtonTemplateAdapter.BotButtonViewHolder> {
    private final Context context;
    private final ArrayList<BotButtonModel> buttons;
    private boolean isLastItem;
    private final boolean isFullWidth;
    private final boolean isStackedButtons;
    private final String variation;

    private final String leftTint;
    private final String botTextColor;
    private final String rightTint;
    private final String userTextColor;

    private int selectedIndex = -1;
    private String msgId;
    private ChatContentStateListener contentStateListener;
    private ComposeFooterInterface composeFooterInterface;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;

    public ButtonTemplateAdapter(Context context, ArrayList<BotButtonModel> buttons, boolean isLastItem, boolean isFullWidth, boolean isStackedButtons, String variation) {
        this.context = context;
        this.buttons = buttons;
        this.isLastItem = isLastItem;
        this.isFullWidth = isFullWidth;
        this.isStackedButtons = isStackedButtons;
        this.variation = variation != null ? variation : "";

        SharedPreferences sharedPreferences = context.getSharedPreferences(BotResponse.THEME_NAME, MODE_PRIVATE);
        leftTint = sharedPreferences.getString(BotResponse.BUBBLE_LEFT_BG_COLOR, "#E0F2E9");
        botTextColor = sharedPreferences.getString(BotResponse.BUBBLE_LEFT_TEXT_COLOR, "#000000");
        rightTint = sharedPreferences.getString(BotResponse.BUBBLE_RIGHT_BG_COLOR, "#7B2D8E");
        userTextColor = sharedPreferences.getString(BotResponse.BUBBLE_RIGHT_TEXT_COLOR, "#FFFFFF");
    }

    @NonNull
    @Override
    public ButtonTemplateAdapter.BotButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.meeting_slot_button, parent, false);
        return new ButtonTemplateAdapter.BotButtonViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ButtonTemplateAdapter.BotButtonViewHolder holder, int position) {
        BotButtonModel buttonMap = buttons.get(position);
        holder.buttonTitle.setText(buttonMap.getTitle());
        applyButtonStyle(holder, position == selectedIndex);

        holder.rootLayout.setOnClickListener(v -> {
            try {
                if (!isLastItem) return;

                int clickedPosition = holder.getBindingAdapterPosition();
                if (clickedPosition == RecyclerView.NO_POSITION) return;

                selectedIndex = clickedPosition;
                if (contentStateListener != null && msgId != null) {
                    contentStateListener.onSaveState(msgId, selectedIndex, BotResponse.SELECTED_ITEM);
                }
                notifyDataSetChanged();

                String type = buttonMap.getType();
                if (invokeGenericWebViewInterface != null && (BundleConstants.BUTTON_TYPE_USER_INTENT.equals(type) ||
                        BundleConstants.BUTTON_TYPE_URL.equals(type) ||
                        BundleConstants.BUTTON_TYPE_WEB_URL.equals(type))) {
                    if (!StringUtils.isNullOrEmpty(buttonMap.getUrl())) {
                        invokeGenericWebViewInterface.invokeGenericWebView(buttonMap.getUrl());
                    }
                } else if (composeFooterInterface != null) {
                    if (!StringUtils.isNullOrEmpty(buttonMap.getPayload())) {
                        composeFooterInterface.onSendClick(buttonMap.getTitle(), buttonMap.getPayload(), false);
                    } else {
                        composeFooterInterface.onSendClick(buttonMap.getTitle(), false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (isFullWidth) {
            holder.rootLayout.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.buttonTitle.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        }
    }

    private void applyButtonStyle(BotButtonViewHolder holder, boolean selected) {
        GradientDrawable bgDrawable;
        if (holder.buttonTitle.getBackground() instanceof GradientDrawable) {
            bgDrawable = (GradientDrawable) holder.buttonTitle.getBackground().mutate();
        } else {
            bgDrawable = new GradientDrawable();
            holder.buttonTitle.setBackground(bgDrawable);
        }
        bgDrawable.setCornerRadius(5 * dp1);
        bgDrawable.setShape(GradientDrawable.RECTANGLE);

        if (selected) {
            bgDrawable.setColor(Color.parseColor(userTextColor));
            bgDrawable.setStroke((int) (1.5 * dp1), Color.parseColor("#D0D5DD"));
            holder.buttonTitle.setTextColor(Color.BLACK);
            return;
        }

        int color = Color.parseColor(rightTint);
        bgDrawable.setColor(color);
        bgDrawable.setStroke((int) (1.5 * dp1), color);
        holder.buttonTitle.setTextColor(Color.parseColor(userTextColor));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return buttons.size();
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

    public void setLastItem(boolean lastItem) {
        isLastItem = lastItem;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public static class BotButtonViewHolder extends RecyclerView.ViewHolder {
        TextView buttonTitle;
        LinearLayout rootLayout;

        public BotButtonViewHolder(View view) {
            super(view);
            buttonTitle = view.findViewById(R.id.text_view);
            rootLayout = view.findViewById(R.id.root_layout);
        }
    }
}
