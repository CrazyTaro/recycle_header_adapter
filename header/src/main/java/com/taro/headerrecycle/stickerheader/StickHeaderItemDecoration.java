package com.taro.headerrecycle.stickerheader;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by taro on 16/4/25.
 * 参考来自: https://github.com/timehop/sticky-headers-recyclerview
 */
public class StickHeaderItemDecoration extends RecyclerView.ItemDecoration {
    //固定头部操作接口
    protected IStickerHeaderDecoration mHeaderHandler = null;
    //缓存View
    protected SparseArrayCompat<View> mViewCacheMap = null;
    //全局得胜的rect
    protected Rect mOutRect = null;
    protected Point mOutPoint = null;

    //是否横向(此处只是作为方向的状态保存,确保切换方向时可以正常处理)
    protected boolean mIsHorizontal = false;
    //是否第一次进行渲染加载
    private boolean mIsFirstDecoration = true;
    //用于缓存recycleView中的子view的位置
    private int mChildPosition = 0;
    //上一次渲染的头部位置
    private int mLastDecorationPosition = 0;

    private RecyclerView mRvSticker = null;

    /**
     * 创建固定头部的itemDecoration
     *
     * @param handler 固定头部操作接口
     */
    public StickHeaderItemDecoration(IStickerHeaderDecoration handler) {
        mHeaderHandler = handler;
        mViewCacheMap = new SparseArrayCompat<View>();
        mOutRect = new Rect();
        mOutPoint = new Point();
    }

    /**
     * 设置固定头部item的判断及处理接口
     *
     * @param handler
     */
    public void setIStickerHeaderDecoration(IStickerHeaderDecoration handler) {
        mViewCacheMap.clear();
        mHeaderHandler = handler;
    }

    /**
     * 关联到指定的recycleView,此方法会取消上一个关联的recycleView
     *
     * @param rv
     */
    public void attachToRecyclerView(RecyclerView rv) {
        if (rv != mRvSticker) {
            unAttachToRecyclerView();
            mRvSticker = rv;
            if (rv != null) {
                rv.addItemDecoration(this);
            }
        }
    }

