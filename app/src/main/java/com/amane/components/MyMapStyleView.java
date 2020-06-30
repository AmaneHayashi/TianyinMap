package com.amane.components;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.amane.tianyin.R;

import java.util.Objects;

/**
 * MapStyle图层
 * @author Amane Hayashi
 */
public class MyMapStyleView extends ConstraintLayout {

    public MyMapStyleView(Context context) {
        super(context);
    }

    public MyMapStyleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMapStyleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Objects.requireNonNull(inflater).inflate(R.layout.view_my_map_style, this, true);
    }
}
