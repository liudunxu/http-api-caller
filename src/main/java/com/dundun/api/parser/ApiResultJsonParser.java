
package com.dundun.api.parser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.dundun.api.ApiInvocation;
import com.dundun.api.logger.Logger;
import com.dundun.api.logger.LoggerFactory;
import com.dundun.api.utils.GsonValueUtils;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.dundun.api.utils.GenericUtils;

/**
 * desc:json格式的api解析
 * author: liudunxu
 */
public class ApiResultJsonParser extends AbstractApiResultParser {

    static final Logger LOGGER = LoggerFactory.getLogger(ApiResultJsonParser.class);

    // json转换工具
    static final JsonParser JSON_PARSER = new JsonParser();

    // 验证规则正则表达式缓存
    static final ConcurrentHashMap<String, Pattern> VALID_RULE_PATTERNS = new ConcurrentHashMap<String, Pattern>();

    @Override
    public Object parseRawObject(ApiInvocation apiInvocation) {
        // 否则转换json
        return JSON_PARSER.parse(apiInvocation.getRawResult());
    }

    @Override
    public Object resolveResult(ApiInvocation apiInvocation, Object formatedResult) {

        Method method = apiInvocation.getMethod();
        JsonElement jsonElement = (JsonElement) formatedResult;
        jsonElement = getDataElement(jsonElement, apiInvocation.getDataPath());

        // 避免null集合
        if (jsonElement == null || jsonElement.isJsonNull()) {

            if (method.getReturnType().isAssignableFrom(List.class)) {
                return Collections.emptyList();
            }
            if (method.getReturnType().isAssignableFrom(Map.class)) {
                return Collections.emptyMap();
            }
            if (method.getReturnType().isAssignableFrom(Set.class)) {
                return Collections.emptySet();
            }
            return null;
        }

        // 如果返回值是List类型
        if (method.getReturnType().isAssignableFrom(List.class)) {
            Class<?>[] genericTypes = GenericUtils.getActualClass(method.getGenericReturnType());
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException("Collection generic");
            }
            Class<?> elementType = genericTypes[0];
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            List<Object> list = new ArrayList<Object>();
            for (JsonElement curElement : jsonArray) {
                list.add(apiInvocation.getApiResultResolver().resolveResult(apiInvocation, curElement, elementType));
            }
            return list;
        }

        return apiInvocation.getApiResultResolver().resolveResult(apiInvocation, jsonElement, method.getReturnType());
    }

    @Override
    public Boolean validateResult(ApiInvocation apiInvocation, Object obj) {
        if (null == obj || !(obj instanceof JsonElement)) {
            return false;
        }
        try {
            // 验证数据字段
            if (!StringUtils.isBlank(apiInvocation.getValidatePath()) &&
                    !StringUtils.isBlank(apiInvocation.getValidateRule())) {
                JsonElement jsonElement = (JsonElement) obj;
                JsonObject jsonElem = jsonElement.getAsJsonObject();
                String result = GsonValueUtils.getAsString(jsonElem, apiInvocation.getValidatePath());
                if (StringUtils.isEmpty(result)) {
                    return false;
                }
                String validateRule = apiInvocation.getValidateRule();
                ensureValidateCache(validateRule);
                Pattern pattern = VALID_RULE_PATTERNS.get(validateRule);
                return pattern.matcher(result).matches();
            }
        } catch (Exception e) {
            LOGGER.error("validate result error,url:" + apiInvocation.getUrl(), e);
            return false;
        }
        return true;
    }

    /**
     * 确保验证规则可用
     *
     * @param rule
     */
    private void ensureValidateCache(String rule) {
        if (!VALID_RULE_PATTERNS.containsKey(rule)) {
            VALID_RULE_PATTERNS.putIfAbsent(rule, Pattern.compile(rule));
        }
    }

    /**
     * 获取真正的jsonElement
     *
     * @param jsonElement
     * @param path
     *
     * @return
     */
    private JsonElement getDataElement(JsonElement jsonElement, String path) {

        if (StringUtils.isBlank(path)) {
            return jsonElement;
        }
        if (null == jsonElement) {
            throw new IllegalArgumentException("[http api] real jsonElement get error,param error");
        }
        Iterable<String> paths = Splitter.on(CharMatcher.anyOf("./")).split(path);
        for (String curPath : paths) {
            if (jsonElement == null) {
                LOGGER.warn("http-api-caller,result path:" + path + " has no data");
                return null;
            }
            // 如果是jsonObject，直接写入
            if (jsonElement.isJsonObject()) {
                jsonElement = jsonElement.getAsJsonObject().get(curPath);
            }
            //  如果path是jsonArray，取第一个
            else if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if (jsonArray.size() > 0) {
                    // 仅支持两层的array
                    jsonElement = jsonArray.get(0);
                    if (!jsonElement.isJsonObject()) {
                        throw new IllegalStateException("json level is so complex");
                    }
                    jsonElement = jsonElement.getAsJsonObject().get(curPath);
                } else {
                    LOGGER.warn("http-api-caller result path is array,but array size is 0");
                    jsonElement = null;
                }
            }
        }
        return jsonElement;
    }

}
