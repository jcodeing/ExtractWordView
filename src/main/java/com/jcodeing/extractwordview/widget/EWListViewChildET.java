package com.jcodeing.extractwordview.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewDebug.ExportedProperty;
import android.widget.EditText;

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
public class EWListViewChildET extends EditText {
    /**
     * 相对自身的x,y
     */
    public int x, y;

    // ---------三个构造----------------------------------------------$构造
    // 当设置,指定样式时调用
    public EWListViewChildET(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    // 布局文件初始化的时候,调用-------该构造方法,重用------------★
    // 布局文件里面定义的属性都放在 AttributeSet attrs
    public EWListViewChildET(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    // 该方法,一般,在代码中 new 该类的时候_使用
    public EWListViewChildET(Context context) {
        super(context);
        initialize(context);
    }

    // --------------------------------------------------------------$初始
    private void initialize(Context context) {
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.TRANSPARENT);// 背景透明-去掉底部输入框
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        // 不做任何处理，为了阻止长按的时候弹出上下文菜单
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean getDefaultEditable() {
        return false;
    }

    // 其实当前控件并没有获得焦点，我只是欺骗Android系统，让Android系统以我获得焦点的方式去处理
    // 用于将该控件Add到其他View下,导致失去焦点.
    @Override
    @ExportedProperty(category = "focus")
    public boolean isFocused() {
        return super.isFocused();// return true一定有焦点
    }
}