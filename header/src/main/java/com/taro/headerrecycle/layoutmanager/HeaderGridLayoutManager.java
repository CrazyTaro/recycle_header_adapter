package com.taro.headerrecycle.layoutmanager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

import com.taro.headerrecycle.adapter.HeaderRecycleAdapter;
import com.taro.headerrecycle.adapter.HeaderSpanSizeLookup;

/**
 * Created by taro on 16/4/28.
 */
public class HeaderGridLayoutManager extends GridLayoutManager {
    private HeaderSpanSizeLookup mLookup = null;

    /**
     * 创建适用于头部显示的gridLayoutManager
     *
     * @param context
     * @param spanCount 网格列数
     * @param adapter   关联的adapter
     */
    public HeaderGridLayoutManager(Context context, int spanCount, HeaderRecycleAdapter adapter) {
        this(context, spanCount, GridLayoutManager.VERTICAL, false, adapter);
    }

    /**
     * 创建适用于头部显示的gridLayoutManager
     *
     * @param context
     * @param spanCount     网格列数
     * @param orientation   方向
     * @param reverseLayout 是否从尾到头加载layout
     * @param adapter       关联的adapter
     */
    public HeaderGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout, HeaderRecycleAdapter adapter) {
        super(context, spanCount, orientation, reverseLayout);
        if (adapter == null) {
            throw new NullPointerException("headerRecycleAdapter can not be null");
        }
        if (mLookup == null) {
            mLookup = new HeaderSpanSizeLookup(adapter, spanCount);
        }
        this.setAdapter(adapter);
        this.setSpanSizeLookup(mLookup);
    }

    /**
     * 设置关联的adapter
     *
     * @param adapter
     * @return
     */
    public boolean setAdapter(HeaderRecycleAdapter adapter) {
        return mLookup.setAdapter(adapter);
    }

    @Override
    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        if (spanSizeLookup instanceof HeaderSpanSizeLookup) {
            super.setSpanSizeLookup(spanSizeLookup);
        } else {
            throw new RuntimeException("spanSizeLookup must be headerSpanSizeLookup");
        }
    }

    @Override
    public void setSpanCount(int spanCount) {
        super.setSpanCount(spanCount);
        if (mLookup != null) {
            mLookup.setSpanCount(spanCount);
        }
    }
}
