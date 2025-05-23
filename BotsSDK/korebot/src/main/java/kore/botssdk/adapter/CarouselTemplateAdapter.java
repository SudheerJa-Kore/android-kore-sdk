package kore.botssdk.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.itemdecoration.VerticalSpaceItemDecoration;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotCarouselModel;
import kore.botssdk.models.BotListDefaultModel;
import kore.botssdk.models.BotResponse;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.utils.Utils;

public class CarouselTemplateAdapter extends RecyclerView.Adapter<CarouselTemplateAdapter.ViewHolder> {
    private ArrayList<? extends BotCarouselModel> botCarouselModels;
    private boolean isEnabled;
    private ComposeFooterInterface composeFooterInterface;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    private final LayoutInflater layoutInflater;

    private String type;

    public CarouselTemplateAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.carousel_template_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BotCarouselModel botCarouselModel = getItem(position);
        if (botCarouselModel == null) return;

        holder.carouselItemTitle.setText(botCarouselModel.getTitle());
        if (!StringUtils.isNullOrEmptyWithTrim(botCarouselModel.getSubtitle())) {
            holder.carouselItemSubTitle.setText(BotResponse.TEMPLATE_TYPE_WELCOME_CAROUSEL.equalsIgnoreCase(type) ? botCarouselModel.getSubtitle() : Html.fromHtml(StringEscapeUtils.unescapeHtml4(botCarouselModel.getSubtitle()).replaceAll("<br>", "")));
            holder.carouselItemSubTitle.setMaxLines(BotResponse.TEMPLATE_TYPE_WELCOME_CAROUSEL.equalsIgnoreCase(type) ? Integer.MAX_VALUE : 3);
            holder.carouselItemSubTitle.setVisibility(VISIBLE);
        } else {
            holder.carouselItemSubTitle.setVisibility(GONE);
        }
        try {
            if (botCarouselModel.getImage_url() != null && !botCarouselModel.getImage_url().isEmpty()) {
                Picasso.get().load(botCarouselModel.getImage_url()).into(holder.carouselItemImage);
                holder.carouselItemImage.setVisibility(VISIBLE);
            } else {
                holder.carouselItemImage.setVisibility(GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (botCarouselModel.getButtons() != null) {
            CarouselItemButtonAdapter botCarouselItemButtonAdapter = new CarouselItemButtonAdapter(holder.itemView.getContext());
            holder.buttons.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.buttons.addItemDecoration(new VerticalSpaceItemDecoration(2));
            holder.buttons.setAdapter(botCarouselItemButtonAdapter);
            botCarouselItemButtonAdapter.setComposeFooterInterface(composeFooterInterface);
            botCarouselItemButtonAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
            botCarouselItemButtonAdapter.populateData(botCarouselModel.getButtons(), isEnabled);
        }

        String price = Utils.isNullOrEmpty(botCarouselModel.getPrice()) ? "" : botCarouselModel.getPrice();
        String cost_price = Utils.isNullOrEmpty(botCarouselModel.getCost_price()) ? "" : botCarouselModel.getCost_price();

        String text = (price + " " + cost_price).trim();

        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(text);

        // Initialize a new StrikeThroughSpan to display strike through text
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();

        // Apply the strike through text to the span
        ssBuilder.setSpan(strikethroughSpan, // Span to add
                text.indexOf(price), // Start of the span (inclusive)
                text.indexOf(price) + price.length(), // End of the span (exclusive)
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Do not extend the span when text add later
        );

        if (!Utils.isNullOrEmpty(botCarouselModel.getPrice()) || !Utils.isNullOrEmpty(botCarouselModel.getCost_price())) {
            holder.carouselOfferPrice_FL.setVisibility(VISIBLE);
            if (!Utils.isNullOrEmpty(botCarouselModel.getCost_price())) holder.carousel_item_offer.setText(ssBuilder);
            else holder.carousel_item_offer.setText(text);
        } else {
            holder.carouselOfferPrice_FL.setVisibility(View.GONE);
        }

        if (!Utils.isNullOrEmpty(botCarouselModel.getSaved_price())) {
            holder.carouselSavedPrice_FL.setVisibility(VISIBLE);
            holder.carousel_item_save_price.setText(botCarouselModel.getSaved_price());
        } else {
            holder.carouselSavedPrice_FL.setVisibility(View.GONE);
        }

        BotListDefaultModel botDefaultModel = botCarouselModel.getDefault_action();
        if (botDefaultModel != null) {
            holder.carouselItemDefaultAction.setVisibility(VISIBLE);
            holder.carouselItemDefaultAction.setTextColor(Color.parseColor(SDKConfiguration.BubbleColors.quickReplyColor));

            if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(botDefaultModel.getType())) {
                holder.carouselItemDefaultAction.setText(botDefaultModel.getUrl());
            } else if (BundleConstants.BUTTON_TYPE_POSTBACK.equalsIgnoreCase(botDefaultModel.getType())) {
                holder.carouselItemDefaultAction.setText(botCarouselModel.getDefault_action().getPayload());
            } else if (BundleConstants.BUTTON_TYPE_POSTBACK_DISP_PAYLOAD.equalsIgnoreCase(botDefaultModel.getType())) {
                holder.carouselItemDefaultAction.setText(botCarouselModel.getDefault_action().getPayload());
            }
        }

        holder.carouselItemDefaultAction.setOnClickListener(v -> {
            BotListDefaultModel botListDefaultModel = botCarouselModel.getDefault_action();
            if (invokeGenericWebViewInterface != null && botListDefaultModel != null) {
                if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(botListDefaultModel.getType())) {
                    invokeGenericWebViewInterface.invokeGenericWebView(botListDefaultModel.getUrl());
                    return;
                } else if (BundleConstants.BUTTON_TYPE_USER_INTENT.equalsIgnoreCase(botListDefaultModel.getType())) {
                    invokeGenericWebViewInterface.handleUserActions(botListDefaultModel.getAction(), botListDefaultModel.getCustomData());
                    return;
                }
            }
            if (isEnabled && composeFooterInterface != null && botListDefaultModel != null) {
                if (BundleConstants.BUTTON_TYPE_POSTBACK.equalsIgnoreCase(botListDefaultModel.getType())) {
                    String buttonPayload = botCarouselModel.getDefault_action().getPayload();
                    composeFooterInterface.onSendClick(botCarouselModel.getDefault_action().getTitle(), buttonPayload, false);
                } else if (BundleConstants.BUTTON_TYPE_POSTBACK_DISP_PAYLOAD.equalsIgnoreCase(botListDefaultModel.getType())) {
                    String buttonPayload = botCarouselModel.getDefault_action().getPayload();
                    composeFooterInterface.onSendClick(buttonPayload, false);
                }
            }
        });
        holder.carouselItemRoot.setOnClickListener(v -> {
            BotListDefaultModel botListDefaultModel = botCarouselModel.getDefault_action();
            if (invokeGenericWebViewInterface != null && botListDefaultModel != null) {
                if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(botListDefaultModel.getType())) {
                    invokeGenericWebViewInterface.invokeGenericWebView(botListDefaultModel.getUrl());
                    return;
                } else if (BundleConstants.BUTTON_TYPE_USER_INTENT.equalsIgnoreCase(botListDefaultModel.getType())) {
                    invokeGenericWebViewInterface.handleUserActions(botListDefaultModel.getAction(), botListDefaultModel.getCustomData());
                    return;
                }
            } if (isEnabled && composeFooterInterface != null && botListDefaultModel != null) {
                if (BundleConstants.BUTTON_TYPE_POSTBACK.equalsIgnoreCase(botListDefaultModel.getType())) {
                    String buttonPayload = botCarouselModel.getDefault_action().getPayload();
                    composeFooterInterface.onSendClick(botCarouselModel.getDefault_action().getTitle(), buttonPayload, false);
                } else if (BundleConstants.BUTTON_TYPE_POSTBACK_DISP_PAYLOAD.equalsIgnoreCase(botListDefaultModel.getType())) {
                    String buttonPayload = botCarouselModel.getDefault_action().getPayload();
                    composeFooterInterface.onSendClick(buttonPayload, false);
                }
            }
        });

