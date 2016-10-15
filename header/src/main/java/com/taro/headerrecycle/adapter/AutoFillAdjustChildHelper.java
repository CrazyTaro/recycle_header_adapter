package com.taro.headerrecycle.adapter;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.taro.headerrecycle.utils.RecyclerViewUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.taro.headerrecycle.adapter.IAdjustCountOption.NO_USE_ADJUST_COUNT;

/**
 * Created by taro on 16/10/15.
 */

public final class AutoFillAdjustChildHelper {
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
    private boolean mIsMarginChanged = false;
    private int mComputeWhen = COMPUTE_WHEN_CREATE_VIEW;
    private int mExpectCount = IAdjustCountOption.NO_USE_ADJUST_COUNT;

    private boolean mIsFirstTimeCompute = true;
    private boolean mIsNeedToRelayout = false;

    private boolean mIsResizeEdge = false;
    private boolean mIsAutoCompute = true;
    private boolean mIsRecompute = true;
    private boolean mIsRelyOnWidth = false;
    private Point mParentParams = null;
    private Point mChildParams = null;
    private Rect mChildMargin = null;

    private AutoFillAdjustChildAdapterOption.OnComputeStatusErrorListener mErrorListener = null;

    public AutoFillAdjustChildHelper() {
        mParentParams = new Point();
        mChildParams = new Point();
        mChildMargin = new Rect();
    }

    /**
     * 设置计算出错的错误通知回调
     *
     * @param listener
     */
    public void setOnComputeStatusErrorListener(AutoFillAdjustChildAdapterOption.OnComputeStatusErrorListener listener) {
        mErrorListener = listener;
    }

    /**
     * 设置当前需要依赖的边长.此方法会决定到实际运行时是否真的会计算出结果;<br>
     * 宽高中比较小的边才能作为依赖边;此方法设置后返回的值是一个建议值,由此方法返回{@link #checkIfRelyOnValid()};
     * 若为true说明此次设置是可行有效的;若为false说明此次设置很可能在运行时不会生效;
     * 但注意设置依然是正常保存的,只是是否会在计算起起作用而已;
     *
     * @param isRelyOnWidth true为依赖父控件宽度进行计算;false为依赖父控件高度进行计算;
     * @return 返回当前设置是否可能在运行时生效, 此返回值只是一个参考值, 不会影响实际的一个参数设置
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
        if (mChildMargin.left != left || mChildMargin.top != top ||
                mChildMargin.right != right || mChildMargin.bottom != bottom) {
            mChildMargin.set(left, top, right, bottom);
            mIsMarginChanged = true;
        }
    }

    /**
     * 统一设置子控件的margin部分
     *
     * @param margin
     */
    public void setChildMarginForAll(int margin) {
        setChildMarginForAll(margin, margin, margin, margin);
    }

    public int getChildMarginLeft() {
        return mChildMargin.left;
    }

    public int getChildMarginRight() {
        return mChildMargin.right;
    }

    public int getChildMarginTop() {
        return mChildMargin.top;
    }

    public int getChildMarginBottom() {
        return mChildMargin.bottom;
    }

    /**
     * 获取当前实际可均分显示的itemCount最大数.
     *
     * @return
     */
    public int getDisplayItemCount() {
        return mChildParams.x;
    }

    /**
     * 获取parentView被均分显示childView后剩余的空间填充到ChildView之后作为额外margin的长度.<br>
     * 不好理解请查看工具类{@link RecyclerViewUtil#computeSquareChildViewEdgeSize(int, int, int, boolean)}
     *
     * @return
     */
    public int getDisplayItemMarginEdgeSize() {
        return mChildParams.y;
    }

    /**
     * 此方法在界面更新时生效,直接调用不会马上生效,设置后在下一次界面刷新时生效.<br>
     * 强制进行重新计算,当parentView的大小改变或者任何需要强制更新数据或者界面显示不正常时,可设置重新计算;<br>
     * 请注意,设置后并不会自动更新,当{@link #isComputeWhenBind()} = true时,可以通过{@code adapter.notifyDataSetChanged()}进行数据更新;<br>
     * 当{@link #isComputeWhenCreate()} = false时,往往需要通过重新设置一次adapter来触发更新{@code rv.setAdapter(adapter)}
     */
    public void requestForceRecompute() {
        mIsRecompute = true;
    }

    /**
     * 默认为true;<br>
     * 设置是否进行自动计算,若false为不进行自动计算,则需要通过手动设置adjustCount来调整item;<br>
     * 若true为进行自动计算,会自动计算当前父控件需要填充的childView的数量,用户对adjustCount的设置将会无效,用户无法控件item数量
     *
     * @param isAutoCompute
     */
    public void setIsAutoCompute(boolean isAutoCompute) {
        mIsAutoCompute = isAutoCompute;
    }

    /**
     * 获取当前是否进行自动计算(若自动计算则item显示个数由parentView大小决定,不由用户决定)
     *
     * @return
     */
    public boolean isAutoCompute() {
        return mIsAutoCompute;
    }

