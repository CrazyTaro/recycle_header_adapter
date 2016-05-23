package com.henrytaro.ct.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by taro on 16/5/20.
 */
public class BubbleBoxLayout extends ViewGroup {
    private Paint mPaint = null;
    private static final float BUBBLE_SIZE_RATE_IN_WIDTH = 10f;
    private float mBubbleSize = 0;
    private float mEdgeLineSize = 0;

    private float mLeftEdgeInterval = 0;
    private float mRightEdgeInterval = 0;
    private float mTopEdgeInterval = 0;
    private float mBottomEdgeInterval = 0;

    private float mDrawLeftEdge = 0;
    private float mDrawRightEdge = 0;
    private float mDrawTopEdge = 0;
    private float mDrawBottomEdge = 0;

    private boolean mIsDrawBottomSplitLine = true;
    private String mBottomText = null;
    private int mBottomLineTextColor = Color.BLACK;
    private float mBottomLineTextSize = 0;
    private float mBottomLineHeight = 0;

    private int mLineColor = Color.parseColor("#bbbbbb");
    private int mBubbleBgColor = Color.parseColor("#ff5500");
    private float mBubbleIntervalTop = 0;
    private float mBubbleIntervalRight = 0;

    private boolean mIsDrawableTest = false;
    private PointF mViewBoxDrawableSize = null;
    private Drawable mBubbleInnerDrawable = null;
    private Drawable mViewBoxDrawable = null;

    public BubbleBoxLayout(Context context) {
        super(context);
        this.initial();
    }

