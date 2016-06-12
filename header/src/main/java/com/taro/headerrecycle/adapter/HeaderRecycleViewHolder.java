package com.taro.headerrecycle.adapter;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by taro on 16/4/19.
 */
public class HeaderRecycleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    /**
     * 根布局的ID,使用0
     */
    public static final int ROOT_VIEW_ID = 0;

    private int mGroupId = -1;
    private int mChildId = -1;
    private View mRootView;
    private OnItemClickListener mRootViewClickListener = null;
    //当前项是否可以响应单击事件
    private boolean mIsClickEnabled = true;
    private HeaderRecycleAdapter mParentAdapter = null;
    //View缓存
    private ArrayMap<Integer, View> mViewHolder = null;

    private Map<Integer, OnItemClickListener> mItemClickMap = null;

    /**
     * 带adapter的holder,推荐使用此方法(很常会用到adapter)
     *
     * @param adapter
     * @param itemView holder的rootView
     */
    public HeaderRecycleViewHolder(HeaderRecycleAdapter adapter, View itemView) {
        super(itemView);
        mRootView = itemView;
        mParentAdapter = adapter;
        mViewHolder = new ArrayMap<Integer, View>();
    }

    /**
     * 设置holder的GroupID及当前项在在该组的位置ID
     *
     * @param groupId 分组ID,从0开始
     * @param childId 组内元素ID,从0开始.当childID为负数时,当前项为该组header
     */
    public void setGroupIdAndChildId(int groupId, int childId) {
        mGroupId = groupId;
        mChildId = childId;
    }

    /**
     * 获取分组ID
     *
     * @return
     */
    public int getGroupId() {
        return mGroupId;
    }

    /**
     * 获取当前项在组内的位置ID,当此值为负数时,当前项为该组的Header
     *
     * @return
     */
    public int getChildId() {
        return mChildId;
    }

    /**
     * 获取当前项是否为该组的Header,实际判断方式为当childID为负数时,当前项即为header
     *
     * @return
     */
    public boolean isHeaderItem() {
        return mChildId < 0;
    }

    /**
     * 从当前item中查找指定ID的View,view仅会查找一次并缓存其引用
     *
     * @param viewId 查找viewID
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViewHolder.get(viewId);
        if (view == null) {
            view = mRootView.findViewById(viewId);
            mViewHolder.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 清除View的缓存
     */
    public void clearViewCache() {
        mViewHolder.clear();
    }

    /**
     * 为textView设置文本
     *
     * @param viewId
     * @param text
     * @return
     */
    public HeaderRecycleViewHolder setTextInTextView(int viewId, String text) {
        TextView tv = this.getView(viewId);
        tv.setText(text);
        return this;
    }

    /**
     * 为imageView设置图片资源
     *
     * @param viewId
     * @param imgResID
     * @return
     */
    public HeaderRecycleViewHolder setImageInImageView(int viewId, int imgResID) {
        ImageView iv = this.getView(viewId);
        iv.setImageResource(imgResID);
        return this;
    }

    /**
     * 为View设置背景色
     *
     * @param viewId
     * @param color  颜色值,不是颜色的资源ID
     * @return
     */
    public HeaderRecycleViewHolder setBackgroundColorInView(int viewId, int color) {
        View v = this.getView(viewId);
        v.setBackgroundColor(color);
        return this;
    }

    /**
     * 为View设置背景资源
     *
     * @param viewId
     * @param resId
     * @return
     */
    public HeaderRecycleViewHolder setBackgroundResourceInView(int viewId, int resId) {
        View v = this.getView(viewId);
        v.setBackgroundResource(resId);
        return this;
    }

    /**
     * 注册rootView的单击响应事件,注册了rootView的响应事件时,将不会响应其子控件的单击事件
     *
     * @param listener
     */
    public void registerRootViewItemClickListener(OnItemClickListener listener) {
        mRootViewClickListener = listener;
        mRootView.setOnClickListener(this);
    }

    /**
     * 清除rootView的单击响应事件
     */
    public void unregisterRootViewItemClickListener() {
        mRootViewClickListener = null;
        mRootView.setOnClickListener(null);
    }

    /**
     * 注册view的单击事件,此处不仅限于对整个item进行注册,可以是item中某个view,
     * 也可以是rootView,但rootView不推荐在此处注册,通过{@link #registerRootViewItemClickListener(OnItemClickListener)}注册rootView的单击响应事件
     *
     * @param viewId   需要注册的viewId
     * @param listener 单击响应事件
     * @return
     */
    public boolean registerViewOnClickListener(int viewId, OnItemClickListener listener) {
        if (mItemClickMap == null) {
            mItemClickMap = new ArrayMap<Integer, OnItemClickListener>(15);
        }
        View view = this.getView(viewId);
        if (view != null) {
            mItemClickMap.put(viewId, listener);
            view.setOnClickListener(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 注册简单的view单击事件,此处不仅限于对整个item进行注册,可以是item中某个view
     *
     * @param viewId   需要注册的viewId
     * @param listener 单击响应事件
     * @return
     */
    public boolean registerSimpleViewOnClickListener(int viewId, SimpleItemClickListener listener) {
        return this.registerViewOnClickListener(viewId, listener);
    }

    /**
     * 清除指定viewId注册的单击事件
     *
     * @param viewId
     * @return 成功清除返回true, 若无该viewId注册事件返回false
     */
    public boolean unregisterViewOnClickListener(int viewId) {
        if (mItemClickMap == null) {
            return true;
        } else {
            View view = this.getView(viewId);
            if (view != null) {
                view.setOnClickListener(null);
            }
            OnItemClickListener listener = mItemClickMap.remove(viewId);
            return listener != null;
        }
    }

    /**
     * 清除所有单击注册监听事件
     */
    public void unregisterAllViewOnClickListener() {
        if (mItemClickMap != null) {
            for (int viewId : mItemClickMap.keySet()) {
                View view = this.getView(viewId);
                if (view != null) {
                    view.setOnClickListener(null);
                }
            }
            mItemClickMap.clear();
        }
        mRootView.setOnClickListener(null);
        mRootViewClickListener = null;
    }

    /**
     * 获取根布局
     *
     * @return
     */
    public View getRootView() {
        return mRootView;
    }

    /**
     * 获取父adapter
     *
     * @return
     */
    public HeaderRecycleAdapter getAdatper() {
        return mParentAdapter;
    }

    /**
     * 设置当前item是否可响应单击
     *
     * @param isEnabled
     */
    public void setIsItemClickable(boolean isEnabled) {
        mIsClickEnabled = isEnabled;
    }

    /**
     * 获取当前item是否可响应单击
     *
     * @return
     */
    public boolean getIsItemClickable() {
        return mIsClickEnabled;
    }

    /**
     * 处理当前item的单击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        //允许单击事件
        if (mIsClickEnabled) {
            //如果存在rootView的监听事件,只响应rootView的监听事件
            if (mRootViewClickListener != null) {
                mRootViewClickListener.onItemClick(mGroupId, mChildId, getAdapterPosition(), ROOT_VIEW_ID, this.isHeaderItem(), mRootView, this);
            } else {
                //否则根据注册的view尝试响应监听事件
                int id = v.getId();
                OnItemClickListener listener = mItemClickMap == null ? null : mItemClickMap.get(id);
                if (listener != null) {
                    listener.onItemClick(mGroupId, mChildId, getAdapterPosition(), id, this.isHeaderItem(), mRootView, this);
                }
            }
        } else {
            Log.i("view holder", "itemClickListener监听事件不存在或者该item不可响应点击事件");
        }
    }

    /**
     * 简单的ItemClick事件
     */
    public static abstract class SimpleItemClickListener implements OnItemClickListener {
        /**
         * itemClick事件
         *
         * @param groupId  当前item所在分组,分组ID从0开始
         * @param childId  当前item在所有分组中的ID,从0开始,当此值为-1时,当前为该分组的头部
         * @param position item位置
         * @param viewId   viewId,此参数值为注册监听事件时使用的viewId
         * @param holder
         */
        public abstract void onItemClick(int groupId, int childId, int position, int viewId, HeaderRecycleViewHolder holder);

        @Override
        public void onItemClick(int groupId, int childId, int position, int viewId, boolean isHeader, View rootView, HeaderRecycleViewHolder holder) {
            this.onItemClick(groupId, childId, position, viewId, holder);
        }
    }

    /**
     * itemClick事件
     */
    public interface OnItemClickListener {
        /**
         * 带header的item单击事件
         *
         * @param groupId  当前item所在分组,分组ID从0开始
         * @param childId  当前item在所有分组中的ID,从0开始,当此值为-1时,当前为该分组的头部
         * @param position 当前item所有分组的位置(header也会占用一个位置,请注意)
         * @param viewId   当前响应的单击事件view的ID,若为rootView,则该值为{@link #ROOT_VIEW_ID}
         * @param isHeader 当前item是否为header
         * @param rootView 当前item的rootView
         * @param holder
         */
        public void onItemClick(int groupId, int childId, int position, int viewId, boolean isHeader, View rootView, HeaderRecycleViewHolder holder);
    }
}
