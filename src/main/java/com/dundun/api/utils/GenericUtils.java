
package com.dundun.api.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 实现工具类，检查参数化类型的参数类型。
 *
 * @author han.liao
 */
public abstract class GenericUtils {

    public static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

    /**
     * 从参数, 返回值, 基类的: Generic 类型信息获取传入的实际类信息。
     *
     * @param genericType - Generic 类型信息
     *
     * @return 实际类信息
     */
    public static Class<?>[] getActualClass(Type genericType) {

        if (genericType instanceof ParameterizedType) {

            Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            Class<?>[] actualClasses = new Class<?>[actualTypes.length];

            int i = 0;
            while (i < actualTypes.length) {
                Type actualType = actualTypes[i];
                if (actualType instanceof Class<?>) {
                    actualClasses[i] = (Class<?>) actualType;
                } else if (actualType instanceof GenericArrayType) {
                    Type componentType = ((GenericArrayType) actualType).getGenericComponentType();
                    actualClasses[i] = Array.newInstance((Class<?>) componentType, 0).getClass();
                } else if (actualType instanceof ParameterizedType) {
                    // 只支持第一级
                    Type curType = ((ParameterizedType) actualType).getRawType();
                    actualClasses[i] = (Class<?>) curType;
                }
                i++;
            }

            return actualClasses;
        }

        return EMPTY_CLASSES;
    }
}
