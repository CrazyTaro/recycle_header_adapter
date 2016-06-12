package com.taro.headerrecycle.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by taro on 16/5/19.
 */
public class ExtraViewWrapAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_HEADER_REFRESH = Integer.MAX_VALUE / 2;
    public static final int VIEW_FOOTER_LOAD_MORE = Integer.MIN_VALUE / 2;

    private HeaderFooterViewCache mHeaderOption = null;
    private HeaderFooterViewCache mFooterOption = null;
    private View mRefreshHeader = null;
    private View mLoadMoreFooter = null;
    private boolean mIsRefreshing = false;
    private boolean mIsLoading = false;
    private RecyclerView.Adapter mInnerAdapter;
    private boolean mIsFootViewEnable = true;
    private boolean mIsHeaderViewEnable = true;

    public static final void setRrefreshingViewStatus(boolean isRefreshing, boolean isScrollToStart, RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        } else {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null && adapter instanceof ExtraViewWrapAdapter) {
                ExtraViewWrapAdapter wrapAdapter = (ExtraViewWrapAdapter) adapter;
                wrapAdapter.setRefreshingStatus(isRefreshing);
                if (isScrollToStart) {
                    recyclerView.scrollToPosition(0);
                }
            }
        }
    }

    public static final void setLoadingViewStatus(boolean isLoading, boolean isScrollToEnd, RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        } else {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null && adapter instanceof ExtraViewWrapAdapter) {
                ExtraViewWrapAdapter wrapAdapter = (ExtraViewWrapAdapter) adapter;
                wrapAdapter.setLoadingStatus(isLoading);
                if (isScrollToEnd) {
                    recyclerView.scrollToPosition(wrapAdapter.getItemCount() - 1);
                }
            }
        }
    }

    public static final void setHeaderViewEnable(boolean isHeaderEnable, boolean isScrollToHeader, RecyclerView recycleView) {
        if (recycleView == null) {
            return;
        } else {
            RecyclerView.Adapter adapter = recycleView.getAdapter();
            if (adapter != null && adapter instanceof ExtraViewWrapAdapter) {
                ExtraViewWrapAdapter wrapAdapter = (ExtraViewWrapAdapter) adapter;
                wrapAdapter.setHeaderViewEnable(isHeaderEnable);
                if (isScrollToHeader) {
                    recycleView.scrollToPosition(0);
                }
            }
        }
    }

    public static final void setFooterViewEnable(boolean isFooterEnable, boolean isScrollToFooter, RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        } else {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null && adapter instanceof ExtraViewWrapAdapter) {
                ExtraViewWrapAdapter wrapAdapter = (ExtraViewWrapAdapter) adapter;
                wrapAdapter.setFootViewEnable(isFooterEnable);
                if (isScrollToFooter) {
                    recyclerView.scrollToPosition(wrapAdapter.getItemCount() - 1);
                }
            }
        }
    }

    /**
     * 默认创建多头部/尾部包装类的adapter,头部/尾部都可显示
     *
     * @param context
     * @param innerAdapter
     */
    public ExtraViewWrapAdapter(Context context, @NonNull RecyclerView.Adapter innerAdapter) {
        this(context, innerAdapter, true, true);
    }

    /**
     * 创建多头部/尾部包装类的adapter,使用此adapter包装内部adapter时会改变内部adapter的某些操作,当不再需要时请使用{@link #restoreInnerAdatper()}还原内部adapter的操作
     *
     * @param context
     * @param innerAdapter       内部引用的adapter
     * @param isHeaderViewEnable 头部view是否可用
     * @param isFootViewEnable   尾部view是否可用
     */
    public ExtraViewWrapAdapter(Context context, RecyclerView.Adapter innerAdapter, boolean isHeaderViewEnable, boolean isFootViewEnable) {
        if (innerAdapter == null || context == null) {
            throw new NullPointerException("wrap adapter can not be null");
        }
        mInnerAdapter = innerAdapter;
        mIsHeaderViewEnable = isHeaderViewEnable;
        mIsFootViewEnable = isFootViewEnable;
        mHeaderOption = new HeaderFooterViewCache();
        mFooterOption = new HeaderFooterViewCache();
        mInnerAdapter.registerAdapterDataObserver(mDataObserver);
    }

    /**
     * 还原内部adapter的相关操作
     */
    public void restoreInnerAdatper() {
        mInnerAdapter.unregisterAdapterDataObserver(mDataObserver);
    }

    /**
     * 设置刷新头部view
     *
     * @param refreshingView
     */
    public void setRefreshingHeaderView(View refreshingView) {
        mRefreshHeader = refreshingView;
    }

    /**
     * 设置加载尾部view
     *
     * @param loadingView
     */
    public void setLoadingFooterView(View loadingView) {
        mLoadMoreFooter = loadingView;
    }

    /**
     * 添加某个view及其标签到头部view中,返回被替换的view,当前添加不成功时或者原本不存在该标签成功添加新view时返回null
     *
     * @param viewTag
     * @param headerView
     * @return
     */
    public View addHeaderView(int viewTag, View headerView) {
        return mHeaderOption.addView(viewTag, headerView);
    }

    /**
     * 添加某个view及其标签到尾部view中,返回被替换的view,,当前添加不成功时或者原本不存在该标签成功添加新view时返回null
     *
     * @param viewTag
     * @param footerView
     * @return
     */
    public View addFootView(int viewTag, View footerView) {
        return mFooterOption.addView(viewTag, footerView);
    }

    /**
     * 移除某个标签对应的头部view
     *
     * @param viewTag
     * @return
     */
    public boolean removeHeaderView(int viewTag) {
        return mHeaderOption.removeView(viewTag);
    }

    /**
     * 移除某个标签对应的尾部view
     *
     * @param viewTag
     * @return
     */
    public boolean removeFooterView(int viewTag) {
        return mFooterOption.removeView(viewTag);
    }

    /**
     * 清除所有头部view
     */
    public void clearHeaderView() {
        mHeaderOption.clearAllView();
    }

    /**
     * 清除所有尾部view
     */
    public void clearFootView() {
        mFooterOption.clearAllView();
    }

    /**
     * 当前刷新状态,当前是否正在刷新状态
     *
     * @param isRefreshing
     */
    public void setRefreshingStatus(boolean isRefreshing) {
        mIsRefreshing = isRefreshing;
        this.notifyDataSetChanged();
    }

    /**
     * 设置加载状态,当前是否正在加载状态
     *
     * @param isLoading
     */
    public void setLoadingStatus(boolean isLoading) {
        mIsLoading = isLoading;
        this.notifyDataSetChanged();
    }

    /**
     * 设置头部view是否可用,不可用时所有头部view不显示
     *
     * @param isEnable
     */
    public void setHeaderViewEnable(boolean isEnable) {
        mIsHeaderViewEnable = isEnable;
        this.notifyDataSetChanged();
    }

    /**
     * 设置尾部view是否可用,不可用时所有尾部view不显示
     *
     * @param isEnable
     */
    public void setFootViewEnable(boolean isEnable) {
        mIsFootViewEnable = isEnable;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (isRefreshingView(position)) {
            return VIEW_HEADER_REFRESH;
        } else if (isLoadingView(position)) {
            return VIEW_FOOTER_LOAD_MORE;
        } else if (isHeaderView(position)) {
            return mHeaderOption.getViewViewTag(position);
        } else if (isFooterView(position)) {
            return mFooterOption.getViewViewTag(getFooterPosition(position));
        } else {
            return mInnerAdapter.getItemViewType(position - getRefreshingViewCount() - getHeaderViewCount());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_HEADER_REFRESH) {
            return new ExtraViewHolder(mRefreshHeader);
        } else if (viewType == VIEW_FOOTER_LOAD_MORE) {
            return new ExtraViewHolder(mLoadMoreFooter);
        } else if (mFooterOption.isContainsView(viewType)) {
            return new ExtraViewHolder(mFooterOption.getView(viewType));
        } else if (mHeaderOption.isContainsView(viewType)) {
            return new ExtraViewHolder(mHeaderOption.getView(viewType));
        } else {
            return mInnerAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //若为头部/尾部/刷新或者加载view之一,不需要绑定数据
        if (isRefreshingView(position) || isLoadingView(position) ||
                isHeaderView(position) || isFooterView(position)) {
            return;
        } else {
            mInnerAdapter.onBindViewHolder(holder, position - getRefreshingViewCount() - getHeaderViewCount());
        }
    }

    @Override
    public int getItemCount() {
        //总item数应该为 刷新view+加载view+头部view+尾部view+内部adapter的item个数
        return getRefreshingViewCount() + getLoadingViewCount() +
                getHeaderViewCount() + getFooterViewCount() + mInnerAdapter.getItemCount();
    }

    /**
     * 判断当前位置的view是否为头部view
     *
     * @param position
     * @return
     */
    private boolean isHeaderView(int position) {
        if (!mIsHeaderViewEnable) {
            return false;
        }
        //计算头部view开始的位置
        int headerStartPosition = getRefreshingViewCount();
        //计算头部view结束的益
        int headerEndPosition = headerStartPosition + getHeaderViewCount();
        return position >= headerStartPosition && position < headerEndPosition;
    }

    /**
     * 判断当前位置的view是否为尾部的view
     *
     * @param position
     * @return
     */
    private boolean isFooterView(int position) {
        if (!mIsFootViewEnable) {
            return false;
        }
        //计算尾部view开始的位置
        int footerEndPosition = this.getItemCount() - getLoadingViewCount();
        //计算尾部view结束的位置
        int footerStartPosition = footerEndPosition - getFooterViewCount();
        return position >= footerStartPosition && position < footerEndPosition;
    }

    /**
     * 判断当前位置的view是否为刷新view
     *
     * @param position
     * @return
     */
    private boolean isRefreshingView(int position) {
        if (getRefreshingViewCount() != 0 && position == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断当前位置的view是否为加载view
     *
     * @param position
     * @return
     */
    private boolean isLoadingView(int position) {
        if (getLoadingViewCount() != 0 && position == this.getItemCount() - 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取加载的viewe的数据,0或者1,可能不存在
     *
     * @return
     */
    private int getLoadingViewCount() {
        return (mIsLoading && mLoadMoreFooter != null) ? 1 : 0;
    }

    /**
     * 获取刷新的view的数量,0或者1(可能不存在)
     *
     * @return
     */
    private int getRefreshingViewCount() {
        return (mIsRefreshing && mRefreshHeader != null) ? 1 : 0;
    }

    /**
     * 计算当前位置的view在尾部的位置,该position必须是尾部的view才行
     */
    private int getFooterPosition(int position) {
        return position - getRefreshingViewCount() - getHeaderViewCount() - mInnerAdapter.getItemCount();
    }

    //获取头部view的个数
    private int getHeaderViewCount() {
        return mIsHeaderViewEnable ? mHeaderOption.size() : 0;
    }

    //获取尾部view的个数
    private int getFooterViewCount() {
        return mIsFootViewEnable ? mFooterOption.size() : 0;
    }

    //重写adapterObserver,包装内部的adapter将会使用新的observer处理item
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyItemRangeChanged(positionStart + getHeaderViewCount() + getRefreshingViewCount(), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyItemRangeInserted(positionStart + getHeaderViewCount() + getRefreshingViewCount(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyItemRangeRemoved(positionStart + getHeaderViewCount() + getRefreshingViewCount(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            int aboveViewsCountCount = getHeaderViewCount() + getRefreshingViewCount();
            notifyItemRangeChanged(fromPosition + aboveViewsCountCount, toPosition + aboveViewsCountCount + itemCount);
        }
    };

    /**
     * 用于显示view的holder,没有实际的逻辑意义
     */
    public static class ExtraViewHolder extends RecyclerView.ViewHolder {

        public ExtraViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 头部/尾部添加额外View的缓存处理类
     */
    public static class HeaderFooterViewCache {
        private List<Map.Entry<Integer, View>> mViewCacheMap;
        private Map<Integer, Integer> mIndexMap;

        public HeaderFooterViewCache() {
            mViewCacheMap = new LinkedList<Map.Entry<Integer, View>>();
            mIndexMap = new ArrayMap<Integer, Integer>();
        }

        /**
         * 返回当前保存的View的个数
         *
         * @return
         */
        public int size() {
            return mViewCacheMap.size() == mIndexMap.size() ? mViewCacheMap.size() : 0;
        }

        /**
         * 根据位置获取对应的标签
         *
         * @param position
         * @return
         */
        public int getViewViewTag(int position) {
            return mViewCacheMap.get(position).getKey();
        }

        /**
         * 根据标签获取对应的view
         *
         * @param viewTag
         * @return
         */
        public View getView(int viewTag) {
            int index = mIndexMap.get(viewTag);
            Map.Entry<Integer, View> entry = mViewCacheMap.get(index);
            return entry.getValue();
        }

        /**
         * 检测是否已经存在某个标签
         *
         * @param viewTag
         * @return
         */
        public boolean isContainsView(int viewTag) {
            return mIndexMap.containsKey(viewTag);
        }

        /**
         * 添加新view及其唯一标签,当该标签已经存在某个view时,将替换该view
         *
         * @param viewTag
         * @param view
         * @return 返回被替换的view, 或者是null;若view为null,返回null,添加失败
         */
        public View addView(int viewTag, View view) {
            View oldView = null;
            if (view == null) {
                return oldView;
            } else {
                int index = 0;
                Integer location = null;
                if (isContainsView(viewTag)) {
                    location = mIndexMap.get(viewTag);
                    index = location.intValue();
                } else {
                    index = mViewCacheMap.size();
                    mIndexMap.put(viewTag, index);
                }
                if (location != null && location.intValue() < mViewCacheMap.size()) {
                    Map.Entry<Integer, View> entry = mViewCacheMap.remove(location.intValue());
                    if (entry != null) {
                        oldView = entry.getValue();
                    }
                }
                mViewCacheMap.add(index, new AbstractMap.SimpleEntry<Integer, View>(viewTag, view));
                mIndexMap.put(viewTag, index);
                return oldView;
            }
        }

        /**
         * 移除某个标签对应的view
         *
         * @param viewTag
         * @return
         */
        public boolean removeView(int viewTag) {
            if (isContainsView(viewTag)) {
                int index = mIndexMap.get(viewTag);
                mViewCacheMap.remove(index);
                mIndexMap.remove(viewTag);
                return true;
            } else {
                return false;
            }
        }

        /**
         * 清除所有的view
         */
        public void clearAllView() {
            mViewCacheMap.clear();
            mIndexMap.clear();
        }

        /**
         * 获取view的标签列表,标签应该是唯一的
         *
         * @return
         */
        public Set<Integer> getViewTags() {
            return mIndexMap.keySet();
        }
    }
}
