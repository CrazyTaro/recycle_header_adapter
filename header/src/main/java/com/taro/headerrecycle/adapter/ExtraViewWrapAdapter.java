package com.taro.headerrecycle.adapter;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.taro.headerrecycle.layoutmanager.HeaderSpanSizeLookup;
import com.taro.headerrecycle.stickerheader.StickHeaderItemDecoration;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.R.string.no;

/**
 * Created by taro on 16/5/19.
 */
public class ExtraViewWrapAdapter extends BaseAdapter<RecyclerView.ViewHolder> {
    public static final int VIEW_HEADER_REFRESH = Integer.MAX_VALUE / 3;
    public static final int VIEW_FOOTER_LOAD_MORE = Integer.MIN_VALUE / 3;

    private HeaderFooterViewCache mHeaderCache = null;
    private HeaderFooterViewCache mFooterCache = null;
    private StickHeaderItemDecoration.IStickerHeaderDecoration mIStickHeaderDecoration = null;
    private HeaderSpanSizeLookup.ISpanSizeHandler mISpanSizeLookup = null;


    private View mRefreshHeader = null;
    private View mLoadMoreFooter = null;
    private boolean mIsRefreshing = false;
    private boolean mIsLoading = false;
    private RecyclerView.Adapter mInnerAdapter;
    private boolean mIsFootViewEnable = true;
    private boolean mIsHeaderViewEnable = true;

