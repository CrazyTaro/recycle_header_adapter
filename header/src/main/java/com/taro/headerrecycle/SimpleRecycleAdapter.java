package com.taro.headerrecycle;

import android.content.Context;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by taro on 16/4/19.
 */
public class SimpleRecycleAdapter<T> extends HeaderRecycleAdapter<T> implements HeaderRecycleViewHolder.OnItemClickListener {
    private OnSimpleItemClickListener mItemClickListener = null;
    private List<T> mItemList = null;

    /**
     * 创建简单的不带header的adapter
     *
     * @param context
     * @param option   item加载配置接口
     * @param itemList item数据列表
     * @param listener item单击事件监听接口
     */
    public SimpleRecycleAdapter(Context context, IHeaderAdapterOption option, List<T> itemList, OnSimpleItemClickListener listener) {
        super(context, option, null, null, null);
        mItemClickListener = listener;
        mItemList = itemList;

        this.setIsShowHeader(false);
        this.setItemList(itemList);
    }

    /**
     * 创建简单的不带header的adapter
     *
     * @param context
     * @param option   item加载配置接口
     * @param itemList item数据列表
     */
    public SimpleRecycleAdapter(Context context, IHeaderAdapterOption option, List<T> itemList) {
        this(context, option, itemList, null);
    }

    /**
     * 获取item展示数据列表
     *
     * @return
     */
    public List<T> getItemList() {
        return mItemList;
    }

    /**
     * 设置item展示数据列表
     *
     * @param itemList
     */
    public void setItemList(List<T> itemList) {
        List<List<T>> groupList = this.getGroupList();
        if (groupList == null) {
            groupList = new LinkedList<List<T>>();
        }
        groupList.clear();
        groupList.add(itemList);
        this.setGroupList(groupList);
        mItemList = itemList;
    }

    /**
     * 设置item单击响应事件
     *
     * @param listener
     */
    public void setOnSimpleItemClickListener(OnSimpleItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * 重写了父类的设置头部接口,永远都不会有头部
     *
     * @param isShowHeader
     */
    @Override
    public void setIsShowHeader(boolean isShowHeader) {
        super.setIsShowHeader(false);
    }

    @Override
    public void onItemClick(int groupId, int childId, int position, boolean isHeader, View rootView, HeaderRecycleViewHolder holder) {
        if (mItemClickListener != null) {
            mItemClickListener.onSimpleItemClick(position, rootView, holder);
        }
    }

    /**
     * 简单版的item单击回调事件
     */
    public interface OnSimpleItemClickListener {
        /**
         * 简单的item(不带header)被单击回调事件
         *
         * @param position 位置
         * @param rootView item根View
         * @param holder   viewholder
         */
        public void onSimpleItemClick(int position, View rootView, HeaderRecycleViewHolder holder);
    }

    /**
     * 继承自 IHeaderAdapterOption 接口的简单Adapter配置抽象类
     */
    public static abstract class SimpleAdapterOption<T> implements IHeaderAdapterOption<T, Object> {

        @Override
        public int getItemViewType(int position, int groupId, int childId, boolean isHeaderItem, boolean isShowHeader) {
            return getViewType(position);
        }

        @Override
        public int getHeaderViewType(int groupId, int position) {
            return IHeaderAdapterOption.NO_HEADER_TYPE;
        }

        @Override
        public void setHeaderHolder(int groupId, Object header, HeaderRecycleViewHolder holder) {
            //简单Adapter不处理Header,所以此方法不需要使用到,空实现
        }

        @Override
        public void setViewHolder(int groupId, int childId, int position, T itemData, HeaderRecycleViewHolder holder) {
            setViewHolder(itemData, position, holder);
        }

        /**
         * 获取ViewType
         *
         * @param position 当前项的位置
         * @return
         */
        public abstract int getViewType(int position);

        /**
         * 设置view数据绑定
         *
         * @param itemData 绑定数据
         * @param position 当前项位置
         * @param holder
         */
        public abstract void setViewHolder(T itemData, int position, HeaderRecycleViewHolder holder);
    }
}
