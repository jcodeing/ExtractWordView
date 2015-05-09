package com.jcodeing.extractwordview.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.jcodeing.extractwordview.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Jcodeing
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class EWListView extends ListView {
    public Activity activity;
    public boolean isSupportExtractWord = true;
    private boolean isLongPressState;
    Context context;

    // ---------三个构造----------------------------------------------$构造
    // 当设置,指定样式时调用
    public EWListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    // 布局文件初始化的时候,调用-------该构造方法,重用------------★
    // 布局文件里面定义的属性都放在 AttributeSet attrs
    public EWListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    // 该方法,一般,在代码中 new 该类的时候_使用
    public EWListView(Context context) {
        super(context);
        initialize(context);
    }

    // --------------------------------------------------------------$初始
    private void initialize(Context context) {
        this.context = context;
        initMagnifier();
    }


    private final int LONGPRESS = 1;
    private Handler mPressHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //长按->初次启动--->显示放大镜&提词
                case LONGPRESS:
                    isLongPressState = true;
                    Bundle data = msg.getData();
                    int X = data.getInt("X");
                    int RawX = data.getInt("RawX");
                    int Y = data.getInt("Y");
                    int RawY = data.getInt("RawY");
                    if (!isMoved) {
                        et = findMotionView(X, Y);
                        word = getSelectWord(et.getEditableText(), extractWordCurOff(et.getLayout(), et.x, et.y));
                    }
                    resBitmap = getBitmap(activity, RawX - WIDTH / 2, RawY - HEIGHT / 2, WIDTH, HEIGHT);
                    //放大镜-初次显示
                    calculate(RawX, RawY, MotionEvent.ACTION_DOWN);
                    break;
            }
        }

    };


    private int mLastMotionX,
            mLastMotionY;
    // 是否移动了
    private boolean isMoved;
    // 移动的阈值
    private static final int TOUCH_SLOP = 20;

    private EWListViewChildET et;
    private String word;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSupportExtractWord)
            return super.onTouchEvent(event);
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                isMoved = false;

                Message message = mPressHandler != null ? mPressHandler.obtainMessage()
                        : new Message();
                //传对象,过去后,getRawY,不是相对的Y轴.
