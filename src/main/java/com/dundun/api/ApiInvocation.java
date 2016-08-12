
package com.dundun.api;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;

import com.dundun.api.annotation.ApiInfo;
import com.dundun.api.parser.ReqMethod;

/**
 * desc:http api 执行上下文
 * author: liudunxu
 */
public final class ApiInvocation {

    // 代理对象
    private final Object proxy;
    // 执行的方法
    private final Method method;
    // 方法参数
    private final Object[] args;
    // 返回类型
    private final Class returnType;
    // 泛型返回类型
    private final Type genericReturnType;
    // 请求url
    private String url;
    // http请求方法
    private ReqMethod httpMethod;
    // http超时时间
    private int readTimeout;
    // 重试次数
    private int retryCount;
    // 数据字段path
    private String dataPath;
    // 验证字段path
    private String validatePath;
    // 验证规则
    private String validateRule;
    // 原始结果
    private String rawResult;
    // 是否成功
    private boolean isSuccess = false;
    // result resolver
    private ApiResultResolver apiResultResolver;

    /**
     * 构造函数
     *
     * @param proxy
     * @param method
     * @param args
     */
    public ApiInvocation(Object proxy, Method method, Object[] args, String serviceUrl,
                         ApiResultResolver apiResultResolver) {
        this.proxy = proxy;
        this.method = method;
        this.args = args;
        this.returnType = method.getReturnType();
        this.genericReturnType = method.getGenericReturnType();
        ApiInfo apiInfo = method.getAnnotation(ApiInfo.class);
        this.setUrl(buildRequestUrl(serviceUrl, apiInfo.url()));
        this.setHttpMethod(apiInfo.method());
        this.retryCount = apiInfo.retryCount();
        this.setDataPath(apiInfo.resultPath());
        this.setValidatePath(apiInfo.validatePath());
        this.setValidateRule(apiInfo.validateRule());
        this.setReadTimeout(apiInfo.timeout());
        this.apiResultResolver = apiResultResolver;
    }

    public Object getProxy() {
        return proxy;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public ApiResultResolver getApiResultResolver() {
        return apiResultResolver;
    }

    public void setApiResultResolver(ApiResultResolver apiResultResolver) {
        this.apiResultResolver = apiResultResolver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ReqMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(ReqMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getRawResult() {
        return rawResult;
    }

    public void setRawResult(String rawResult) {
        this.rawResult = rawResult;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getValidatePath() {
        return validatePath;
    }

    public void setValidatePath(String validatePath) {
        this.validatePath = validatePath;
    }

    public String getValidateRule() {
        return validateRule;
    }

    public void setValidateRule(String validateRule) {
        this.validateRule = validateRule;
    }

    public Class getReturnType() {
        return returnType;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Type getGenericReturnType() {
        return genericReturnType;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * 拼接请求url
     *
     * @return
     */
    private String buildRequestUrl(String baseUrl, String path) {
        if (StringUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("[http api] missing baseUrl");
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += '/';
        }
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + path;
    }
}
