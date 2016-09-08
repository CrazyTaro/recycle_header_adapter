package com.taro.headerrecycle.helper;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by taro on 16/5/18.
 */
public class RecycleViewOnClickHelper implements RecyclerView.OnItemTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private GestureDetectorCompat mGestureCompat = null;
    private RecyclerView mRvTarget = null;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private OnDoubleClickListener mDoubleClickListener;
    private OnTouchListener mOnTouchListener;

    public RecycleViewOnClickHelper(Context context) {
        mGestureCompat = new GestureDetectorCompat(context, this);
        mGestureCompat.setOnDoubleTapListener(this);
    }

    public RecycleViewOnClickHelper(Context context, GestureDetector.OnGestureListener singleTapListener, GestureDetector.OnDoubleTapListener doubleTapListener) {
        mGestureCompat = new GestureDetectorCompat(context, singleTapListener);
        mGestureCompat.setOnDoubleTapListener(doubleTapListener);
    }

    public void attachToRecycleView(RecyclerView rv) {
        if (mRvTarget != rv) {
            detachToRecycleView();
        }
        mRvTarget = rv;
        if (mRvTarget != null) {
            mRvTarget.addOnItemTouchListener(this);
        }
    }

    public void detachToRecycleView() {
        if (mRvTarget != null) {
            mRvTarget.removeOnItemTouchListener(this);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public void setDoubleClickListener(OnDoubleClickListener listener) {
        mDoubleClickListener = listener;
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureCompat.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mRvTarget != null) {
            View child = mRvTarget.findChildViewUnder(e.getX(), e.getY());
            if (child == null) {
                return false;
            }
            RecyclerView.ViewHolder holder = mRvTarget.getChildViewHolder(child);
            int position = mRvTarget.getChildAdapterPosition(child);
            if (holder != null) {
                if (mOnTouchListener != null) {
                    mOnTouchListener.onItemClick(child, position, holder);
                } else if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(child, position, holder);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mRvTarget != null) {
            View child = mRvTarget.findChildViewUnder(e.getX(), e.getY());
            if (child == null) {
                return;
            }
            RecyclerView.ViewHolder holder = mRvTarget.getChildViewHolder(child);
            int position = mRvTarget.getChildAdapterPosition(child);
            if (child != null && holder != null) {
                if (mOnTouchListener != null) {
                    mOnTouchListener.onItemLongClick(child, position, holder);
                } else if (mItemLongClickListener != null) {
                    mItemLongClickListener.onItemLongClick(child, position, holder);
                }
            }
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mRvTarget != null) {
            View child = mRvTarget.findChildViewUnder(e.getX(), e.getY());
            if (child == null) {
                return false;
            }
            RecyclerView.ViewHolder holder = mRvTarget.getChildViewHolder(child);
            int position = mRvTarget.getChildAdapterPosition(child);
            if (child != null && holder != null) {
                if (mOnTouchListener != null) {
                    mOnTouchListener.onDoubleClickEvent(child, position, holder);
                } else if (mDoubleClickListener != null) {
                    mDoubleClickListener.onDoubleClickEvent(child, position, holder);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    public interface OnItemClickListener {
        public boolean onItemClick(View view, int position, RecyclerView.ViewHolder holder);
    }

    public interface OnItemLongClickListener {
        public void onItemLongClick(View view, int position, RecyclerView.ViewHolder holder);
    }

    public interface OnDoubleClickListener {
        public boolean onDoubleClickEvent(View view, int position, RecyclerView.ViewHolder holder);
    }

    public interface OnTouchListener extends OnItemClickListener, OnItemLongClickListener, OnDoubleClickListener {
    }
}