    public BubbleBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initial();
    }

    private void initial() {
        mPaint = new Paint();
        mEdgeLineSize = this.getPxFromDp(1);
        mViewBoxDrawableSize = new PointF(-1, -1);
        Drawable bgDrawable = this.getBackground();
        if (bgDrawable == null) {
            this.setBackgroundColor(Color.TRANSPARENT);
        }
        if (mIsDrawableTest) {
            mBubbleInnerDrawable = getResources().getDrawable(android.R.drawable.stat_sys_download);
            mViewBoxDrawable = new ColorDrawable(Color.parseColor("#ff5500"));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBubbleSize = w / BUBBLE_SIZE_RATE_IN_WIDTH;
        measureSelf(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDecorationDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = (int) (this.getPaddingLeft() + mBubbleSize + mBubbleIntervalRight * 2 + mEdgeLineSize);
        int top = (int) (this.getPaddingTop() + mEdgeLineSize);
        int right = (int) (this.getMeasuredWidth() - this.getPaddingRight() + mEdgeLineSize);
        int bottom = (int) (this.getMeasuredHeight() - this.getPaddingBottom() + mEdgeLineSize);

        int childCount = getChildCount();
        if (childCount >= 1) {
            View child = getChildAt(0);
            child.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Point specPoint = measureChildView(widthMeasureSpec, heightMeasureSpec);
        measureSelf(MeasureSpec.getSize(specPoint.x), MeasureSpec.getSize(specPoint.y));
    }

    /**
     * 是否绘制底部文字线条(底部线条与文字是一起绘制的,如果不需要显示文字只要线条将文字设为null或者空字符串)
     *
     * @param isDraw
     */
    public void setIsDrawBottomSplitLine(boolean isDraw) {
        mIsDrawBottomSplitLine = isDraw;
    }

    /**
     * 是否显示测试使用的背景色/背景图
     *
     * @param isDrawableTest
     */
    public void setIsDrawableTest(boolean isDrawableTest) {
        mIsDrawableTest = isDrawableTest;
        if (isDrawableTest) {
            if (mBubbleInnerDrawable == null) {
                mBubbleInnerDrawable = getResources().getDrawable(android.R.drawable.stat_sys_download);
            }
            if (mViewBoxDrawable == null) {
                mViewBoxDrawable = new ColorDrawable(Color.parseColor("#ff5500"));
            }
        }
    }

    /**
     * 设置四边空白的间距,不会像padding或者margin一样切断线条
     *
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void setEdgeInterval(float left, float right, float top, float bottom) {
        mLeftEdgeInterval = left;
        mRightEdgeInterval = right;
        mTopEdgeInterval = top;
        mBottomEdgeInterval = bottom;
    }

    /**
     * 设置填充布局的背景图片
     *
     * @param drawable
     */
    public void setViewBoxBackgroundDrawable(Drawable drawable) {
        mViewBoxDrawable = drawable;
    }

    /**
     * 设置泡泡内部的图形,显示的范围为泡泡的内切圆
     *
     * @param drawable
     */
    public void setBubbleInnerDrawable(Drawable drawable) {
        mBubbleInnerDrawable = drawable;
    }

    /**
     * 设置泡泡的背景色
     *
     * @param color
     */
    public void setBubbleBackgroundColor(int color) {
        mBubbleBgColor = color;
    }

    /**
     * 设置底部的
     *
     * @param color
     */
    public void setBottomSplitLineTextColor(int color) {
        mBottomLineTextColor = color;
    }

    /**
     * 设置所有线条的颜色,默认白灰色
     *
     * @param color
     */
    public void setLineColor(int color) {
        mLineColor = color;
    }

    /**
     * 设置所有线条的大小,默认1DP
     *
     * @param lineWidth
     */
    public void setLineSize(float lineWidth) {
        mEdgeLineSize = lineWidth;
    }

    /**
     * 设置底部线条的文字
     *
     * @param text
     */
    public void setButtomText(String text) {
        mBottomText = text;
    }

    private void onDecorationDraw(Canvas canvas) {
        calculateDrawEdge(this.getWidth(), this.getHeight());
        PointF bubbleCenter = calculateBubbleCenter();
        drawLinkLine(canvas, bubbleCenter, mEdgeLineSize, mLineColor);
        drawTriangle(canvas, bubbleCenter, mBubbleSize, mBubbleIntervalRight, mBubbleBgColor);
        drawBubble(canvas, bubbleCenter, mBubbleSize, mBubbleBgColor, mBubbleInnerDrawable);
        RectF viewBoxEdgeRectF = drawViewBox(canvas, bubbleCenter, mBubbleSize, mBubbleIntervalRight, mBottomLineHeight, mEdgeLineSize, mLineColor, mViewBoxDrawable);
        drawBottomLine(canvas, bubbleCenter, mBubbleSize, viewBoxEdgeRectF, mBottomText, mBottomLineTextSize, mEdgeLineSize, mLineColor, mBottomLineTextColor);
    }

    private Point measureChildView(int widthSpec, int heightSpec) {
        Point specPoint = new Point();
        if (this.getChildCount() >= 1) {
            View child = this.getChildAt(0);
            child.measure(widthSpec, heightSpec);
            int widthMode = MeasureSpec.getMode(widthSpec);
            int heightMode = MeasureSpec.getMode(heightSpec);
            specPoint.x = MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), widthMode);
            specPoint.y = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), heightMode);
        } else {
            specPoint.x = widthSpec;
            specPoint.y = heightSpec;
        }
        return specPoint;
    }

    private void measureSelf(int width, int height) {
        mBubbleSize = width / BUBBLE_SIZE_RATE_IN_WIDTH;
        measureBubbleInterval(mBubbleSize);

        int decorationHeight = measureDecorationHeight(mBubbleSize, mBubbleIntervalTop);
        float smallestHeight = mBubbleSize + decorationHeight;
        height += decorationHeight;
        if (height < smallestHeight) {
            height = (int) smallestHeight;
        }
        setMeasuredDimension(width, height);
        calculateDrawEdge(width, height);
    }

    private float getPxFromDp(int dp) {
        Resources res = this.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
        return px;
    }


    private void measureBubbleInterval(float bubbleSize) {
        mBubbleIntervalTop = bubbleSize / 4;
        mBubbleIntervalRight = bubbleSize / 8;
    }

    private int measureDecorationHeight(float bubbleSize, float bubbleIntervalTop) {
        if (mBottomLineTextSize == 0) {
            mBottomLineTextSize = bubbleSize * 1 / 5;
        }
        mBottomLineHeight = measureBottomSplitLineTopInterval(bubbleSize) + mBottomLineTextSize / 2 + mEdgeLineSize;
        return (int) (bubbleIntervalTop * 2 + mBottomLineHeight);
    }

    private float measureBottomSplitLineTopInterval(float bubbleSize) {
        return bubbleSize / 3;
    }

    private void calculateDrawEdge(int width, int height) {
        mDrawLeftEdge = this.getPaddingLeft();
        mDrawRightEdge = width - this.getPaddingRight();
        mDrawTopEdge = this.getPaddingTop();
        mDrawBottomEdge = height - getPaddingBottom();
    }

    private PointF calculateBubbleCenter() {
        float bubbleCenterX = mLeftEdgeInterval + mBubbleSize / 2;
        float bubbleCenterY = mTopEdgeInterval + mBubbleSize / 2 + mBubbleIntervalTop;
        return new PointF(bubbleCenterX, bubbleCenterY);
    }

    private void drawBubble(Canvas canvas, PointF bubbleCenter, float bubbleSize, int bubbleColor, Drawable drawable) {
        mPaint.setColor(bubbleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(bubbleCenter.x, bubbleCenter.y, bubbleSize / 2, mPaint);

        float squareSize = (float) Math.sqrt(2) / 2 * bubbleSize;
        Rect bubbleInner = new Rect();
        bubbleInner.left = (int) (bubbleCenter.x - squareSize / 2);
        bubbleInner.right = (int) (bubbleInner.left + squareSize);
        bubbleInner.top = (int) (bubbleCenter.y - squareSize / 2);
        bubbleInner.bottom = (int) (bubbleInner.top + squareSize);
        if (drawable != null) {
            drawable.setBounds(bubbleInner);
            drawable.draw(canvas);
        }
    }

    private void drawTriangle(Canvas canvas, PointF bubbleCenter, float bubbleSize, float bubbleIntervalRight, int bubbleColor) {
        Path triPath = new Path();
        triPath.moveTo(bubbleCenter.x, bubbleCenter.y - bubbleSize / 2);
        triPath.lineTo(bubbleCenter.x + bubbleSize / 2 + bubbleIntervalRight, bubbleCenter.y);
        triPath.lineTo(bubbleCenter.x, bubbleCenter.y + bubbleSize / 2);
        triPath.close();
        mPaint.setColor(bubbleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(triPath, mPaint);
    }

    private void drawLinkLine(Canvas canvas, PointF bubbleCenter, float lineWidth, int linkLineColor) {
        RectF rectF = new RectF();
        rectF.left = bubbleCenter.x - lineWidth / 2;
        rectF.right = bubbleCenter.x + lineWidth / 2;
        rectF.top = mDrawTopEdge;
        rectF.bottom = mDrawBottomEdge;
        mPaint.setColor(linkLineColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rectF, mPaint);
    }

    private void drawBottomLine(Canvas canvas, PointF bubbleCenter, float bubbleSize, RectF viewBoxEdgeRectF, String text, float textSize, float lineWidth, int lineColor, int textColor) {
        if (!mIsDrawBottomSplitLine) {
            return;
        }
        float yLine = viewBoxEdgeRectF.bottom + measureBottomSplitLineTopInterval(bubbleSize);
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.FILL);
        //画左边小圆点
        canvas.drawCircle(bubbleCenter.x, yLine, lineWidth * 2, mPaint);

        float triangleStartX = mDrawRightEdge - mRightEdgeInterval - textSize / 2;
        float triangleEndX = mDrawRightEdge - mRightEdgeInterval;
        Path trianglePath = new Path();
        trianglePath.moveTo(triangleStartX, yLine);
        trianglePath.lineTo(triangleEndX, yLine - textSize / 2);
        trianglePath.lineTo(triangleEndX, yLine + textSize / 2);
        trianglePath.close();
        canvas.drawPath(trianglePath, mPaint);

        float textLength = 0;
        if (!TextUtils.isEmpty(text)) {
            mPaint.setTextSize(textSize);
            mPaint.setColor(textColor);
            textLength = mPaint.measureText(text);
            canvas.drawText(text, triangleStartX - textLength, yLine + textSize / 2, mPaint);
        }

        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(lineColor);
        canvas.drawLine(bubbleCenter.x, yLine, triangleStartX - textLength, yLine, mPaint);
    }

    private RectF drawViewBox(Canvas canvas, PointF bubbleCenter, float bubbleSize, float decoratedTriangleSize, float bottomSplitLineHeight, float lineWidth, int lineColor, Drawable drawable) {
        RectF boxEdgeRectF = new RectF();
        Path boxPath = new Path();
        float leftEdge = mDrawLeftEdge + bubbleCenter.x + bubbleSize / 2 + decoratedTriangleSize;
        float rightEdge = mDrawRightEdge - mRightEdgeInterval;
        float topEdge = mDrawTopEdge + mTopEdgeInterval;
        float bottomEdge = mDrawBottomEdge - mBottomEdgeInterval - bottomSplitLineHeight;

        boxEdgeRectF.left = leftEdge;
        boxEdgeRectF.top = topEdge;
        boxEdgeRectF.right = rightEdge;
        boxEdgeRectF.bottom = bottomEdge;

        float decoratedAngleTop = bubbleCenter.y - decoratedTriangleSize;
        float decoratedAngleRight = leftEdge + decoratedTriangleSize;
        float decoratedAngleBottom = bubbleCenter.y + decoratedTriangleSize;

        RectF arcRectF = new RectF();
        float acrDiameter = bubbleSize / 2;
        boxPath.moveTo(leftEdge + acrDiameter / 2, topEdge);

        boxPath.lineTo(rightEdge - acrDiameter / 2, topEdge);
        arcRectF.left = rightEdge - acrDiameter;
        arcRectF.top = topEdge;
        arcRectF.right = arcRectF.left + acrDiameter;
        arcRectF.bottom = arcRectF.top + acrDiameter;
        boxPath.arcTo(arcRectF, -90, 90);

        boxPath.lineTo(rightEdge, bottomEdge - acrDiameter / 2);
        arcRectF.left = rightEdge - acrDiameter;
        arcRectF.top = bottomEdge - acrDiameter;
        arcRectF.right = arcRectF.left + acrDiameter;
        arcRectF.bottom = arcRectF.top + acrDiameter;
        boxPath.arcTo(arcRectF, 0, 90);

        boxPath.lineTo(leftEdge + acrDiameter / 2, bottomEdge);
        arcRectF.left = leftEdge;
        arcRectF.top = bottomEdge - acrDiameter;
        arcRectF.right = arcRectF.left + acrDiameter;
        arcRectF.bottom = arcRectF.top + acrDiameter;
        boxPath.arcTo(arcRectF, 90, 90);

        boxPath.lineTo(leftEdge, decoratedAngleBottom);
        boxPath.lineTo(decoratedAngleRight, bubbleCenter.y);
        boxPath.lineTo(leftEdge, decoratedAngleTop);

        boxPath.lineTo(leftEdge, topEdge + acrDiameter / 2);
        arcRectF.left = leftEdge;
        arcRectF.top = topEdge;
        arcRectF.right = arcRectF.left + acrDiameter;
        arcRectF.bottom = topEdge + acrDiameter;
        boxPath.arcTo(arcRectF, 180, 90);
        boxPath.close();

        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(boxPath, mPaint);

        if (drawable != null) {
            canvas.save();
            canvas.clipPath(boxPath);
            drawable.setBounds((int) leftEdge, (int) topEdge, (int) rightEdge, (int) bottomEdge);
            drawable.draw(canvas);
            canvas.restore();
        }

        return boxEdgeRectF;
    }

}
