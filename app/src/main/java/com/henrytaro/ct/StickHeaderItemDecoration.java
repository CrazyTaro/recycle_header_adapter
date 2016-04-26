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
 * 参考来自: https://github.com/timehop/sticky-headers-recyclerview
 */
public class StickHeaderItemDecoration extends RecyclerView.ItemDecoration {
    //固定头部操作接口
    private StickerHeaderHandler mHeaderHandler = null;
    //缓存View
    private SparseArrayCompat<View> mViewCacheMap = null;
    //全局得胜的rect
    private Rect mOutRect = null;

    /**
     * 创建固定头部的itemDecoration
     *
     * @param handler 固定头部操作接口
     */
    public StickHeaderItemDecoration(StickerHeaderHandler handler) {
        mHeaderHandler = handler;
        mViewCacheMap = new SparseArrayCompat<View>();
        mOutRect = new Rect();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        //在绘制完整个recycleView之后绘制,绘制的结果将显示在所有Item上面
        super.onDrawOver(c, parent, state);

        int position = 0;
        //recycleView中的item可见的top,即除去recycleView本身的padding的最高点位置
        int topOfViewCanSeen = parent.getPaddingTop();
        View itemView = null;
        //获取第一项View,注意此处的View不一定是第一项可见的View,可能是被缓存了的View(不可见)
        itemView = parent.getChildAt(0);
        //获取对应View的位置
        position = parent.getChildAdapterPosition(itemView);

        //查找从0开始的第一项HeaderView
        View firstHeaderView = this.searchFirstHeaderView(0, state.getItemCount(), parent);
        //当第一项HeaderView处于最高点可见的过渡期时(topOfViewCanSeen在view.top与view.bottom之间)
        if (firstHeaderView != null && firstHeaderView.getTop() <= topOfViewCanSeen && firstHeaderView.getBottom() > topOfViewCanSeen) {
            //说明此时的第一项headerView还没有被移出界面,将此headerView的位置作为下面处理显示的headerView
            //即绘制其副本到界面上作为stick header view
            position = parent.getChildAdapterPosition(firstHeaderView);
        }

        //当前位置的item需要显示headerView
        if (mHeaderHandler.hasStickHeader(position)) {
            //获取headerView的layoutID
            int headerID = mHeaderHandler.getHeaderViewID(position, parent);
            //获取layoutID对应的缓存headerView
            View headerView = mViewCacheMap.get(headerID);
            //不存在缓存view时加载headerView
            if (headerView == null) {
                headerView = mHeaderHandler.getHeaderView(position, headerID, parent);
                measureHeaderView(parent, headerView);
                //保存到缓存中
                mViewCacheMap.put(headerID, headerView);
            }
            //设置headerView的数据显示
            mHeaderHandler.setHeaderView(position, headerID, parent, headerView);
            //计算当前headerView需要绘制的区域
            this.calculateViewDrawRect(mOutRect, parent, headerView);

            //计算当前headerView是否受到其它View的影响(有可能下一个headerView正在替换当前headerView的位置)
            //并返回受到影响的偏移量
            Point offset = this.calculateViewDrawRectInflunceByOtherView(mOutRect, parent, state);
            //更新当前绘制区域的偏移量
            //此处决定了headerView是否显示完全(可能整个绘制区域只是headerView的一部分)
            this.updateViewDrawRect(mOutRect, offset);
            //在canvas中指定绘制的区域
            c.clipRect(mOutRect);
            //根据recycleView计算当前headerView开始绘制的起点X,Y
            //此处决定了headerView是否绘制完整
            this.calculateParentStartDrawPoint(mOutRect, parent, offset);
            //调整canvas的绘制起点
            c.translate(mOutRect.left, mOutRect.top);
            //将View绘制到canvas上
            headerView.draw(c);
        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //由于只绘制固定的headerView,不需要计算每个item之间需要绘制的区域(绘制item间间隔时需要计算)
    }

    /**
     * 从指定位置向后查找第一个headerView
     *
     * @param beginSearchPosition 开始查找的位置,此处的位置是指 {@link RecyclerView#getChildAt(int)},不是adapter中的position
     * @param searchCount         需要查找的位置总数,一般来自 {@link RecyclerView.State#getItemCount()} ,是recycleView可见View的总数,不是adapter中item的总数
     * @param parent
     * @return 返回查找到的第一项headerView, 若查找不到返回null, 通过 {@link RecyclerView#getChildAdapterPosition(View)}可以获取当前view在adapter中的位置
     */
    private View searchFirstHeaderView(int beginSearchPosition, int searchCount, RecyclerView parent) {
        View viewInSearch = null;
        int positionInSearch = 0;
        for (int i = beginSearchPosition; i < searchCount; i++) {
            //获取指定位置的view(其实这应该是recycleView缓存的view)
            viewInSearch = parent.getChildAt(i);
            //获取其position
            positionInSearch = parent.getChildAdapterPosition(viewInSearch);
            //判断当前view是否为header
            if (mHeaderHandler.isHeader(positionInSearch)) {
                return viewInSearch;
            }
        }
        return null;
    }

    /**
     * 计算headerView绘制区域是否被其它View影响,此方法用于处理其它headerItem正在替换当前stick header View的情况
     * 计算顶出上一个headerView的偏移量
     *
     * @param normalRect 正常的绘制区域,这里指的是在不考虑任何偏移量时当前headerView的绘制区域
     * @param parent
     * @param state      用于获取recycleView缓存的View总数
     * @return
     */
    private Point calculateViewDrawRectInflunceByOtherView(Rect normalRect, RecyclerView parent, RecyclerView.State state) {
        int childCount = state.getItemCount();
        //当有且只有一项时,不可能存在偏移量,不处理
        if (childCount <= 1) {
            return new Point(0, 0);
        }

        int offsetY = 0;
        //recycleView中的item可见的top,即除去recycleView本身的padding的最高点位置
        int topOfViewCanSeen = parent.getPaddingTop();
        //除第一项View,从第二项开始查找最近的一个headerView
        //这是因为当前rect已经是使用第一项View计算出来的,第一项View可能是一个headerView
        //也可能是某一组分组中的子项,但此时需要显示的还是该分组的headerView
        //从第二项开始查找是为了查找最近的一个headerView
        //该headerView可能是当前正在替换旧headerView的头部,也可能是远未达到顶部的headerView
        View itemView = this.searchFirstHeaderView(1, state.getItemCount(), parent);
        if (itemView != null && itemView.getTop() < normalRect.bottom && itemView.getTop() > topOfViewCanSeen) {
            //如果查找得到的headerView已经在替换当前的stick headerView
            //计算出需要处理的偏移量,否则不处理(即不存在偏移量,返回0)
            offsetY = normalRect.bottom - itemView.getTop();
        }
        return new Point(0, offsetY * -1);
    }

    /**
     * 更新当前需要绘制View的区域
     *
     * @param outRect 原始需要绘制的区域(没有任何偏移量时的绘制区域)
     * @param offset  偏移量数据,来自方法 {@link #calculateViewDrawRectInflunceByOtherView(Rect, RecyclerView, RecyclerView.State)}
     */
    private void updateViewDrawRect(Rect outRect, Point offset) {
        //获取原始区域的宽高
        int width = outRect.width();
        int height = outRect.height();
        //将宽高处理偏移量
        width += offset.x;
        height += offset.y;
        //重新计算其绘制区域(一般为缩小了)
        //此处是改变绘制区域的大小而不是调整绘制区域的位置
        int newRight = outRect.left + width;
        int newBottom = outRect.top + height;
        outRect.set(outRect.left, outRect.top, newRight, newBottom);
    }

    /**
     * 计算recycleView开始绘制stick header view的位置
     *
     * @param outRect 暂存变量,没有任何意义,只是用于保存返回的数据;可以是新创建的也可以是任何一个不再存储有用数据的rect
     * @param parent
     * @param offset  偏移量数据,来自方法 {@link #calculateViewDrawRectInflunceByOtherView(Rect, RecyclerView, RecyclerView.State)}
     */
    private void calculateParentStartDrawPoint(Rect outRect, RecyclerView parent, Point offset) {
        //计算正常情况下的绘制起点位置
        int drawLeft = parent.getLeft() + parent.getPaddingLeft();
        int drawTop = parent.getTop() + parent.getPaddingTop();
        outRect.set(drawLeft, drawTop, 0, 0);
        //更新偏移量
        outRect.offset(offset.x, offset.y);
    }

    /**
     * 计算stick header view绘制的区域
     *
     * @param outRect    用于存放计算后的数据
     * @param parent
     * @param headerView 需要绘制的stick header view,此view决定了头部绘制的宽高
     */
    private void calculateViewDrawRect(Rect outRect, RecyclerView parent, View headerView) {
        //以下所有坐标都以recycleView为基础,是相对于RecycleView的坐标

        //获取可开始绘制的位置
        int drawLeft = parent.getPaddingLeft();
        int drawTop = parent.getPaddingTop();
        int drawRight = drawLeft + (parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight());
        int drawBottom = headerView.getHeight() + drawTop;
        //获取headerView的layout参数
        ViewGroup.LayoutParams params = headerView.getLayoutParams();
        //判断headerView是否存在margin
        ViewGroup.MarginLayoutParams marginParams = null;
        if (params instanceof ViewGroup.MarginLayoutParams) {
            //存在margin时,绘制时的区域需要去除margin的部分
            marginParams = (ViewGroup.MarginLayoutParams) params;
            drawLeft += marginParams.leftMargin;
            drawTop += marginParams.topMargin;
            drawRight -= marginParams.rightMargin;
        }
        //设置绘制的区域
        outRect.set(drawLeft, drawTop, drawRight, drawBottom);
    }

    /**
     * 计算headerView的宽高
     *
     * @param parent
     * @param headerView
     */
    public void measureHeaderView(RecyclerView parent, View headerView) {
        int widthSpec;
        int heightSpec;


        widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);
//            widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED);
//            heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY);

        //测量并计算headerView宽高
        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), headerView.getLayoutParams().width);
        int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.getPaddingTop() + parent.getPaddingBottom(), headerView.getLayoutParams().height);
        //测量并设置headerView宽高
        headerView.measure(childWidth, childHeight);
        //刷新layout布局
        headerView.layout(0, 0, headerView.getMeasuredWidth(), headerView.getMeasuredHeight());
    }


    /**
     * 固定头部操作接口
     */
    public interface StickerHeaderHandler {
        /**
         * 判断当前位置的item是否为一个header
         *
         * @param position adapter中的item位置
         * @return
         */
        public boolean isHeader(int position);

        /**
         * 判断当前位置的item是否需要一个stick header view
         *
         * @param position adapter中的item位置
         * @return
         */
        public boolean hasStickHeader(int position);

        /**
         * 获取指定位置需要显示的headerView的ID
         *
         * @param position adapter中的item位置
         * @param parent
         * @return
         */
        public int getHeaderViewID(int position, RecyclerView parent);

        /**
         * 根据headerID获取需要的headerView
         *
         * @param position adapter中的item位置,当前需要显示headerView的位置
         * @param layoutId headerView的ID
         * @param parent
         * @return
         */
        public View getHeaderView(int position, int layoutId, RecyclerView parent);

        /**
         * 设置headerView显示的数据
         *
         * @param position   adapter中的item位置,当前需要显示headerView的位置
         * @param layoutId   headerView的ID
         * @param parent
         * @param headerView 加载得到的或者缓存的headerView
         */
        public void setHeaderView(int position, int layoutId, RecyclerView parent, View headerView);
    }
}
