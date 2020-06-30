package com.amane.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.amane.tianyin.R;
import com.amane.utils.BaseUtils;

import java.util.Objects;

/**
 * Search图层
 * @author Amane hayashi
 */
public class MySearchView extends ConstraintLayout {

    private Context mContext;

    private AutoCompleteTextView searchText;
    private ImageView searchImg;

    private int imgResId;

    public MySearchView(Context context) {
        super(context);
    }

    public MySearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = Objects.requireNonNull(inflater).inflate(R.layout.view_my_search, this, true);
        searchText = mView.findViewById(R.id.my_search_text);
        searchImg = mView.findViewById(R.id.my_search_img);

        // 获得并设置可选参数项
        @SuppressLint("Recycle")
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MySearchView);
        setSearchText(typedArray.getString(R.styleable.MySearchView_hint_text));
        setSearchImg(typedArray.getResourceId(R.styleable.MySearchView_img_reference, 10000));
        setGroupVisible(typedArray.getBoolean(R.styleable.MySearchView_visibility, false) ? View.VISIBLE : View.GONE);
    }

    public AutoCompleteTextView getSearchText() {
        return searchText;
    }

    public void setSearchText(String hintText) {
        if(hintText != null){
            this.searchText.setHint(hintText);
        }
        else {
            this.searchText.setHint(BaseUtils.getResourceString(mContext, R.string.search_hint_default));
        }
    }

    public void setSearchImg(int imgResId) {
        if(imgResId != 10000){
            this.imgResId = imgResId;
            this.searchImg.setImageResource(imgResId);
        }
        else {
            this.searchImg.setImageResource(R.drawable.search_input);
        }
    }

    public void setGroupVisible(int v){
        findViewById(R.id.my_search_group).setVisibility(v);
    }

    public int getImgResId() {
        return imgResId;
    }

}
