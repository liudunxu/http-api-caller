
package com.dundun.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dundun.api.parser.ReqMethod;

/**
 * desc:http api调用方法基本信息
 * author: liudunxu
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInfo {
    /**
     * api相对路径url
     *
     * @return
     */
    String url() default "";

    /**
     * api方法
     *
     * @return
     */
    ReqMethod method() default ReqMethod.POST;

    /**
     * 数据字段路径
     *
     * @return
     */
    String resultPath() default "";

    /**
     * 验证字段路径
     *
     * @return
     */
    String validatePath() default "";

    /**
     * 验证规则，正则表达式
     *
     * @return
     */
    String validateRule() default "^0$";

    /**
     * 重试次数，默认不重试
     * @return
     */
    int retryCount() default 0;

    /**
     * 超时时间
     * @return
     */
    int timeout() default -1;
}
