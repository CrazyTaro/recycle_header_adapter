package com.taro.headerrecycle.utils;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * Created by taro on 16/9/17.
 */
public class RecyclerViewUtil {
    /**
     * 修改{@link android.support.v7.widget.RecyclerView.State}时,state对象未初始化创建
     */
    public static final int STATE_RECYCLERVIEW_UNINITIALIZED = -3;
    /**
     * 修改{@link android.support.v7.widget.RecyclerView.State}时,传入的参数不合法
     */
    public static final int STATE_RECYCLERVIEW_ILLEGAL_PARAMS = -1;
    /**
     * 修改{@link android.support.v7.widget.RecyclerView.State}时,出现异常错误
     */
    public static final int STATE_RECYCLERVIEW_EXCEPTION = -2;

    /**
     * 修改RecycelView中的State对象中的itemCount,当动态改变显示的itemCount时,必须进行修改.
     *
     * @param itemCount 需要更新的itemCount,该值必须 &gt;=0,小于RecycleView的itemList中的item数量.
     * @param rv
     * @return 返回值为修改前的itemCount, 当为负数时说明修改失败, 根据常量确定失败原因
     */
    public static final int setRecyclerViewStateItemCount(int itemCount, @Nullable RecyclerView rv) {
        int result = -1;
        if (rv == null) {
            return STATE_RECYCLERVIEW_ILLEGAL_PARAMS;
        }
        try {
            //获取state
            Field stateField = RecyclerView.class.getDeclaredField("mState");
            stateField.setAccessible(true);
            Object state = stateField.get(rv);

            if (state != null) {
                //获取state的mItemCount字段
                Field itemCountField = RecyclerView.State.class.getDeclaredField("mItemCount");
                itemCountField.setAccessible(true);
                int originalItemCount = (int) itemCountField.get(state);


                if (itemCount <= 0 || itemCount >= originalItemCount) {
                    //参数不合法
                    result = -1;
                } else {
                    //更改itemCount
                    itemCountField.setInt(state, itemCount);
                    result = originalItemCount;
                }
            } else {
                //state未初始化
                result = -3;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //异常
            result = -2;
        }
        return result;
    }

    /**
     * 设置view的宽高参数
     *
     * @param itemView
     * @param width    宽
     * @param height   高
     */
    public static final void setViewWidthAndHeight(View itemView, int width, int height) {
        if (itemView != null) {
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(width, height);
            }
            params.width = width;
            params.height = height;
            itemView.setLayoutParams(params);
        }
    }

