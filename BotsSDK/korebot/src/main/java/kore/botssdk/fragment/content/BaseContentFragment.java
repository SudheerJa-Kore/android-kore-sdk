package kore.botssdk.fragment.content;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import kore.botssdk.R;
import kore.botssdk.adapter.ChatAdapter;
import kore.botssdk.listener.BotContentFragmentUpdate;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.TTSUpdate;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotBrandingModel;
import kore.botssdk.models.BotRequest;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.ComponentModel;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.PayloadOuter;
import kore.botssdk.models.QuickReplyTemplate;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.BundleUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.view.BrandedDayViewDecorator;
import kore.botssdk.viewmodels.content.BotContentViewModel;
import kore.botssdk.viewmodels.content.BotContentViewModelFactory;
import kore.botssdk.websocket.SocketWrapper;

@SuppressWarnings("UnKnownNullness")
public abstract class BaseContentFragment extends Fragment implements BotContentFragmentUpdate {
    private final int limit = 10;
    protected ChatAdapter botsChatAdapter;
    protected ComposeFooterInterface composeFooterInterface;
    protected InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    protected String mChannelIconURL;
    protected String mBotNameInitials;
    protected int mBotIconId;
    protected boolean fetching = false;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected int offset = 0;
    protected String jwt;
    protected BotContentViewModel mContentViewModel;
    protected TTSUpdate ttsUpdate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundleInfo();
        botsChatAdapter = new ChatAdapter();
        botsChatAdapter.setComposeFooterInterface(composeFooterInterface);
        botsChatAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
        BotContentViewModelFactory factory = new BotContentViewModelFactory(requireActivity(), BaseContentFragment.this);
        mContentViewModel = new ViewModelProvider(this, factory).get(BotContentViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = getSwipeRefreshLayout(view);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (botsChatAdapter != null) loadChatHistory(botsChatAdapter.getItemCount(), limit);
            else loadChatHistory(0, limit);
        });
    }

    public void setJwtTokenForWebHook(String jwt) {
        if (!StringUtils.isNullOrEmpty(jwt)) this.jwt = jwt;
    }

    public void setTtsUpdate(TTSUpdate ttsUpdate) {
        this.ttsUpdate = ttsUpdate;
    }

    public void refreshLanguageDirection(int layoutDirection) {
        View rootView = getView();
        if (rootView != null) {
            rootView.setLayoutDirection(layoutDirection);
            rootView.requestLayout();
        }
        if (botsChatAdapter != null) {
            botsChatAdapter.notifyDataSetChanged();
        }
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        if (botsChatAdapter != null)
            botsChatAdapter.setComposeFooterInterface(composeFooterInterface);
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
        if (botsChatAdapter != null)
            botsChatAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
    }

    private void getBundleInfo() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mChannelIconURL = bundle.getString(BundleUtils.CHANNEL_ICON_URL);
            mBotNameInitials = bundle.getString(BundleUtils.BOT_NAME_INITIALS, "B");
            mBotIconId = bundle.getInt(BundleUtils.BOT_ICON_ID, -1);
        }
    }

    public abstract void setBotBrandingModel(BotBrandingModel botBrandingModel);

    protected abstract SwipeRefreshLayout getSwipeRefreshLayout(View view);

    public abstract void showTypingStatus();

    public abstract void stopTypingStatus();

    public abstract void setQuickRepliesIntoFooter(BotResponse botResponse);

    public void showCalendarIntoFooter(BotResponse botResponse) {
        if (botResponse != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
            if (compModel != null) {
                String compType = compModel.getType();
                if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(compType)) {
                    PayloadOuter payOuter = compModel.getPayload();
                    PayloadInner payInner = payOuter == null ? null : payOuter.getPayload();
                    if (payInner == null) return;

                    boolean rangeMode = BotResponse.TEMPLATE_TYPE_DATE_RANGE.equalsIgnoreCase(payInner.getTemplate_type());
                    boolean singleMode = BotResponse.TEMPLATE_TYPE_DATE.equalsIgnoreCase(payInner.getTemplate_type());
                    if (!singleMode && !rangeMode) return;

                    if (getChildFragmentManager().findFragmentByTag(MaterialDatePicker.class.getName()) != null) {
                        return;
                    }

                    SharedPreferences preferences = requireContext().getSharedPreferences(
                            BotResponse.THEME_NAME,
                            Context.MODE_PRIVATE);
                    int userBubbleColor = parseBrandColor(
                            preferences.getString(
                                    BotResponse.BUBBLE_RIGHT_BG_COLOR,
                                    SDKConfiguration.BubbleColors.rightBubbleSelected),
                            Color.parseColor(SDKConfiguration.BubbleColors.rightBubbleSelected));
                    int userBubbleTextColor = parseBrandColor(
                            preferences.getString(
                                    BotResponse.BUBBLE_RIGHT_TEXT_COLOR,
                                    SDKConfiguration.BubbleColors.rightBubbleTextColor),
                            Color.parseColor(SDKConfiguration.BubbleColors.rightBubbleTextColor));

                    BrandedDayViewDecorator dayViewDecorator = new BrandedDayViewDecorator(
                            userBubbleColor,
                            userBubbleTextColor);

                    if (singleMode) {
                        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
                        builder.setTitleText(payInner.getTitle());
                        builder.setPositiveButtonText(R.string.confirm);
                        builder.setNegativeButtonText(R.string.cancel);
                        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR);
                        builder.setCalendarConstraints(mContentViewModel.minRange(
                                payInner.getStartDate(),
                                payInner.getEndDate(),
                                payInner.getFormat()).build());
                        builder.setTheme(R.style.MyMaterialCalendarTheme);
                        builder.setDayViewDecorator(dayViewDecorator);

                        MaterialDatePicker<Long> picker = builder.build();
                        picker.addOnPositiveButtonClickListener(selection -> {
                            if (selection != null && composeFooterInterface != null) {
                                composeFooterInterface.onSendClick(
                                        formatPickerDate(selection, payInner.getFormat(), false),
                                        false);
                            }
                        });
                        showBrandedMaterialPicker(picker, userBubbleColor, userBubbleTextColor);
                    } else {
                        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
                        builder.setTitleText(payInner.getTitle());
                        builder.setPositiveButtonText(R.string.confirm);
                        builder.setNegativeButtonText(R.string.cancel);
                        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR);
                        builder.setCalendarConstraints(mContentViewModel.minRange(
                                payInner.getStartDate(),
                                payInner.getEndDate(),
                                payInner.getFormat()).build());
                        builder.setTheme(R.style.MyMaterialCalendarTheme);
                        builder.setDayViewDecorator(dayViewDecorator);

                        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
                        picker.addOnPositiveButtonClickListener(selection -> {
                            if (selection != null
                                    && selection.first != null
                                    && selection.second != null
                                    && composeFooterInterface != null) {
                                String start = formatPickerDate(selection.first, payInner.getFormat(), true);
                                String end = formatPickerDate(selection.second, payInner.getFormat(), true);
                                composeFooterInterface.onSendClick(start + " to " + end, false);
                            }
                        });
                        showBrandedMaterialPicker(picker, userBubbleColor, userBubbleTextColor);
                    }
                }
            }
        }
    }

    private void showBrandedMaterialPicker(
            MaterialDatePicker<?> picker,
            int backgroundColor,
            int textColor) {
        picker.show(getChildFragmentManager(), MaterialDatePicker.class.getName());
        getChildFragmentManager().executePendingTransactions();

        View pickerView = picker.requireView();
        applyMaterialPickerBranding(pickerView, backgroundColor, textColor);
        pickerView.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> tintMaterialRangeFill(pickerView, backgroundColor));
    }

    private void applyMaterialPickerBranding(View pickerView, int backgroundColor, int textColor) {
        View header = pickerView.findViewById(com.google.android.material.R.id.mtrl_picker_header);
        if (header != null) header.setBackgroundColor(backgroundColor);

        TextView title = pickerView.findViewById(com.google.android.material.R.id.mtrl_picker_title_text);
        if (title != null) title.setTextColor(textColor);

        TextView selection = pickerView.findViewById(com.google.android.material.R.id.mtrl_picker_header_selection_text);
        if (selection != null) selection.setTextColor(textColor);

        ImageButton toggle = pickerView.findViewById(com.google.android.material.R.id.mtrl_picker_header_toggle);
        if (toggle != null) toggle.setImageTintList(ColorStateList.valueOf(textColor));

        Button cancel = pickerView.findViewById(com.google.android.material.R.id.cancel_button);
        if (cancel != null) cancel.setTextColor(backgroundColor);

        Button confirm = pickerView.findViewById(com.google.android.material.R.id.confirm_button);
        if (confirm != null) {
            int disabledColor = Color.argb(
                    97,
                    Color.red(backgroundColor),
                    Color.green(backgroundColor),
                    Color.blue(backgroundColor));
            confirm.setTextColor(new ColorStateList(
                    new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}},
                    new int[]{disabledColor, backgroundColor}));
        }

        tintMaterialRangeFill(pickerView, backgroundColor);
    }

    private void tintMaterialRangeFill(View view, int backgroundColor) {
        if (view instanceof GridView
                && "MaterialCalendarGridView".equals(view.getClass().getSimpleName())) {
            try {
                Object adapter = ((GridView) view).getAdapter();
                if (adapter != null) {
                    Field calendarStyleField = adapter.getClass().getDeclaredField("calendarStyle");
                    calendarStyleField.setAccessible(true);
                    Object calendarStyle = calendarStyleField.get(adapter);
                    if (calendarStyle != null) {
                        Field rangeFillField = calendarStyle.getClass().getDeclaredField("rangeFill");
                        rangeFillField.setAccessible(true);
                        Paint rangeFill = (Paint) rangeFillField.get(calendarStyle);
                        if (rangeFill != null) {
                            rangeFill.setColor(Color.argb(
                                    60,
                                    Color.red(backgroundColor),
                                    Color.green(backgroundColor),
                                    Color.blue(backgroundColor)));
                            view.invalidate();
                        }
                    }
                }
            } catch (ReflectiveOperationException ignored) {
                // MaterialDatePicker still renders normally if its internal structure changes.
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                tintMaterialRangeFill(group.getChildAt(index), backgroundColor);
            }
        }
    }

    private String formatPickerDate(long selection, String payloadFormat, boolean rangeMode) {
        String dateFormat = StringUtils.isNullOrEmpty(payloadFormat)
                ? (rangeMode ? "dd-MM-yyyy" : "MM/dd/yyyy")
                : payloadFormat.replace("DD", "dd").replace("YYYY", "yyyy").replace("YY", "yy");
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date(selection));
    }

    private int parseBrandColor(String color, int fallback) {
        try {
            return StringUtils.isNullOrEmpty(color) ? fallback : Color.parseColor(color);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    protected ArrayList<QuickReplyTemplate> getQuickReplies(BotResponse botResponse) {
        ArrayList<QuickReplyTemplate> quickReplyTemplates = null;
        if (botResponse != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
            if (compModel != null) {
                String compType = compModel.getType();
                if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(compType)) {
                    PayloadOuter payOuter = compModel.getPayload();
                    PayloadInner payInner = payOuter.getPayload();
                    if (payInner != null && BotResponse.TEMPLATE_TYPE_QUICK_REPLIES.equalsIgnoreCase(payInner.getTemplate_type())) {
                        quickReplyTemplates = payInner.getQuick_replies();
                    }
                }
            }
        }

        return quickReplyTemplates;
    }

    public abstract void addMessageToBotChatAdapter(BotResponse botResponse);

    public abstract void addStreamingMessage(String message);

    public abstract void addMessagesToBotChatAdapter(ArrayList<BaseBotMessage> list, boolean scrollToBottom);

    public abstract void addMessagesToBotChatAdapter(ArrayList<BaseBotMessage> list, boolean scrollToBottom, boolean isFirst);

    public abstract void updateContentListOnSend(BotRequest botRequest);

    @Override
    public void onChatHistory(ArrayList<BaseBotMessage> list, int offset, boolean scrollToBottom) {
        fetching = false;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (list != null) {
            this.offset = offset;
            addMessagesToBotChatAdapter(list, scrollToBottom);
        }
    }

    @Override
    public void onReconnectionChatHistory(ArrayList<BaseBotMessage> list, int offset, boolean isReconnectionHistory) {
        fetching = false;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (list != null) {
            this.offset = offset;
            addMessagesToBotChatAdapter(list, true, isReconnectionHistory);
        }
    }

    public void loadChatHistory(final int _offset, final int limit) {
        if (fetching) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        fetching = true;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        if (!SDKConfiguration.Client.isWebHook)
            mContentViewModel.loadChatHistory(_offset, limit, SocketWrapper.getInstance(requireActivity().getApplicationContext()).getAccessToken());
        else mContentViewModel.loadChatHistory(_offset, limit, jwt);
    }

    public int getAdapterCount() {
        if (botsChatAdapter != null) return botsChatAdapter.getItemCount();
        return 0;
    }

    public void loadReconnectionChatHistory(final int _offset, final int limit) {
        if (fetching) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        fetching = true;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        if (!SDKConfiguration.Client.isWebHook)
            mContentViewModel.loadReconnectionChatHistory(_offset, limit, SocketWrapper.getInstance(requireActivity().getApplicationContext()).getAccessToken(), botsChatAdapter.getBaseBotMessageArrayList());
        else
            mContentViewModel.loadReconnectionChatHistory(_offset, limit, jwt, botsChatAdapter.getBaseBotMessageArrayList());
    }

    public abstract void updateMessageStatus(BotRequest botRequest);

    public void deleteMessage(BaseBotMessage message) {
        if (botsChatAdapter != null && botsChatAdapter.getItemCount() > 0) {
            botsChatAdapter.deleteMessage(message);
        }
    }
}
