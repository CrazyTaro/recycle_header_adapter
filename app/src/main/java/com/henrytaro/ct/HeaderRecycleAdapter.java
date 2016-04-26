package com.henrytaro.ct;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.GridLayoutManager;
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
public class HeaderRecycleAdapter extends RecyclerView.Adapter<HeaderRecycleViewHolder> implements StickHeaderItemDecoration.StickerHeaderHandler {
    //分组数据列表
    private List<List> mGroupList;
    private List<Integer> mEachGroupCountList;
    //头部数据
    private Map<Integer, ? extends Object> mHeaderMap;
    private Context mApplicationContext;
    private IHeaderAdapterOption mOptions = null;
    private HeaderRecycleViewHolder.OnItemClickListener mItemClickListener;
    private HeaderSpanSizeLookup mLookup = null;
    private RecyclerView.LayoutManager mLayoutManager = null;

    private boolean mIsShowHeader = true;
    private boolean mIsGridLayout = false;
    private int mCount = 0;

    /**
     * 创建可以显示分组头部的recycleAdapter,其中Context与option不可为空
     *
     * @param context
     * @param option    分组头部需要的配置接口
     * @param groupList 分组数据
     * @param headerMap 分组头部匹配的Map
     * @param listener  每项单击的回调事件
     */
    public HeaderRecycleAdapter(Context context, IHeaderAdapterOption option, List<List> groupList, Map<Integer, ? extends Object> headerMap, HeaderRecycleViewHolder.OnItemClickListener listener) {
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
     * 设置使用的LayoutManager,当且仅当需要使用分组的GridLayoutManager时才使用此方法,否则不建议使用.
     *
     * @param isGridLayout  是否使用GridLayoutManger
     * @param layoutManager 用于设置的GridLayoutManager
     * @return 返回参数layoutManager, 当layoutManager为null时, 将不会有任何设置
     */
    public GridLayoutManager setUsingLayoutManager(boolean isGridLayout, GridLayoutManager layoutManager) {
        this.mIsGridLayout = isGridLayout && layoutManager != null;
        if (mIsGridLayout) {
            //创建或者设置分组头部占用空格的计算类
            if (mLookup == null) {
                mLookup = new HeaderSpanSizeLookup(layoutManager.getSpanCount(), mEachGroupCountList, mIsShowHeader);
            } else {
                mLookup.setParams(layoutManager.getSpanCount(), mEachGroupCountList);
                mLookup.setIsShowHeader(mIsShowHeader);
            }
            layoutManager.setSpanSizeLookup(mLookup);
        }
        return layoutManager;
    }

    /**
     * 创建一个用于显示分组头部的GridLayoutManager
     *
     * @param context
     * @param spanCount   列数
     * @param orientation 方向,默认为竖向.{@link GridLayoutManager#VERTICAL}
     * @return
     */
    public GridLayoutManager createHeaderGridLayoutManager(Context context, int spanCount, int orientation) {
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount, orientation, false);
        this.setUsingLayoutManager(true, layoutManager);
        return layoutManager;
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
    public List<List> getGroupList() {
        return mGroupList;
    }

    /**
     * 设置分组灵气
     *
     * @param groupList
     */
    public void setGroupList(List<List> groupList) {
        mGroupList = groupList;
        mEachGroupCountList = getEachGroupCountList(groupList);
        //更新数据总数,需要计算header数据在内
        updateCount(mEachGroupCountList, mIsShowHeader);
        setUsingLayoutManager(mIsGridLayout, null);
    }

    /**
     * 设置是否显示header
     *
     * @param isShowHeader
     */
    public void setIsShowHeader(boolean isShowHeader) {
        mIsShowHeader = isShowHeader;
        updateCount(mEachGroupCountList, isShowHeader);
        setUsingLayoutManager(mIsGridLayout, null);
    }

    /**
     * 设置spanCount
     *
     * @param layoutManager
     * @param spanCount
     */
    public void setSpanCount(GridLayoutManager layoutManager, int spanCount) {
        if (layoutManager != null) {
            layoutManager.setSpanCount(spanCount);
        }
        setUsingLayoutManager(mIsGridLayout, layoutManager);
    }

    /**
     * 设置保存的LayoutManager
     *
     * @param manager
     */
    public void setHoldLayoutManager(RecyclerView.LayoutManager manager) {
        this.mLayoutManager = manager;
    }

    /**
     * 获取保存的LayoutManager
     *
     * @param <T> 此方法会将Manager自动转换成需要的类型,但是必须确保使用的类型是正确的,否则会造成转换异常
     * @return
     */
    public <T extends RecyclerView.LayoutManager> T getHoldLayoutManager() {
        return (T) this.mLayoutManager;
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
        //清除上一个Holder的View缓存
        holder.clearViewCache();
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
    private boolean isHeaderItem(Point p) {
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
    private Point getGroupIdAndChildIdFromPosition(List<Integer> eachGroupCountList, int position, boolean isShowGroup) {
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
    private void updateCount(List<Integer> eachGroupCountList, boolean isShowGroup) {
        mCount = 0;
        int headerSize = 1;
        int groupEachLine = isShowGroup ? headerSize : 0;
        if (eachGroupCountList != null) {
            for (int groupCount : eachGroupCountList) {
                //若显示Header将Header添加到总数据量中,每一个Header占用一行
                mCount += groupCount + groupEachLine;
            }
        }
    }

    /**
     * 根据分数数据列表获取分组数据量的列表
     *
     * @param groupList 分组数据列表
     * @return 返回分组数据量列表, 每一项都对应分组列表中的每一组的数据量
     */
    public static List<Integer> getEachGroupCountList(List<List> groupList) {
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
     * 计算头部占用空格的类
     */
    public static class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        private List<Integer> mEachGroupCountList = null;
        private int mSpanCount = 0;
        private boolean mIsShowHeader = true;

        /**
         * @param spanCount          网格列数
         * @param eachGroupCountList 分组的数据量列表
         * @param isShowHeader       是否显示头部
         */
        public HeaderSpanSizeLookup(int spanCount, List<Integer> eachGroupCountList, boolean isShowHeader) {
            this.mSpanCount = spanCount;
            this.mIsShowHeader = isShowHeader;
            this.mEachGroupCountList = eachGroupCountList;
        }

        /**
         * 设置参数,网格列数及分组数据量列表,此二值必须与实际显示的设置保持一致
         *
         * @param spanCount
         * @param eachGroupCountList
         */
        public void setParams(int spanCount, List<Integer> eachGroupCountList) {
            this.mSpanCount = spanCount;
            this.mEachGroupCountList = eachGroupCountList;
        }

        /**
         * 是否显示头部
         *
         * @param isShowHeader
         */
        public void setIsShowHeader(boolean isShowHeader) {
            this.mIsShowHeader = isShowHeader;
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
            if (mEachGroupCountList != null) {
                int groupEachLine = mIsShowHeader ? 1 : 0;
                int childId = 0;
                for (int groupCount : mEachGroupCountList) {
                    childId = groupCount;
                    position = position - groupEachLine - childId;
                    //直到计算到当前position数据为负说明当前位置在此分组中
                    if (position < 0) {
                        childId += position;
                        //至少返回1列或者是整行的所有列数
                        return childId < 0 ? (groupEachLine == 0 ? 1 : mSpanCount) : 1;
                    }
                }
            }
            return 1;
        }
    }


    /**
     * 带头部的adapter配置接口
     */
    public interface IHeaderAdapterOption {
        /**
         * 不存在headerView类型,{@value Integer#MIN_VALUE}
         */
        public static final int NO_HEADER_TYPE = Integer.MIN_VALUE;

        /**
         * 获取headerView的类型
         *
         * @param groupId 分组类型
         * @return
         */
        public int getHeaderViewType(int groupId, int position);

        /**
         * 获取普通View的类型
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
        public void setHeaderHolder(int groupId, Object header, HeaderRecycleViewHolder holder);

        /**
         * 设置子项ViewHolder
         *
         * @param groupId  分组ID
         * @param childId  当前组子项ID
         * @param position 当前项位置(此位置为recycleView中的位置)
         * @param itemData
         * @param holder
         */
        public void setViewHolder(int groupId, int childId, int position, Object itemData, HeaderRecycleViewHolder holder);
    }

}