    /**
     * 设置view的margin参数
     *
     * @param itemView     设置margin的子view
     * @param marginLeft
     * @param marginTop
     * @param marginRight
     * @param marginBottom
     */
    public static final boolean setViewMarginLayoutParams(View itemView, int marginLeft, int marginTop, int marginRight, int marginBottom) {
        if (itemView != null) {
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            ViewGroup.MarginLayoutParams marginParams = null;
            if (params == null) {
                params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            if (!(params instanceof ViewGroup.MarginLayoutParams)) {
                marginParams = new ViewGroup.MarginLayoutParams(params);
            } else {
                marginParams = (ViewGroup.MarginLayoutParams) params;
            }
            marginParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            itemView.setLayoutParams(marginParams);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 计算并设置子view的layoutParams
     *
     * @param childView           子view,可以是直接创建出来的view,可以设置任何的margin/padding等参数,包括view的大小也会重新计算
     * @param childMarginLeft     childView需要的margin-left
     * @param childMarginTop      childView需要的margin-top
     * @param childMarginRight    childView需要的margin-right
     * @param childMarginBottom   childView需要的margin-bottom
     * @param edgeSize            放置childView后父控件剩余的空间进行分配后的边缘的空间部分,可通过方法获取{@link #computeSquareViewEdgeSize(int, int, int, boolean)}
     * @param parentWidth         父控件宽
     * @param parentHeight        父控件高
     * @param isRelyOnParentWidth 是否依赖于父控件的宽
     */
    public static final void computeAndSetSquareViewLayoutParams(View childView, int childMarginLeft, int childMarginTop, int childMarginRight, int childMarginBottom,
                                                                 int edgeSize, int parentWidth, int parentHeight, boolean isRelyOnParentWidth) {
        if (childView == null || parentWidth <= 0 || parentHeight <= 0) {
            return;
        }
        //计算子控件的实际宽高大小
        int childWidth = computeSquareViewSize(parentWidth, parentHeight, childMarginLeft, childMarginTop, childMarginRight, childMarginBottom, isRelyOnParentWidth);
        ViewGroup.LayoutParams params = childView.getLayoutParams();
        ViewGroup.MarginLayoutParams margin = null;
        if (params == null) {
            params = new ViewGroup.LayoutParams(0, 0);
        }
        //创建margin
        margin = new ViewGroup.MarginLayoutParams(params);
        //设置子控件的大小
        margin.width = childWidth;
        margin.height = childWidth;
        //设置margin大小
        margin.setMargins(childMarginLeft, childMarginTop, childMarginRight, childMarginBottom);
        //根据依赖边对多余的空间部分调整添加到margin中
        if (isRelyOnParentWidth) {
            margin.topMargin += edgeSize;
            margin.bottomMargin += edgeSize;
        } else {
            margin.leftMargin += edgeSize;
            margin.rightMargin += edgeSize;
        }
        childView.setLayoutParams(margin);
    }

    /**
     * 计算非依赖边划分的子控件后剩余部分的空间大小用于添加到子控件的margin(宽则添加到left/right,高则添加到top/bottom)部分的大小<br>
     * 如若依赖于宽,则计算规则如下:<br>
     * <pre>
     *     int baseSize = parentWidth;
     *     int layoutSize = baseSize * count;
     *     if (layoutWidth < parentHeight) {
     *     edgeSize = (parentHeight - layoutWidth) / (count * 2);
     *     }
     * </pre>
     * 在例子中的edgeSize就是将添加到childView的marginTop/marginBottom中,用于平均分布到界面上(否则将挤在一起)
     *
     * @param parentWidth         父控件宽
     * @param parentHeight        父控件高
     * @param count               需要显示的子控件
     * @param isRelyOnParentWidth 是否依赖于父控件的宽进行计算
     * @return
     */
    public static final int computeSquareViewEdgeSize(int parentWidth, int parentHeight, int count,
                                                      boolean isRelyOnParentWidth) {
        if (parentWidth > 0 && parentHeight > 0 && count > 0) {
            int baseSize = isRelyOnParentWidth ? parentWidth : parentHeight;
            int dividerSize = isRelyOnParentWidth ? parentHeight : parentWidth;
            int layoutWidth = baseSize * count;
            int edgeSize = 0;
            if (layoutWidth < dividerSize) {
                edgeSize = (dividerSize - layoutWidth) / (count * 2);
            }
            return edgeSize;
        } else {
            return 0;
        }
    }

    /**
     * 基于父控件的宽或者高计算另一边的相对依赖边的可分割数量;如基于宽则返回(width/height),基于高则返回(height/width);
     * 此方法的parentWidth/parentHeight应该已经排除父控件的padding;可通过方法获取{@link #computeParentViewDrawArea(View, Point)}
     *
     * @param isRelyOnParentWidth true为基于宽进行计算,false为基于高进行计算
     * @return 返回值向下取整, 参数不合法返回-1
     */

    public static final int computeSquareViewCountOnParentView(int parentWidth, int parentHeight,
                                                               boolean isRelyOnParentWidth) {
        if (parentWidth > 0 && parentHeight > 0) {
            int dividerSize = 0;
            int baseSize = 0;
            //基于宽
            if (isRelyOnParentWidth) {
                baseSize = parentWidth;
                dividerSize = parentHeight;
            } else {
                //基于高
                baseSize = parentHeight;
                dividerSize = parentWidth;
            }
            return dividerSize / baseSize;
        } else {
            return -1;
        }
    }

    /**
     * 计算子控件的实际宽高;子控件显示时依赖于父控件的某个宽或者高,包括margin部分也计算在内;<br>
     * 所以子控件的实际宽高应该是依赖的(父控件的)宽高减去子控件需要的margin大小
     *
     * @param parentWidth         父控件宽
     * @param parentHeight        父控件高
     * @param childMarginLeft     子控件需要的margin-left
     * @param childMarginTop      子控件需要的margin-top
     * @param childMarginRight    子控件需要的margin-right
     * @param childMarginBottom   子控件需要的margin-bottom
     * @param isRelyOnParentWidth 依赖于宽或是高;true为以父控件宽为基准,false为以父控件高为基准.
     * @return
     */
    public static final int computeSquareViewSize(int parentWidth, int parentHeight, int childMarginLeft, int childMarginTop,
                                                  int childMarginRight, int childMarginBottom, boolean isRelyOnParentWidth) {
        if (parentWidth > 0 && parentHeight > 0) {
            int baseSize = 0;
            //基于宽
            if (isRelyOnParentWidth) {
                //计算是将整个子控件的margin算在内以依赖边的大小(正方形)填充,所以实际的子控件宽高应该是除去margin部分
                baseSize = parentWidth - childMarginTop - childMarginBottom;
            } else {
                //基于高
                baseSize = parentHeight - childMarginLeft - childMarginRight;
            }
            return baseSize;
        } else {
            return 0;
        }
    }

    /**
     * 计算父控件的可绘制界面大小,实际上获取宽高后除去parentView的padding也是一样的;此部分可以自己计算.
     *
     * @param parentView 父控件,不可为null
     * @param outPoint   用于输出保存绘制界面宽高的point对象,允许为null,若该参数为null将创建一个新的对象并返回;<br>
     *                   建议使用一个复用的对象,以降低反复创建对象的时间和空间开销.
     * @return 返回计算的结果.point.x=width,point.y=height;
     */
    @NonNull
    public static final Point computeParentViewDrawArea(@NonNull View parentView, @Nullable Point outPoint) {
        if (outPoint == null) {
            outPoint = new Point();
        }
        //计算实际可显示的宽高(去除padding)
        int parentWidth = parentView.getWidth() - parentView.getPaddingLeft() - parentView.getPaddingRight();
        int parentHeight = parentView.getHeight() - parentView.getPaddingTop() - parentView.getPaddingBottom();
        outPoint.set(parentWidth, parentHeight);
        return outPoint;
    }

    /**
     * 计算子控件完全填充到父控件中自动适应大小.以最小边为基准,作为子控件的宽高大小(包含margin),自动计算需要填充的子控件大小并获取其相关的数据信息.<br>
     * 对于参数{@code edgeCountPoint}需要特别说明一下.该point对象中存放了两个整数数据,分别为edgeSize和childCount;其意义分如下<br>
     * <li>edgeSize,用于添加到childView的marginTop/marginBottom中,用于平均分布到界面上(否则将挤在一起),详见方法{@link #computeSquareViewEdgeSize(int, int, int, boolean)}</li>
     * <li>childCount,基于父控件的宽或者高计算另一边的相对依赖边的可分割数量,也就是最多可以显示的child的数量,详见方法{@link #computeSquareViewCountOnParentView(int, int, boolean)}</li>
     *
     * @param parentView       父控件
     * @param widthHeightPoint 用于存放父控件提供给子view显示的实际宽高大小;
     * @param edgeCountPoint   用于存放父控件布局子view后多余的空间及需要布局的子view的数量;
     * @return 返回此次计算基于的父控件的宽或者高;true为基于宽计算;false为基于高计算
     */
    public static final boolean computeSquareViewToFixParent(@NonNull View parentView, @NonNull Point widthHeightPoint, @NonNull Point edgeCountPoint) {
        widthHeightPoint = computeParentViewDrawArea(parentView, widthHeightPoint);
        boolean isRelyOnParentWidth = widthHeightPoint.x < widthHeightPoint.y;
        int count = computeSquareViewCountOnParentView(widthHeightPoint.x, widthHeightPoint.y, isRelyOnParentWidth);
        int edgeSize = computeSquareViewEdgeSize(widthHeightPoint.x, widthHeightPoint.y, count, isRelyOnParentWidth);
        edgeCountPoint.set(edgeSize, count);
        return isRelyOnParentWidth;
    }
}
