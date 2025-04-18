package kore.botssdk.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import kore.botssdk.R;
import kore.botssdk.activity.GenericWebViewActivity;
import kore.botssdk.dialogs.WidgetActionSheetFragment;
import kore.botssdk.event.KoreEventCenter;
import kore.botssdk.events.EntityEditEvent;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.RecyclerViewDataAccessor;
import kore.botssdk.listener.VerticalListViewActionHelper;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.ButtonsLayoutModel;
import kore.botssdk.models.LoginModel;
import kore.botssdk.models.Widget;
import kore.botssdk.models.WidgetListElementModel;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.Constants;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.viewholders.EmptyWidgetViewHolder;
import kore.botssdk.viewUtils.RoundedCornersTransform;

@SuppressLint("UnknownNullness")
public class ListWidgetAdapter extends RecyclerView.Adapter implements RecyclerViewDataAccessor {
    VerticalListViewActionHelper verticalListViewActionHelper;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    private ComposeFooterInterface composeFooterInterface;
    private BottomSheetDialog bottomSheetDialog;
    private final SharedPreferences sharedPreferences;
    ArrayList<String> selectedIds;
    private ArrayList<WidgetListElementModel> items = new ArrayList<>();
    private LayoutInflater inflater;
    final Context mContext;
    String skillName;
    LoginModel loginModel;
    private final int EMPTY_CARD = 0;
    private final int MESSAGE = 2;
    private final int REPORTS = 3;
    int previewLength;
    String msg;
    Drawable errorIcon;
    final String trigger;
    private boolean isLoginNeeded;

    private boolean isEnable = true;

    public ListWidgetAdapter(Context mContext, String trigger) {
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        this.trigger = trigger;
        this.sharedPreferences = mContext.getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
        selectedIds = new ArrayList<>();
    }

    public WidgetListElementModel getItem(int position) {
        if (position < items.size()) return items.get(position);
        else return null;
    }

    public void setIsEnabled(boolean isEnable) {
        this.isEnable = isEnable;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoginNeeded()) {
            return REPORTS;
        }
        if (items != null && items.size() > 0) {
            return 1;
        }

