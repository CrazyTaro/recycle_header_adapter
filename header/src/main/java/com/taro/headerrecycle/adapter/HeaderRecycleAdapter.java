package com.taro.headerrecycle.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
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
public class HeaderRecycleAdapter<T, H> extends RecyclerView.Adapter<HeaderRecycleViewHolder> implements StickHeaderItemDecoration.IStickerHeaderDecoration, HeaderSpanSizeLookup.ISpanSizeHandler {
    private static final int FIRST_LOAD_ITEM_COUNT = Integer.MAX_VALUE;

    //分组数据列表
    protected List<List<T>> mGroupList;
    protected List<Integer> mEachGroupCountList;
    //头部数据
    protected Map<Integer, H> mHeaderMap;
    protected LayoutInflater mInflater;
    protected IHeaderAdapterOption<T, H> mOptions = null;
    private OnHeaderParamsUpdateListener mParamsUpdateListener = null;
    private RecyclerView mParentRecycle = null;

    protected boolean mIsShowHeader = true;
    protected int mCount = 0;
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
        mOptions = option;
        mHeaderMap = headerMap;
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
    public List<List<T>> getGroupList() {
        return mGroupList;
    }

    /**
     * 获取头部数据
     *
     * @return
     */
    public Map<Integer, H> getHeaderMap() {
        return mHeaderMap;
    }

    /**
     * 获取指定分组的头部的数据,可能返回null若不存在该分组
     *
     * @param groupId
     * @return
     */
    public H getHeader(int groupId) {
        return mHeaderMap.get(groupId);
    }

