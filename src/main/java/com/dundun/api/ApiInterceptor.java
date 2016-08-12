package com.dundun.api;

import java.lang.reflect.Method;

import org.apache.http.NameValuePair;

/**
 * api的拦截器
 * Created by dunxuliu on 2015/9/6.
 */
public interface ApiInterceptor extends Comparable<ApiInterceptor> {

    /**
     * http调用结束后调用的方法
     *
     * @param httpStatus
     * @param requestUrl
     * @param nameValuePairs
     * @param responseContent
     * @param replyTime
     */
    void afterCompletion(int httpStatus, Method method, String requestUrl, NameValuePair[] nameValuePairs,
                         String responseContent, Long replyTime);

    /**
     * 拦截器执行顺序
     * @return
     */
    int getOrder();
}
