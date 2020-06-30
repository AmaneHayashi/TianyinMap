package com.amane.components;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.amane.tianyin.R;

import java.util.Objects;

/**
 * POI图层
 * @author Amane Hayashi
 */
public class MyPoiView extends ConstraintLayout {

    public MyPoiView(Context context) {
        super(context);
    }

    public MyPoiView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyPoiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Objects.requireNonNull(inflater).inflate(R.layout.view_my_poi, this, true);
    }
}