
package com.dundun.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * desc:gson jsonelement 工具类
 * author: liudunxu
 */
public final class GsonValueUtils {

    // 空字符串
    private static final String EMPTY_STRING = "";

    // 默认日期格式
    private static final String DEFAULT_DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    // 时间戳正则表达式
    private static final Pattern TIME_STAMP_PATTERN = Pattern.compile("^\\d+$");

    /**
     * 不能实例化
     */
    private GsonValueUtils() {
    }

    /**
     * 获取string
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     *
     * @return
     */
    public static String getAsString(JsonObject jsonObject, String key, String defaultValue) {
        JsonElement jsonElement = jsonObject.get(key);
        return null == jsonElement || jsonElement.isJsonNull() ? defaultValue : jsonElement.getAsString();
    }

    /**
     * 获取string
     *
     * @param jsonObject
     * @param key
     *
     * @return
     */
    public static String getAsString(JsonObject jsonObject, String key) {
        return getAsString(jsonObject, key, EMPTY_STRING);
    }

    /**
     * 获取double
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     *
     * @return
     */
    public static Double getAsDouble(JsonObject jsonObject, String key, Double defaultValue) {
        String result = getAsString(jsonObject, key);
        if (!StringUtils.isBlank(result)) {
            return Double.parseDouble(result);
        }
        return defaultValue;
    }

    /**
     * 获取integer
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     *
     * @return
     */
    public static Integer getAsInteger(JsonObject jsonObject, String key, Integer defaultValue) {
        String result = getAsString(jsonObject, key);
        if (!StringUtils.isBlank(result)) {
            return Integer.parseInt(result);
        }
        return defaultValue;
    }

    /**
     * 获取long
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     *
     * @return
     */
    public static Long getAsLong(JsonObject jsonObject, String key, Long defaultValue) {
        String result = getAsString(jsonObject, key);
        if (!StringUtils.isBlank(result)) {
            return Long.parseLong(result);
        }
        return defaultValue;
    }

    /**
     * 获取date
     *
     * @param jsonObject
     * @param key
     *
     * @return
     */
    public static Date getAsDate(JsonObject jsonObject, String key) {
        String data = getAsString(jsonObject, key);
        return getAsDate(data);
    }

    /**
     * 获取date
     *
     * @return
     */
    public static Date getAsDate(String data) {
        if (!StringUtils.isBlank(data)) {
            try {
                // 时间戳格式
                if (TIME_STAMP_PATTERN.matcher(data).matches()) {
                    return new Date(Long.parseLong(data));
                }
                // 时间字符戳格式
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);
                return simpleDateFormat.parse(data);
            } catch (ParseException e) {
                return null;
            }
        }
        return null;

    }

    /**
     * 获取boolean
     *
     * @param jsonObject
     * @param key
     *
     * @return
     */
    public static Boolean getAsBoolean(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return null == jsonElement || jsonElement.isJsonNull() ? Boolean.FALSE : jsonElement.getAsBoolean();
    }
}