//                message.obj = event;
                Bundle bundle = new Bundle();
                bundle.putInt("X", (int) event.getX());
                bundle.putInt("RawX", (int) event.getRawX());
                bundle.putInt("Y", (int) event.getY());
                bundle.putInt("RawY", (int) event.getRawY());
                message.setData(bundle);
                message.what = LONGPRESS;
                mPressHandler.sendMessageDelayed(message, 500);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLongPressState)
                    if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                            || Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
                        //提词
                        et = findMotionView(x, y);
                        Log.e("J", et.x + "-ET--move--ET-" + et.y + "cont:" + et.getEditableText().toString().charAt(0));
                        word = getSelectWord(et.getEditableText(), extractWordCurOff(et.getLayout(), et.x, et.y));
                        //放大镜
                        resBitmap = getBitmap(activity, (int) event.getRawX() - WIDTH / 2, (int) event.getRawY() - HEIGHT / 2, WIDTH, HEIGHT);
                        calculate((int) event.getRawX(), (int) event.getRawY(), MotionEvent.ACTION_MOVE);
                        return true;
                    }
                if (isMoved && !isLongPressState)
                    break;
                //如果移动超过阈值
                if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                        || Math.abs(mLastMotionY - y) > TOUCH_SLOP)
                    //并且非长按状态下
                    if (!isLongPressState) {
                        // 则表示移动了
                        isMoved = true;
                        cleanLongPress();// 如果超出规定的移动范围--取消[长按事件]

                    }
                break;
            case MotionEvent.ACTION_UP:
                if (isLongPressState) {
                    //dis掉放大镜
                    removeCallbacks(showZoom);
                    //drawLayout();
                    popup.dismiss();

                    //TODO --单词pop
                    cleanLongPress();

                    if (!TextUtils.isEmpty(word) && et != null)
                        onLongPressWord(word, et);
                    break;
                }
                cleanLongPress();// 只要一抬起就释放[长按事件]
                break;
            case MotionEvent.ACTION_CANCEL:
                // 事件一取消也释放[长按事件],解决在ListView中滑动的时候长按事件的激活
                cleanLongPress();
                break;
        }
        return super.onTouchEvent(event);
    }


    private void cleanLongPress() {
        isLongPressState = false;
        mPressHandler.removeMessages(LONGPRESS);
    }

    private boolean calculate(int x, int y, int action) {
        dstPoint.set(x - WIDTH / 2, y - 3 * HEIGHT);
        if (y < 0) {
            // hide popup if out of bounds
            popup.dismiss();
            return true;
        }
        if (action == MotionEvent.ACTION_DOWN) {
            removeCallbacks(showZoom);
            postDelayed(showZoom, DELAY_TIME);
        } else if (!popup.isShowing()) {
            showZoom.run();
        }
        popup.update(getLeft() + dstPoint.x, getTop() + dstPoint.y, -1, -1);
        magnifier.invalidate();
        return true;
    }
    // --------------------------------------------------------------$方法

    // 单词提取

    /**
     * @param layout
     * @param x      相对自己ev.getX()
     * @param y
     * @return
     */
    public int extractWordCurOff(Layout layout, int x, int y) {
        int line;
        line = layout
                .getLineForVertical(getScrollY() + y - 10);
        int curOff = layout.getOffsetForHorizontal(line, x);
        return curOff;
    }

    public String getSelectWord(Editable content, int curOff) {
        String word = "";
        int start = getWordLeftIndex(content, curOff);
        int end = getWordRightIndex(content, curOff);
        if (start >= 0 && end >= 0) {
            word = content.subSequence(start, end).toString();
            if (!"".equals(word)) {
                // setFocusable(false);
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                Selection.setSelection(content, start, end);// 设置当前具有焦点的文本字段的选择范围,当前文本必须具有焦点，否则此方法无效
            }
        }
        return word;
    }

    public int getWordLeftIndex(Editable content, int cur) {
        // --left
        String editableText = content.toString();// getText().toString();
        if (cur >= editableText.length())
            return cur;

        int temp = 0;
        if (cur >= 20)
            temp = cur - 20;
        Pattern pattern = Pattern.compile("[^'A-Za-z]");
        Matcher m = pattern.matcher(editableText.charAt(cur) + "");
        if (m.find())
            return cur;

        String text = editableText.subSequence(temp, cur).toString();
        int i = text.length() - 1;
        for (; i >= 0; i--) {
            Matcher mm = pattern.matcher(text.charAt(i) + "");
            if (mm.find())
                break;
        }
        int start = i + 1;
        start = cur - (text.length() - start);
        return start;
    }

    public int getWordRightIndex(Editable content, int cur) {
        // --right
        String editableText = content.toString();
        if (cur >= editableText.length())
            return cur;

        int templ = editableText.length();
        if (cur <= templ - 20)
            templ = cur + 20;
        Pattern pattern = Pattern.compile("[^'A-Za-z]");
        Matcher m = pattern.matcher(editableText.charAt(cur) + "");
        if (m.find())
            return cur;

        String text1 = editableText.subSequence(cur, templ).toString();
        int i = 0;
        for (; i < text1.length(); i++) {
            Matcher mm = pattern.matcher(text1.charAt(i) + "");
            if (mm.find())
                break;
        }
        int end = i;
        end = cur + end;
        return end;
    }


    /**
     * Find the View closest to y.
     *
     * @param x
     * @param y
     * @return
     */
    EWListViewChildET findMotionView(int x, int y) {
        //是否从顶部开始find提高效率
        boolean isTopStart = y < getHeight() / 2;
        int childCount = getChildCount();
        if (childCount > 0) {
            if (isTopStart) {
                for (int i = 0; i < childCount; i++) {
                    if (!(getChildAt(i) instanceof EWListViewChildET))
                        return null;
                    EWListViewChildET v = (EWListViewChildET) getChildAt(i);
                    if (y <= v.getBottom()) {
                        //特殊处理--更新EditText--相对自己的x,y
                        v.y = y - v.getTop();
                        v.x = x;
                        Log.e("J", "ET-->y::" + y + "--updata->" + v.y);
                        return v;
                    }
                }
            } else {
                for (int i = childCount - 1; i >= 0; i--) {
                    if (!(getChildAt(i) instanceof EWListViewChildET))
                        return null;
                    EWListViewChildET v = (EWListViewChildET) getChildAt(i);
                    if (y >= v.getTop()) {
                        v.y = y - v.getTop();
                        v.x = x;
                        Log.e("J", "ET-->y::" + y + "--updata->" + v.y);
                        return v;
                    }
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------$放大镜
    private PopupWindow popup;
    private static final int WIDTH = 400;
    private static final int HEIGHT = 100;
    private static final long DELAY_TIME = 250;
    private Magnifier magnifier;

    private void initMagnifier() {
        BitmapDrawable resDrawable = (BitmapDrawable) context.getResources().getDrawable(R.mipmap.ic_launcher);
        resBitmap = resDrawable.getBitmap();

        magnifier = new Magnifier(context);

        //pop在宽高的基础上多加出边框的宽高
        popup = new PopupWindow(magnifier, WIDTH + 2, HEIGHT + 10);
        popup.setAnimationStyle(android.R.style.Animation_Toast);

        dstPoint = new Point(0, 0);
    }

    Runnable showZoom = new Runnable() {
        public void run() {
            popup.showAtLocation(EWListView.this,
                    Gravity.NO_GRAVITY,
                    getLeft() + dstPoint.x,
                    getTop() + dstPoint.y);
        }
    };


    private Bitmap resBitmap;
    private Point dstPoint;

    class Magnifier extends View {
        private Paint mPaint;

        public Magnifier(Context context) {
            super(context);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xff008000);
            mPaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            // draw popup
            mPaint.setAlpha(255);
            canvas.drawBitmap(resBitmap, 0, 0, mPaint);
            canvas.restore();

            //draw popup frame
            mPaint.reset();//重置
            mPaint.setColor(Color.LTGRAY);
            mPaint.setStyle(Paint.Style.STROKE);//设置空心
            mPaint.setStrokeWidth(2);
            Path path1 = new Path();
            path1.moveTo(0, 0);
            path1.lineTo(WIDTH, 0);
            path1.lineTo(WIDTH, HEIGHT);
            path1.lineTo(WIDTH / 2 + 15, HEIGHT);
            path1.lineTo(WIDTH / 2, HEIGHT + 10);
            path1.lineTo(WIDTH / 2 - 15, HEIGHT);
            path1.lineTo(0, HEIGHT);
            path1.close();//封闭
            canvas.drawPath(path1, mPaint);
        }
    }


    private Bitmap bitmap;//生成的位图
    //截图

    /**
     * @param activity
     * @param x        截图起始的横坐标
     * @param y        截图起始的纵坐标
     * @param width
     * @param height
     * @return
     */
    private Bitmap getBitmap(Activity activity, int x, int y, int width, int height) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        //边界处理,否则会崩滴
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x + width > bitmap.getWidth()) {
//            x = x + WIDTH / 2;
//            width = bitmap.getWidth() - x;
            //保持不改变,截取图片宽高的原则
            x = bitmap.getWidth() - width;
        }
        if (y + height > bitmap.getHeight()) {
//            y = y + HEIGHT / 2;
//            height = bitmap.getHeight() - y;
            y = bitmap.getHeight() - height;
        }
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int toHeight = frame.top;
        bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }


    public void onLongPressWord(String word, EWListViewChildET ewe) {
        if (!"".equals(word))
            Toast.makeText(context, word, Toast.LENGTH_SHORT).show();
        else {
            ewe.requestFocus();
            ewe.setFocusable(false);
            // ewe.setFocusableInTouchMode(false);
        }
    }


}
