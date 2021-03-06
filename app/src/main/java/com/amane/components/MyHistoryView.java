package com.amane.components;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.amane.tianyin.R;

import java.util.Objects;

/**
 * History图层
 * @author Amane Hayashi
 */
public class MyHistoryView extends ConstraintLayout {

    public MyHistoryView(Context context) {
        super(context);
    }

    public MyHistoryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyHistoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Objects.requireNonNull(inflater).inflate(R.layout.view_my_history, this, true);
    }
}
