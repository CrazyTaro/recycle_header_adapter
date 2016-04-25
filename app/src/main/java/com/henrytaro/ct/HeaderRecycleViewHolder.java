package com.henrytaro.ct;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by taro on 16/4/19.
 */
public class HeaderRecycleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private OnItemClickListener mItemClickListener;
    private int mGroupId = -1;
    private int mChildId = -1;
    private View mRootView;
    //当前项是否可以响应单击事件
    private boolean mIsClickEnabled = true;
    private RecyclerView.Adapter mParentAdapter = null;
    //View缓存
    private ArrayMap<Integer, View> mViewHolder = null;

    /**
     * 带adapter的holder,推荐使用此方法(很常会用到adapter)
     *
     * @param adapter
     * @param itemView holder的rootView
     * @param listener 监听事件
     */
    public HeaderRecycleViewHolder(RecyclerView.Adapter adapter, View itemView, OnItemClickListener listener) {
        super(itemView);
        mRootView = itemView;
        mItemClickListener = listener;
        mParentAdapter = adapter;
        mViewHolder = new ArrayMap<Integer, View>();

        itemView.setOnClickListener(this);
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
     * 设置Holder的监听事件
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * 获取监听事件
     *
     * @return
     */
    public OnItemClickListener getItemClickListener() {
        return mItemClickListener;
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
    public RecyclerView.Adapter getAdatper() {
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
        if (mItemClickListener != null && mIsClickEnabled) {
            mItemClickListener.onItemClick(mGroupId, mChildId, getAdapterPosition(), this.isHeaderItem(), mRootView, this);
        } else {
            Log.i("view holder", "itemClickListener监听事件不存在或者该item不可响应点击事件");
        }
    }

    /**
     * itemClick事件
     */
    public interface OnItemClickListener {
        public void onItemClick(int groupId, int childId, int position, boolean isHeader, View rootView, HeaderRecycleViewHolder holder);
    }
}
