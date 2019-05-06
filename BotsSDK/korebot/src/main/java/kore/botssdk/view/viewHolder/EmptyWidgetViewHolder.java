package kore.botssdk.view.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kore.botssdk.R;

public class EmptyWidgetViewHolder extends RecyclerView.ViewHolder {
    public ImageView img_icon;
    public TextView tv_disrcription;

    public EmptyWidgetViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_disrcription = itemView.findViewById(R.id.tv_message);
        img_icon = itemView.findViewById(R.id.img_icon);
    }
}
