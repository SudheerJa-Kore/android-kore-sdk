package kore.botssdk.viewholders;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static kore.botssdk.viewUtils.DimensionUtil.dp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import kore.botssdk.R;
import kore.botssdk.adapter.ButtonLinkTemplateAdapter;
import kore.botssdk.itemdecoration.VerticalSpaceItemDecoration;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.utils.markdown.MarkdownImageTagHandler;
import kore.botssdk.utils.markdown.MarkdownTagHandler;
import kore.botssdk.utils.markdown.MarkdownUtil;

public class ButtonLinkTemplateHolder extends BaseViewHolder {
    private final RecyclerView recyclerView;
    private final TextView tvButtonLinkTitle;
    private final Context context;

    public static ButtonLinkTemplateHolder getInstance(ViewGroup parent) {
        return new ButtonLinkTemplateHolder(createView(R.layout.template_button_link, parent));
    }

    private ButtonLinkTemplateHolder(@NonNull View itemView) {
        super(itemView, itemView.getContext());
        context = itemView.getContext();
        recyclerView = itemView.findViewById(R.id.botCustomButtonList);
        recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration((int) (1 * dp1)));
        tvButtonLinkTitle = itemView.findViewById(R.id.tvButtonLinkTitle);
        SharedPreferences sharedPreferences = context.getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
        String leftBgColor = sharedPreferences.getString(BotResponse.BUBBLE_LEFT_BG_COLOR, "#FFFFFF");
        GradientDrawable leftDrawable = (GradientDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.theme1_left_bubble_bg, context.getTheme());
        if (leftDrawable != null) {
            leftDrawable.setColor(Color.parseColor(leftBgColor));
            leftDrawable.setStroke((int) (1 * dp1), Color.parseColor(leftBgColor));
            tvButtonLinkTitle.setBackground(leftDrawable);
        }
    }

    @Override
    public void bind(BaseBotMessage baseBotMessage) {
        PayloadInner payloadInner = getPayloadInner(baseBotMessage);
        if (!StringUtils.isNullOrEmpty(payloadInner.getText())) {
            tvButtonLinkTitle.setVisibility(View.VISIBLE);
            String textualContent = unescapeHtml4(payloadInner.getText().trim());
            textualContent = StringUtils.unescapeHtml3(textualContent.trim());
            textualContent = MarkdownUtil.processMarkDown(textualContent);
            CharSequence sequence = HtmlCompat.fromHtml(
                    textualContent.replace("\n", "<br />"),
                    HtmlCompat.FROM_HTML_MODE_LEGACY,
                    new MarkdownImageTagHandler(context, tvButtonLinkTitle, textualContent),
                    new MarkdownTagHandler()
            );
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            tvButtonLinkTitle.setText(strBuilder);
            tvButtonLinkTitle.setMovementMethod(null);
        } else {
            tvButtonLinkTitle.setVisibility(View.GONE);
        }

        ButtonLinkTemplateAdapter buttonTypeAdapter = new ButtonLinkTemplateAdapter(payloadInner.getButtons(), isLastItem());
        recyclerView.setAdapter(buttonTypeAdapter);
        buttonTypeAdapter.setComposeFooterInterface(composeFooterInterface);
        buttonTypeAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
    }
}
