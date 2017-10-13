package com.drwang.tapeview.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.text.DecimalFormat;

/**
 * Created by wang on 2017/10/9.
 * 通过重绘来平移
 */

public class TapeView extends View {
    public static final int DEFAULT_DIVIDE_VALUE = -1; //默认刻度值
    private Paint paint;
    private float density;
    private float deltaX; //每个刻度之间的距离
    private float scrollerXDefault;
    private float scrollerCurrent;
    private float totalWidth;
    private int w;
    private int range;
    private OnSelectedChangedListener mOnSelectedChangedListener;
    private int rangeMin;
    private int rangeMax;
    private int minMoney;
    private int maxMoney;
    private int devideMoneyColor;
    private int moneyColor;
    private int middleColor;
    private int deltaMoney = 100;
    private boolean isFullScreenWidth;  //是否需要满屏幕
    int currentMoney;
    private int startMoney;
    DecimalFormat df = new DecimalFormat("0.00");
    private int titleColor;
    private int defaultSelectedMoney;
    private boolean isNeedSetDefaultLocation;

    public TapeView(Context context) {
        this(context, null);
    }

    public TapeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);
        density = getResources().getDisplayMetrics().density;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(density);
        deltaX = 15 * density;
        setNumbers(1, 0, 150, 0, 0, DEFAULT_DIVIDE_VALUE, true);
        setColors(Color.GRAY, Color.RED, Color.RED, Color.GRAY);
