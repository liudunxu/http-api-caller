
package com.dundun.api;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import com.google.common.collect.Lists;

import com.dundun.api.parser.ReqMethod;

/**
 * desc:http method简单工厂
 * author: liudunxu
 */
public final class HttpMethodFactory {

    /**
     * 不能实例化
     */
    private HttpMethodFactory() {
    }

    /**
     * 根据string获取http method
     *
     * @param method
     *
     * @return
     */
    public static HttpUriRequest createHttpMethod(ReqMethod method, String url, RequestConfig requestConfig,
                                                  List<NameValuePair> headers, NameValuePair[] nameValuePairs,
                                                  String rawData) throws UnsupportedEncodingException {

        // 验证参数
        if (null == method) {
            throw new IllegalArgumentException("[http api]cann't create method,missing method name");
        }

        RequestBuilder requestBuilder;

        if (method == ReqMethod.GET) {
            requestBuilder = RequestBuilder.get();
            requestBuilder.setUri(url).addParameters(nameValuePairs).setConfig(requestConfig);

        } else if (method == ReqMethod.POST) {
            requestBuilder = RequestBuilder.post();
            requestBuilder.setUri(url).setConfig(requestConfig);
            requestBuilder.setEntity(new UrlEncodedFormEntity(Lists.newArrayList(nameValuePairs), HTTP.UTF_8));
        } else {
            // 未知http方法
            throw new IllegalArgumentException("[http api]cann't create method,unrecognized method name,method:" +
                                                   method);
        }

        if (!StringUtils.isEmpty(rawData)) {
            requestBuilder.setEntity(new StringEntity(rawData));
        }
        // http的header
        if (null != headers) {
            for (NameValuePair header : headers) {
                requestBuilder.addHeader(header.getName(), header.getValue());
            }
        }
        return requestBuilder.build();
    }
}
