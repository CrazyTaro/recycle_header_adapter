package com.henrytaro.ct;

import android.content.Context;

import java.util.*;

/**
 * Created by taro on 16/4/19.
 */
public class SimpleRecycleAdapter<T> extends HeaderRecycleAdapter {
    private SimpleRecycleViewHolder.OnItemClickListener mItemClickListener = null;
    private List<T> mItemList = null;

    public SimpleRecycleAdapter(Context context, IHeaderAdapterOption option, List<T> itemList, SimpleRecycleViewHolder.OnItemClickListener listener) {
        super(context, option, null, null, null);
        mItemClickListener = listener;
        mItemList = itemList;

        this.setIsShowHeader(false);
        this.setItemList(itemList);
    }

    public List<T> getItemList() {
        return mItemList;
    }

    public void setItemList(List<T> itemList) {
        List<List> groupList = this.getGroupList();
        if (groupList == null) {
            groupList = new LinkedList<List>();
        }
        groupList.clear();
        groupList.add(itemList);
        this.setGroupList(groupList);
        mItemList = itemList;
    }

    @Override
    public void setIsShowHeader(boolean isShowHeader) {
        super.setIsShowHeader(false);
    }

    /**
     * 继承自 IHeaderAdapterOption 接口的简单Adapter配置抽象类
     */
    public static abstract class SimpleAdapterOption implements IHeaderAdapterOption {

        @Override
        public int getItemViewType(int position, boolean isShowHeader) {
            return getViewType(position);
        }

        @Override
        public int getHeaderViewType(int groupId) {
            return IHeaderAdapterOption.NO_HEADER_TYPE;
        }

        @Override
        public void setHeaderHolder(int groupId, Object header, HeaderRecycleViewHolder holder) {
            //简单Adapter不处理Header,所以此方法不需要使用到,空实现
        }

        @Override
        public void setViewHolder(int groupId, int childId, int position, Object itemData, HeaderRecycleViewHolder holder) {
            setViewHolder(itemData, position, (SimpleRecycleViewHolder) holder);
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
        public abstract void setViewHolder(Object itemData, int position, SimpleRecycleViewHolder holder);
    }
}
