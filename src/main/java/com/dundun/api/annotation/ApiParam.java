
package com.dundun.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * desc:api参数
 * author: liudunxu
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {

    /**
     * 请求参数key
     *
     * @return
     */
    String paramKey() default "";

    /**
     * 当参数为null是，是否需要拼接到http参数中
     *
     * @return
     */
    boolean serializeNull() default false;

    /**
     * 是否必须，默认为非必须
     *
     * @return
     */
    boolean required() default false;
}
