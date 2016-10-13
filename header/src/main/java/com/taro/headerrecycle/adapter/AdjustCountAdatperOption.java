package com.taro.headerrecycle.adapter;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.taro.headerrecycle.utils.RecyclerViewUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by taro on 16/10/13.
 */

public abstract class AdjustCountAdatperOption<T> extends SimpleRecycleAdapter.SimpleAdapterOption<T> {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {ERROR_CODE_HAPPEND_ON_BIND_PARAMS_NULL, ERROR_CODE_HAPPEND_ON_BIND_RELY_ON_INVALID, ERROR_CODE_HAPPEND_ON_CREATE_RELY_ON_INVALID, ERROR_CODE_HAPPEND_ON_CREATE_PARAMS_NULL})
    public @interface ErrorCode {
    }

    //第二位都使用0
    public static final int ERROR_CODE_HAPPEND_ON_CREATE_PARAMS_NULL = 0;
    public static final int ERROR_CODE_HAPPEND_ON_CREATE_RELY_ON_INVALID = 1;

    //第二位都使用1
    public static final int ERROR_CODE_HAPPEND_ON_BIND_PARAMS_NULL = 2;
    public static final int ERROR_CODE_HAPPEND_ON_BIND_RELY_ON_INVALID = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {COMPUTE_WHEN_BIND_VIEW, COMPUTE_WHEN_CREATE_VIEW})
    public @interface ComputeWhen {
    }

    public static final int COMPUTE_WHEN_CREATE_VIEW = 0;
    public static final int COMPUTE_WHEN_BIND_VIEW = 1;
    private int mComputeWhen = COMPUTE_WHEN_CREATE_VIEW;
    private int mExpectCount = NO_USE_ADJUST_COUNT;
    private boolean mIsResizeEdge = false;
    private boolean mIsAutoCompute = true;
    private boolean mIsRecompute = true;
    private boolean mIsRelyOnWidth = false;
    private Point mParentParams = null;
    private Point mChildParams = null;
    private Rect mChildMargin = null;

    private OnComputeStatusErrorListener mErrorListener = null;

    public AdjustCountAdatperOption() {
        mParentParams = new Point();
        mChildParams = new Point();
        mChildMargin = new Rect();
    }

    /**
     * 设置计算出错的错误通知回调
     *
     * @param listener
     */
    public void setOnComputeStatusErrorListener(OnComputeStatusErrorListener listener) {
        mErrorListener = listener;
    }

    /**
     * 设置当前需要依赖的边长.此方法会决定到实际运行时是否真的会计算出结果;<br>
     * 宽高中比较小的边才能作为依赖边;此方法设置后返回的值是一个建议值,由此方法返回{@link #checkIfRelyOnValid()};
     * 若为true说明此次设置是可行有效的;若为false说明此次设置很可能在运行时不会生效;
     * 但注意设置依然是正常保存的,只是是否会在计算起起作用而已;
     *
     * @param isRelyOnWidth true为依赖父控件宽度进行计算;false为依赖父控件高度进行计算;
     * @return
     */
    public boolean setIsRelyOnWidth(boolean isRelyOnWidth) {
        mIsRelyOnWidth = isRelyOnWidth;
        return checkIfRelyOnValid();
    }


    /**
     * 若为true说明当前依赖边是可行有效的;若为false说明依赖边很可能在运行时不会生效;
     * 仅当依赖边为宽高中的最小边才会返回true;
     *
     * @return
     */
    public boolean checkIfRelyOnValid() {
        return mIsRelyOnWidth == (mParentParams.x <= mParentParams.y);
    }

    /**
     * 返回当前的依赖边,true为依赖宽;false为依赖高
     *
     * @return
     */
    public boolean isRelyOnWidth() {
        return mIsRelyOnWidth;
    }

    /**
     * 统一设置子控件的margin部分
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setChildMarginForAll(int left, int top, int right, int bottom) {
        mChildMargin.set(left, top, right, bottom);
    }

    /**
     * 强制进行重新计算,当parentView的大小改变或者任何需要强制更新数据或者界面显示不正常时,可设置重新计算;<br>
     * 请注意,设置后并不会自动更新,当{@link #isComputeWhenBind()} = true时,可以通过{@code notifyDataSetChanged()}进行数据更新;<br>
     * 当{@link #isComputeWhenCreate()} = false时,往往需要通过重新设置一次adapter来触发更新
     */
    public void setForceRecompute() {
        mIsRecompute = true;
    }

    /**
     * 设置是否进行自动计算,若不进行自动计算,则需要通过手动设置adjustCount来调整item
     *
     * @param isAutoCompute
     */
    public void setIsAutoCompute(boolean isAutoCompute) {
        mIsAutoCompute = isAutoCompute;
    }

    /**
     * 获取当前是否进行自动计算(item显示个数由parentView大小决定,不由用户决定)
     *
     * @return
     */
    public boolean isAutoCompute() {
        return mIsAutoCompute;
    }

    /**
     * 设置计算设置childView layoutParams参数的时机,在createView中还是在bindView中进行操作
     *
     * @param computeWhen
     */
    public void setComputeWhen(@ComputeWhen int computeWhen) {
        mComputeWhen = computeWhen;
    }

    /**
     * 返回当前进行计算操作所在的时机
     *
     * @return
     */
    @ComputeWhen
    public int getComputeWhen() {
        return mComputeWhen;
    }

    /**
     * 是否在createView的时候进行计算并更新childView layoutParams
     *
     * @return
     */
    public boolean isComputeWhenCreate() {
        return mComputeWhen == COMPUTE_WHEN_CREATE_VIEW;
    }

    /**
     * 是否在BindView的时候进行计算并更新childView layoutParams
     *
     * @return
     */
    public boolean isComputeWhenBind() {
        return mComputeWhen == COMPUTE_WHEN_BIND_VIEW;
    }

    /**
     * 设置希望调整为的item个数,但实际的个数是受到实际的情况影响,如果实际的数据达不到这个数量,会被调整
     *
     * @param adjustCount
     */
    @Override
    public void setAdjustCount(int adjustCount) {
        //当其值为0时,应该直接设置item值为0,否则在bind时已经返回了一个item,但是实际的item数量要求是0,不合理,也会出现错误
        if (adjustCount == 0) {
            super.setAdjustCount(adjustCount);
        } else if (this.getAdjustCount() == 0) {
            //当调整的值原本是0时,不可能会进行任何界面的渲染和绑定,所以应该调整原来显示的item数量为初始值;
            // 若初始值为0则不考虑,若初始值不为0至少会返回一个数据,这时就可以对数据进行重新的计算和修正了;
            super.setAdjustCount(NO_USE_ADJUST_COUNT);
        }
        mExpectCount = adjustCount;
        mIsResizeEdge = true;
    }

    @Override
    public void onCreateViewEverytime(@NonNull View itemView, @NonNull ViewGroup parentView, @NonNull HeaderRecycleAdapter adapter, int viewType) {
        if (isComputeWhenCreate()) {
            computeChildView(adapter.getParentRecycleView(), itemView, COMPUTE_WHEN_CREATE_VIEW);
        }
    }

    @Override
    public void setViewHolder(T itemData, int position, @NonNull HeaderRecycleViewHolder holder) {
        if (isComputeWhenBind()) {
            computeChildView(holder.getAdatper().getParentRecycleView(), holder.getRootView(), COMPUTE_WHEN_BIND_VIEW);
        }
    }

    private void computeChildView(ViewGroup parentView, View childView, int happenOn) {
        if (parentView == null || childView == null) {
            if (mErrorListener != null) {
                mErrorListener.onComputeStatusError(happenOn << 1);
            }
            return;
        }
        if (mIsRecompute) {
            RecyclerViewUtil.computeParentViewDrawArea(parentView, mParentParams);
            if (!checkIfRelyOnValid()) {
                if (mErrorListener != null) {
                    mErrorListener.onComputeStatusError(happenOn << 1 | 1);
                }
                mIsRecompute = false;
                return;
            }
        }
        if (mIsRecompute | mIsResizeEdge) {
            mChildParams.x = RecyclerViewUtil.computeSquareViewCountOnParentView(mParentParams.x, mParentParams.y, mIsRelyOnWidth);
            if (mIsAutoCompute || mExpectCount < 0) {
                mExpectCount = mChildParams.x;
            }
            mChildParams.y = RecyclerViewUtil.computeSquareViewEdgeSize(mParentParams.x, mParentParams.y, mExpectCount, mIsRelyOnWidth);
            this.setInnerAdjustCount(mExpectCount);
            mIsResizeEdge = false;
            mIsRecompute = false;
        }
        RecyclerViewUtil.computeAndSetSquareViewLayoutParams(childView, mChildMargin.left, mChildMargin.top, mChildMargin.right, mChildMargin.bottom,
                mChildParams.y, mParentParams.x, mParentParams.y, mIsRelyOnWidth);
    }

    /**
     * 当计算过程出现错误时的回调接口
     */
    public interface OnComputeStatusErrorListener {
        /**
         * 当计算过程出现错误时回调,参数为对应的错误码
         *
         * @param errorCode
         */
        public void onComputeStatusError(int errorCode);
    }
}
