package com.taro.headerrecycle.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
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
    public SimpleRecycleAdapter(@NonNull Context context, @Nullable IHeaderAdapterOption<T, ? extends Object> option, @Nullable List<T> itemList) {
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
    public SimpleRecycleAdapter(@NonNull LayoutInflater inflater, @Nullable IHeaderAdapterOption<T, ? extends Object> option, @Nullable List<T> itemList) {
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

        @Deprecated
        @Override
        public int getHeaderViewType(int groupId, int position) {
            return NO_HEADER_TYPE;
        }

        @Deprecated
        @Override
        public void setHeaderHolder(int groupId, Object header, @NonNull HeaderRecycleViewHolder holder) {
            //简单Adapter不处理Header,所以此方法不需要使用到,空实现
        }

        @Deprecated
        @Override
        public void setViewHolder(int groupId, int childId, int position, T itemData, @NonNull HeaderRecycleViewHolder holder) {
            setViewHolder(itemData, position, holder);
        }

        @Override
        public int getAdjustCount() {
            return mAdjustCount;
        }

        @Override
        public void setAdjustCount(int adjustCount) {
            setInnerAdjustCount(adjustCount);
        }

        @Override
        public void onCreateViewEverytime(@NonNull View itemView, @NonNull ViewGroup parentView, @NonNull HeaderRecycleAdapter adapter, int viewType) {

        }
        
        /**
         * 内部的实际设置调整item的方法;存在这个方法的原因是方便{@link #setAdjustCount(int)}方法可以被子类适当地进行一些重写操作而不会直接影响到设置adjustCount;<br>
         * 子类在进行了计算后对实际的调整值的设置可以使用此方法进行设置,或者使用{@code super.setAdjustCount()}
         *
         * @param adjustCount
         */
        void setInnerAdjustCount(int adjustCount) {
            mAdjustCount = adjustCount;
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
        public abstract void setViewHolder(T itemData, int position, @NonNull HeaderRecycleViewHolder holder);
    }
}
