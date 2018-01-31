package kore.botssdk.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;

import kore.botssdk.R;
import kore.botssdk.fragment.ComposeFooterFragment.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotCarouselModel;
import kore.botssdk.models.KnowledgeDetailModel;
import kore.botssdk.view.viewUtils.CarouselItemViewHelper;

import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

/**
 * Created by Pradeep Mahato on 14-July-17.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class BotCarouselAdapter extends PagerAdapter {

    ArrayList<? extends BotCarouselModel> botCarouselModels = new ArrayList<>();
    Activity activityContext;
    ComposeFooterInterface composeFooterInterface;
    InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    LayoutInflater ownLayoutInflater;
    float pageWidth = 1.0f;
    ArrayList<Integer> heights = new ArrayList<>();
    ArrayList<View> views = new ArrayList<>();

    public BotCarouselAdapter(ComposeFooterInterface composeFooterInterface,
                              InvokeGenericWebViewInterface invokeGenericWebViewInterface,
                              Activity activityContext) {
        super();
        this.activityContext = activityContext;
        this.composeFooterInterface = composeFooterInterface;
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
        ownLayoutInflater = activityContext.getLayoutInflater();

        TypedValue typedValue = new TypedValue();
        activityContext.getResources().getValue(R.dimen.carousel_item_width_factor, typedValue, true);
        pageWidth = typedValue.getFloat();
    }

    @Override
    public int getCount() {
        if (botCarouselModels == null) {
            return 0;
        } else {
            return botCarouselModels.size();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View carouselItemLayout = ownLayoutInflater.inflate(R.layout.carousel_item_layout, container, false);

        CarouselItemViewHelper.initializeViewHolder(carouselItemLayout);
        CarouselItemViewHelper.populateStuffs((CarouselItemViewHelper.CarouselViewHolder) carouselItemLayout.getTag(), composeFooterInterface, invokeGenericWebViewInterface, botCarouselModels.get(position), activityContext);
        container.addView(carouselItemLayout);
        ViewTreeObserver vto = carouselItemLayout.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                CarouselItemViewHelper.CarouselViewHolder holder = (CarouselItemViewHelper.CarouselViewHolder) carouselItemLayout.getTag();
                int height = (int)(holder.carouselItemImage.getMeasuredHeight() + holder.carouselItemSubTitle.getMeasuredHeight() + holder.carouselItemTitle.getMeasuredHeight() + (botCarouselModels.get(position).getButtons() != null ? botCarouselModels.get(position).getButtons().size() * 48 * dp1 :0)+ 25 * dp1);
                if(botCarouselModels.get(position) instanceof KnowledgeDetailModel){
                    height = height+holder.hashTagsView.getMeasuredHeight()+(holder.knowledgeMode.getMeasuredHeight() > holder.knowledgeType.getMeasuredHeight() ? holder.knowledgeMode.getMeasuredHeight() : holder.knowledgeType.getMeasuredHeight());
                }

                heights.add(height);
                ViewGroup.LayoutParams layoutParams = holder.carouselItemRoot.getLayoutParams();
                layoutParams.height = height;
                holder.carouselItemRoot.setLayoutParams(layoutParams);

              //  views.add(holder.carouselItemRoot);
                return true;
            }
        });
        return carouselItemLayout;

    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
       // applyParams();
    }

    private void applyParams() {
        int maxHeight = getMaxChildHeight();
        for(View view : views) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = maxHeight;
            view.setLayoutParams(layoutParams);
        }
        Log.d("called","view pager 1");
    }

    public int getMaxChildHeight(){
        if(heights.size()>0) {
            return Collections.max(heights);
        }else{
            return 0;
        }
    }

    public void setBotCarouselModels(ArrayList<? extends BotCarouselModel> botCarouselModels) {
        this.botCarouselModels = botCarouselModels;
    }

    @Override
    public float getPageWidth(int position) {
        if (getCount() == 0) {
            return super.getPageWidth(position);
        } else {
            return pageWidth;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}
