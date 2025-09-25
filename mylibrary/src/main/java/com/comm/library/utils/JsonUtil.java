package com.comm.library.utils;

/**
 * @author xqm
 * @date 2025/7/25 10:00
 * @description JsonUtil 类功能说明
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final Gson gson = new Gson();

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * JSON 字符串转 List<T>
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * JSON 字符串转 Map<String, Object>
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

