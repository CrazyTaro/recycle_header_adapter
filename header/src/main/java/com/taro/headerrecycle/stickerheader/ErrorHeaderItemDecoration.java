package com.taro.headerrecycle.stickerheader;

import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 此类只是个示例使用的测试类
 * Created by taro on 16/6/22.
 */
public class ErrorHeaderItemDecoration extends StickHeaderItemDecoration {
    private boolean mIsFindFirstVisibleChildView = false;
    private boolean mIsUpdateDrawRect = false;

    /**
     * 创建固定头部的itemDecoration
     *
     * @param handler 固定头部操作接口
     */
    public ErrorHeaderItemDecoration(IStickerHeaderDecoration handler) {
        super(handler);
    }

    public void setIsFindFirstVisibleChildView(boolean isFind) {
        mIsFindFirstVisibleChildView = isFind;
        mIsUpdateDrawRect = true;
    }

    public void setIsUpdateDrawRect(boolean isDrawRect) {
        mIsUpdateDrawRect = isDrawRect;
        mIsFindFirstVisibleChildView = true;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        //在绘制完整个recycleView之后绘制,绘制的结果将显示在所有Item上面
        if (mHeaderHandler == null) {
            return;
        }

        int position = 0;
        boolean isHorizontal = isHorizontal(parent);

        View firstView = findFirstVisibleChildView(isHorizontal, state, parent);
        //当第一项HeaderView处于最高点可见的过渡期时(topOfViewCanSeen在view.top与view.bottom之间)
        if (firstView != null && mIsFindFirstVisibleChildView) {
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
            //设置headerView的数据显示
            mHeaderHandler.setHeaderView(position, headerTag, parent, headerView);

            /**
             * 测量工作必须在这里处理,因为默认是布局处理的布局是wrap_content,需要设置数据之后再进行测量计算工作
             * 否则如果布局中某些view是wrap_content,当不存在数据时该view大小将为0,即无法显示
             * **/
            measureHeaderView(parent, headerView, isHorizontal);


            //计算当前headerView需要绘制的区域
            this.calculateViewDrawRect(mOutRect, parent, headerView, isHorizontal);

            if (mIsUpdateDrawRect) {
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
            } else {
                c.clipRect(mOutRect);
            }
            //将View绘制到canvas上
            headerView.draw(c);
        }
    }
}
