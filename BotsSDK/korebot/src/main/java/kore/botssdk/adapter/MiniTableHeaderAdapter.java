package kore.botssdk.adapter;

import static android.view.Gravity.CENTER_VERTICAL;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kore.botssdk.R;

public class MiniTableHeaderAdapter extends RecyclerView.Adapter<MiniTableHeaderAdapter.ViewHolder> {

    private final String RIGHT = "right";
    private final String CENTER = "center";
    private final List<List<String>> tableItems;
    private boolean isEnabled;
    private final LayoutInflater layoutInflater;

    public MiniTableHeaderAdapter(Context context, List<List<String>> tableItems, boolean isEnabled) {
        layoutInflater = LayoutInflater.from(context);
        this.isEnabled = isEnabled;
        this.tableItems = tableItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.mini_table_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> item = getItem(position);
        if (item == null) return;
        holder.value.setText(item.get(0));
        holder.value.setGravity(Gravity.START | CENTER_VERTICAL);
        if (item.size() > 1) {
            switch (item.get(1)) {
                case CENTER:
                    holder.value.setGravity(Gravity.CENTER);
                    break;
                case RIGHT:
                    holder.value.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                    break;
            }
        }
    }

    private List<String> getItem(int position) {
        return tableItems != null ? tableItems.get(position) : null;
    }

    @Override
    public int getItemCount() {
        return tableItems == null ? 0 : tableItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView value;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            value = itemView.findViewById(R.id.tvTableValue);
        }
    }
}
