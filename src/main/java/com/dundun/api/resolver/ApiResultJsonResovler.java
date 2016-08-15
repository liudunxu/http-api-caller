
package com.dundun.api.resolver;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dundun.api.ApiResultResolver;
import com.dundun.api.json.GsonDateAdapter;
import com.dundun.api.utils.ApiObjectUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.dundun.api.ApiInvocation;
import com.dundun.api.utils.GenericUtils;

/**
 * desc:默认的result resolver
 * author: liudunxu
 */
public class ApiResultJsonResovler implements ApiResultResolver {

    static boolean useFastJson = false;

    static {
        try {
            ApiResultJsonResovler.class.getClassLoader().loadClass("com.alibaba.fastjson.JSONObject");
            useFastJson = true;
        } catch (Throwable throwable) {
            useFastJson = false;
        }
    }

    // gson转换工具
    static final Gson GSON =
        new GsonBuilder().serializeNulls().disableHtmlEscaping().registerTypeAdapter(Date.class, new GsonDateAdapter())
            .create();

    @Override
    public Object resolveResult(ApiInvocation apiInvocation, Object formatedResult, Class<?> requiredType) {

        JsonElement jsonElement = (JsonElement) formatedResult;

        // 如果是基础类型
        if (ApiObjectUtils.isRawType(requiredType)) {
            String result = jsonElement.getAsString();
            return ApiObjectUtils.convertArg(result, requiredType);
        }

        // 如果是jsonElement类型
        else if (JsonElement.class.equals(requiredType)) {
            return jsonElement;
        }
        // jsonObject类型
        else if (JsonObject.class.equals(requiredType)) {
            return jsonElement.getAsJsonObject();
        }
        // 如果是jsonArray
        else if (JsonArray.class.equals(requiredType)) {
            return jsonElement.getAsJsonArray();
        }

        if (useFastJson) {
            if (JSONObject.class.equals(requiredType)) {
                return JSON.parseObject(apiInvocation.getRawResult(), requiredType);
            }
            if (JSONArray.class.equals(requiredType)) {
                return JSON.parseArray(apiInvocation.getRawResult());
            }
        }

        Class<?>[] genericTypes = GenericUtils.getActualClass(apiInvocation.getGenericReturnType());

        // 非泛型
        if (genericTypes.length == 0 || Arrays.equals(genericTypes, GenericUtils.EMPTY_CLASSES)) {
            return GSON.fromJson(jsonElement, requiredType);
        } else {
            if (!apiInvocation.getMethod().getReturnType().isAssignableFrom(List.class)) {
                return GSON.fromJson(jsonElement, apiInvocation.getGenericReturnType());
            }
            return GSON.fromJson(jsonElement, requiredType);
        }
    }
}
