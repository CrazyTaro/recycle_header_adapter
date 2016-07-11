package com.taro.recycle.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

/**
 * Created by taro on 16/6/16.
 */
public class LineRelativeLayout extends RelativeLayout {
    private Paint mPaint = null;

    public LineRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineRelativeLayout(Context context) {
        super(context);
    }



    private void initialPaint() {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.FILL);
            float lineWidth = getPxFromDp(1);
            mPaint.setStrokeWidth(lineWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initialPaint();
        int width = this.getWidth();
        int height = this.getHeight();
        float paddingLeft = getPxFromDp(150);
        if (width > paddingLeft) {
            canvas.drawLine(paddingLeft, 0, paddingLeft, height, mPaint);
        }
    }

    private float getPxFromDp(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.getContext().getResources().getDisplayMetrics());
    }
}
