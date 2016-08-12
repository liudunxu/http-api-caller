
package com.dundun.api;

/**
 * desc: api函数返回结果
 * author: liudunxu
 */
public interface ApiResultResolver {

    /**
     * api函数返回结果处理
     *
     * @return
     */
    Object resolveResult(ApiInvocation apiInvocation, Object formatedResult, Class<?> requiredType);
}