    /**
     * 获取非header位置的item数据,可以的情况下优先使用{@link #getItem(int, int)},可以加快检索效率,不需要根据位置再计算一次该item的相关信息
     *
     * @param position item位置(非header,若该位置为header,返回null)
     * @return
     */
    public T getItem(int position) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return getItem(p);
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
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return mOptions.getItemViewType(position, p.x, p.y, isHeaderItem(p), mIsShowHeader);
    }

    @Override
    public HeaderRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = mOptions.getLayoutId(viewType);
        View rootView = mInflater.inflate(layoutId, parent, false);
        if (mOptions instanceof IAdjustCountOption) {
            ((IAdjustCountOption) mOptions).onCreateViewEverytime(parent, this);
        }
        return new HeaderRecycleViewHolder(this, rootView);
    }

    @Override
    public void onBindViewHolder(HeaderRecycleViewHolder holder, int position) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        //设置当前项的组ID,与组内数据ID
        holder.setGroupIdAndChildId(p.x, p.y);
        T itemData = null;
        H headerData = null;
        if (isHeaderItem(p)) {
            //设置头部数据显示
            headerData = mHeaderMap.get(p.x);
            mOptions.setHeaderHolder(p.x, headerData, holder);
        } else {
            //设置普通Item数据显示
            List<T> itemList = mGroupList.get(p.x);
            itemData = itemList != null ? itemList.get(p.y) : null;
            mOptions.setViewHolder(p.x, p.y, position, itemData, holder);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int getItemCount() {
        int adjustCount = mCount;
        //第一次加载时,lastAdjustCount都必定更新为当前的itemCount
        if (mLastAdjustCount == FIRST_LOAD_ITEM_COUNT) {
            mLastAdjustCount = mCount;
        }
        if (mOptions instanceof IAdjustCountOption) {
            IAdjustCountOption justOption = (IAdjustCountOption) mOptions;
            adjustCount = justOption.getAdjustCount();
            //当item数量在有效范围内才会进行调整
            if (adjustCount < 0 || adjustCount > mCount) {
                adjustCount = mCount;
            }
            if (mLastAdjustCount != adjustCount) {
                int result = RecyclerViewUtil.setRecyclerViewStateItemCount(adjustCount, mParentRecycle);
                if (result >= 0) {
                    //记录最后一次调整的item数量
                    mLastAdjustCount = adjustCount;
                }
//                try {
//                    //获取state
//                    Field stateField = RecyclerView.class.getDeclaredField("mState");
//                    stateField.setAccessible(true);
//                    Object state = stateField.get(mParentRecycle);
//
//                    //获取state的mItemCount字段
//                    Field itemCountField = RecyclerView.State.class.getDeclaredField("mItemCount");
//                    itemCountField.setAccessible(true);
//
//                    //更改itemCount
//                    itemCountField.setInt(state, adjustCount);
//                    Log.i("layout", "success");
//                } catch (NoSuchFieldException | IllegalAccessException e) {
//                    e.printStackTrace();
//                    Log.i("layout", "fail");
//                }
            }
        }
        return adjustCount;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mParentRecycle = recyclerView;
    }

    /**
     * 获取此类实现的用于固定头部展示的 stickHeaderDecoration 接口
     *
     * @return
     */
    public StickHeaderItemDecoration.IStickerHeaderDecoration getImplementStickHeaderDecoration() {
        return this;
    }

    /**
     * 获取此类实现的用于gridLayoutManager中显示item占用网格数的 spanSizeHandler 接口
     *
     * @return
     */
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
    public Point getGroupIdAndChildIdFromPosition(List<Integer> eachGroupCountList, int position, boolean isShowGroup) {
        if (position < 0 || position >= mCount) {
            return new Point(-1, 0);
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
        return new Point(groupId, childId);
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
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return isHeaderItem(p);
    }

    @Override
    public boolean hasStickHeader(int position) {
        return mIsShowHeader;
    }

    @Override
    public int getHeaderViewTag(int position, RecyclerView parent) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return mOptions.getLayoutId(mOptions.getHeaderViewType(p.x, position));
    }

    @Override
    public View getHeaderView(int position, int headerViewTag, RecyclerView parent) {
        View itemView = mInflater.inflate(headerViewTag, parent, false);
        return itemView;
    }

    @Override
    public void setHeaderView(int position, int headerViewTag, RecyclerView parent, View headerView) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        H headerObj = mHeaderMap.get(p.x);
        HeaderRecycleViewHolder holder = (HeaderRecycleViewHolder) headerView.getTag();
        if (holder == null) {
            holder = new HeaderRecycleViewHolder(this, headerView);
            headerView.setTag(holder);
        }
        mOptions.setHeaderHolder(p.x, headerObj, holder);
    }

    @Override
    public boolean isBeenDecorated(int lastDecoratedPosition, int nowDecoratingPosition) {
        Point lastPoint = getGroupIdAndChildIdFromPosition(mEachGroupCountList, lastDecoratedPosition, mIsShowHeader);
        Point newPoint = getGroupIdAndChildIdFromPosition(mEachGroupCountList, nowDecoratingPosition, mIsShowHeader);
        if (lastPoint != null && newPoint != null) {
            return lastPoint.x == newPoint.x;
        } else {
            return false;
        }
    }

    /************
     * GridLayoutManage头部spanSizeLookup实现
     *************/
    @Override
    public boolean isSpecialItem(int position) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return isHeaderItem(p);
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
     * 调整界面显示的item数接口
     */
    public interface IAdjustCountOption {
        /**
         * 无效的item长度,使用此值时不会改变原来的item长度
         */
        public static final int NO_USE_ADJUST_COUNT = -1;

        /**
         * 设置调整后需要显示的itemCount.
         *
         * @param adjustCount count必须小于原始添加数据的量(否则多出的数据根本不可能找到填充的对象), 同时也必须大于1才有效.返回负数无效, 将使用原数据量.
         *                    默认值为负数{@link #NO_USE_ADJUST_COUNT}
         */
        public void setAdjustCount(int adjustCount);

        /**
         * 返回调整后需要显示的itemCount.
         *
         * @return 返回的count必须小于原始添加数据的量(否则多出的数据根本不可能找到填充的对象), 同时也必须大于1才有效.返回负数无效, 将使用原数据量.
         * 默认值为负数{@link #NO_USE_ADJUST_COUNT}
         */
        public int getAdjustCount();

        /**
         * 每一次当itemView被创建的时候此方法会被回调,建议在这个地方根据parentView进行计算并设置需要调整的itemCount
         *
         * @param parentView 此处为RecycleView
         * @param adapter    适配器
         */
        public void onCreateViewEverytime(ViewGroup parentView, HeaderRecycleAdapter adapter);
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
        public void setHeaderHolder(int groupId, H header, HeaderRecycleViewHolder holder);

        /**
         * 设置子项ViewHolder
         *
         * @param groupId  分组ID
         * @param childId  当前组子项ID
         * @param position 当前项位置(此位置为recycleView中的位置)
         * @param itemData
         * @param holder
         */
        public void setViewHolder(int groupId, int childId, int position, T itemData, HeaderRecycleViewHolder holder);
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
        public void onUpdateGroupList(List<List<T>> groupList);

        /**
         * 更新分组数据量(指将每个分组中的数据量取出按顺序存放到一个list中,直接通过此list可以获取每组的数据量并进行,不需要遍历分组数据去list.size()获取每个分组的数据量)
         *
         * @param eachGroupCountList
         */
        public void onUpdateEachGroupCountList(List<Integer> eachGroupCountList);

        /**
         * 更新item的总数,当显示header和不显示header时,此值会有变化
         *
         * @param count
         */
        public void OnUpdateCount(int count);
    }
}
