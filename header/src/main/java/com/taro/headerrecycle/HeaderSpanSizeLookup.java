package com.taro.headerrecycle;

/**
 * Created by taro on 16/4/28.
 */

import android.graphics.Point;
import android.support.v7.widget.GridLayoutManager;

import java.util.List;

/**
 * 计算头部占用空格的类
 */
public class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    private int mSpanCount = 0;
    private HeaderRecycleAdapter mAdapter = null;

    /**
     * 创建分组显示的spanSizeLookup
     *
     * @param adapter   设置或者更新adapter
     * @param spanCount gridLayout网格列数
     */
    public HeaderSpanSizeLookup(HeaderRecycleAdapter adapter, int spanCount) {
        if (adapter == null) {
            throw new RuntimeException("adapter can not be null");
        }
        this.setAdapter(adapter);
        this.setSpanCount(spanCount);
    }

    /**
     * 设置或更新adapter
     *
     * @param adapter
     * @return
     */
    public boolean setAdapter(HeaderRecycleAdapter adapter) {
        if (adapter != null) {
            mAdapter = adapter;
            return true;
        } else {
            return false;
        }
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

    /**
     * 继承此类重写spanSize时,建议重写此方法
     *
     * @param groupId  分组ID
     * @param childId  组内元素ID
     * @param position 位置(包括头部)
     * @return
     */
    public int getSpanSize(int groupId, int childId, int position) {
        return 1;
    }

    @Override
    public int getSpanSize(int position) {
        List<Integer> eachGroupCountList = mAdapter.getEachGroupCountList();
        boolean isShowHeader = mAdapter.isShowHeader();

        Point p = mAdapter.getGroupIdAndChildIdFromPosition(eachGroupCountList, position, isShowHeader);
        if (mAdapter.isHeaderItem(p)) {
            return mSpanCount;
        } else {
            return p != null ? getSpanSize(p.x, p.y, position) : 1;
        }
    }
}