
package com.dundun.api.auth;


import java.util.List;

import org.apache.http.NameValuePair;

import com.dundun.api.ApiAuthHandler;

/**
 * desc:api鉴权handler,不做任何处理
 * author: liudunxu
 */
public class ApiVoidAuthHandler implements ApiAuthHandler {
    @Override
    public void handleSign(List<NameValuePair> params, List<NameValuePair> headers) {
    }
}