        if (msg != null && !msg.equalsIgnoreCase("")) {
            return MESSAGE;
        }
        return EMPTY_CARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == REPORTS) {
            View view = inflater.inflate(R.layout.need_login_widget_layout, parent, false);
            return new ReportsViewHolder(view);
        } else if (viewType == EMPTY_CARD || viewType == MESSAGE) {
            View view = inflater.inflate(R.layout.card_empty_widget_layout, parent, false);
            return new EmptyWidgetViewHolder(view);
        } else return new ViewHolder(inflater.inflate(R.layout.listwidget_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderData, int position) {
        if (holderData.getItemViewType() == REPORTS) {
            ReportsViewHolder holder = (ReportsViewHolder) holderData;

            holder.loginBtn.setOnClickListener(view -> {
                if (mContext instanceof Activity) {
                    if (loginModel != null) {
                        Intent intent = new Intent(mContext, GenericWebViewActivity.class);
                        intent.putExtra("url", loginModel.getUrl());
                        intent.putExtra("header", mContext.getResources().getString(R.string.app_name));
                        ((Activity) mContext).startActivityForResult(intent, BundleConstants.REQ_CODE_REFRESH_CURRENT_PANEL);
                    }
                } else {
                    Toast.makeText(mContext, "Instance not activity", Toast.LENGTH_LONG).show();
                }
            });
        } else if (holderData.getItemViewType() == EMPTY_CARD || holderData.getItemViewType() == MESSAGE) {
            EmptyWidgetViewHolder emptyHolder = (EmptyWidgetViewHolder) holderData;

            emptyHolder.tv_disrcription.setText(msg != null ? msg : "No data");
            emptyHolder.img_icon.setImageDrawable(holderData.getItemViewType() == EMPTY_CARD ? ContextCompat.getDrawable(mContext, R.drawable.no_meeting) : errorIcon);
        } else {

            final ListWidgetAdapter.ViewHolder holder = (ListWidgetAdapter.ViewHolder) holderData;
            final WidgetListElementModel model = items.get(position);

            if (StringUtils.isNullOrEmpty(model.getTitle())) {
                holder.txtTitle.setVisibility(GONE);
            } else {
                holder.txtTitle.setText(model.getTitle().trim());

//                if (sharedPreferences != null) {
//                    holder.txtTitle.setTextColor(Color.parseColor(sharedPreferences.getString(BotResponse.BUTTON_ACTIVE_TXT_COLOR, "#000000")));
//                }
            }

            if (StringUtils.isNullOrEmpty(model.getSubtitle())) {
                holder.txtSubTitle.setVisibility(GONE);
            } else {
                holder.txtSubTitle.setText(model.getSubtitle().trim());

//                if (sharedPreferences != null) {
//                    holder.txtSubTitle.setTextColor(Color.parseColor(sharedPreferences.getString(BotResponse.BUTTON_ACTIVE_TXT_COLOR, "#000000")));
//                }
            }


            holder.img_up_down.setOnClickListener(v -> {
                boolean expanded = holder.buttonLayout.isExpanded();
                if (!expanded)
                    holder.img_up_down.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_arrow_drop_up_24px, mContext.getTheme()));
                else
                    holder.img_up_down.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_arrow_drop_down_24px, mContext.getTheme()));
                holder.buttonLayout.setExpanded(!expanded);
            });

            if (model.getImage() != null && !StringUtils.isNullOrEmpty(model.getImage().getImage_src()) && Patterns.WEB_URL.matcher(model.getImage().getImage_src()).matches()) {
                String url = model.getImage().getImage_src().trim();
                url = url.replace("http://", "https://");
                Picasso.get().load(url).error(R.drawable.ic_image_photo).transform(new RoundedCornersTransform()).into(holder.imageIcon);
            } else {
                holder.imageIcon.setVisibility(GONE);
            }

            if (model.getValue() != null && model.getValue().getType() != null) {
                switch (model.getValue().getType()) {
                    case "button":
                        holder.icon_image_load.setVisibility(GONE);
                        holder.imgMenu.setVisibility(GONE);
                        holder.tvText.setVisibility(GONE);
                        holder.tvUrl.setVisibility(GONE);
                        holder.tvButtonParent.setVisibility(VISIBLE);

                        holder.tvButton.setOnClickListener(v -> buttonAction(model.getValue().getButton(), TextUtils.isEmpty(Constants.SKILL_SELECTION) || !StringUtils.isNullOrEmpty(skillName) && !skillName.equalsIgnoreCase(Constants.SKILL_SELECTION)));
                        String btnTitle = "";
                        if (model.getValue().getButton() != null && model.getValue().getButton().getTitle() != null)
                            btnTitle = model.getValue().getButton().getTitle();
                        else btnTitle = model.getValue().getText();
                        if (!StringUtils.isNullOrEmpty(btnTitle)) holder.tvButton.setText(btnTitle);
                        else holder.tvButtonParent.setVisibility(GONE);

                        break;
                    case "menu":
                        holder.icon_image_load.setVisibility(GONE);
                        holder.imgMenu.setVisibility(VISIBLE);
                        holder.imgMenu.bringToFront();
                        holder.imgMenu.setOnClickListener(v -> {
                            if (model.getValue() != null && model.getValue().getMenu() != null && model.getValue().getMenu().size() > 0) {
                                WidgetActionSheetFragment bottomSheetDialog = new WidgetActionSheetFragment();
                                bottomSheetDialog.setisFromFullView(false);
                                bottomSheetDialog.setSkillName(skillName, trigger);
                                bottomSheetDialog.setData(model, true);
                                bottomSheetDialog.setVerticalListViewActionHelper(verticalListViewActionHelper);
                                bottomSheetDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "add_tags");
                            }
                        });
                        holder.tvText.setVisibility(GONE);
                        holder.tvButtonParent.setVisibility(GONE);
                        holder.tvButton.setVisibility(GONE);
                        holder.tvUrl.setVisibility(GONE);
                        break;
                    case "text":
                        holder.icon_image_load.setVisibility(GONE);
                        holder.imgMenu.setVisibility(GONE);
                        holder.tvText.setVisibility(VISIBLE);
                        holder.tvText.setText(model.getValue().getText());
                        holder.tvButtonParent.setVisibility(GONE);
                        holder.tvUrl.setVisibility(GONE);
                        break;
                    case "url":
                        holder.icon_image_load.setVisibility(GONE);
                        holder.imgMenu.setVisibility(GONE);
                        holder.tvText.setVisibility(GONE);
                        SpannableString content = new SpannableString(model.getValue().getUrl().getTitle() != null ? model.getValue().getUrl().getTitle() : model.getValue().getUrl().getLink());
                        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                        holder.tvUrl.setText(content);
                        holder.tvButtonParent.setVisibility(GONE);
                        holder.tvUrl.setVisibility(VISIBLE);
                        holder.tvUrl.setOnClickListener(v -> {
                            if (model.getValue().getUrl().getLink() != null) {
                                Intent intent = new Intent(mContext, GenericWebViewActivity.class);
                                intent.putExtra("url", model.getValue().getUrl().getLink());
                                intent.putExtra("header", mContext.getResources().getString(R.string.app_name));
                                mContext.startActivity(intent);
                            }
                        });

                        break;
                    case "image":
                        holder.icon_image_load.setVisibility(VISIBLE);
                        holder.imgMenu.setVisibility(GONE);
                        holder.tvText.setVisibility(GONE);
                        holder.tvButtonParent.setVisibility(GONE);
                        holder.tvUrl.setVisibility(GONE);
                        if (model.getValue().getImage() != null && !StringUtils.isNullOrEmpty(model.getValue().getImage().getImage_src())) {
                            Picasso.get().load(model.getValue().getImage().getImage_src()).into(holder.icon_image_load);
                            holder.icon_image_load.setOnClickListener(v -> defaultAction(model.getValue().getImage().getUtterance() != null ? model.getValue().getImage().getUtterance() : model.getValue().getImage().getPayload() != null ? model.getValue().getImage().getPayload() : "", Constants.SKILL_SELECTION.equalsIgnoreCase(Constants.SKILL_HOME) || TextUtils.isEmpty(Constants.SKILL_SELECTION) || (!StringUtils.isNullOrEmpty(skillName) && !skillName.equalsIgnoreCase(Constants.SKILL_SELECTION))));
                        }
                        break;
                }

            }

            if (model.getButtons() != null && !model.getButtons().isEmpty()) {
                ListWidgetButtonAdapter buttonRecyclerAdapter = new ListWidgetButtonAdapter(mContext, model.getButtons(), trigger);
                buttonRecyclerAdapter.setSkillName(skillName);
                buttonRecyclerAdapter.setIsFromFullView(isFullView);
                buttonRecyclerAdapter.setEnabled(isEnable);
                buttonRecyclerAdapter.setComposeFooterInterface(composeFooterInterface);
                buttonRecyclerAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
                buttonRecyclerAdapter.setBottomSheet(bottomSheetDialog);

                if (model.getButtonsLayout() != null) {
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(model.getButtonsLayout());

                    Type type = new TypeToken<ButtonsLayoutModel>() {
                    }.getType();
                    ButtonsLayoutModel hashMap = gson.fromJson(jsonString, type);

                    if (hashMap != null) {
                        buttonRecyclerAdapter.setDisplayLimit(hashMap.getDisplayLimit().getCount());
                        if (hashMap.getStyle().equalsIgnoreCase(BundleConstants.FIT_TO_WIDTH)) {
                            buttonRecyclerAdapter.setType(1);
                            holder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                        } else {
                            buttonRecyclerAdapter.setType(0);
                            holder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                        }
                    }

                }

                holder.recyclerView.setAdapter(buttonRecyclerAdapter);
                buttonRecyclerAdapter.notifyItemRangeChanged(0, model.getButtons().size() - 1);
            } else {
                holder.img_up_down.setVisibility(GONE);
            }

            holder.alDetails.setVisibility(GONE);
            if (model.getDetails() != null && !model.getDetails().isEmpty()) {
                holder.alDetails.setVisibility(VISIBLE);
                ListWidgetDetailsAdapter listWidgetDetailsAdapter = new ListWidgetDetailsAdapter(mContext, model.getDetails());
                holder.alDetails.setAdapter(listWidgetDetailsAdapter);
            }

            holder.innerlayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model.getDefault_action() != null && model.getDefault_action().getType() != null && model.getDefault_action().getType().equals("url")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getDefault_action().getUrl()));
                        try {
                            mContext.startActivity(browserIntent);
                        } catch (ActivityNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    } else if (model.getDefault_action() != null && model.getDefault_action().getType() != null && model.getDefault_action().getType().equals("postback")) {
                        defaultAction(model.getDefault_action().getPayload(), TextUtils.isEmpty(Constants.SKILL_SELECTION) || !StringUtils.isNullOrEmpty(skillName) && !skillName.equalsIgnoreCase(Constants.SKILL_SELECTION));
                    }
                }
            });
            if (position == items.size() - 1 && items.size() < 3) {
                holder.divider.setVisibility(View.GONE);
            }
        }
    }

    public void defaultAction(String utterance, boolean appendUtterance) {
        EntityEditEvent event = new EntityEditEvent();
        StringBuffer msg = new StringBuffer();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("refresh", Boolean.TRUE);
        if (appendUtterance && trigger != null) msg = msg.append(trigger).append(" ");
        msg.append(utterance);
        event.setMessage(msg.toString());
        event.setPayLoad(new Gson().toJson(hashMap));
        event.setScrollUpNeeded(true);
        KoreEventCenter.post(event);
        if (isFullView) {
            ((Activity) mContext).finish();
        }
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public void buttonAction(Widget.Button button, boolean appendUtterance) {
        String utterance = null;
        if (button != null) {
            utterance = button.getUtterance();
        }
        if (utterance == null) return;
        if (utterance.startsWith("tel:") || utterance.startsWith("mailto:")) {
            if (utterance.startsWith("tel:")) {
                launchDialer(mContext, utterance);
            } else if (utterance.startsWith("mailto:")) {
                showEmailIntent((Activity) mContext, utterance.split(":")[1]);
            }
            return;
        }
        EntityEditEvent event = new EntityEditEvent();
        StringBuffer msg = new StringBuffer();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("refresh", Boolean.TRUE);
        if (appendUtterance && trigger != null) msg = msg.append(trigger).append(" ");
        msg.append(utterance);
        event.setMessage(msg.toString());
        event.setPayLoad(new Gson().toJson(hashMap));
        event.setScrollUpNeeded(true);
        KoreEventCenter.post(event);

        try {
            if (isFullView) {
                ((Activity) mContext).finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showEmailIntent(Activity activity, String recepientEmail) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + recepientEmail));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");

        try {
            activity.startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Error while launching email intent!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    public static void launchDialer(Context context, String url) {
        try {
            Intent intent = new Intent(hasPermission(context, Manifest.permission.CALL_PHONE) ? Intent.ACTION_CALL : Intent.ACTION_DIAL);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Invalid url!", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean hasPermission(Context context, String... permission) {
        boolean shouldShowRequestPermissionRationale = true;
        for (String s : permission) {
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale && ActivityCompat.checkSelfPermission(context, s) == PackageManager.PERMISSION_GRANTED;
        }
        return shouldShowRequestPermissionRationale;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items != null && items.size() > 0 ? (Math.min(items.size(), previewLength)) : 1;
    }


    @Override
    public ArrayList getData() {
        return items;
    }

    @Override
    public void setData(ArrayList data) {
        this.items = data;
        if (items != null) notifyItemRangeChanged(0, data.size() - 1);
    }

    public void setWidgetData(ArrayList<WidgetListElementModel> data) {
        this.items = data;
        notifyItemRangeChanged(0, data.size() - 1);
    }

    @Override
    public void setExpanded(boolean isExpanded) {
    }

    @Override
    public void setVerticalListViewActionHelper(VerticalListViewActionHelper verticalListViewActionHelper) {
        this.verticalListViewActionHelper = verticalListViewActionHelper;

    }

    public void setPreviewLength(int previewLength) {
        this.previewLength = previewLength;
    }

    public void setBottomSheet(BottomSheetDialog bottomSheetDialog) {
        this.bottomSheetDialog = bottomSheetDialog;
    }

    public void setMessage(String msg, Drawable errorIcon) {
        this.msg = msg;
        this.errorIcon = errorIcon;
    }

    boolean isFullView;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout innerlayout;
        public final TextView txtTitle;
        public final TextView txtSubTitle;
        public final ImageView imageIcon;
        public final ImageView img_up_down;
        public final ExpandableLayout buttonLayout;
        public final View divider;
        public final RecyclerView recyclerView;
        public final ImageView imgMenu;
        public final ImageView icon_image_load;
        public final TextView tvText;
        public final TextView tvUrl;
        public final TextView tvButton;
        public final LinearLayout tvButtonParent;
        public final ListView alDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSubTitle = itemView.findViewById(R.id.txtSubtitle);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            buttonLayout = itemView.findViewById(R.id.button_layout);
            img_up_down = itemView.findViewById(R.id.img_up_down);
            innerlayout = itemView.findViewById(R.id.innerlayout);
            recyclerView = itemView.findViewById(R.id.buttonsList);
            divider = itemView.findViewById(R.id.divider);
            imgMenu = itemView.findViewById(R.id.icon_image);
            tvButton = itemView.findViewById(R.id.tv_button);
            tvText = itemView.findViewById(R.id.tv_text);
            tvUrl = itemView.findViewById(R.id.tv_url);
            icon_image_load = itemView.findViewById(R.id.icon_image_load);
            tvButtonParent = itemView.findViewById(R.id.tv_values_layout);
            alDetails = itemView.findViewById(R.id.alDetails);
        }
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public boolean isLoginNeeded() {
        return this.isLoginNeeded;
    }

    static class ReportsViewHolder extends RecyclerView.ViewHolder {
        final Button loginBtn;
        final TextView txt;

        public ReportsViewHolder(@NonNull View itemView) {
            super(itemView);
            loginBtn = itemView.findViewById(R.id.login_button);
            txt = itemView.findViewById(R.id.tv_message);
        }
    }
}
