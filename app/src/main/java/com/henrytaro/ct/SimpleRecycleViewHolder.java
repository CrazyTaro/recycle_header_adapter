package com.henrytaro.ct;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by taro on 16/4/19.
 */
public class SimpleRecycleViewHolder extends HeaderRecycleViewHolder {
    /**
     * 带adapter的holder,推荐使用此方法(很常会用到adapter)
     *
     * @param adapter
     * @param itemView holder的rootView
     * @param listener 监听事件
     */
    public SimpleRecycleViewHolder(RecyclerView.Adapter adapter, View itemView, SimpleRecycleViewHolder.OnItemClickListener listener) {
        super(adapter, itemView, new InternalItemClickListener(listener));
    }

    /**
     * 内部类,用于包装回调事件
     */
    private static class InternalItemClickListener implements HeaderRecycleViewHolder.OnItemClickListener {
        private SimpleRecycleViewHolder.OnItemClickListener mItemClickListener = null;

        public InternalItemClickListener(SimpleRecycleViewHolder.OnItemClickListener listener) {
            mItemClickListener = listener;
        }

        @Override
        public void onItemClick(int groupId, int childId, int position, boolean isHeader, View rootView, HeaderRecycleViewHolder holder) {
            if (mItemClickListener != null) {
                //单击事件回调,取消部分不会用到的参数传递
                mItemClickListener.onItemClickListener(position, rootView, (SimpleRecycleViewHolder) holder);
            }
        }
    }

    /**
     * 简单的单击事件回调
     */
    public interface OnItemClickListener {
        /**
         * 单击事件回调
         *
         * @param position 当前项位置
         * @param rootView 单击view
         * @param holder
         */
        public void onItemClickListener(int position, View rootView, SimpleRecycleViewHolder holder);
    }
}
