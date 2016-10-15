package com.taro.headerrecycle.adapter;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taro.headerrecycle.layoutmanager.HeaderSpanSizeLookup;
import com.taro.headerrecycle.stickerheader.StickHeaderItemDecoration;
import com.taro.headerrecycle.utils.RecyclerViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/4/19.
 */
public class HeaderRecycleAdapter<T, H> extends BaseAdapter<HeaderRecycleViewHolder> {
    private static final int FIRST_LOAD_ITEM_COUNT = Integer.MAX_VALUE;

    //分组数据列表
    private List<List<T>> mGroupList;
    private List<Integer> mEachGroupCountList;
    //头部数据
    private Map<Integer, H> mHeaderMap;
    private LayoutInflater mInflater;
    private IHeaderAdapterOption<T, H> mOptions = null;
    private IAdjustCountOption mAdjustOption = null;
    //通知参数更新的回调接口
    private OnHeaderParamsUpdateListener mParamsUpdateListener = null;

    private Point mRecyclePoint = new Point();
    private boolean mIsShowHeader = true;
    //实际数据源的itemCount数量
    private int mCount = 0;
    //最后一次调整后的count数量
    private int mLastAdjustCount = FIRST_LOAD_ITEM_COUNT;

    public HeaderRecycleAdapter(@NonNull Context context) {
        this(context, null, null, null);
    }

    /**
     * 创建可以显示分组头部的recycleAdapter,其中Context与option不可为空
     *
     * @param context   布局加载对象
     * @param option    分组头部需要的配置接口
     * @param groupList 分组数据
     * @param headerMap 分组头部匹配的Map
     */
    public HeaderRecycleAdapter(@NonNull Context context, @Nullable IHeaderAdapterOption<T, H> option, @Nullable List<List<T>> groupList, @Nullable Map<Integer, H> headerMap) {
        this.init(LayoutInflater.from(context), option, groupList, headerMap);
    }

    /**
     * 创建可以显示分组头部的recycleAdapter,其中Context与option不可为空
     *
     * @param inflater  布局加载对象
     * @param option    分组头部需要的配置接口
     * @param groupList 分组数据
     * @param headerMap 分组头部匹配的Map
     */
    public HeaderRecycleAdapter(@NonNull LayoutInflater inflater, @Nullable IHeaderAdapterOption<T, H> option, @Nullable List<List<T>> groupList, @Nullable Map<Integer, H> headerMap) {
        this.init(inflater, option, groupList, headerMap);
    }

    /**
     * 创建可以显示分组头部的recycleAdapter,其中Context与option不可为空
     *
     * @param inflater  布局加载对象
     * @param option    分组头部需要的配置接口
     * @param groupList 分组数据
     * @param headerMap 分组头部匹配的Map
     */
    private void init(@NonNull LayoutInflater inflater, @Nullable IHeaderAdapterOption<T, H> option, @Nullable List<List<T>> groupList, @Nullable Map<Integer, H> headerMap) {
        mInflater = inflater;
        mHeaderMap = headerMap;
        this.setHeaderAdapterOption(option);
        this.setGroupList(groupList);
    }

    /**
     * 设置或者更新adapter的option
     *
     * @param option
     */
    public void setHeaderAdapterOption(IHeaderAdapterOption option) {
        if (option != null) {
            this.mOptions = option;
        }
    }

    /**
     * 设置header更新参数的监听接口,触发对应的更新事件时将回调此接口
     *
     * @param listener
     */
    public void setHeaderParamsUpdateListener(OnHeaderParamsUpdateListener listener) {
        mParamsUpdateListener = listener;
    }

    /**
     * 获取分组列表的分组数据量list,list中每一项存放了对应的每一个分组的size
     *
     * @return
     */
    @Nullable
    public List<Integer> getEachGroupCountList() {
        return mEachGroupCountList;
    }

    /**
     * 获取是否显示header
     *
     * @return
     */
    public boolean isShowHeader() {
        return mIsShowHeader;
    }

    /**
     * 获取分组数据
     *
     * @return
     */
    @Nullable
    public List<List<T>> getGroupList() {
        return mGroupList;
    }

