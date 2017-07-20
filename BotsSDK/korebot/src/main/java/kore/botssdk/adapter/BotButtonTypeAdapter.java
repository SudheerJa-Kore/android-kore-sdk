package kore.botssdk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.models.ButtonTemplate;

/**
 * Created by Anil Kumar on 12/1/2016.
 */
public class BotButtonTypeAdapter extends BaseAdapter /*implements View.OnClickListener*/ {
    private Context mContext;

    private ArrayList<ButtonTemplate> optionsList;
    private LayoutInflater inflater = null;
    ButtonTemplate tempValues;
    private static final int OPTIONS_LIST_LIMIT = 3;
    public static boolean isInExpandedMode;
    private MoreSelectionListener moreSelectionListener;

    public BotButtonTypeAdapter(Context a) {
        mContext = a;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        if (isInExpandedMode || !showMore())
            return optionsList.size();
        else
            return (OPTIONS_LIST_LIMIT + 1);
    }

    public int getOptionsSize() {
        return optionsList.size();
    }

    @Override
    public ButtonTemplate getItem(int position) {
        return optionsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ButtonTemplate option = getItem(position);
        if (!TextUtils.isEmpty(option.getTitle())) {
            if (!isInExpandedMode && showMore()) {
                if (position == OPTIONS_LIST_LIMIT) {
                    convertView = getShowMoreView(convertView, parent);
                } else {
                    convertView = getOptionsView(convertView, position);
                }
            } else {
                convertView = getOptionsView(convertView, position);
            }
        }

        return convertView;
    }

    private View getOptionsView(View convertView, int position) {
        View vi = convertView;
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            vi = inflater.inflate(R.layout.bot_custom_list_row, null);
            holder = new ViewHolder();
            holder.text = (TextView) vi.findViewById(R.id.botTextView1);
            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (optionsList.size() <= 0) {
            holder.text.setText("No Data");
        } else {
            tempValues = null;
            tempValues = (ButtonTemplate) optionsList.get(position);
            holder.text.setText(tempValues.getTitle());
        }
        return vi;
    }

    private View getShowMoreView(View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.bot_options_more_list_item, parent, false);
        LinearLayout container = (LinearLayout) convertView.findViewById(R.id.bot_options_more);
        TextView moreTextView = (TextView) convertView.findViewById(R.id.more_txt_view);
        moreTextView.setText("More");

        // LinearLayout membersMoreOption = (LinearLayout) convertView.findViewById(R.id.space_members_more);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInExpandedMode = true;
                //notifyDataSetChanged();

                if (moreSelectionListener != null)
                    moreSelectionListener.onMoreSelected();
            }
        });
        convertView.setTag(null);
        return convertView;
    }

    private boolean showMore() {
        if (optionsList != null && !optionsList.isEmpty()) {
            return (optionsList.size() > OPTIONS_LIST_LIMIT) ? true : false;
        }
        return false;
    }

    public interface MoreSelectionListener {
        void onMoreSelected();
    }

    public void setMoreSelectionListener(MoreSelectionListener moreSelectionListener) {
        this.moreSelectionListener = moreSelectionListener;
    }

    public ArrayList<ButtonTemplate> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(ArrayList<ButtonTemplate> optionsList) {
        this.optionsList = optionsList;
    }
}