package kore.botssdk.adapter;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.SourceModel;

public class AnswerSourceAdapter extends RecyclerView.Adapter<AnswerSourceAdapter.ViewHolder> {
    private final ArrayList<SourceModel> sources;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;

    public AnswerSourceAdapter(ArrayList<SourceModel> sources) {
        this.sources = sources;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.answer_source_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SourceModel source = sources.get(position);
        String prefix = (position + 1) + ". ";
        String sourceTitle = source.getTitle() != null ? source.getTitle() : "";
        SpannableString title = new SpannableString(prefix + sourceTitle);
        int linkColor;
        try {
            String userBubbleColor = holder.itemView.getContext()
                    .getSharedPreferences(BotResponse.THEME_NAME, 0)
                    .getString(BotResponse.BUBBLE_RIGHT_BG_COLOR, "#1B8A5A");
            linkColor = Color.parseColor(userBubbleColor);
        } catch (IllegalArgumentException exception) {
            linkColor = Color.parseColor("#1B8A5A");
        }
        title.setSpan(
                new ForegroundColorSpan(linkColor),
                prefix.length(),
                title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        title.setSpan(
                new UnderlineSpan(),
                prefix.length(),
                title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        holder.tvTitle.setText(title);
        holder.tvTitle.setOnClickListener(v -> {
            if (invokeGenericWebViewInterface != null && source.getUrl() != null) {
                invokeGenericWebViewInterface.invokeGenericWebView(source.getUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return sources != null ? sources.size() : 0;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.root);
        }
    }
}
