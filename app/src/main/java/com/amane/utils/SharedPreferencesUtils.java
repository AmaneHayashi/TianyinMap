package com.amane.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SharedPreferences工具类
 * @author Amane Hayashi
 */
public class SharedPreferencesUtils {

    // SharedPreference的默认key起始值
    private static final int DEFAULT_KEY_INIT = 10000;

    // SharedPreference的默认表容量
    private static final int DEFAULT_CAPACITY = 12;

    private SharedPreferencesUtils(){}

    /**
     * 将对象写为String后，使用<code>Base64</code>加密
     * @param object 待加密的对象
     * @param <T> 对象必须实现<code>Serializable</code>接口
     * @return String
     */
    private static <T extends Serializable> String writeToString(T object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            String string = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            objectOutputStream.close();
            return string.replaceAll("\n", "").trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将String使用<code>Base64</code>解密后，读为对象
     *
     * @param s 待解密的String
     * @return Object
     */
    private static Object readToObject(String s) {
        byte[] mobileBytes = Base64.decode(s.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用SharedPreference保存字符串
     * @param context 当前的Activity
     * @param fileKey 表的key
     * @param key 对象的key
     * @param s 字符串
     */
    private static void save(Context context, String fileKey, String key, String s) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, s);
        editor.apply();
    }

    /**
     * 使用SharedPreference保存对象(此时对象的key默认从10000开始计数)
     * @param context 当前的Activity
     * @param fileKey 表的key
     * @param saveObject 对象
     * @param <T> 对象必须实现<code>Serializable</code>接口
     */
    public static <T extends Serializable> void save(Context context, String fileKey, T saveObject){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
        String str = writeToString(saveObject);
        if(sharedPreferences.getAll().values().stream().noneMatch(s -> s.toString().equals(str))){
            int size = sharedPreferences.getInt("" + DEFAULT_KEY_INIT, 0);
            int key = DEFAULT_KEY_INIT + size + 1;
            sharedPreferences.edit().putInt("" + DEFAULT_KEY_INIT,  size + 1).apply();
            save(context, fileKey, "" + key, str);
        }
    }

    /**
     * 获取SharedPreference保存的对象
     *
     * @param context 当前的Activity
     * @param fileKey 表的key
     * @param key     对象的key
     * @return Object
     */
    public static Object get(Context context, String fileKey, String key) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
        String string = sharedPreferences.getString(key, null);
        if (string != null) {
            return readToObject(string);
        } else {
            return null;
        }
    }

    /**
     * 清空SharedPreference的一张表
     * @param context 当前的Activity
     * @param fileKey 表的key
     */
    public static void truncate(Context context, String fileKey){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    /**
     * 获得SharedPreference一张表的后12个存储对象
     * @param context 当前的Activity
     * @param fileKey 表的key
     * @return List
     */
    public static List<Object> getObjectsInCapacity(Context context, String fileKey){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
        int size = sharedPreferences.getInt("" + DEFAULT_KEY_INIT, 0);
        List<Object> list = new ArrayList<>();
        int limit = Math.max(0, size - DEFAULT_CAPACITY);
        for(int i = size; i > limit; i --){
            String s = sharedPreferences.getString("" + (i + DEFAULT_KEY_INIT), "");
            list.add(readToObject(s));
        }
        return list;
    }
}
