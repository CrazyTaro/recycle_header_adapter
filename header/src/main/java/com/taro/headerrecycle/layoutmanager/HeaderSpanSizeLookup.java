package com.taro.headerrecycle.layoutmanager;

/**
 * Created by taro on 16/4/28.
 */

import android.support.v7.widget.GridLayoutManager;

/**
 * 计算头部占用空格的类
 */
public class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    private int mSpanCount = 0;
    private ISpanSizeHandler mSpanLookup = null;

    /**
     * 创建分组显示的spanSizeLookup
     *
     * @param lookup    设置或者更新adapter
     * @param spanCount gridLayout网格列数
     */
    public HeaderSpanSizeLookup(ISpanSizeHandler lookup, int spanCount) {
        this.setISpanSizeLookup(lookup);
        this.setSpanCount(spanCount);
    }

    /**
     * 创建分组显示的spanSizeLookup
     *
     * @param spanCount GridLayout网格列数
     */
    public HeaderSpanSizeLookup(int spanCount) {
        this.setSpanCount(spanCount);
    }

    /**
     * 设置或更新adapter
     *
     * @param lookup
     */
    public void setISpanSizeLookup(ISpanSizeHandler lookup) {
        mSpanLookup = lookup;
    }

    /**
     * 设置网格列数
     *
     * @param spanCount
     */
    public void setSpanCount(int spanCount) {
        this.mSpanCount = spanCount;
    }

    /**
     * 设置网格列数
     *
     * @return
     */
    public int getSpanCount() {
        return mSpanCount;
    }

    @Override
    public int getSpanSize(int position) {
        if (mSpanLookup != null) {
            if (mSpanLookup.isSpecialItem(position)) {
                return mSpanLookup.getSpecialItemSpanSize(mSpanCount, position);
            } else {
                return mSpanLookup.getNormalItemSpanSize(mSpanCount, position);
            }
        } else {
            return 1;
        }
    }


    /**
     * GridLayoutManager 对特殊item的SpanSize进行处理的接口
     */
    public interface ISpanSizeHandler {
        /**
         * 是否特殊的item,如header或者某些特别的item
         *
         * @param position 当前item的position的位置
         * @return
         */
        public boolean isSpecialItem(int position);

        /**
         * 获取特殊item占用的网格数,此方法仅会在是特殊item时才调用
         *
         * @param spanCount 当前GridLayoutManager设置的每行网格数
         * @param position  当前特殊item的位置
         * @return
         */
        public int getSpecialItemSpanSize(int spanCount, int position);

        /**
         * 获取正常item占用的网格数,默认值理论上应该为1,但可以由实现类决定,此方法仅会在非特殊item时调用
         *
         * @param spanCount 当前GridLayoutManger设置的每行网格数
         * @param position  当前正常item的位置
         * @return
         */
        public int getNormalItemSpanSize(int spanCount, int position);
    }
}