    /**
     * 设置计算设置childView layoutParams参数的时机,在createView中还是在bindView中进行操作<br>
     * 一般来说,对于同一个风格的计算不管在哪里计算都只会进行一次,adjustAdapterOption会缓存计算的结果;<br>
     * 但是对View的大小及layoutParams的设置则是每一次都会进行的(因为无法确定此view是否已经被设置过,可能是来自缓存的view,也可能不是)
     *
     * @param computeWhen
     */
    public void setComputeWhen(@AutoFillAdjustChildAdapterOption.ComputeWhen int computeWhen) {
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
    public void setAdjustCount(int adjustCount, @NonNull IAdjustCountOption option) {
        //当其值为0时,应该直接设置item值为0,否则在bind时已经返回了一个item,但是实际的item数量要求是0,不合理,也会出现错误
        if (adjustCount == 0) {
            option.setAdjustCount(adjustCount);
        } else if (option.getAdjustCount() == 0) {
            //当调整的值原本是0时,不可能会进行任何界面的渲染和绑定,所以应该调整原来显示的item数量为初始值;
            // 若初始值为0则不考虑,若初始值不为0至少会返回一个数据,这时就可以对数据进行重新的计算和修正了;
            option.setAdjustCount(NO_USE_ADJUST_COUNT);
        }

        if (adjustCount != option.getAdjustCount()) {
            mExpectCount = adjustCount;
            mIsResizeEdge = true;
        }
    }

    /**
     * 在 onCreateViewHolder 中进行计算并设置childView 的参数
     *
     * @param option     调整itemCount的接口
     * @param itemView   childView
     * @param parentView
     */
    public void computeOnCreateViewHolder(@NonNull IAdjustCountOption option, @NonNull View itemView, @NonNull ViewGroup parentView) {
        if (isComputeWhenCreate()) {
            computeChildView(option, parentView, itemView, COMPUTE_WHEN_CREATE_VIEW);
        }
    }

    /**
     * 在 onBindViewHolder 中进行计算并设置bindView 参数
     *
     * @param option 调整itemCount接口
     * @param holder viewHolder,用于获取adapter及绑定的recycleView,holder缓存的子view
     */
    public void computeOnBindViewHolder(@NonNull IAdjustCountOption option, @NonNull HeaderRecycleViewHolder holder) {
        if (isComputeWhenBind()) {
            computeChildView(option, holder.getAdatper().getParentRecycleView(), holder.getRootView(), COMPUTE_WHEN_BIND_VIEW);
        }
    }

    /**
     * 在 onBindViewHolder 中进行计算并设置bindView 参数
     *
     * @param option     调整itemCount接口
     * @param parentView
     * @param childView  childView
     */
    public void computeOnBindViewHolder(@NonNull IAdjustCountOption option, @NonNull ViewGroup parentView, @Nullable View childView) {
        if (isComputeWhenBind()) {
            computeChildView(option, parentView, childView, COMPUTE_WHEN_BIND_VIEW);
        }
    }

    private void computeChildView(IAdjustCountOption option, final ViewGroup parentView, View childView, int happenOn) {
        //计算要求的基本信息
        if (option == null || parentView == null || childView == null) {
            if (mErrorListener != null) {
                mErrorListener.onComputeStatusError(happenOn << 1);
            }
            return;
        }
        //是否强制重新进行计算,此处只计算parent的参数
        if (mIsRecompute) {
            RecyclerViewUtil.computeParentViewDrawArea(parentView, mParentParams);
            mChildParams.x = RecyclerViewUtil.computeSquareChildViewCountOnParentView(mParentParams.x, mParentParams.y, mIsRelyOnWidth);
            if (!checkIfRelyOnValid()) {
                if (mErrorListener != null) {
                    mErrorListener.onComputeStatusError(happenOn << 1 | 1);
                }
                mIsRecompute = false;
                return;
            }
        }
        //当需要强制重新计算或者需要重新更新edgeSize时
        if (mIsRecompute | mIsResizeEdge) {
            if (mIsAutoCompute || mExpectCount < 0) {
                mExpectCount = mChildParams.x;
            } else if (mExpectCount > mChildParams.x && mChildParams.y > 0) {
                parentView.post(new Runnable() {
                    @Override
                    public void run() {
                        parentView.requestLayout();
                    }
                });
            }
            mChildParams.y = RecyclerViewUtil.computeSquareChildViewEdgeSize(mParentParams.x, mParentParams.y, mExpectCount, mIsRelyOnWidth);
            //不使用setAdjustCount()是因为该方法已经被重写了.
            option.setAdjustCount(mExpectCount);
            mIsResizeEdge = false;
            mIsRecompute = false;

//            //判断是否第一次计算加载,只会执行一次
//            if (mIsFirstTimeCompute) {
//                //任何第一次加载后edgeSize不小于0(有剩余空间时),都说明parentView之后有可能需要重新layout(某些情况下)
//                mIsNeedToRelayout = mChildParams.y > 0;
//                mIsFirstTimeCompute = false;
//            }
//            //当调整的数据超过自动计算后的结果;
//            //并且自动计算时存在剩余空间;
//            //并且在第一次加载时判断到需要进行layout
//            if (mExpectCount > mChildParams.x && mIsNeedToRelayout) {
//                mIsNeedToRelayout = false;
//                //通过parentView进行layout,必须也仅需一次
//                parentView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        parentView.requestLayout();
//                    }
//                });
//            }
        }
        RecyclerViewUtil.computeSquareChildViewLayoutParamsWithSet(childView, mChildMargin.left, mChildMargin.top, mChildMargin.right, mChildMargin.bottom,
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
