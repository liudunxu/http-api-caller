
package com.dundun.api.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dundun.api.ApiInvocation;
import com.dundun.api.ApiResultParser;
import com.dundun.api.utils.ApiObjectUtils;

/**
 * desc:api结果转换抽象类
 * author: liudunxu
 */
public abstract class AbstractApiResultParser implements ApiResultParser {

    @Override
    public Object parse(ApiInvocation apiInvocation) {

        // 无返回结果，直接返回null
        if (StringUtils.isBlank(apiInvocation.getRawResult())) {
            return null;
        }

        // 如果返回值是基本类型并且datapath没有设置，直接返回
        if (ApiObjectUtils.isRawType(apiInvocation.getReturnType()) &&
                StringUtils.isBlank(apiInvocation.getDataPath())) {
            return ApiObjectUtils.convertArg(apiInvocation.getRawResult(), apiInvocation.getReturnType());
        }

        Object obj = parseRawObject(apiInvocation);
        // 转换失败，返回null
        if (null == obj) {
            return null;
        }
        // boolean 类型返回值特殊处理
        Boolean validate = validateResult(apiInvocation, obj);
        if (Boolean.class.equals(apiInvocation.getMethod().getReturnType()) &&
                StringUtils.isBlank(apiInvocation.getDataPath())) {
            return validate;
        }
        // 其他类型验证失败
        if (!validate) {
            // 如果是list类型，不返回null
            if (apiInvocation.getMethod().getReturnType().isAssignableFrom(List.class)) {
                return Collections.emptyList();
            }
            // 如果是map类型，返回空map
            if (apiInvocation.getMethod().getReturnType().isAssignableFrom(Map.class)) {
                return Collections.emptyMap();
            }
            // 其他类型返回null
            return null;
        }
        return resolveResult(apiInvocation, obj);
    }

    // 转换原始结果到格式化数据
    public abstract Object parseRawObject(ApiInvocation apiInvocation);

    // 转换格式化数据到正式数据
    public abstract Object resolveResult(ApiInvocation apiInvocation, Object result);

    // 验证格式是否合法
    public abstract Boolean validateResult(ApiInvocation apiInvocation, Object rawObject);
}