    /**
     * 取消当前关联的recycleView
     */
    public void unAttachToRecyclerView() {
        if (mRvSticker != null) {
            mRvSticker.removeItemDecoration(this);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        //在绘制完整个recycleView之后绘制,绘制的结果将显示在所有Item上面
        super.onDrawOver(c, parent, state);

        if (mHeaderHandler == null) {
            return;
        }

        int position = 0;
        boolean isHorizontal = isHorizontal(parent);

        View firstView = findFirstVisibleChildView(isHorizontal, state, parent);
        //当第一项HeaderView处于最高点可见的过渡期时(topOfViewCanSeen在view.top与view.bottom之间)
        if (firstView != null) {
            //说明此时的第一项headerView还没有被移出界面,将此headerView的位置作为下面处理显示的headerView
            //即绘制其副本到界面上作为stick header view
            position = parent.getChildAdapterPosition(firstView);
        } else {
            //获取第一项View,注意此处的View不一定是第一项可见的View,可能是被缓存了的View(不可见)
            firstView = parent.getChildAt(0);
            saveChildPositionToView(0);
            //获取对应View的位置
            position = parent.getChildAdapterPosition(firstView);
        }

        //当前位置的item需要显示headerView
        if (mHeaderHandler.hasStickHeader(position)) {
            //获取headerView的标志tag
            int headerTag = mHeaderHandler.getHeaderViewTag(position, parent);
            //获取头部tag对应的缓存headerView
            View headerView = mViewCacheMap.get(headerTag);
            //不存在缓存view时加载headerView
            if (headerView == null) {
                headerView = mHeaderHandler.getHeaderView(position, headerTag, parent);
                //保存到缓存中
                mViewCacheMap.put(headerTag, headerView);
            }

            //判断是否需要进行绑定数据并测量headerView
            //当第一次加载时
            //当方向切换时
            //当headerView从未被测量时
            //当上一次渲染的固定头部与当前需要渲染的固定头部不一致时(包括布局与绑定的数据不同)
            if (isNeedToBindAndMeasureView(headerView, isHorizontal, position)) {
                //设置headerView的数据显示
                mHeaderHandler.setHeaderView(position, headerTag, parent, headerView);
                /**
                 * 测量工作必须在这里处理,因为默认是布局处理的布局是wrap_content,需要设置数据之后再进行测量计算工作
                 * 否则如果布局中某些view是wrap_content,当不存在数据时该view大小将为0,即无法显示
                 * **/
                measureHeaderView(parent, headerView, isHorizontal);
            }

            //计算当前headerView需要绘制的区域
            this.calculateViewDrawRect(mOutRect, parent, headerView, isHorizontal);

            //计算当前headerView是否受到其它View的影响(有可能下一个headerView正在替换当前headerView的位置)
            //并返回受到影响的偏移量
            this.calculateViewDrawRectInflunceByOtherView(mOutPoint, getChildPositionInView(), mOutRect, parent, state, isHorizontal);
            //更新当前绘制区域的偏移量
            //此处决定了headerView是否显示完全(可能整个绘制区域只是headerView的一部分)
            this.updateViewDrawRect(mOutRect, mOutPoint);


            //此处不能使用canvas.save()来保存当前的状态再用canvas.restore()回复
            //否则的话固定header不会被其它头部顶出界面,因为界面被还原了

            //在canvas中指定绘制的区域
            c.clipRect(mOutRect);
            //根据recycleView计算当前headerView开始绘制的起点X,Y
            //此处决定了headerView是否绘制完整
            this.calculateParentStartDrawPoint(mOutRect, parent, mOutPoint);
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

    protected void saveChildPositionToView(int childPosition) {
        mChildPosition = childPosition;
    }

    protected int getChildPositionInView() {
        return mChildPosition;
    }

    /**
     * 查找第一个可见的childView
     *
     * @param isHorizontal 方向
     * @param state        状态
     * @param parent
     * @return
     */
    protected View findFirstVisibleChildView(boolean isHorizontal, RecyclerView.State state, RecyclerView parent) {
        //获取缓存的childView总数
        int childCount = parent.getChildCount();
        //逐一遍历
        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            if (isViewCanSeenAtFirstPosition(childView, parent, isHorizontal)) {
                //查找到第一个可见childView时保存其当前的childView位置(后面需要查找此位置之后的headerView)
                saveChildPositionToView(i);
                return childView;
            }
        }
        //若没有返回null,除非RecycleView不存在childView,不然正常情况下不会返回null
        return null;
    }

    /**
     * 是否横向布局,由layoutManager决定
     *
     * @param parent
     * @return
     */
    protected boolean isHorizontal(RecyclerView parent) {
        boolean isHorizontal = false;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        //获取layoutManager的布局方向
        if (layoutManager instanceof LinearLayoutManager) {
            isHorizontal = ((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL;
        }
        return isHorizontal;
    }

    /**
     * 判断当前的childView是否可见且在第一项位置,用于更精确处理固定头部显示的数据
     *
     * @param childView    recycleView缓存的子view
     * @param parent
     * @param isHorizontal 是否横向布局
     * @return
     */
    protected boolean isViewCanSeenAtFirstPosition(View childView, RecyclerView parent, boolean isHorizontal) {
        if (childView == null) {
            return false;
        }
        int edgeOfCanSeen = isHorizontal ? parent.getPaddingLeft() : parent.getPaddingTop();
        int edgeOfStart = isHorizontal ? childView.getLeft() : childView.getTop();
        int edgeOfEnd = isHorizontal ? childView.getRight() : childView.getBottom();

        return edgeOfStart <= edgeOfCanSeen && edgeOfEnd > edgeOfCanSeen;
    }

    /**
     * 根据布局方向计算偏移量
     *
     * @param outPoint
     * @param headerView   当前第一项可见view后的第一个headerView,来自 {@link #searchFirstHeaderView(int, int, RecyclerView)}
     * @param rect         当前固定头部需要绘制的完整区域(不考虑其它view的影响)
     * @param parent
     * @param isHorizontal 布局方向
     * @return
     */
    protected Point calculateOffsetAccordingOrientation(Point outPoint, View headerView, Rect rect, RecyclerView parent, boolean isHorizontal) {
        int offsetX = 0;
        int offsetY = 0;
        if (headerView != null) {
            //根据布局方向确定可见边缘为左边界还是上边界
            if (isHorizontal) {
                //横向布局处理
                if (headerView.getLeft() < rect.right && headerView.getLeft() > rect.left) {
                    offsetX = rect.right - headerView.getLeft();
                }
            } else {
                if (headerView.getTop() < rect.bottom && headerView.getTop() > rect.top) {
                    //如果查找得到的headerView已经在替换当前的stick headerView
                    //计算出需要处理的偏移量,否则不处理(即不存在偏移量,返回0)
                    offsetY = rect.bottom - headerView.getTop();
                }
            }
        }
        if (outPoint == null) {
            outPoint = new Point();
        }
        outPoint.set(offsetX * -1, offsetY * -1);
        //偏移量是负值,因为绘制区域将向上或者向左移动出界面
        return outPoint;
    }

    /**
     * 从指定位置向后查找第一个headerView
     *
     * @param beginSearchPosition 开始查找的位置,此处的位置是指 {@link RecyclerView#getChildAt(int)},不是adapter中的position
     * @param searchCount         需要查找的位置总数,一般来自 {@link RecyclerView.State#getItemCount()} ,是recycleView可见View的总数,不是adapter中item的总数
     * @param parent
     * @return 返回查找到的第一项headerView, 若查找不到返回null, 通过 {@link RecyclerView#getChildAdapterPosition(View)}可以获取当前view在adapter中的位置
     */
    protected View searchFirstHeaderView(int beginSearchPosition, int searchCount, RecyclerView parent) {
        View viewInSearch = null;
        int positionInSearch = 0;
        for (int i = beginSearchPosition; i < searchCount; i++) {
            //获取指定位置的view(其实这应该是recycleView缓存的view)
            viewInSearch = parent.getChildAt(i);
            //获取其position
            positionInSearch = parent.getChildAdapterPosition(viewInSearch);
            //判断当前view是否为header
            if (mHeaderHandler.isHeaderPosition(positionInSearch)) {
                viewInSearch.setTag(-1, i);
                return viewInSearch;
            }
        }
        return null;
    }

    /**
     * 计算headerView绘制区域是否被其它View影响,此方法用于处理其它headerItem正在替换当前stick header View的情况
     * 计算顶出上一个headerView的偏移量
     *
     * @param outPoint
     * @param childPosition
     * @param normalRect    正常的绘制区域,这里指的是在不考虑任何偏移量时当前headerView的绘制区域
     * @param parent
     * @param state         用于获取recycleView缓存的View总数
     * @param isHorizontal  布局方向
     * @return
     */
    protected Point calculateViewDrawRectInflunceByOtherView(Point outPoint, int childPosition, Rect normalRect, RecyclerView parent, RecyclerView.State state, boolean isHorizontal) {
        int childCount = state.getItemCount();
        //当有且只有一项时,不可能存在偏移量,不处理
        if (childCount <= 1) {
            return new Point(0, 0);
        }
        //childPosition是前面查找到的第一项可见childView,+1是从其后开始查找最近的一个headerView
        //这是因为第一项View可能是一个headerView,也可能是某一组分组中的子项,若是上一个分组的子项,则此时需要显示的还是该分组的headerView,否则需要显示下一个分组的header
        //从第二项开始查找是为了查找最近的一个headerView
        //该headerView可能是当前正在替换旧headerView的头部,也可能是远未达到顶部的headerView
        View itemView = this.searchFirstHeaderView(childPosition + 1, state.getItemCount(), parent);
        //根据布局方向计算并返回固定头部的偏移量
        return calculateOffsetAccordingOrientation(outPoint, itemView, normalRect, parent, isHorizontal);
    }

    /**
     * 更新当前需要绘制View的区域
     *
     * @param outRect 原始需要绘制的区域(没有任何偏移量时的绘制区域)
     * @param offset  偏移量数据,来自方法 {@link #calculateViewDrawRectInflunceByOtherView(Point, int, Rect, RecyclerView, RecyclerView.State, boolean)}
     */
    protected void updateViewDrawRect(Rect outRect, Point offset) {
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
     * 固定头部view是否需要重新测量数据,当布局方向改变或者本身未被测量时将返回true
     *
     * @param measureView  需要被测量的view
     * @param isHorizontal 布局方向
     * @param newPosition
     * @return
     */
    protected boolean isNeedToBindAndMeasureView(View measureView, boolean isHorizontal, int newPosition) {
        boolean isNeed = true;
        //第一次渲染加载,必定进行测量
        if (mIsFirstDecoration) {
            mIsHorizontal = isHorizontal;
            //取消第一次加载标识
            mIsFirstDecoration = false;
        } else if (mIsHorizontal != isHorizontal) {
            //当前处理的布局方向与上一次处理的布局方向不同
            //重新测量加载
            mIsHorizontal = isHorizontal;
        } else if (measureView.getWidth() <= 0 || measureView.getHeight() <= 0) {
            //当前view未被测量过
        } else {
            //被加载过的情况下,不再需要进行绑定数据及测量
            //否则返回true进行绑定数据及测量
            isNeed = !mHeaderHandler.isBeenDecorated(mLastDecorationPosition, newPosition);
        }

        //不管如何处理,最终必定会将当前渲染的位置保存起来
        mLastDecorationPosition = newPosition;
        return isNeed;
    }

    /**
     * 计算recycleView开始绘制stick header view的位置
     *
     * @param outRect 暂存变量,没有任何意义,只是用于保存返回的数据;可以是新创建的也可以是任何一个不再存储有用数据的rect
     * @param parent
     * @param offset  偏移量数据,来自方法 {@link #calculateViewDrawRectInflunceByOtherView(Point, int, Rect, RecyclerView, RecyclerView.State, boolean)}
     */
    protected void calculateParentStartDrawPoint(Rect outRect, RecyclerView parent, Point offset) {
        //计算正常情况下的绘制起点位置
//        int drawLeft = parent.getLeft() + parent.getPaddingLeft();
//        int drawTop = parent.getTop() + parent.getPaddingTop();
        int drawLeft = parent.getPaddingLeft();
        int drawTop = parent.getPaddingTop();
        outRect.set(drawLeft, drawTop, 0, 0);
        //更新偏移量
        outRect.offset(offset.x, offset.y);
    }

    /**
     * 计算stick header view绘制的区域
     *
     * @param outRect      用于存放计算后的数据
     * @param parent
     * @param headerView   需要绘制的stick header view,此view决定了头部绘制的宽高
     * @param isHorizontal
     */
    protected void calculateViewDrawRect(Rect outRect, RecyclerView parent, View headerView, boolean isHorizontal) {
        //以下所有坐标都以recycleView为基础,是相对于RecycleView的坐标

        //获取可开始绘制的位置
        int drawLeft = parent.getPaddingLeft();
        int drawTop = parent.getPaddingTop();

        int drawRight = 0;
        int drawBottom = 0;

        //若横向布局
        if (isHorizontal) {
            //宽根据view处理
            //高填充整个parent
            drawRight = drawLeft + headerView.getWidth();
            drawBottom = drawTop + (parent.getHeight() - parent.getPaddingBottom() - parent.getPaddingTop());
        } else {
            //竖向布局
            //宽填充整个parent
            //高根据view处理
            drawRight = drawLeft + (parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight());
            drawBottom = drawTop + headerView.getHeight();
        }

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
     * @param isHorizontal
     */
    protected void measureHeaderView(RecyclerView parent, View headerView, boolean isHorizontal) {
        //不存在layoutparams的view添加默认布局参数
        if (headerView.getLayoutParams() == null) {
            headerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        int widthSpec;
        int heightSpec;

        //分横向及竖向进行测量处理
        if (!isHorizontal) {
            widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
            heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);
        } else {
            widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED);
            heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY);
        }

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
    public interface IStickerHeaderDecoration {
        /**
         * 判断当前位置的item是否为一个header
         *
         * @param position adapter中的item位置
         * @return
         */
        public boolean isHeaderPosition(int position);

        /**
         * 判断当前位置的item是否需要一个stick header view
         *
         * @param position adapter中的item位置
         * @return
         */
        public boolean hasStickHeader(int position);

        /**
         * 获取指定位置需要显示的headerView的标志,该标志用于缓存唯一的一个header类型的view.
         * 不同的headerView应该使用不同的tag,否则会被替换
         *
         * @param position adapter中的item位置
         * @param parent
         * @return
         */
        public int getHeaderViewTag(int position, RecyclerView parent);

        /**
         * 根据header标志或者position获取需要的headerView
         *
         * @param position      adapter中的item位置,当前需要显示headerView的位置
         * @param headerViewTag headerView的标志
         * @param parent
         * @return
         */
        public View getHeaderView(int position, int headerViewTag, RecyclerView parent);


        /**
         * 设置headerView显示的数据
         *
         * @param position      adapter中的item位置,当前需要显示headerView的位置
         * @param headerViewTag headerView的标志
         * @param parent
         * @param headerView    加载得到的或者缓存的headerView
         */
        public void setHeaderView(int position, int headerViewTag, RecyclerView parent, View headerView);

        /**
         * 判断当前渲染的header是否与上一次渲染的header为同一分组,若是可以不再测量与绑定数据
         *
         * @param lastDecoratedPosition 上一次渲染stickHeader的位置
         * @param nowDecoratingPosition 当前需要渲染stickHeader的位置
         * @return
         */
        public boolean isBeenDecorated(int lastDecoratedPosition, int nowDecoratingPosition);
    }
}
