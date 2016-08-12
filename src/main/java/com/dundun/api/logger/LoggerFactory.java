
package com.dundun.api.logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dundun.api.logger.log4j.Log4jLoggerAdapter;
import com.dundun.api.logger.slf4j.Slf4jLoggerAdapter;
import com.dundun.api.logger.support.FailsafeLogger;

/**
 * 日志输出器工厂
 *
 * @author william.liangf
 */
public class LoggerFactory {

    private static final ConcurrentMap<String, FailsafeLogger> LOGGERS =
        new ConcurrentHashMap<String, FailsafeLogger>();
    private static volatile LoggerAdapter LOGGER_ADAPTER;

    // 查找常用的日志框架
    static {
        String logger = System.getProperty("httpapi.application.logger");
        if ("slf4j".equals(logger)) {
            setLoggerAdapter(new Slf4jLoggerAdapter());
        } else if ("log4j".equals(logger)) {
            setLoggerAdapter(new Log4jLoggerAdapter());
        } else {
            try {
                setLoggerAdapter(new Log4jLoggerAdapter());
            } catch (Throwable e1) {
                setLoggerAdapter(new Slf4jLoggerAdapter());
            }
        }
    }

    private LoggerFactory() {
    }

    /**
     * 设置日志输出器供给器
     *
     * @param loggerAdapter 日志输出器供给器
     */
    public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
        if (loggerAdapter != null) {
            Logger logger = loggerAdapter.getLogger(LoggerFactory.class.getName());
            logger.info("using logger: " + loggerAdapter.getClass().getName());
            LoggerFactory.LOGGER_ADAPTER = loggerAdapter;
            for (Map.Entry<String, FailsafeLogger> entry : LOGGERS.entrySet()) {
                entry.getValue().setLogger(LOGGER_ADAPTER.getLogger(entry.getKey()));
            }
        }
    }

    /**
     * 获取日志输出器
     *
     * @param key 分类键
     *
     * @return 日志输出器, 后验条件: 不返回null.
     */
    public static Logger getLogger(Class<?> key) {
        FailsafeLogger logger = LOGGERS.get(key.getName());
        if (logger == null) {
            LOGGERS.putIfAbsent(key.getName(), new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key.getName());
        }
        return logger;
    }

    /**
     * 获取日志输出器
     *
     * @param key 分类键
     *
     * @return 日志输出器, 后验条件: 不返回null.
     */
    public static Logger getLogger(String key) {
        FailsafeLogger logger = LOGGERS.get(key);
        if (logger == null) {
            LOGGERS.putIfAbsent(key, new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key);
        }
        return logger;
    }

    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    public static Level getLevel() {
        return LOGGER_ADAPTER.getLevel();
    }

    /**
     * 动态设置输出日志级别
     *
     * @param level 日志级别
     */
    public static void setLevel(Level level) {
        LOGGER_ADAPTER.setLevel(level);
    }

    /**
     * 获取日志文件
     *
     * @return 日志文件
     */
    public static File getFile() {
        return LOGGER_ADAPTER.getFile();
    }

}