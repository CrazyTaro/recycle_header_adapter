package com.taro.headerrecycle.layoutmanager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

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
     * @param lookup    spanSizeLookup的实现
     */
    public HeaderGridLayoutManager(Context context, int spanCount, HeaderSpanSizeLookup.ISpanSizeHandler lookup) {
        this(context, spanCount, GridLayoutManager.VERTICAL, false, lookup);
    }

    /**
     * 创建适用于头部显示的gridLayoutManager
     *
     * @param context
     * @param spanCount     网格列数
     * @param orientation   方向
     * @param reverseLayout 是否从尾到头加载layout
     * @param lookup        关联的adapter
     */
    public HeaderGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout, HeaderSpanSizeLookup.ISpanSizeHandler lookup) {
        super(context, spanCount, orientation, reverseLayout);
        if (lookup == null) {
            throw new NullPointerException("headerRecycleAdapter can not be null");
        }
        if (mLookup == null) {
            mLookup = new HeaderSpanSizeLookup(lookup, spanCount);
        }
        this.setSpanSizeLookup(mLookup);
    }

    /**
     * 设置关联的adapter
     *
     * @param lookup
     */
    public void setISpanSizeHandler(HeaderSpanSizeLookup.ISpanSizeHandler lookup) {
        if (mLookup != null) {
            mLookup.setISpanSizeLookup(lookup);
        }
    }

    /**
     * 设置使用的SpanSizeLookup
     *
     * @param spanSizeLookup 参数类型必须是 {@link HeaderSpanSizeLookup},因为这个GridLayoutManager是使用于展示Header的
     */
    @Override
    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        if (spanSizeLookup instanceof HeaderSpanSizeLookup) {
            mLookup = (HeaderSpanSizeLookup) spanSizeLookup;
            super.setSpanSizeLookup(spanSizeLookup);
        } else {
            throw new IllegalArgumentException("spanSizeLookup must be HeaderSpanSizeLookup");
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
