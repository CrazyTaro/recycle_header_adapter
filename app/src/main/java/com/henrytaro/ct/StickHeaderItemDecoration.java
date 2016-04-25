package com.henrytaro.ct;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by taro on 16/4/25.
 */
public class StickHeaderItemDecoration extends RecyclerView.ItemDecoration {
    private StickerHeaderHandler mHeaderHandler = null;
    private SparseArrayCompat<View> mViewCacheMap = null;
    private Rect mOutRect = null;

    public StickHeaderItemDecoration(StickerHeaderHandler handler) {
        mHeaderHandler = handler;
        mViewCacheMap = new SparseArrayCompat<View>();
        mOutRect = new Rect();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        int position = 0;
        int firstItemViewTop = parent.getPaddingTop();
        View itemView = null;
        itemView = parent.getChildAt(0);
        position = parent.getChildAdapterPosition(itemView);

//        if (mHeaderHandler.isHeader(position) && (itemView.getBottom() <= firstItemViewTop)) {
//            int nextPosition = position + mHeaderHandler.getItemCountInOneLine();
//            position = state.getItemCount() > nextPosition ? nextPosition : position;
//        }

        if (mHeaderHandler.hasStickHeader(position)) {
            int headerID = mHeaderHandler.getHeaderViewID(position, parent);
            View headerView = mViewCacheMap.get(headerID);
            if (headerView == null) {
                headerView = mHeaderHandler.getHeaderView(position, headerID, parent);
                messureHeaderView(parent, headerView);
                mViewCacheMap.put(headerID, headerView);
            }

            mHeaderHandler.setHeaderView(position, headerID, parent, headerView);
            this.calculateViewDrawRect(mOutRect, parent, headerView);

            Point offset = calculateViewDrawRectInflunceByOtherView(mOutRect, parent, state);
            this.updateViewDrawRect(mOutRect, offset);
            c.clipRect(mOutRect);
            this.calculateParentStartDrawPoint(mOutRect, parent, offset);
            c.translate(mOutRect.left, mOutRect.top);
            headerView.draw(c);
        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }

    private Point calculateViewDrawRectInflunceByOtherView(Rect normalRect, RecyclerView parent, RecyclerView.State state) {
        int childCount = state.getItemCount();
        if (childCount <= 1) {
            return new Point(0, 0);
        }

        int offsetY = 0;
        View itemView = null;
        int position = 0;
        int firstItemViewTop = parent.getPaddingTop();
        for (int i = 1; i < childCount; i++) {
            itemView = parent.getChildAt(i);
            position = parent.getChildAdapterPosition(itemView);
            if (mHeaderHandler.isHeader(position)) {
                if (itemView.getTop() >= normalRect.bottom || itemView.getTop() <= firstItemViewTop) {
                    break;
                } else {
                    offsetY = normalRect.bottom - itemView.getTop();
                    break;
                }
            } else {
                continue;
            }
        }
        return new Point(0, offsetY * -1);
    }

    private void updateViewDrawRect(Rect outRect, Point offset) {
        int width = outRect.width();
        int height = outRect.height();
        width += offset.x;
        height += offset.y;
        int newRight = outRect.left + width;
        int newBottom = outRect.top + height;
        outRect.set(outRect.left, outRect.top, newRight, newBottom);
    }

    private void calculateParentStartDrawPoint(Rect outRect, RecyclerView parent, Point offset) {
        int drawLeft = parent.getLeft() + parent.getPaddingLeft();
        int drawTop = parent.getTop() + parent.getPaddingTop();
        outRect.set(drawLeft, drawTop, 0, 0);
        outRect.offset(offset.x, offset.y);
    }

    private void calculateViewDrawRect(Rect outRect, RecyclerView parent, View headerView) {
        int drawLeft = parent.getPaddingLeft();
        int drawTop = parent.getPaddingTop();
        int drawRight = drawLeft + (parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight());
        int drawBottom = headerView.getHeight() + drawTop;
        ViewGroup.LayoutParams params = headerView.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams = null;
        if (params instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) params;
            drawLeft += marginParams.leftMargin;
            drawTop += marginParams.topMargin;
            drawRight -= marginParams.rightMargin;
        }
        outRect.set(drawLeft, drawTop, drawRight, drawBottom);
    }

    public void messureHeaderView(RecyclerView parent, View headerView) {
        int widthSpec;
        int heightSpec;

        widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);
//            widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED);
//            heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY);

        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), headerView.getLayoutParams().width);
        int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.getPaddingTop() + parent.getPaddingBottom(), headerView.getLayoutParams().height);
        headerView.measure(childWidth, childHeight);
        headerView.layout(0, 0, headerView.getMeasuredWidth(), headerView.getMeasuredHeight());
    }


    public interface StickerHeaderHandler {
        public boolean isHeader(int position);

        public boolean hasStickHeader(int position);

        public int getItemCountInOneLine();

        public int getHeaderViewID(int position, RecyclerView parent);

        public View getHeaderView(int position, int layoutId, RecyclerView parent);

        public void setHeaderView(int position, int layoutId, RecyclerView parent, View headerView);
    }
}