    /**
     * 设置刷新view的状态
     *
     * @param isRefreshing    当前是否在刷新
     * @param isScrollToStart 刷新时是否滑动到recycleView第一项
     * @param recyclerView
     */
    public static final void setRefreshingViewStatus(boolean isRefreshing, boolean isScrollToStart, RecyclerView recyclerView) {
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

    /**
     * 设置加载view的状态
     *
     * @param isLoading     当前是否在加载
     * @param isScrollToEnd 加载时是否滑动到recycleView最后一项
     * @param recyclerView
     */
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

    /**
     * 设置头部是否可用,不可用时不会显示任何头部
     *
     * @param isHeaderEnable   头部是否可用
     * @param isScrollToHeader 是否在设置后滑动到recycleView第一项
     * @param recycleView
     */
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

    /**
     * 设置尾部是否可用,不可用时不会显示任何尾部
     *
     * @param isFooterEnable   尾部是否可用
     * @param isScrollToFooter 是否在设置后滑动到recycleView最后一项
     * @param recyclerView
     */
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
     * @param innerAdapter
     */
    public ExtraViewWrapAdapter(@NonNull RecyclerView.Adapter innerAdapter) {
        this(innerAdapter, true, true);
    }

    /**
     * 创建多头部/尾部包装类的adapter,使用此adapter包装内部adapter时会改变内部adapter的某些操作,当不再需要时请使用{@link #unregisterInnerAdatperDataObserver()}还原内部adapter的操作
     *
     * @param innerAdapter       内部引用的adapter,不可为null
     * @param isHeaderViewEnable 头部view是否可用
     * @param isFootViewEnable   尾部view是否可用
     */
    public ExtraViewWrapAdapter(@NonNull RecyclerView.Adapter innerAdapter, boolean isHeaderViewEnable, boolean isFootViewEnable) {
        this.setInnerAdapter(innerAdapter);
        mIsHeaderViewEnable = isHeaderViewEnable;
        mIsFootViewEnable = isFootViewEnable;
        mHeaderCache = new HeaderFooterViewCache();
        mFooterCache = new HeaderFooterViewCache();
//        mInnerAdapter.registerAdapterDataObserver(mDataObserver);
    }

    /**
     * 设置固定头部item渲染的接口回调
     *
     * @param decoration 固定头部接口,用于回调判断当前项是否为头部/是否需要显示及绘制固定头部
     */
    public void setIStickHeaderItemDecoration(StickHeaderItemDecoration.IStickerHeaderDecoration decoration) {
        mIStickHeaderDecoration = decoration;
    }

    /**
     * 设置 gridLayoutManager 需要显示的spanSizeLookup
     *
     * @param spanSizeLookup 用于正确显示item占用的网格数
     */
    public void setISpanSizeLookup(HeaderSpanSizeLookup.ISpanSizeHandler spanSizeLookup) {
        mISpanSizeLookup = spanSizeLookup;
    }

    /**
     * 设置内置的innerAdapter,不可为null
     *
     * @param innerAdapter recycleView.Adapter,若该adapter已经实现了 {@link StickHeaderItemDecoration.IStickerHeaderDecoration}或者是{@link HeaderSpanSizeLookup.ISpanSizeHandler}
     *                     将直接引用此实例,不需要再重新设置一次两个接口
     */
    public void setInnerAdapter(@NonNull RecyclerView.Adapter innerAdapter) {
        mInnerAdapter = innerAdapter;
        if (mInnerAdapter != null && mInnerAdapter instanceof StickHeaderItemDecoration.IStickerHeaderDecoration) {
            mIStickHeaderDecoration = (StickHeaderItemDecoration.IStickerHeaderDecoration) mInnerAdapter;
        }

        if (mInnerAdapter != null && mInnerAdapter instanceof HeaderSpanSizeLookup.ISpanSizeHandler) {
            mISpanSizeLookup = (HeaderSpanSizeLookup.ISpanSizeHandler) mInnerAdapter;
        }

        this.attachInnerAdapterToParent(getParentRecycleView());
        this.detachInnerAdapterToParent(getParentRecycleView());
    }

//    /**
//     * 反注册内部innerAdapter的数据更新observer
//     */
//    public void unregisterInnerAdatperDataObserver() {
//        if (mInnerAdapter != null) {
//            mInnerAdapter.unregisterAdapterDataObserver(mDataObserver);
//        }
//    }
//
//    /**
//     * 重新注册内部innerAdapter的数据更新observer
//     */
//    public void reregisterInnerAdapterDataObserver() {
//        if (mInnerAdapter != null) {
//            mInnerAdapter.registerAdapterDataObserver(mDataObserver);
//        }
//    }

    /**
     * 获取当前WrapAdapter位置在InnerAdapter中对应的位置,即除去headerView及refreshView<br>
     *
     * @param wrapAdapterPosition 当前WrapAdapter的位置
     * @return 当无法得到内部InnerAdapter中对应的位置时(可能是header或者footer或者innerAdapter为null等), 返回-1
     */
    public int getInnerAdapterPosition(int wrapAdapterPosition) {
        int refreshViewCount = getRefreshingViewCount();
        int headerViewCount = getHeaderViewCount();
        int innerPosition = wrapAdapterPosition - refreshViewCount - headerViewCount;
        if (mInnerAdapter != null && innerPosition < mInnerAdapter.getItemCount()) {
            return innerPosition;
        } else {
            return -1;
        }
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
        return mHeaderCache.addView(viewTag, headerView);
    }

    /**
     * 添加某个view及其标签到尾部view中,返回被替换的view,,当前添加不成功时或者原本不存在该标签成功添加新view时返回null
     *
     * @param viewTag
     * @param footerView
     * @return
     */
    public View addFootView(int viewTag, View footerView) {
        return mFooterCache.addView(viewTag, footerView);
    }

    /**
     * 移除某个标签对应的头部view
     *
     * @param viewTag
     * @return
     */
    public boolean removeHeaderView(int viewTag) {
        return mHeaderCache.removeView(viewTag);
    }

    /**
     * 移除某个标签对应的尾部view
     *
     * @param viewTag
     * @return
     */
    public boolean removeFooterView(int viewTag) {
        return mFooterCache.removeView(viewTag);
    }

    /**
     * 清除所有头部view
     */
    public void clearHeaderView() {
        mHeaderCache.clearAllView();
    }

    /**
     * 清除所有尾部view
     */
    public void clearFootView() {
        mFooterCache.clearAllView();
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
            return mHeaderCache.getViewViewTag(position);
        } else if (isFooterView(position)) {
            return mFooterCache.getViewViewTag(getFooterPosition(position));
        } else {
            return mInnerAdapter.getItemViewType(getInnerAdapterPosition(position));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_HEADER_REFRESH) {
            return new ExtraViewHolder(mRefreshHeader);
        } else if (viewType == VIEW_FOOTER_LOAD_MORE) {
            return new ExtraViewHolder(mLoadMoreFooter);
        } else if (mFooterCache.isContainsView(viewType)) {
            return new ExtraViewHolder(mFooterCache.getView(viewType));
        } else if (mHeaderCache.isContainsView(viewType)) {
            return new ExtraViewHolder(mHeaderCache.getView(viewType));
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.attachInnerAdapterToParent(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.detachInnerAdapterToParent(recyclerView);
    }

    //将内部adapter绑定当前的parentView
    private void attachInnerAdapterToParent(RecyclerView recyclerView) {
        if (mInnerAdapter != null) {
            mInnerAdapter.onAttachedToRecyclerView(recyclerView);
        }
    }

    //将内部的adapter解绑定当前的parentView
    private void detachInnerAdapterToParent(RecyclerView recyclerView) {
        if (mInnerAdapter != null) {
            mInnerAdapter.onDetachedFromRecyclerView(recyclerView);
        }
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
        return mIsHeaderViewEnable ? mHeaderCache.size() : 0;
    }

    //获取尾部view的个数
    private int getFooterViewCount() {
        return mIsFootViewEnable ? mFooterCache.size() : 0;
    }


    /*********
     * 固定头部接口回调
     ***********/

    @Override
    public boolean isHeaderPosition(int position) {
        if (mIStickHeaderDecoration != null) {
            return mIStickHeaderDecoration.isHeaderPosition(getInnerAdapterPosition(position));
        } else {
            return false;
        }
    }

    //此方法决定了是否需要显示固定头部,装饰类只需要处理此部分正确返回innerAdapter的位置就可以了
    //其它的方法都是将position转成正确的innerAdapter的位置信息进行接口回调.
    @Override
    public boolean hasStickHeader(int position) {
        if (isLoadingView(position) || isRefreshingView(position) || isHeaderView(position) || isFooterView(position)) {
            return false;
        } else if (mIStickHeaderDecoration != null) {
            return mIStickHeaderDecoration.hasStickHeader(getInnerAdapterPosition(position));
        } else {
            return false;
        }
    }

    @Override
    public int getHeaderViewTag(int position, RecyclerView parent) {
        if (mIStickHeaderDecoration != null) {
            return mIStickHeaderDecoration.getHeaderViewTag(getInnerAdapterPosition(position), parent);
        } else {
            return 0;
        }
    }

    @Override
    public View getHeaderView(int position, int headerViewTag, RecyclerView parent) {
        if (mIStickHeaderDecoration != null) {
            return mIStickHeaderDecoration.getHeaderView(getInnerAdapterPosition(position), headerViewTag, parent);
        } else {
            return null;
        }
    }

    @Override
    public void setHeaderView(int position, int headerViewTag, RecyclerView parent, View headerView) {
        if (mIStickHeaderDecoration != null) {
            mIStickHeaderDecoration.setHeaderView(getInnerAdapterPosition(position), headerViewTag, parent, headerView);
        }
    }

    @Override
    public boolean isBeenDecorated(int lastDecoratedPosition, int nowDecoratingPosition) {
        if (mIStickHeaderDecoration != null) {
            int lastInnerPos = getInnerAdapterPosition(lastDecoratedPosition);
            int nowInnerPos = getInnerAdapterPosition(nowDecoratingPosition);
            return mIStickHeaderDecoration.isBeenDecorated(lastInnerPos, nowInnerPos);
        } else {
            return false;
        }
    }

    /***********
     * GridLayoutManager中SpanSizeLookup的使用
     *************/

    @Override
    public boolean isSpecialItem(int position) {
        if (isLoadingView(position) || isRefreshingView(position) || isHeaderView(position) || isFooterView(position)) {
            return true;
        } else if (mISpanSizeLookup != null) {
            return mISpanSizeLookup.isSpecialItem(getInnerAdapterPosition(position));
        } else {
            return false;
        }
    }

    @Override
    public int getSpecialItemSpanSize(int spanCount, int position) {
        if (mISpanSizeLookup != null) {
            return mISpanSizeLookup.getSpecialItemSpanSize(spanCount, getInnerAdapterPosition(position));
        } else {
            return spanCount;
        }
    }

    @Override
    public int getNormalItemSpanSize(int spanCount, int position) {
        if (mISpanSizeLookup != null) {
            return mISpanSizeLookup.getNormalItemSpanSize(spanCount, getInnerAdapterPosition(position));
        } else {
            return 1;
        }
    }

//    //重写adapterObserver,包装内部的adapter将会使用新的observer处理item
//    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
//        @Override
//        public void onChanged() {
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public void onItemRangeChanged(int positionStart, int itemCount) {
//            notifyItemRangeChanged(positionStart + getHeaderViewCount() + getRefreshingViewCount(), itemCount);
//        }
//
//        @Override
//        public void onItemRangeInserted(int positionStart, int itemCount) {
//            notifyItemRangeInserted(positionStart + getHeaderViewCount() + getRefreshingViewCount(), itemCount);
//        }
//
//        @Override
//        public void onItemRangeRemoved(int positionStart, int itemCount) {
//            notifyItemRangeRemoved(positionStart + getHeaderViewCount() + getRefreshingViewCount(), itemCount);
//        }
//
//        @Override
//        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
//            int aboveViewsCountCount = getHeaderViewCount() + getRefreshingViewCount();
//            notifyItemRangeChanged(fromPosition + aboveViewsCountCount, toPosition + aboveViewsCountCount + itemCount);
//        }
//    };

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
