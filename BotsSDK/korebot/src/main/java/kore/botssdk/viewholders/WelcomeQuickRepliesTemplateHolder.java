package kore.botssdk.viewholders;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.Map;

import kore.botssdk.R;
import kore.botssdk.adapter.QuickRepliesTemplateAdapter;
import kore.botssdk.itemdecoration.VerticalSpaceItemDecoration;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.QuickReplyTemplate;

public class WelcomeQuickRepliesTemplateHolder extends BaseViewHolder {
    private final RecyclerView recyclerView;

    public static WelcomeQuickRepliesTemplateHolder getInstance(ViewGroup parent) {
        return new WelcomeQuickRepliesTemplateHolder(createView(R.layout.template_welcome_quick_replies, parent));
    }

    private WelcomeQuickRepliesTemplateHolder(@NonNull View itemView) {
        super(itemView, itemView.getContext());
        LinearLayoutCompat layoutBubble = itemView.findViewById(R.id.layoutBubble);
        initBubbleText(layoutBubble, false);
        recyclerView = itemView.findViewById(R.id.replies);
        recyclerView.setClipToPadding(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        if (recyclerView.getItemDecorationCount() == 0) {
            recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(15));
        }
    }

    @Override
    public void bind(BaseBotMessage baseBotMessage) {
        PayloadInner payloadInner = getPayloadInner(baseBotMessage);
        if (payloadInner == null) return;
        setResponseText(itemView.findViewById(R.id.layoutBubble), payloadInner.getText(), baseBotMessage.getTimeStamp());
        ArrayList<QuickReplyTemplate> quickReplyTemplates = payloadInner.getQuick_replies();
        if (quickReplyTemplates == null) return;

        StaggeredGridLayoutManager staggeredGridLayoutManager;
        if (quickReplyTemplates.size() / 2 > 0)
            staggeredGridLayoutManager = new StaggeredGridLayoutManager(quickReplyTemplates.size() / 2, LinearLayoutManager.HORIZONTAL);
        else
            staggeredGridLayoutManager = new StaggeredGridLayoutManager(Math.max(quickReplyTemplates.size(), 1), LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        int selectedIndex = -1;
        Map<String, Object> contentState = ((BotResponse) baseBotMessage).getContentState();
        if (contentState != null && contentState.containsKey(BotResponse.SELECTED_ITEM)) {
            Object value = contentState.get(BotResponse.SELECTED_ITEM);
            if (value instanceof Integer) {
                selectedIndex = (int) value;
            }
        }

        QuickRepliesTemplateAdapter quickRepliesAdapter = (QuickRepliesTemplateAdapter) recyclerView.getAdapter();
        if (quickRepliesAdapter == null) {
            quickRepliesAdapter = new QuickRepliesTemplateAdapter(itemView.getContext(), recyclerView, isLastItem());
            recyclerView.setAdapter(quickRepliesAdapter);
        }
        quickRepliesAdapter.setEnabled(isLastItem());
        quickRepliesAdapter.setMsgId(((BotResponse) baseBotMessage).getMessageId());
        quickRepliesAdapter.setContentStateListener(contentStateListener);
        quickRepliesAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
        quickRepliesAdapter.setComposeFooterInterface(composeFooterInterface);
        quickRepliesAdapter.setSelectedIndex(selectedIndex);
        quickRepliesAdapter.setQuickReplyTemplateArrayList(quickReplyTemplates);
        quickRepliesAdapter.notifyDataSetChanged();
    }
}
