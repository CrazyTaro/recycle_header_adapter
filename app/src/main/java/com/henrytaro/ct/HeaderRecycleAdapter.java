package com.henrytaro.ct;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/4/19.
 */
public class HeaderRecycleAdapter<T> extends RecyclerView.Adapter<HeaderRecycleViewHolder> implements StickHeaderItemDecoration.StickerHeaderHandler {
    //分组数据列表
    protected List<List<T>> mGroupList;
    protected List<Integer> mEachGroupCountList;
    //头部数据
    protected Map<Integer, ? extends Object> mHeaderMap;
    protected Context mApplicationContext;
    protected IHeaderAdapterOption mOptions = null;
    protected HeaderRecycleViewHolder.OnItemClickListener mItemClickListener;
    private OnHeaderParamsUpdateListener mParamsUpdateListener = null;

    protected boolean mIsShowHeader = true;
    protected int mCount = 0;


    public HeaderRecycleAdapter(Context context, IHeaderAdapterOption option, List<List<T>> groupList, Map<Integer, ? extends Object> headerMap) {
        this(context, option, groupList, headerMap, null);
    }

    /**
     * 创建可以显示分组头部的recycleAdapter,其中Context与option不可为空
     *
     * @param context
     * @param option    分组头部需要的配置接口
     * @param groupList 分组数据
     * @param headerMap 分组头部匹配的Map
     * @param listener  每项单击的回调事件,此处也可以不进行设置,在绑定view的时候再设置onClick事件
     */
    public HeaderRecycleAdapter(Context context, IHeaderAdapterOption option, List<List<T>> groupList, Map<Integer, ? extends Object> headerMap, HeaderRecycleViewHolder.OnItemClickListener listener) {
        if (context == null || option == null) {
            throw new RuntimeException("context and option can not be null");
        }
        mApplicationContext = context.getApplicationContext();
        mOptions = option;
        mHeaderMap = headerMap;
        mItemClickListener = listener;
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
            mParamsUpdateListener.updateGroupList(groupList);
            mParamsUpdateListener.updateEachGroupCountList(mEachGroupCountList);
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
                mParamsUpdateListener.updateIsShowHeader(isShowHeader);
            }
        }
    }


    @Override
    public void onViewRecycled(HeaderRecycleViewHolder holder) {
        super.onViewRecycled(holder);
        holder.clearViewCache();
    }

    @Override
    public int getItemViewType(int position) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return mOptions.getItemViewType(position, p.x, p.y, isHeaderItem(p), mIsShowHeader);
    }

    @Override
    public HeaderRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = mOptions.getLayoutId(viewType);
        View rootView = LayoutInflater.from(mApplicationContext).inflate(layoutId, parent, false);
        return new HeaderRecycleViewHolder(this, rootView, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(HeaderRecycleViewHolder holder, int position) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        //设置当前项的组ID,与组内数据ID
        holder.setGroupIdAndChildId(p.x, p.y);
        Object holderData = null;
        if (isHeaderItem(p)) {
            //设置头部数据显示
            holderData = mHeaderMap.get(p.x);
            mOptions.setHeaderHolder(p.x, holderData, holder);
        } else {
            //设置普通Item数据显示
            holderData = mGroupList.get(p.x).get(p.y);
            mOptions.setViewHolder(p.x, p.y, position, holderData, holder);
        }
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    /**
     * 判断当前item是否为headerView
     *
     * @param p item相关的分组及子元素信息
     * @return
     */
    protected boolean isHeaderItem(Point p) {
        if (p != null && p.x >= 0 && p.y == -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据position计算分组的ID及当前项所有的组内ID
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
            mParamsUpdateListener.updateCount(mCount);
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

    @Override
    public boolean isHeader(int position) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return isHeaderItem(p);
    }

    @Override
    public boolean hasStickHeader(int position) {
        return mIsShowHeader;
    }

    @Override
    public int getHeaderViewID(int position, RecyclerView parent) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        return mOptions.getLayoutId(mOptions.getHeaderViewType(p.x, position));
    }

    @Override
    public View getHeaderView(int position, int layoutId, RecyclerView parent) {
        View itemView = LayoutInflater.from(mApplicationContext).inflate(layoutId, parent, false);
        return itemView;
    }

    @Override
    public void setHeaderView(int position, int layoutId, RecyclerView parent, View headerView) {
        Point p = getGroupIdAndChildIdFromPosition(mEachGroupCountList, position, mIsShowHeader);
        Object headerObj = mHeaderMap.get(p.x);
        HeaderRecycleViewHolder holder = (HeaderRecycleViewHolder) headerView.getTag();
        if (holder == null) {
            holder = new HeaderRecycleViewHolder(this, headerView, null);
            headerView.setTag(holder);
        }
        mOptions.setHeaderHolder(p.x, headerObj, holder);
    }


    /**
     * 带头部的adapter配置接口
     * 其中参数T为每个item对应的设置的数据类型
     * 参数H为每个header对应设置的数据类型
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
        public void updateIsShowHeader(boolean isShowHeader);

        /**
         * 更新分组数据
         *
         * @param groupList
         */
        public void updateGroupList(List<List<T>> groupList);

        /**
         * 更新分组数据量(指将每个分组中的数据量取出按顺序存放到一个list中,直接通过此list可以获取每组的数据量并进行,不需要遍历分组数据去list.size()获取每个分组的数据量)
         *
         * @param eachGroupCountList
         */
        public void updateEachGroupCountList(List<Integer> eachGroupCountList);

        /**
         * 更新item的总数,当显示header和不显示header时,此值会有变化
         *
         * @param count
         */
        public void updateCount(int count);
    }
}
