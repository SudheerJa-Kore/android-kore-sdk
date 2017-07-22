package kore.botssdk.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.fragment.ComposeFooterFragment.ComposeFooterInterface;
import kore.botssdk.models.BotCarouselModel;
import kore.botssdk.view.viewUtils.CarouselItemViewHelper;

/**
 * Created by Pradeep Mahato on 14-July-17.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class BotCarouselAdapter extends PagerAdapter {

    ArrayList<BotCarouselModel> botCarouselModels = new ArrayList<>();
    Activity activityContext;
    ComposeFooterInterface composeFooterInterface;
    LayoutInflater ownLayoutInflater;
    float pageWidth = 1.0f;

    public BotCarouselAdapter(ComposeFooterInterface composeFooterInterface, Activity activityContext) {
        super();
        this.activityContext = activityContext;
        this.composeFooterInterface = composeFooterInterface;
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
        return view == ((CardView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View carouselItemLayout = ownLayoutInflater.inflate(R.layout.carousel_item_layout, container, false);

        CarouselItemViewHelper.initializeViewHolder(carouselItemLayout);
        CarouselItemViewHelper.populateStuffs((CarouselItemViewHelper.CarouselViewHolder) carouselItemLayout.getTag(), composeFooterInterface, botCarouselModels.get(position), activityContext);

        container.addView(carouselItemLayout);

        return carouselItemLayout;

    }

    public void setBotCarouselModels(ArrayList<BotCarouselModel> botCarouselModels) {
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
        container.removeView((CardView) object);
    }

}
