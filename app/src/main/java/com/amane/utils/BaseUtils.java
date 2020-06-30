package com.amane.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.amane.tianyin.R;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * 基本工具类
 * @author Amane Hayashi
 */

public final class BaseUtils {

    private BaseUtils(){}

    /**
     * 根据stringId获得string
     * @param context 当前的Activity
     * @param stringId 字符串ID(<code>res/values/string</code>)
     * @param args 字符串参数
     * @return String
     */
    public static String getResourceString(Context context, int stringId, Object... args){
        return Objects.requireNonNull(context.getResources().getString(stringId, args));
    }

    /**
     * 获得集合中最短的字符串，如果得到的字符串为<code>dislike</code>，则用<code>els</code>替代
     * @param collections 字符串集合
     * @param els 替代字符串
     * @param dislike 不希望得到的结果字符串
     * @return String
     */
    public static String getShortestOrElse(Collection<String> collections, String els, String dislike){
        String desc = collections   .parallelStream()
                                    .min(Comparator.comparing(String::length))
                                    .orElse(els);
        return dislike == null ? desc : desc.equals(dislike) ? els : desc;
    }

    /**
     * 判断两字符串中是否有相同的子字符串
     * @param s 第一个字符串
     * @param t 另一个字符串
     * @return boolean
     */
    static boolean anySubstring(String s, String t){
        return Objects.nonNull(s) && Objects.nonNull(t) && s.chars().anyMatch(c -> t.indexOf(c) > -1);
    }

    /**
     * 收起软键盘
     * @param activity 当前的<code>Activity</code>
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    /**
     * 根据<code>drawable</code>名获得<code>drawable ID</code>
     * @param key <code>drawable</code>名
     * @return <code>drawable ID</code>
     */
    static int getDrawableId(String key) {
        try {
            Field field = R.drawable.class.getField(key);
            return field.getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return -1;
        }
    }
}