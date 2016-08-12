package com.dundun.api.parser;

import com.dundun.api.utils.ApiObjectUtils;
import com.dundun.api.ApiInvocation;

/**
 * 基本类型转换器
 * Created by dunxuliu on 2015/8/24.
 */
public class ApiResultRawParser extends AbstractApiResultParser {
    @Override
    public Object parseRawObject(ApiInvocation apiInvocation) {
        return apiInvocation.getRawResult();
    }

    @Override
    public Object resolveResult(ApiInvocation apiInvocation, Object result) {
        return ApiObjectUtils.convertArg(result.toString(), apiInvocation.getMethod().getReturnType());
    }

    @Override
    public Boolean validateResult(ApiInvocation apiInvocation, Object rawObject) {
        return Boolean.TRUE;
    }
}
