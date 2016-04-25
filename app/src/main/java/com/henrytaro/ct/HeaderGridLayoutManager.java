package com.henrytaro.ct;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

import java.util.List;

/**
 * Created by taro on 16/4/21.
 */
public class HeaderGridLayoutManager extends GridLayoutManager {
    private boolean mIsShowHeader = true;
    private List<Integer> mEachGroupCountList = null;
    private HeaderSpanSizeLookup mLookup = null;

    public HeaderGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HeaderGridLayoutManager(Context context, int spanCount, List<Integer> eachGroupCountList) {
        this(context, spanCount, GridLayoutManager.VERTICAL, false, eachGroupCountList);
    }

    public HeaderGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout, List<Integer> eachGroupCountList) {
        super(context, spanCount, orientation, reverseLayout);
        if (mLookup == null) {
            mLookup = new HeaderSpanSizeLookup(this.getSpanCount(), eachGroupCountList, mIsShowHeader);
        } else {
            mLookup.setParams(this.getSpanCount(), eachGroupCountList);
        }
        this.setSpanSizeLookup(mLookup);
    }

    @Override
    public void setSpanCount(int spanCount) {
        super.setSpanCount(spanCount);
        mLookup.setParams(this.getSpanCount(), mEachGroupCountList);
    }

    public void setIsShowHeader(boolean isShowHeader) {
        mIsShowHeader = isShowHeader;
        mLookup.setIsShowHeader(isShowHeader);
    }

    public void setEachGroupCountList(List<Integer> eachGroupCountList) {
        mLookup.setParams(this.getSpanCount(), eachGroupCountList);
    }


    public static class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        private List<Integer> mEachGroupCountList = null;
        private int mSpanCount = 0;
        private boolean mIsShowHeader = true;

        public HeaderSpanSizeLookup(int spanCount, List<Integer> eachGroupCountList, boolean isShowHeader) {
            this.mSpanCount = spanCount;
            this.mIsShowHeader = isShowHeader;
            this.mEachGroupCountList = eachGroupCountList;
        }

        public void setParams(int spanCount, List<Integer> eachGroupCountList) {
            this.mSpanCount = spanCount;
            this.mEachGroupCountList = eachGroupCountList;
        }

        public void setIsShowHeader(boolean isShowHeader) {
            this.mIsShowHeader = isShowHeader;
        }

        public int getSpanCount() {
            return mSpanCount;
        }

        @Override
        public int getSpanSize(int position) {
            if (mEachGroupCountList != null) {
                int groupEachLine = mIsShowHeader ? 1 : 0;
                int childId = 0;
                for (int groupCount : mEachGroupCountList) {
                    childId = groupCount;
                    position = position - groupEachLine - childId;
                    //直到计算到当前position数据为负说明当前位置在此分组中
                    if (position < 0) {
                        childId += position;
                        return childId < 0 ? (groupEachLine == 0 ? 1 : mSpanCount) : 1;
                    }
                }
            }
            return 1;
        }
    }
}