//        setIsFullScreenWidth(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        calculateTranslate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isNeedResetScrollerDefault) {
            isNeedResetScrollerDefault = false;
            scrollerXDefault += deltaX / 2.0f;
            scrollerCurrent = scrollerXDefault;
        }
        if (isNeedSetDefaultLocation) { //计算默认刻度值
            isNeedSetDefaultLocation = false;
//            计算index  再计算需要平移的距离
            int index = (int) ((defaultSelectedMoney - startMoney) / deltaMoney + 0.5f);
            float v = scrollerCurrent - scrollerXDefault;
            int delta;
            if (v >= 0) {
                delta = (int) (v / deltaX + 0.5f);
            } else {
                delta = (int) (v / deltaX - 0.5f);
            }
//        if (currentX == delta || delta > rangeMax - minMoney || delta < rangeMin + (range - maxMoney)) { //如果本次计算的位置相同 或者超出了默认的范围  那么就return
//            return delta * deltaX + scrollerXDefault;
//        }
            //因为默认是会居中平移 所以 需要计算一下默认的金额
            int money = (range / 2 - delta) * deltaMoney + startMoney;
//            if (money < minMoney * deltaMoney) {
//                money = minMoney * deltaMoney;
//            }
//            if (money > maxMoney * deltaMoney) {
//                money = maxMoney * deltaMoney;
//            }
            int mid;
//            if (minMoney == maxMoney) {
//                mid = (int) (money / deltaMoney + 0.5f);
//            } else {
            //计算mid 的index
            mid = (int) ((money - startMoney) / deltaMoney + 0.5f);
//            }
            //根据设置的默认值 与mid 的index 的差值  去增加当前需要平移的数值
            scrollerCurrent += (mid - index) * deltaX;

        }
        paint.setTextSize(density * 16);
        paint.setColor(devideMoneyColor);
        paint.setStyle(Paint.Style.FILL);
        int height = getHeight();
        float lineShort = density * 10; //短刻度的长度
        float lineLong = density * 20;  //长刻度的高度
        float startX = 0;
        float startY;
        canvas.save();
        canvas.translate(scrollerCurrent, 0);
        //画金额的线
        for (int i = 0; i <= range; i++) {
            if (i % 5 == 0) {
                startY = height - lineLong;
                drawText(i, canvas, startX, startY - 10 * density);
            } else {
                startY = height - lineShort;
            }

            canvas.drawLine(startX, startY, startX, height, paint);
            startX += deltaX;
        }
        canvas.drawLine(0,height,deltaX * range,height,paint);
        canvas.restore();

        drawOthers(canvas);
    }

    private void drawOthers(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float lineLength = height - 50 * density;
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(middleColor);
        //画中线
        canvas.drawLine(width / 2, lineLength, width / 2, height, paint);
        //画小圆
        canvas.drawCircle(width / 2, lineLength, 2 * density, paint);
        //画金额
        paint.setStyle(Paint.Style.FILL);
        drawMoney(canvas, width / 2, height / 2); //金额的位置
        //画标题
        drawTitle(canvas, width / 2, (int) (height / 2 - 30 * density));
    }

    private void drawTitle(Canvas canvas, int centerX, int centerY) {
        paint.setColor(titleColor);
        paint.setTextSize(17 * density);
        String text = "体重(kg)";
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect); //获取文字边界
        float lineX = centerX - (rect.left + rect.right) / 2.0f; // 计算文字的基准线
        float lineY = centerY - (rect.top + rect.bottom) / 2.0f;
        canvas.drawText(text, lineX, lineY, paint);

    }


    private void drawMoney(Canvas canvas, int centerX, int centerY) {
        paint.setTextSize(25 * density);
        paint.setColor(moneyColor);
        float v = scrollerCurrent - scrollerXDefault;
        int delta;
        if (v >= 0) {
            delta = (int) (v / deltaX + 0.5f);
        } else {
            delta = (int) (v / deltaX - 0.5f);
        }
//        if (currentX == delta || delta > rangeMax - minMoney || delta < rangeMin + (range - maxMoney)) { //如果本次计算的位置相同 或者超出了默认的范围  那么就return
//            return delta * deltaX + scrollerXDefault;
//        }
        int money = (range / 2 - delta) * deltaMoney + startMoney;
        if (money < minMoney * deltaMoney) {
            money = minMoney * deltaMoney;
        }
        if (money > maxMoney * deltaMoney) {
            money = maxMoney * deltaMoney;
        }
        if (currentMoney != money) {
            Log.i("wang", "drawMoney: money = " + money);
            if (mOnSelectedChangedListener != null) {
                mOnSelectedChangedListener.onChanged(money / 10f);
            }
        }
        currentMoney = money;
        float kg = money / 10f;
        Rect rect = new Rect();
        String text = df.format(kg);
        paint.getTextBounds(text, 0, text.length(), rect); //获取文字边界
        float lineX = centerX - (rect.left + rect.right) / 2.0f; // 计算文字的基准线
        float lineY = centerY - (rect.top + rect.bottom) / 2.0f;
        canvas.drawText(text, lineX, lineY, paint);

    }

    private void drawText(int i, Canvas canvas, float x, float y) {
        String text = String.valueOf((i * deltaMoney + startMoney) / 10f);
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect); //获取文字边界
        float lineX = x - (rect.left + rect.right) / 2.0f; // 计算文字的基准线
        float lineY = y - (rect.top + rect.bottom) / 2.0f;
        //画文字
        canvas.drawText(text, lineX, lineY, paint);
    }

    float startX;
    float moveX;
    float startY;
    boolean canMoveDivide;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ViewParent parent = getParent();//请求不要拦截事件
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }

                startX = event.getX();
                startY = event.getY();
                if (startY > getHeight() - 50 * density) {  //限制滑动范围   从底部向上 50dp
                    canMoveDivide = true;
                }
                ;
                break;
            case MotionEvent.ACTION_MOVE:
                if (startX == 0) {
                    startX = event.getX();
                    return true;
                }
                if (startY == 0) {
                    startY = event.getY();
                    canMoveDivide = true;
                    return true;
                }
                if (!canMoveDivide) {
                    return false;
                }
                moveX = event.getX();
                scrollerCurrent += moveX - startX;
                startX = moveX;
                calculatorCurrentMoney();
                invalidate();//申请重绘
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!canMoveDivide) {
                    return false;
                }
                canMoveDivide = false;
                startX = 0;
                startY = 0;
                /*回到最近的刻度*/
                if (scrollerCurrent > w / 2 - minMoney * deltaX + startMoney / deltaMoney * deltaX) { //向右滑 超过最小范围
                    //超过左边
                    animToLocation(scrollerCurrent, w / 2 - minMoney * deltaX + startMoney / deltaMoney * deltaX, 200);
//                    scrollTo(scrollerXDefault, 0);
                } else if (scrollerCurrent < -(totalWidth - w / 2 - (range - maxMoney) * deltaX) + startMoney / deltaMoney * deltaX) { //向左滑 超过中线
                    animToLocation(scrollerCurrent, -(totalWidth - w / 2 - (range - maxMoney) * deltaX) + startMoney / deltaMoney * deltaX, 200);
                } else {  //在中间  需要计算 归零点
                    float lineX = calculatorCurrentMoney();
                    animToLocation(scrollerCurrent, lineX, 200);
                }
                break;
        }
        return true;
    }

    int currentX = -1;

    private float calculatorCurrentMoney() {
        float v = scrollerCurrent - scrollerXDefault;
        int delta;
        if (v >= 0) {
            delta = (int) (v / deltaX + 0.5f);
        } else {
            delta = (int) (v / deltaX - 0.5f);
        }
        currentX = delta;
//        int money = (range / 2 - delta) * deltaMoney;
        return delta * deltaX + scrollerXDefault;
    }

    private void animToLocation(float start, float end, long duration) {
        ObjectAnimator animateX = ObjectAnimator.ofFloat(this, "scrollerCurrent", start, end);
        animateX.setDuration(duration);
        animateX.start();
    }

    /**
     * 用于执行动画
     *
     * @param scrollerCurrent
     */
    public void setScrollerCurrent(float scrollerCurrent) {
        this.scrollerCurrent = scrollerCurrent;
        invalidate();
    }

    boolean isNeedResetScrollerDefault;

    /**
     * 设置view显示的金额的范围  从startMoney开始显示
     *
     * @param range
     */
    private void setRange(int range) {
        range = (range) / deltaMoney;
        /*------------保证range 是5的倍数 去掉这段代码后 则------------------*/
        if (range % 5 != 0) {
            range += 5 - range % 5;
        }
        /*------------------------------*/
        if (range % 2 != 0) {
            isNeedResetScrollerDefault = true;
        }
        this.range = range;
        rangeMin = -range / 2;
        rangeMax = range / 2;
    }

    /**
     * 设置刻度尺最小值默认为0
     *
     * @param startMoney
     */
    private void setDivideMinMoney(int startMoney) {
        this.startMoney = startMoney;
    }

    /**
     * 设置金额可选最小值
     *
     * @param minMoney
     */

    private void setMinMoney(int minMoney) {

        this.minMoney = minMoney / deltaMoney;
    }

    /**
     * 设置金额可选最大值
     *
     * @param maxMoney
     */
    private void setMaxMoney(int maxMoney) {
        this.maxMoney = maxMoney / deltaMoney;
    }

    /**
     * 数值变化回调的监听
     *
     * @param onSelectedChangedListener
     */
    public void setOnSelectedChangedListener(OnSelectedChangedListener onSelectedChangedListener) {
        this.mOnSelectedChangedListener = onSelectedChangedListener;
    }

    public interface OnSelectedChangedListener {
        void onChanged(float money); //数值变化

        void onMoneySetError();//数值设置有错误
    }

    /**
     * 设置下方金额文字和刻度尺的颜色
     *
     * @param color
     */
    private void setDeviDeMoneyColor(int color) {
        this.devideMoneyColor = color;
    }

    /**
     * 设置title下方的金额的颜色
     *
     * @param color
     */
    private void setMoneyColor(int color) {
        this.moneyColor = color;
    }

    /**
     * 设置中线的颜色
     *
     * @param color
     */
    private void setMiddleLineColor(int color) {
        this.middleColor = color;
    }

    private void setTitleColor(int color) {
        this.titleColor = color;
    }

    /**
     * 设置刻度值
     *
     * @param money
     */
    private void setDeltaMoney(int money) {
//        if (money % 100 != 0) {
//            money = 100 * (money / 100);
//        }
        if (money == 0) {
            return;
        }
        deltaMoney = money;
    }

    /**
     * 设置各种数值 需要同时设置
     *
     * @param delta           刻度  实际显示 是 刻度/10
     * @param min             最小的体重 实际显示 是 传值/10
     * @param max             最大的体重 实际显示 是 传值/10
     * @param start           刻度的起始值  最小值为0
     * @param ranges               设置刻度范围
     * @param defalutSelected 默认选中的金额  传 -1 {@link TapeView#DEFAULT_DIVIDE_VALUE}  则默认选中居中值 否则 则传具体的数值
     */
    public void setNumbers(int delta, int min, int max, int start, int ranges, int defalutSelected) {
        setNumbers(delta, min, max, start, ranges, defalutSelected, false);
    }

    private void setNumbers(int deltaMoney, int minMoney, int maxMoney, int startMoney, int ranges, int defalutSelectedMoney, boolean isInit) {
        //保证所有金额都不小于0
        if (deltaMoney <= 0 || minMoney < 0 || maxMoney < 0 || ranges < 0 || startMoney < 0) {
            if (mOnSelectedChangedListener != null) {//金额设置有误  可能是服务器传值问题
                mOnSelectedChangedListener.onMoneySetError();
            }
            return;
        }
        /*--------保证所有值 都可以被deltaMoney 整除----------*/
        minMoney = deltaMoney * (minMoney / deltaMoney);
        maxMoney = deltaMoney * (maxMoney / deltaMoney);
        startMoney = deltaMoney * (startMoney / deltaMoney);
        if (defalutSelectedMoney != DEFAULT_DIVIDE_VALUE) {
            defalutSelectedMoney = deltaMoney * (defalutSelectedMoney / deltaMoney);
        }
        ranges = deltaMoney * (ranges / deltaMoney);
        /*------------------------------------------------*/
        if (minMoney > maxMoney) { //保证最小值 小于等于最大值
            minMoney = maxMoney;
        }
        if (startMoney > minMoney) { //保证起始值 小于等于最小值  否则会出现绘制错误
            startMoney = minMoney;
        }
        if (defalutSelectedMoney == DEFAULT_DIVIDE_VALUE) {
            defalutSelectedMoney = (maxMoney + minMoney) / 2;
        }
        if (defalutSelectedMoney > maxMoney) {//保证默认选择的值位小于等于最大值
            defalutSelectedMoney = maxMoney;
        }
        if (defalutSelectedMoney < minMoney) {
            defalutSelectedMoney = minMoney;
        }
        if ((maxMoney - startMoney) > ranges) { //保证范围至少是最大值与起始值之间的
            ranges = maxMoney - startMoney;
        }
        setDeltaMoney(deltaMoney);
        setMinMoney(minMoney);
        setMaxMoney(maxMoney);
        setDivideMinMoney(startMoney);
        setRange(ranges);
        setDefaultSelectedMoney(defalutSelectedMoney);
        if (!isInit) {
            calculateTranslate();
        }
        requestLayout();
        invalidate();

    }

    private void calculateTranslate() {
        totalWidth = deltaX * range;
        if (totalWidth <= getMeasuredWidth() && isFullScreenWidth) {
            totalWidth = getMeasuredWidth();
            deltaX = totalWidth / (range * 1.0f);
            scrollerXDefault = 0;
        } else {
            scrollerXDefault = -(totalWidth - getMeasuredWidth()) / 2.0f;
        }
        scrollerCurrent = scrollerXDefault;
    }

    /**
     * 设置是否刻度必须满一屏
     * 默认不满
     *
     * @param isFullScreenWidth
     */
    public void setIsFullScreenWidth(boolean isFullScreenWidth) {
        this.isFullScreenWidth = isFullScreenWidth;
        requestLayout();
        invalidate();
    }

    /**
     * 设置颜色
     *
     * @param devideAndMoneyColor 刻度和刻度金额颜色
     * @param moneyColor          当前金额的颜色
     * @param midLineColor        中线颜色
     * @param titleColor          标题颜色
     */
    public void setColors(int devideAndMoneyColor, int moneyColor, int midLineColor, int titleColor) {
        setDeviDeMoneyColor(devideAndMoneyColor);
        setMoneyColor(moneyColor);
        setMiddleLineColor(midLineColor);
        setTitleColor(titleColor);
        invalidate();
    }

    /**
     * 设置默认的选择体重
     *
     * @param money
     */
    private void setDefaultSelectedMoney(int money) {
        //默认金额小于0  小于起始值  小于最小值  大于最大值 return
//        if (money < 0 || money < startMoney || money < minMoney * deltaMoney || money > maxMoney * deltaMoney) {
//            return;
//        }
        this.defaultSelectedMoney = money;
        this.isNeedSetDefaultLocation = true;
    }


}
