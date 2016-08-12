package com.dundun.api.parser;

/**
 * http请求方法枚举，目前暂时只支持GET和POST
 * Created by dunxuliu on 2015/8/27.
 */
public enum ReqMethod {

    // enums
    GET, POST;

    public static ReqMethod parse(String method) {
        if ("GET".equalsIgnoreCase(method)) {
            return GET;
        }
        if ("POST".equalsIgnoreCase(method)) {
            return POST;
        }
        return null;
    }
}