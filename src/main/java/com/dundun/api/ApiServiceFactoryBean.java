
package com.dundun.api;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.Ordered;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

import com.dundun.api.auth.ApiVoidAuthHandler;
import com.dundun.api.parser.ApiResultJsonParser;
import com.dundun.api.resolver.ApiResultJsonResovler;

/**
 * desc:http api服务接口工厂bean
 * author: liudunxu
 */
public class ApiServiceFactoryBean extends UrlBasedRemoteAccessor implements FactoryBean, BeanClassLoaderAware,
                                                                                 BeanFactoryAware,Ordered {

    // 请求参数编码
    private String encoding = "UTF-8";
    // 结果处理器
    private ApiResultResolver resultResolver = new ApiResultJsonResovler();
    // 结果转换器
    private ApiResultParser resultParser = new ApiResultJsonParser();
    // 鉴权处理器
    private ApiAuthHandler apiAuthHandler = new ApiVoidAuthHandler();
    // 拦截器
    private List<ApiInterceptor> apiInterceptors = new ArrayList<ApiInterceptor>();
    // 拦截器名称
    private String[] interceptorNames = new String[0];
    private transient BeanFactory beanFactory;
    // 超时时间
    private Integer timeout = 5000;

    /**
     * 工厂方法
     *
     * @return
     *
     * @throws Exception
     */
    @Override
    public Object getObject() throws Exception {
        for (String interceptor : interceptorNames) {
            apiInterceptors.add(beanFactory.getBean(interceptor, ApiInterceptor.class));
        }

        ApiServiceInvocationHandler handler =
            new ApiServiceInvocationHandler(getServiceUrl(), encoding, timeout, resultParser, resultResolver,
                                               apiAuthHandler, apiInterceptors);
        return Proxy.newProxyInstance(getBeanClassLoader(), new Class[] {getServiceInterface()}, handler);
    }

    @Override
    public Class getObjectType() {
        return getServiceInterface();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setResultResolver(ApiResultResolver resultResolver) {
        this.resultResolver = resultResolver;
    }

    public void setResultParser(ApiResultParser resultParser) {
        this.resultParser = resultParser;
    }

    public void setApiAuthHandler(ApiAuthHandler apiAuthHandler) {
        this.apiAuthHandler = apiAuthHandler;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public void setInterceptorNames(String[] interceptorNames) {
        this.interceptorNames = interceptorNames;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
