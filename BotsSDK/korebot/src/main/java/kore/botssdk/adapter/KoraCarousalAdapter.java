package kore.botssdk.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.fragment.ComposeFooterFragment;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.KoraSearchDataSetModel;
import kore.botssdk.utils.KaFontUtils;
import kore.botssdk.view.viewUtils.KoraCarousalViewHelper;

/**
 * Created by Shiva Krishna on 2/8/2018.
 */

public class KoraCarousalAdapter extends PagerAdapter {
    Context mContext;
    ArrayList<KoraSearchDataSetModel> data;
    InvokeGenericWebViewInterface genericWebViewInterface;
    ComposeFooterFragment.ComposeFooterInterface composeFooterInterface;
    LayoutInflater ownLayoutInflater;
    float pageWidth = 1.0f;
    public KoraCarousalAdapter(ArrayList<KoraSearchDataSetModel> koraSearchDataSetModels, Context mContext, InvokeGenericWebViewInterface webViewInterface, ComposeFooterFragment.ComposeFooterInterface composeFooterInterface){
        super();
        this.data = koraSearchDataSetModels;
        this.mContext = mContext;
        this.composeFooterInterface = composeFooterInterface;
        this.genericWebViewInterface = webViewInterface;
        ownLayoutInflater = LayoutInflater.from(mContext);
        TypedValue typedValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.carousel_item_width_factor, typedValue, true);
        pageWidth = typedValue.getFloat();
    }
    @Override
    public int getCount() {
        if (data == null) {
            return 0;
        } else {
            return data.size();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View carouselItemLayout;
        if(data.get(position).getViewType() == KoraSearchDataSetModel.ViewType.EMAIL_VIEW) {
            carouselItemLayout = ownLayoutInflater.inflate(R.layout.kora_email_view, container, false);
        }else {
            carouselItemLayout = ownLayoutInflater.inflate(R.layout.kora_knowledge_view, container, false);
        }
        KaFontUtils.applyCustomFont(mContext,carouselItemLayout);
        KoraCarousalViewHelper.initializeViewHolder(carouselItemLayout,data.get(position).getViewType());
        KoraCarousalViewHelper.populateStuffs((KoraCarousalViewHelper.KoraCarousalViewHolder) carouselItemLayout.getTag(), composeFooterInterface, genericWebViewInterface, data.get(position), mContext);
        container.addView(carouselItemLayout);
        return carouselItemLayout;

    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
        // applyParams();
    }





    public void setBotCarouselModels(ArrayList<KoraSearchDataSetModel> data) {
        this.data = data;
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
