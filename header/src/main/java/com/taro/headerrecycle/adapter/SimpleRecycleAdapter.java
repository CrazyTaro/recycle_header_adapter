package com.taro.headerrecycle.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by taro on 16/4/19.
 */
public class SimpleRecycleAdapter<T> extends HeaderRecycleAdapter {
    private List<T> mItemList = null;

    /**
     * 创建简单的不带header的adapter
     *
     * @param context
     */
    public SimpleRecycleAdapter(@NonNull Context context) {
        super(context);
        this.init(null);
    }

    /**
     * 创建简单的不带header的adapter
     *
     * @param context  布局加载
     * @param option   item加载配置接口
     * @param itemList item数据列表
     */
    public SimpleRecycleAdapter(@NonNull Context context, @Nullable IHeaderAdapterOption option, @Nullable List<T> itemList) {
        super(context, option, null, null);
        this.init(itemList);
    }

    /**
     * 创建简单的不带header的adapter
     *
     * @param inflater 布局加载
     * @param option   item加载配置接口
     * @param itemList item数据列表
     */
    public SimpleRecycleAdapter(@NonNull LayoutInflater inflater, @Nullable IHeaderAdapterOption
            option, @Nullable List<T> itemList) {
        super(inflater, option, null, null);
        this.init(itemList);
    }

    private void init(@Nullable List<T> itemList) {
        mItemList = itemList;
        this.setIsShowHeader(false);
        this.setItemList(itemList);
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
     * 重写了父类的设置头部接口,永远都不会有头部
     *
     * @param isShowHeader
     */
    @Override
    public void setIsShowHeader(boolean isShowHeader) {
        super.setIsShowHeader(false);
    }

    /**
     * 继承自 IHeaderAdapterOption 接口的简单Adapter配置抽象类
     */
    public static abstract class SimpleAdapterOption<T> implements IHeaderAdapterOption<T, Object>, IAdjustCountOption {
        private int mAdjustCount = -1;

        @Override
        public int getItemViewType(int position, int groupId, int childId, boolean isHeaderItem, boolean isShowHeader) {
            return getViewType(position);
        }

        @Override
        public int getHeaderViewType(int groupId, int position) {
            return NO_HEADER_TYPE;
        }

        @Override
        public void setHeaderHolder(int groupId, Object header, HeaderRecycleViewHolder holder) {
            //简单Adapter不处理Header,所以此方法不需要使用到,空实现
        }

        @Override
        public void setViewHolder(int groupId, int childId, int position, T itemData, HeaderRecycleViewHolder holder) {
            setViewHolder(itemData, position, holder);
        }

        @Override
        public int getAdjustCount() {
            return mAdjustCount;
        }

        @Override
        public void setAdjustCount(int adjustCount) {
            mAdjustCount = adjustCount;
        }

        @Override
        public void onCreateViewEverytime(ViewGroup parentView, HeaderRecycleAdapter adapter) {

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
