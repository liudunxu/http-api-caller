package com.dundun.api.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * 类型转换器
 * Created by dunxuliu on 2015/8/24.
 */
public final class ApiObjectUtils {

    // 基础数据类型
    private static final Set<Class> RAW_TYPE_CLASSES;

    /**
     * 静态构造函数
     */
    static {
        RAW_TYPE_CLASSES = new HashSet<Class>();
        RAW_TYPE_CLASSES.add(Integer.class);
        RAW_TYPE_CLASSES.add(String.class);
        RAW_TYPE_CLASSES.add(Long.class);
        RAW_TYPE_CLASSES.add(Short.class);
        RAW_TYPE_CLASSES.add(Boolean.class);
    }

    /**
     * 是否是基础类型
     *
     * @param type
     *
     * @return
     */
    public static boolean isRawType(Class type) {
        return RAW_TYPE_CLASSES.contains(type);
    }

    /**
     * Convert <code>val</code> a String parameter to an object of a
     * given type.
     */
    public static Object convertArg(String val, Class type) {

        if (val == null) {
            return null;
        }

        String v = val.trim();
        if (String.class.isAssignableFrom(type)) {
            return val;
        } else if (type == Integer.class || Integer.TYPE.isAssignableFrom(type)) {
            return new Integer(v);
        } else if (type == Long.class || Long.TYPE.isAssignableFrom(type)) {
            return new Long(v);
        } else if (type == Boolean.class || Boolean.TYPE.isAssignableFrom(type)) {
            if ("true".equalsIgnoreCase(v)) {
                return Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(v)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }
}
