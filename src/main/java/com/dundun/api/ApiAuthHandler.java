package com.dundun.api;

import java.util.List;

import org.apache.http.NameValuePair;

/**
 * desc:api鉴权功能
 * author: liudunxu
 */
public interface ApiAuthHandler {

    /**
     * 处理鉴权
     *
     * @param params
     */
    void handleSign(List<NameValuePair> params, List<NameValuePair> headers);
}
