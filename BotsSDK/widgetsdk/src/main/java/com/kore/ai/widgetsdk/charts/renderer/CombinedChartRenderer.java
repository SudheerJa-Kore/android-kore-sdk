package com.kore.ai.widgetsdk.charts.renderer;

import android.graphics.Canvas;
import android.util.Log;

import com.kore.ai.widgetsdk.charts.animation.ChartAnimator;
import com.kore.ai.widgetsdk.charts.charts.Chart;
import com.kore.ai.widgetsdk.charts.charts.CombinedChart;
import com.kore.ai.widgetsdk.charts.data.ChartData;
import com.kore.ai.widgetsdk.charts.data.CombinedData;
import com.kore.ai.widgetsdk.charts.highlight.Highlight;
import com.kore.ai.widgetsdk.charts.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CombinedChartRenderer extends DataRenderer {
    protected List<DataRenderer> mRenderers = new ArrayList(5);
    protected WeakReference<Chart> mChart;
    protected List<Highlight> mHighlightBuffer = new ArrayList();

    public CombinedChartRenderer(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        this.mChart = new WeakReference(chart);
        this.createRenderers();
    }

    public void createRenderers() {
        this.mRenderers.clear();
        CombinedChart chart = (CombinedChart)this.mChart.get();
        if (chart != null) {
            CombinedChart.DrawOrder[] orders = chart.getDrawOrder();
            CombinedChart.DrawOrder[] var3 = orders;
            int var4 = orders.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                CombinedChart.DrawOrder order = var3[var5];
                switch(order) {
                    case BAR:
                        if (chart.getBarData() != null) {
                            this.mRenderers.add(new com.kore.ai.widgetsdk.charts.renderer.BarChartRenderer(chart, this.mAnimator, this.mViewPortHandler));
                        }
                        break;
                    case BUBBLE:
                        if (chart.getBubbleData() != null) {
                            this.mRenderers.add(new com.kore.ai.widgetsdk.charts.renderer.BubbleChartRenderer(chart, this.mAnimator, this.mViewPortHandler));
                        }
                        break;
                    case LINE:
                        if (chart.getLineData() != null) {
                            this.mRenderers.add(new LineChartRenderer(chart, this.mAnimator, this.mViewPortHandler));
                        }
                        break;
                    case CANDLE:
                        if (chart.getCandleData() != null) {
                            this.mRenderers.add(new com.kore.ai.widgetsdk.charts.renderer.CandleStickChartRenderer(chart, this.mAnimator, this.mViewPortHandler));
                        }
                        break;
                    case SCATTER:
                        if (chart.getScatterData() != null) {
                            this.mRenderers.add(new ScatterChartRenderer(chart, this.mAnimator, this.mViewPortHandler));
                        }
                }
            }

        }
    }

    public void initBuffers() {
        Iterator var1 = this.mRenderers.iterator();

        while(var1.hasNext()) {
            DataRenderer renderer = (DataRenderer)var1.next();
            renderer.initBuffers();
        }

    }

    public void drawData(Canvas c) {
        Iterator var2 = this.mRenderers.iterator();

        while(var2.hasNext()) {
            DataRenderer renderer = (DataRenderer)var2.next();
            renderer.drawData(c);
        }

    }

    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        Log.e("MPAndroidChart", "Erroneous call to drawValue() in CombinedChartRenderer!");
    }

    public void drawValues(Canvas c) {
        Iterator var2 = this.mRenderers.iterator();

        while(var2.hasNext()) {
            DataRenderer renderer = (DataRenderer)var2.next();
            renderer.drawValues(c);
        }

    }

    public void drawExtras(Canvas c) {
        Iterator var2 = this.mRenderers.iterator();

        while(var2.hasNext()) {
            DataRenderer renderer = (DataRenderer)var2.next();
            renderer.drawExtras(c);
        }

    }

    public void drawHighlighted(Canvas c, Highlight[] indices) {
        Chart chart = (Chart)this.mChart.get();
        if (chart != null) {
            Iterator var4 = this.mRenderers.iterator();

            while(var4.hasNext()) {
                DataRenderer renderer = (DataRenderer)var4.next();
                ChartData data = null;
                if (renderer instanceof com.kore.ai.widgetsdk.charts.renderer.BarChartRenderer) {
                    data = ((BarChartRenderer)renderer).mChart.getBarData();
                } else if (renderer instanceof LineChartRenderer) {
                    data = ((LineChartRenderer)renderer).mChart.getLineData();
                } else if (renderer instanceof com.kore.ai.widgetsdk.charts.renderer.CandleStickChartRenderer) {
                    data = ((CandleStickChartRenderer)renderer).mChart.getCandleData();
                } else if (renderer instanceof ScatterChartRenderer) {
                    data = ((ScatterChartRenderer)renderer).mChart.getScatterData();
                } else if (renderer instanceof com.kore.ai.widgetsdk.charts.renderer.BubbleChartRenderer) {
                    data = ((BubbleChartRenderer)renderer).mChart.getBubbleData();
                }

                int dataIndex = data == null ? -1 : ((CombinedData)chart.getData()).getAllData().indexOf(data);
                this.mHighlightBuffer.clear();
                Highlight[] var8 = indices;
                int var9 = indices.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    Highlight h = var8[var10];
                    if (h.getDataIndex() == dataIndex || h.getDataIndex() == -1) {
                        this.mHighlightBuffer.add(h);
                    }
                }

                renderer.drawHighlighted(c, (Highlight[])this.mHighlightBuffer.toArray(new Highlight[this.mHighlightBuffer.size()]));
            }

        }
    }

    public DataRenderer getSubRenderer(int index) {
        return index < this.mRenderers.size() && index >= 0 ? (DataRenderer)this.mRenderers.get(index) : null;
    }

    public List<DataRenderer> getSubRenderers() {
        return this.mRenderers;
    }

    public void setSubRenderers(List<DataRenderer> renderers) {
        this.mRenderers = renderers;
    }
}
