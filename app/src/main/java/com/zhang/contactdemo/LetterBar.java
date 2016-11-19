package com.zhang.contactdemo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Mr.Z on 2016/11/19 0019.
 */

public class LetterBar extends LinearLayout {

    public interface OnLetterSelectedListener {
        void onLetterSelected(String letter);
    }

    private OnLetterSelectedListener onLetterSelectedListener;

    public void setOnLetterSelectedListener(OnLetterSelectedListener onLetterSelectedListener) {
        this.onLetterSelectedListener = onLetterSelectedListener;
    }

    public LetterBar(Context context) {
        super(context);
        init(context);
    }

    public LetterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LetterBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(0xffA6A6A6);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        for (int i = 0; i < 26; i++) {
            TextView tv = new TextView(context);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1);
            tv.setLayoutParams(params);
            tv.setText((char) ('A' + i) + "");
            tv.setTextColor(Color.WHITE);
            addView(tv);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                int defSize = getHeight()/getChildCount();
                int index = (int) (y/defSize);

                TextView tv = (TextView) getChildAt(index);
                if (tv != null && onLetterSelectedListener != null){
                    onLetterSelectedListener.onLetterSelected(tv.getText().toString());
                }
                break;
            case MotionEvent.ACTION_UP:
                if(onLetterSelectedListener != null) {
                    onLetterSelectedListener.onLetterSelected("");
                }
                break;
        }
        return true;
    }
}
