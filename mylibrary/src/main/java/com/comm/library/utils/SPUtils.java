package com.comm.library.utils;

/**
 * @author xqm
 * @date 2025/7/19 14:50
 * @description SPUtils 类功能说明
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class SPUtils {
    private static final String SP_NAME = "AppCache";
    private static SharedPreferences sp;
    private static Gson gson = new Gson();

    private SPUtils() {}

    public static void init(Context context) {
        if (sp == null) {
            sp = context.getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
    }

    // 基础类型
    public static void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public static String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public static void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public static void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public static void putLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public static long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }

    public static void putFloat(String key, float value) {
        sp.edit().putFloat(key, value).apply();
    }

    public static float getFloat(String key, float defValue) {
        return sp.getFloat(key, defValue);
    }

    // 存储任意对象
    public static <T> void putObject(String key, T obj) {
        String json = gson.toJson(obj);
        sp.edit().putString(key, json).apply();
    }

    // 获取对象
    public static <T> T getObject(String key, Class<T> clazz) {
        String json = sp.getString(key, null);
        return json != null ? gson.fromJson(json, clazz) : null;
    }

    // 获取集合或泛型对象（如 List<User>）
    public static <T> T getObject(String key, Type typeOfT) {
        String json = sp.getString(key, null);
        return json != null ? gson.fromJson(json, typeOfT) : null;
    }

    public static void remove(String key) {
        sp.edit().remove(key).apply();
    }

    public static void clear() {
        sp.edit().clear().apply();
    }

    public static boolean contains(String key) {
        return sp.contains(key);
    }
}
