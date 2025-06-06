package kore.botssdk.viewholders;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import kore.botssdk.R;
import kore.botssdk.adapter.AdvancedListAdapter;
import kore.botssdk.dialogs.AdvancedListActionSheetFragment;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.view.AutoExpandListView;

public class AdvancedListTemplateHolder extends BaseViewHolder {
    public static AdvancedListTemplateHolder getInstance(ViewGroup parent) {
        return new AdvancedListTemplateHolder(createView(R.layout.template_advanced_list_template, parent));
    }

    private AdvancedListTemplateHolder(@NonNull View itemView) {
        super(itemView, itemView.getContext());
    }

    @Override
    public void bind(BaseBotMessage baseBotMessage) {
        PayloadInner payloadInner = getPayloadInner(baseBotMessage);
        AutoExpandListView autoExpandListView = itemView.findViewById(R.id.botCustomListView);
        TextView botCustomListViewButton = itemView.findViewById(R.id.botCustomListViewButton);
        TextView botListViewTitle = itemView.findViewById(R.id.botListViewTitle);
        TextView tvDescription = itemView.findViewById(R.id.tvDescription);
        ImageView ivSearch = itemView.findViewById(R.id.ivSearch);
        ImageView ivFilter = itemView.findViewById(R.id.ivFilter);

        ivSearch.setVisibility(payloadInner.isSearchEnabled() ? VISIBLE : GONE);
        ivFilter.setVisibility(payloadInner.isSortEnabled() ? VISIBLE : GONE);
        botCustomListViewButton.setVisibility(GONE);
        botListViewTitle.setVisibility(!StringUtils.isNullOrEmpty(payloadInner.getTitle()) ? VISIBLE : GONE);
        botListViewTitle.setText(payloadInner.getTitle());

        tvDescription.setVisibility(!StringUtils.isNullOrEmpty(payloadInner.getDescription()) ? VISIBLE : GONE);
        tvDescription.setText(payloadInner.getDescription());

        AdvancedListAdapter botListTemplateAdapter = new AdvancedListAdapter(itemView.getContext(), autoExpandListView);
        botListTemplateAdapter.setComposeFooterInterface(composeFooterInterface);
        botListTemplateAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
        botListTemplateAdapter.dispalyCount(payloadInner.getListItemDisplayCount());
        botListTemplateAdapter.setBotListModelArrayList(payloadInner.getListItems());
        botListTemplateAdapter.notifyDataSetChanged();
        autoExpandListView.setAdapter(botListTemplateAdapter);

        if (!StringUtils.isNullOrEmpty(payloadInner.getSeeMoreTitle()) && payloadInner.getListItemDisplayCount() != 0 && payloadInner.getListItemDisplayCount() < payloadInner.getListItems().size()) {
            botCustomListViewButton.setVisibility(VISIBLE);
            botCustomListViewButton.setText(payloadInner.getSeeMoreTitle());
            botCustomListViewButton.setTextColor(itemView.getContext().getColor(R.color.bgBlueSignup));
        } else {
            botCustomListViewButton.setVisibility(GONE);
        }

        botCustomListViewButton.setOnClickListener(v -> {
            AdvancedListActionSheetFragment bottomSheetDialog = new AdvancedListActionSheetFragment();
            bottomSheetDialog.setTitle(payloadInner.getTitle());
            bottomSheetDialog.setSkillName("skillName");
            bottomSheetDialog.setData(payloadInner.getListItems());
            bottomSheetDialog.setComposeFooterInterface(composeFooterInterface);
            bottomSheetDialog.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
            bottomSheetDialog.show(((FragmentActivity) itemView.getContext()).getSupportFragmentManager(), "add_tags");
        });
    }
}
