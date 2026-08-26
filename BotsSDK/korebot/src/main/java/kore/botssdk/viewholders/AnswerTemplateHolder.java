package kore.botssdk.viewholders;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.adapter.AnswerSourceAdapter;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.DataModel;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.SnippetContentModel;
import kore.botssdk.models.SourceModel;
import kore.botssdk.utils.markdown.MarkdownUtil;

public class AnswerTemplateHolder extends BaseViewHolder {
    public static AnswerTemplateHolder getInstance(ViewGroup parent) {
        return new AnswerTemplateHolder(createView(R.layout.template_answer, parent));
    }

    public AnswerTemplateHolder(@NonNull View itemView) {
        super(itemView, itemView.getContext());
    }

    @Override
    public void bind(BaseBotMessage baseBotMessage) {
        PayloadInner payloadInner = getPayloadInner(baseBotMessage);
        if (payloadInner == null) return;
        TextView tvAnswerContent = itemView.findViewById(R.id.answer_content);
        TextView answeredByAiText = itemView.findViewById(R.id.answered_by_ai_text);
        AppCompatImageView answeredByAiIcon = itemView.findViewById(R.id.answered_by_ai_icon);
        RecyclerView sourceRecycler = itemView.findViewById(R.id.linksRecycler);
        int attributionColor;
        try {
            String userBubbleColor = sharedPreferences.getString(
                    BotResponse.BUBBLE_RIGHT_BG_COLOR,
                    "#1B8A5A"
            );
            attributionColor = Color.parseColor(userBubbleColor);
        } catch (IllegalArgumentException exception) {
            attributionColor = Color.parseColor("#1B8A5A");
        }
        answeredByAiText.setTextColor(attributionColor);
        answeredByAiIcon.setImageTintList(ColorStateList.valueOf(attributionColor));
        String answer = payloadInner.getAnswer();
        if (answer != null) {
            String formattedAnswer = MarkdownUtil.processMarkDown(answer);
            tvAnswerContent.setText(HtmlCompat.fromHtml(
                    formattedAnswer.replace("\n", "<br />"),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
            ));
        } else {
            tvAnswerContent.setText("");
        }

        ArrayList<SourceModel> sources = new ArrayList<>();
        if (payloadInner.getAnswerPayload() != null
                && payloadInner.getAnswerPayload().getCenterPanel() != null
                && payloadInner.getAnswerPayload().getCenterPanel().getDataModels() != null
                && !payloadInner.getAnswerPayload().getCenterPanel().getDataModels().isEmpty()) {
            DataModel dataModel = payloadInner.getAnswerPayload().getCenterPanel().getDataModels().get(0);
            if (dataModel.getSnippetContents() != null) {
                for (SnippetContentModel snippet : dataModel.getSnippetContents()) {
                    if (snippet.getSources() != null && !snippet.getSources().isEmpty()) {
                        sources.add(snippet.getSources().get(0));
                    }
                }
            }
        }

        AnswerSourceAdapter adapter = new AnswerSourceAdapter(sources);
        adapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
        sourceRecycler.setAdapter(adapter);
    }
}
