
package com.dundun.api;

/**
 * desc:api原始结果转换
 * author: liudunxu
 */
public interface ApiResultParser {

    /**
     * 原始结果转换
     *
     * @param apiInvocation
     *
     * @return
     */
    Object parse(ApiInvocation apiInvocation);
}
