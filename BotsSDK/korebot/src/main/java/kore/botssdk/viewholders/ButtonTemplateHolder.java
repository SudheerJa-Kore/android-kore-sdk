package kore.botssdk.viewholders;

import static kore.botssdk.viewUtils.DimensionUtil.dp1;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.adapter.ButtonTemplateAdapter;
import kore.botssdk.itemdecoration.VerticalSpaceItemDecoration;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotButtonModel;
import kore.botssdk.models.PayloadInner;

public class ButtonTemplateHolder extends BaseViewHolder {

    public static ButtonTemplateHolder getInstance(ViewGroup parent) {
        return new ButtonTemplateHolder(createView(R.layout.template_button, parent));
    }

    private ButtonTemplateHolder(@NonNull View itemView) {
        super(itemView, itemView.getContext());
        LinearLayoutCompat layoutBubble = itemView.findViewById(R.id.layoutBubble);
        initBubbleText(layoutBubble, false);
    }

    @Override
    public void bind(BaseBotMessage baseBotMessage) {
        PayloadInner payloadInner = getPayloadInner(baseBotMessage);
        if (payloadInner == null) return;
        setResponseText(itemView.findViewById(R.id.layoutBubble), payloadInner.getText(), baseBotMessage.getTimeStamp());
        ArrayList<BotButtonModel> botButtonModels = payloadInner.getButtons();
        final ButtonTemplateAdapter buttonTypeAdapter;
        RecyclerView buttonsList = itemView.findViewById(R.id.buttonsList);
        buttonsList.setLayoutManager(new LinearLayoutManager(buttonsList.getContext()));

        if (buttonsList.getItemDecorationCount() == 0) {
            buttonsList.addItemDecoration(new VerticalSpaceItemDecoration((int) (5 * dp1)));
        }

        if(botButtonModels != null && !botButtonModels.isEmpty()) {
            buttonTypeAdapter = new ButtonTemplateAdapter(buttonsList.getContext());
            buttonsList.setAdapter(buttonTypeAdapter);
            buttonTypeAdapter.setComposeFooterInterface(composeFooterInterface);
            buttonTypeAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
            buttonTypeAdapter.populateData(botButtonModels, isLastItem());
        }
    }
}