    /**
     * 获取头部数据
     *
     * @return
     */
    @Nullable
    public Map<Integer, H> getHeaderMap() {
        return mHeaderMap;
    }

    /**
     * 获取指定分组的头部的数据,可能返回null若不存在该分组
     *
     * @param groupId
     * @return
     */
    @Nullable
    public H getHeader(int groupId) {
        return mHeaderMap.get(groupId);
    }

    /**
     * 获取非header位置的item数据,可以的情况下优先使用{@link #getItem(int, int)},可以加快检索效率,不需要根据位置再计算一次该item的相关信息
     *
     * @param position item位置(非header,若该位置为header,返回null)
     * @return
     */
    @Nullable
    public T getItem(int position) {
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, position, mIsShowHeader);
        return getItem(mRecyclePoint);
    }

    /**
     * 获取非header位置的item数据,若该位置为header,返回null
     *
     * @param groupId 分组所在的ID
     * @param childId 分组内元素的索引位置ID
     * @return
     */
    public T getItem(int groupId, int childId) {
        Point p = new Point(groupId, childId);
        return getItem(p);
    }

    /**
     * 获取adapter中原始的itemCount
     *
     * @return
     */
    public int getOriginalItemCount() {
        return mCount;
    }

    /**
     * 设置分组灵气
     *
     * @param groupList
     */
    public void setGroupList(List<List<T>> groupList) {
        mGroupList = groupList;
        mEachGroupCountList = getEachGroupCountList(groupList);
        //更新数据总数,需要计算header数据在内
        updateCount(mEachGroupCountList, mIsShowHeader);
        if (mParamsUpdateListener != null) {
            mParamsUpdateListener.onUpdateGroupList(groupList);
            mParamsUpdateListener.onUpdateEachGroupCountList(mEachGroupCountList);
        }
    }

    /**
     * 设置是否显示header
     *
     * @param isShowHeader
     */
    public void setIsShowHeader(boolean isShowHeader) {
        if (mIsShowHeader != isShowHeader) {
            updateCount(mEachGroupCountList, isShowHeader);
            mIsShowHeader = isShowHeader;
            if (mParamsUpdateListener != null) {
                mParamsUpdateListener.onUpdateIsShowHeader(isShowHeader);
            }
        }
    }

    @Override
    public void onViewRecycled(HeaderRecycleViewHolder holder) {
        super.onViewRecycled(holder);
        holder.clearViewCache();
        holder.unregisterAllViewOnClickListener();
    }

    @Override
    public int getItemViewType(int position) {
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, position, mIsShowHeader);
        return mOptions.getItemViewType(position, mRecyclePoint.x, mRecyclePoint.y, isHeaderItem(mRecyclePoint), mIsShowHeader);
    }

    @Override
    public HeaderRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = mOptions.getLayoutId(viewType);
        View rootView = mInflater.inflate(layoutId, parent, false);
        if (mOptions instanceof IAdjustCountOption) {
            ((IAdjustCountOption) mOptions).onCreateViewEverytime(rootView, parent, this, viewType);
        }
        return new HeaderRecycleViewHolder(this, rootView);
    }

    @Override
    public void onBindViewHolder(HeaderRecycleViewHolder holder, int position) {
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, position, mIsShowHeader);
        //设置当前项的组ID,与组内数据ID
        holder.setGroupIdAndChildId(mRecyclePoint.x, mRecyclePoint.y);
        T itemData = null;
        H headerData = null;
        if (isHeaderItem(mRecyclePoint)) {
            //设置头部数据显示
            headerData = mHeaderMap.get(mRecyclePoint.x);
            mOptions.setHeaderHolder(mRecyclePoint.x, headerData, holder);
        } else {
            //设置普通Item数据显示
            List<T> itemList = mGroupList.get(mRecyclePoint.x);
            itemData = itemList != null ? itemList.get(mRecyclePoint.y) : null;
            mOptions.setViewHolder(mRecyclePoint.x, mRecyclePoint.y, position, itemData, holder);
        }

        //当完成第一次绑定后调整view的数据恰好为1时,检测当前option是否是可调整对象
        if (mLastAdjustCount == 1 && mOptions instanceof IAdjustCountOption) {
            mAdjustOption = (IAdjustCountOption) mOptions;
            //获取option的新调整item结果
            int newCount = mAdjustOption.getAdjustCount();
            //当结果大于1时,通知再次更新数据;这里因为当item值大于1时,此次更新已经完成,recycleView不会再进行任何更新或者是创建数据,
            //所以当前的设置其实尽管item数量大于1但不会有任何的效果,除非重新刷新一次.
            if (newCount != 1) {
                //通过recycleView在刷新完界面后再重新刷新一次
                this.getParentRecycleView().post(new Runnable() {
                    @Override
                    public void run() {
                        getParentRecycleView().requestLayout();
                    }
                });
            }
        }
    }


    @Override
    public int getItemCount() {
        int adjustCount = mCount;
        if (mOptions instanceof IAdjustCountOption) {
            //第一次加载时,lastAdjustCount都必定更新为当前的itemCount
            if (mLastAdjustCount == FIRST_LOAD_ITEM_COUNT) {
                mLastAdjustCount = mCount;
            }
            IAdjustCountOption justOption = (IAdjustCountOption) mOptions;
            adjustCount = justOption.getAdjustCount();
            //当item数量在有效范围内才会进行调整
            if (adjustCount < 0 || adjustCount > mCount) {
                adjustCount = mCount;
                mLastAdjustCount = adjustCount;
            } else {
                if (mLastAdjustCount != adjustCount) {
                    //当当前调整值与上一次调整值不同时再进行计算新的调整值
                    int originalCount = RecyclerViewUtil.setRecyclerViewStateItemCount(adjustCount, this.getOriginalItemCount(), this.getParentRecycleView());
                    Exception exception = null;
                    if (originalCount >= 0) {
                        //正常
                        //记录最后一次调整的item数量
                        mLastAdjustCount = adjustCount;
                    } else {
                        //返回负数,出错了,打出错误
                        switch (originalCount) {
                            case RecyclerViewUtil.STATE_RECYCLERVIEW_EXCEPTION:
                                //异常已经在方法中打印出
                                break;
                            case RecyclerViewUtil.STATE_RECYCLERVIEW_ILLEGAL_PARAMS:
                                exception = new IllegalArgumentException("parent recycle can not be null or adjust count should be >= 0");
                                exception.printStackTrace();
                                break;
                            case RecyclerViewUtil.STATE_RECYCLERVIEW_STATE_UNINITIALIZED:
                                //parentView中的state对象还没有被初始化...这个,一般不可能,这里只是以防万一
                                exception = new NullPointerException("state has not been initialized, check if adapter was set to recyclerView");
                                exception.printStackTrace();
                                break;
                            default:
                                break;
                        }
                        //任何的出错都将调整值重置为初始值
                        mLastAdjustCount = mCount;
                    }
                } else {
                    //当前调整数量与上次调整一样,直接返回缓存的值
                }
            }
        } else {
            mLastAdjustCount = mCount;
        }
        return mLastAdjustCount;
    }

    /**
     * 获取此类实现的用于固定头部展示的 stickHeaderDecoration 接口
     *
     * @return
     */
    @NonNull
    public StickHeaderItemDecoration.IStickerHeaderDecoration getImplementStickHeaderDecoration() {
        return this;
    }

    /**
     * 获取此类实现的用于gridLayoutManager中显示item占用网格数的 spanSizeHandler 接口
     *
     * @return
     */
    @NonNull
    public HeaderSpanSizeLookup.ISpanSizeHandler getImplementSpanSizeLookupHandler() {
        return this;
    }

    /**
     * 判断当前item是否为headerView
     *
     * @param p item相关的分组及子元素信息
     * @return
     */
    public boolean isHeaderItem(Point p) {
        if (p != null && p.x >= 0 && p.y == -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断给定的groupId和childId是否对应位置的item为header
     *
     * @param groupId 分组索引
     * @param childId 分组内列表的索引
     * @return
     */
    public boolean isHeaderItem(int groupId, int childId) {
        if (groupId < mEachGroupCountList.size() && groupId >= 0 && childId == -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取指定位置的item数据,该位置的item不可为header,否则返回null
     *
     * @param p
     * @return
     */
    @Nullable
    public T getItem(Point p) {
        //若该位置为header,返回null
        if (isHeaderItem(p)) {
            return null;
            //若该分组位置不合法,返回null
        } else if (p == null || mGroupList == null || p.x < 0 || p.x >= mGroupList.size()) {
            return null;
        } else {
            //分组中元素位置不合法,返回null
            List<T> itemList = mGroupList.get(p.x);
            if (itemList == null || p.y < 0 || p.y >= itemList.size()) {
                return null;
            } else {
                return itemList.get(p.y);
            }
        }
    }

    /**
     * 根据position计算分组的索引及分组内列表的数据项索引
     *
     * @param position
     * @return
     */
    public void getGroupIdAndChildIdFromPosition(@Nullable List<Integer> eachGroupCountList, @NonNull Point outPoint, int position, boolean isShowGroup) {
        if (position < 0 || position >= mCount) {
            outPoint.set(-1, 0);
        }
        int groupId = 0;
        int childId = 0;
        //是否显示Header
        int headerSize = 1;
        int groupEachLine = isShowGroup ? headerSize : 0;
        if (eachGroupCountList != null) {
            for (int groupCount : eachGroupCountList) {
                //获取分组的数据量
                childId = groupCount;
                //将当前position计算与分组数据量进行计算,groupEachLine用于减掉Header
                //Header占用一行,不显示Header时为0(不影响计算)
                position = position - groupEachLine - childId;
                if (position < 0) {
                    childId += position;
                    break;
                }
                //每计算完一组分组ID添加1
                groupId++;
            }
        }
        outPoint.set(groupId, childId);
    }


    /**
     * 更新总数据量
     *
     * @param eachGroupCountList 分组列表
     * @param isShowGroup        是否显示Header
     */
    protected void updateCount(List<Integer> eachGroupCountList, boolean isShowGroup) {
        mCount = 0;
        int headerSize = 1;
        int groupEachLine = isShowGroup ? headerSize : 0;
        if (eachGroupCountList != null) {
            for (int groupCount : eachGroupCountList) {
                //若显示Header将Header添加到总数据量中,每一个Header占用一行
                mCount += groupCount + groupEachLine;
            }
        }
        if (mParamsUpdateListener != null) {
            mParamsUpdateListener.OnUpdateCount(mCount);
        }
    }

    /**
     * 根据分数数据列表获取分组数据量的列表
     *
     * @param groupList 分组数据列表
     * @return 返回分组数据量列表, 每一项都对应分组列表中的每一组的数据量
     */
    @Nullable
    public List<Integer> getEachGroupCountList(List<List<T>> groupList) {
        if (groupList == null) {
            return null;
        } else {
            List<Integer> list = new ArrayList<Integer>(groupList.size());
            for (List group : groupList) {
                list.add(group == null ? 0 : group.size());
            }
            return list;
        }
    }

    /**********
     * 固定头部的实现类
     *********/
    @Override
    public boolean isHeaderPosition(int position) {
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, position, mIsShowHeader);
        return isHeaderItem(mRecyclePoint);
    }

    @Override
    public boolean hasStickHeader(int position) {
        return mIsShowHeader;
    }

    @Override
    public int getHeaderViewTag(int position, RecyclerView parent) {
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, position, mIsShowHeader);
        return mOptions.getLayoutId(mOptions.getHeaderViewType(mRecyclePoint.x, position));
    }

    @Override
    public View getHeaderView(int position, int headerViewTag, RecyclerView parent) {
        View itemView = mInflater.inflate(headerViewTag, parent, false);
        return itemView;
    }

    @Override
    public void setHeaderView(int position, int headerViewTag, RecyclerView parent, View headerView) {
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, position, mIsShowHeader);
        H headerObj = mHeaderMap.get(mRecyclePoint.x);
        HeaderRecycleViewHolder holder = (HeaderRecycleViewHolder) headerView.getTag();
        if (holder == null) {
            holder = new HeaderRecycleViewHolder(this, headerView);
            headerView.setTag(holder);
        }
        mOptions.setHeaderHolder(mRecyclePoint.x, headerObj, holder);
    }

    @Override
    public boolean isBeenDecorated(int lastDecoratedPosition, int nowDecoratingPosition) {
        int lastGroupId = Integer.MIN_VALUE;
        int newGroupId = Integer.MIN_VALUE;
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, lastDecoratedPosition, mIsShowHeader);
        lastGroupId = mRecyclePoint.x;
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, nowDecoratingPosition, mIsShowHeader);
        newGroupId = mRecyclePoint.y;
        return lastGroupId == Integer.MIN_VALUE || newGroupId == Integer.MIN_VALUE || lastGroupId == newGroupId;
    }

    /************
     * GridLayoutManage头部spanSizeLookup实现
     *************/
    @Override
    public boolean isSpecialItem(int position) {
        getGroupIdAndChildIdFromPosition(mEachGroupCountList, mRecyclePoint, position, mIsShowHeader);
        return isHeaderItem(mRecyclePoint);
    }

    @Override
    public int getSpecialItemSpanSize(int spanCount, int position) {
        return spanCount;
    }

    @Override
    public int getNormalItemSpanSize(int spanCount, int position) {
        return 1;
    }


    /**
     * 带头部的adapter配置接口
     *
     * @param <T> 其中参数T为每个item对应的设置的数据类型
     * @param <H> 参数H为每个header对应设置的数据类型
     */
    public interface IHeaderAdapterOption<T, H> {
        /**
         * 不存在headerView类型,{@value Integer#MIN_VALUE}
         */
        public static final int NO_HEADER_TYPE = Integer.MIN_VALUE;

        /**
         * 获取headerView的类型,headerView类型专用
         *
         * @param groupId 分组类型
         * @return
         */
        public int getHeaderViewType(int groupId, int position);

        /**
         * 获取View的类型
         *
         * @param position     位置
         * @param groupId
         * @param childId
         * @param isHeaderItem
         * @param isShowHeader 是否显示header  @return
         */
        public int getItemViewType(int position, int groupId, int childId, boolean isHeaderItem, boolean isShowHeader);

        /**
         * 根据ViewType获取加载的当前项layoutID
         *
         * @param viewType
         * @return
         */
        public int getLayoutId(int viewType);

        /**
         * 设置Header显示绑定数据
         *
         * @param groupId 当前组ID
         * @param header  当前Header数据,来自于Map
         * @param holder
         */
        public void setHeaderHolder(int groupId, H header, @NonNull HeaderRecycleViewHolder holder);

        /**
         * 设置子项ViewHolder
         *
         * @param groupId  分组ID
         * @param childId  当前组子项ID
         * @param position 当前项位置(此位置为recycleView中的位置)
         * @param itemData
         * @param holder
         */
        public void setViewHolder(int groupId, int childId, int position, T itemData, @NonNull HeaderRecycleViewHolder holder);
    }

    /**
     * header参数更新的接口
     */
    public interface OnHeaderParamsUpdateListener<T> {
        /**
         * 更新是否显示header
         *
         * @param isShowHeader
         */
        public void onUpdateIsShowHeader(boolean isShowHeader);

        /**
         * 更新分组数据
         *
         * @param groupList
         */
        public void onUpdateGroupList(@Nullable List<List<T>> groupList);

        /**
         * 更新分组数据量(指将每个分组中的数据量取出按顺序存放到一个list中,直接通过此list可以获取每组的数据量并进行,不需要遍历分组数据去list.size()获取每个分组的数据量)
         *
         * @param eachGroupCountList
         */
        public void onUpdateEachGroupCountList(@Nullable List<Integer> eachGroupCountList);

        /**
         * 更新item的总数,当显示header和不显示header时,此值会有变化
         *
         * @param count
         */
        public void OnUpdateCount(int count);
    }
}
