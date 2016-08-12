package com.dundun.api;

import com.dundun.api.annotation.ApiHeader;
import com.dundun.api.annotation.ApiInfo;
import com.dundun.api.annotation.ApiParam;
import com.dundun.api.annotation.ApiRateLimiter;
import com.dundun.api.annotation.ApiRaw;
import com.dundun.api.logger.Logger;
import com.dundun.api.logger.LoggerFactory;
import com.dundun.api.parser.ReqMethod;
import com.dundun.api.utils.ApiObjectUtils;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * desc: api服务动态代理 author: liudunxu
 */
public class ApiServiceInvocationHandler implements InvocationHandler {

    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiServiceInvocationHandler.class);
    // http连接池配置
    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 10;
    // gson转换工具
    static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    // 线程池
    static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactory() {

        private AtomicLong threadId = new AtomicLong(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("http-api-caller-thread-" + threadId.getAndIncrement());
            return t;
        }
    });
    // 每秒调用频率limiter map
    static final ConcurrentHashMap<Method, RateLimiter> RATE_LIMITER_CACHE;
    // http client 池
    static final ConcurrentHashMap<Integer, CloseableHttpClient> HTTP_CLIENT_MAP;
    // http连接池
    static final PoolingHttpClientConnectionManager HTTP_CLIENT_CONNECTION_MANAGER;

    static final String APP_NAME;

    /**
     * 静态构造函数
     */
    static {
        RATE_LIMITER_CACHE = new ConcurrentHashMap<Method, RateLimiter>();
        HTTP_CLIENT_CONNECTION_MANAGER = new PoolingHttpClientConnectionManager();
        HTTP_CLIENT_CONNECTION_MANAGER.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
        HTTP_CLIENT_CONNECTION_MANAGER.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
        HTTP_CLIENT_MAP = new ConcurrentHashMap<Integer, CloseableHttpClient>();
        String appName;
        try {
            appName = System.getProperty("APP_NAME", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            appName = "UnknownHost";
        }
        APP_NAME = appName;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (CloseableHttpClient closeableHttpClient : HTTP_CLIENT_MAP.values()) {
                    try {
                        closeableHttpClient.close();
                    } catch (IOException e) {
                        // ok
                    }
                }
            }
        }));
    }

    // http服务地址
    private final String serviceUrl;
    // 编码格式
    private final String encoding;
    // timeout
    private final Integer readTimeout;
    // 结果转换器
    private final ApiResultParser resultParser;
    // 结果处理器
    private final ApiResultResolver apiResultResolver;
    // 鉴权处理器
    private final ApiAuthHandler apiAuthHandler;
    // 拦截器
    private List<ApiInterceptor> apiInterceptors = Collections.emptyList();
    // http 请求重试handler
    private HttpRequestRetryHandler noRetryHandler = new DefaultHttpRequestRetryHandler(0, false);

    /**
     * 构造函数
     */
    public ApiServiceInvocationHandler(String serviceUrl, String encoding, Integer readTimeout,
            ApiResultParser apiResultParser, ApiResultResolver apiResultResolver,
            ApiAuthHandler apiAuthHandler, List<ApiInterceptor> apiInterceptors) {
        this.serviceUrl = serviceUrl;
        this.encoding = encoding;
        this.readTimeout = readTimeout;
        this.resultParser = apiResultParser;
        this.apiResultResolver = apiResultResolver;
        this.apiAuthHandler = apiAuthHandler;
        this.apiInterceptors = apiInterceptors;
        // 执行排序
        Collections.sort(this.apiInterceptors);
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {

        // 调用object的方法
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        }

        // 验证参数合法性
        validateMethod(method);

        try {
            // 执行上下文初始化
            final ApiInvocation apiInvocation = new ApiInvocation(proxy, method, args, serviceUrl, apiResultResolver);

            // 如果没有返回值，异步执行api
            if (Void.TYPE == apiInvocation.getReturnType()) {
                EXECUTOR_SERVICE.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            internInvoke(apiInvocation);
                        } catch (Exception e) {
                            LOGGER.error("http invoke error，url:" + apiInvocation.getUrl(), e);
                            throw new RuntimeException(e);
                        }
                    }
                });
                return null;
            }

            // 否则，同步执行
            internInvoke(apiInvocation);
            // 结果转换
            return resultParser.parse(apiInvocation);
        } catch (Exception e) {
            LOGGER.error("interface method invoke error，url:" + serviceUrl, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 真正执行的逻辑
     */
    private String internInvoke(ApiInvocation apiInvocation) throws Exception {

        final Method method = apiInvocation.getMethod();

        // 限流
        if (method.isAnnotationPresent(ApiRateLimiter.class)) {
            ApiRateLimiter apiRateLimiter = method.getAnnotation(ApiRateLimiter.class);
            RateLimiter curLimiter = RATE_LIMITER_CACHE.get(method);
            if (null == curLimiter) {
                curLimiter = RateLimiter.create(apiRateLimiter.value());
                RATE_LIMITER_CACHE.putIfAbsent(method, curLimiter);
            }
            curLimiter.acquire();
        }

        final Object[] args = apiInvocation.getArgs();
        // 获取url参数，动态拼接请求
        String rawData = null;
        List<NameValuePair> httpParamList = new ArrayList<NameValuePair>();
        List<NameValuePair> httpHeaderList = new ArrayList<NameValuePair>();
        httpHeaderList.add(new BasicNameValuePair("User-Agent", APP_NAME));
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();
        if (null != args) {
            for (int i = 0; i < args.length; i++) {
                // 如果是resultResolver类型参数，设置result类型
                if (ApiResultResolver.class.isAssignableFrom(parameterTypes[i])) {
                    apiInvocation.setApiResultResolver((ApiResultResolver) args[i]);
                }
                // 如果是基础类型
                else if (parameterTypes[i].isPrimitive() || ApiObjectUtils.isRawType(parameterTypes[i])) {
                    if (paramAnnotations[i].length == 1 && ApiRaw.class.isInstance(paramAnnotations[i][0])) {
                        rawData = "" + args[i];
                    } else {
                        resolveRawTypeParam(paramAnnotations, i, args, httpParamList, httpHeaderList);
                    }
                }
                // 处理复杂类型
                else {
                    resolveComplexTypeParam(args[i], parameterTypes[i], httpParamList);
                }
            }
        }

        // 鉴权逻辑处理
        if (null != apiAuthHandler) {
            apiAuthHandler.handleSign(httpParamList, httpHeaderList);
        }

        // 根据拼接的动态参数，发送请求
        RequestConfig params = buildRequestConfig(apiInvocation);

        // 参数添加
        NameValuePair[] nameValuePairs = new NameValuePair[httpParamList.size()];
        httpParamList.toArray(nameValuePairs);

        // 构造请求method
        ReqMethod strMethod = apiInvocation.getHttpMethod();

        HttpUriRequest httpMethod = HttpMethodFactory
                .createHttpMethod(strMethod, apiInvocation.getUrl(), params, httpHeaderList,
                        nameValuePairs, rawData);

        String result = null;
        long curTime = System.currentTimeMillis();
        int httpStatus = 200;
        HttpResponse response = null;
        try {
            CloseableHttpClient httpClient = getHttpClient(apiInvocation.getRetryCount());
            response = httpClient.execute(httpMethod);
            httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus == 200) {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, encoding);
            } else {
                throw new RuntimeException("http result error,http code:" + response.getStatusLine().getStatusCode());
            }
            apiInvocation.setRawResult(result);
            return result;
        } catch (HttpHostConnectException e) {
            httpStatus = 404;
            throw e;
        } finally {
            long time = System.currentTimeMillis() - curTime;
            // 记录日志
            LOGGER.info("url:" + apiInvocation.getUrl() + ",time:" + (time) +
                        ",params:" + GSON.toJson(nameValuePairs) +
                        ",header:" + GSON.toJson(httpHeaderList) +
                        ",result:" + result);

            // 执行拦截器
            for (ApiInterceptor apiInterceptor : apiInterceptors) {
                try {
                    apiInterceptor
                            .afterCompletion(httpStatus, apiInvocation.getMethod(), apiInvocation.getUrl(),
                                    nameValuePairs,
                                    result, time);
                } catch (Exception e) {
                    LOGGER.warn("interceptor error", e);
                }
            }
            // 回收资源
            if (response != null) {
                closeHttpResponse(response);
            }
        }
    }

    /**
     * 获取http client
     */
    private CloseableHttpClient getHttpClient(int retryCount) {

        CloseableHttpClient client = HTTP_CLIENT_MAP.get(retryCount);
        if (null == client) {
            synchronized (this) {
                client = HTTP_CLIENT_MAP.get(retryCount);
                if (null == client) {
                    // 构造httpClient
                    HttpClientBuilder httpClientBuilder =
                            HttpClients.custom().setConnectionManager(HTTP_CLIENT_CONNECTION_MANAGER);

                    // 重试次数处理
                    if (retryCount > 0) {
                        HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(retryCount, true);
                        httpClientBuilder.setRetryHandler(retryHandler);
                    } else {
                        httpClientBuilder.setRetryHandler(noRetryHandler);
                    }
                    client = httpClientBuilder.build();
                    HTTP_CLIENT_MAP.put(retryCount, client);
                }
            }
        }
        return client;
    }

    /**
     * 处理基本类型函数参数
     */
    private void resolveRawTypeParam(Annotation[][] paramAnnotations, int index, Object[] args,
            List<NameValuePair> httpParamList, List<NameValuePair> httpHeaderList) {
        Annotation[] curParamAnnotations = paramAnnotations[index];
        for (Annotation annotation : curParamAnnotations) {
            Object curParamValue = args[index];
            if (ApiParam.class.isInstance(annotation)) {
                ApiParam apiParam = (ApiParam) annotation;

                if (null == curParamValue || (curParamValue instanceof String && "".equals(curParamValue))) {
                    // 如果必填参数为null，则抛出异常
                    if (apiParam.required()) {
                        throw new IllegalArgumentException("[http api]missing required param:" + apiParam.paramKey());
                    }
                    // 为null的参数是否需要拼接到参数中
                    if (!apiParam.serializeNull()) {
                        return;
                    }
                }
                httpParamList.add(new BasicNameValuePair(apiParam.paramKey(), parseString(curParamValue)));
            } else if (ApiHeader.class.isInstance(annotation)) {
                ApiHeader apiHeader = (ApiHeader) annotation;
                if (null != curParamValue) {
                    httpHeaderList.add(new BasicNameValuePair(apiHeader.headerKey(), parseString(curParamValue)));
                }
            }
        }
    }

    /**
     * 处理复杂类型参数
     */
    private void resolveComplexTypeParam(Object obj, Class parameterType, List<NameValuePair> nameValuePairList) {
        Field[] fields = parameterType.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ApiParam.class)) {
                if (ApiObjectUtils.isRawType(field.getType())) {
                    ApiParam apiParam = field.getAnnotation(ApiParam.class);
                    try {
                        Object value = field.get(obj);
                        if (null == value && apiParam.required()) {
                            throw new IllegalArgumentException("[http api]missing required param:" +
                                                               apiParam.paramKey());
                        }
                        if (null == value && !apiParam.serializeNull()) {
                            continue;
                        }
                        nameValuePairList.add(new BasicNameValuePair(apiParam.paramKey(), parseString(value)));
                    } catch (IllegalAccessException e) {
                        LOGGER.warn("complex param error", e);
                    }
                }
            }
        }
    }

    /**
     * 验证方法合法性，不合法，直接抛出异常
     */
    private void validateMethod(final Method method) {
        // 不存在api描述注解，直接抛出异常
        if (!method.isAnnotationPresent(ApiInfo.class)) {
            throw new IllegalArgumentException("[http api]missing required annotication ApiInfo");
        }
    }

    /**
     * 回收资源
     */
    private void closeHttpResponse(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * object转换为string，参考string.valueof方法，但当object为null时，返回空字符串
     */
    private String parseString(Object obj) {
        String str = (obj == null) ? "" : obj.toString().replace('\t', ' ');
        return StringUtils.trimToEmpty(str);
    }

    /**
     * 执行object方法
     */
    private Object invokeObjectMethod(Object proxy, Method method, Object[] args) throws CloneNotSupportedException {
        String methodName = method.getName();
        if (methodName.equals("toString")) {
            return ApiServiceInvocationHandler.this.toString();
        }
        if (methodName.equals("hashCode")) {
            return serviceUrl.hashCode() * 13 + this.hashCode();
        }
        if (methodName.equals("equals")) {
            return args[0] == proxy;
        }
        if (methodName.equals("clone")) {
            throw new CloneNotSupportedException("clone is not supported for jade dao.");
        }
        throw new UnsupportedOperationException("#" + method.getName());
    }

    /**
     * http请求参数
     */
    private RequestConfig buildRequestConfig(ApiInvocation apiInvocation) {
        if (-1 != apiInvocation.getReadTimeout()) {
            return RequestConfig.custom().setSocketTimeout(apiInvocation.getReadTimeout()).build();
        }
        return RequestConfig.custom().setSocketTimeout(readTimeout).build();
    }
}