        holder.koraItems.setVisibility(View.GONE);
    }

    private BotCarouselModel getItem(int position) {
        return botCarouselModels != null ? botCarouselModels.get(position) : null;
    }

    @Override
    public int getItemCount() {
        return botCarouselModels != null ? botCarouselModels.size() : 0;
    }

    public void populateData(ArrayList<? extends BotCarouselModel> botCarouselModels, boolean isEnabled, String type) {
        this.type = type;
        this.botCarouselModels = botCarouselModels;
        this.isEnabled = isEnabled;
        notifyDataSetChanged();
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView carouselItemImage;
        TextView carouselItemTitle;
        TextView carouselItemSubTitle;
        TextView carouselItemDefaultAction;
        TextView hashTagsView;
        TextView knowledgeType;
        TextView knowledgeMode;
        RelativeLayout koraItems;
        RecyclerView buttons;
        CardView carouselItemRoot;
        FrameLayout carouselOfferPrice_FL;
        FrameLayout carouselSavedPrice_FL;
        TextView carousel_item_offer;
        TextView carousel_item_save_price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            carouselItemRoot = itemView.findViewById(R.id.carousel_item_root);
            carouselItemImage = itemView.findViewById(R.id.carousel_item_image);
            carouselItemTitle = itemView.findViewById(R.id.carousel_item_title);
            carouselItemSubTitle = itemView.findViewById(R.id.carousel_item_subtitle);
            carouselItemDefaultAction = itemView.findViewById(R.id.carousel_item_default_action);
            buttons = itemView.findViewById(R.id.carousel_button_listview);
            hashTagsView = itemView.findViewById(R.id.hash_tags_view);
            knowledgeType = itemView.findViewById(R.id.knowledge_type);
            knowledgeMode = itemView.findViewById(R.id.knowledge_mode);
            koraItems = itemView.findViewById(R.id.kora_items);
            carouselOfferPrice_FL = itemView.findViewById(R.id.offer_price_fl);
            carouselSavedPrice_FL = itemView.findViewById(R.id.saved_price_fl);
            carousel_item_offer = itemView.findViewById(R.id.carousel_item_offer);
            carousel_item_save_price = itemView.findViewById(R.id.carousel_item_saved);
        }
    }
}
