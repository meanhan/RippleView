package com.xuhan.rippleview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xuhan on 17-8-7.
 * 波纹效果View
 * 按下实现类似声波,水波纹效果。
 *自定义属性：波纹宽度,颜色,速度.
 */

public class RippleView extends View {

    // 波纹,圆圈颜色
    private int mCircleColor;
    //波纹,圆圈宽度
    private int mCircleThickness;
    //速度 单位ms
    private int mDrawSpeed;
    //波纹,圆圈半径
    private int mCircleRadius = 10;
    private int mBackgroundColor;
    private Bitmap mBackgroundBitMap = null;
    private Paint mPaint;
    private boolean isDraw = false;
    private boolean isClearDraw = false;
    private int mDrawX;
    private int mDrawY;
    private DelayDraw mDelayDraw;

    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获得自定义样式属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaterRippleView, defStyleAttr, 0);
        mCircleColor = typedArray.getColor(R.styleable.WaterRippleView_circleColor, Color.parseColor("#00a0e9"));
        mCircleThickness = typedArray.getDimensionPixelSize(R.styleable.WaterRippleView_circleThickness,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, getResources().getDisplayMetrics()));
        mDrawSpeed = typedArray.getInteger(R.styleable.WaterRippleView_drawSpeed, 50);
        typedArray.recycle();
        init();
    }

    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 11:
                    mCircleRadius = mCircleRadius + mCircleThickness;
                    invalidate();
                    break;
                default:
                    break;
            }
        }
    };

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(mCircleColor);
        mPaint.setStrokeWidth(mCircleThickness);
        mPaint.setStyle(Paint.Style.STROKE);
        getBackGroundColor();
    }

    //获取背景颜色
    private void getBackGroundColor(){
        Drawable background = this.getBackground();
        if(background != null){
            if(background instanceof ColorDrawable){
                ColorDrawable colorDrawable = (ColorDrawable) background;
                mBackgroundColor = colorDrawable.getColor();
            }else{
                mBackgroundBitMap = ((BitmapDrawable) background).getBitmap();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = 50;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = 50;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth()/2;
        int height = getMeasuredHeight()/2;
        int maxRadius = Math.min(width, height);
        if(isDraw){
            canvas.drawCircle(mDrawX, mDrawY, mCircleRadius, mPaint);
            if(mCircleRadius > (maxRadius / 3 * 2)){
                mCircleRadius = 10;
            }
        }
        if(isClearDraw){
            canvas.drawColor(mBackgroundColor);
        }
    }

    private void startAnim(){
        AnimatorSet animatorSet = new AnimatorSet();
        this.setPivotX(mDrawX);
        this.setPivotY(mDrawY);
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(this, "scaleX", 1, 2, 4, 6),
                ObjectAnimator.ofFloat(this, "scaleY", 1, 2, 4, 6),
                ObjectAnimator.ofFloat(this, "alpha", 1, 0.8f, 0.5f, 1)
        );
        animatorSet.setDuration(1000);
        animatorSet.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDrawX = (int) event.getX();
                mDrawY = (int) event.getY();
                isDraw = true;
                isClearDraw = false;
                mDelayDraw = new DelayDraw();
                myHandler.postDelayed(mDelayDraw, 0);
                break;
            case MotionEvent.ACTION_MOVE:
//                mDrawX = (int) event.getX();
//                mDrawY = (int) event.getY();
//                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                myHandler.removeCallbacks(mDelayDraw);
                isDraw = false;
                isClearDraw = true;
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    private class DelayDraw implements Runnable{

        private boolean flag = false;
        public DelayDraw() {
        }

        @Override
        public void run() {
            if(flag == true){
                myHandler.removeCallbacks(this);
                return;
            }
            myHandler.sendEmptyMessage(11);
            myHandler.postDelayed(this, 50);
        }
    }
}
