package me.xuxiaoxiao.xtools.common.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 配置工具，此工具被其他依赖，所以此工具不能使用其他工具
 */
public final class XConfigTools {
    private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
    private static final Properties CONFIGS = new Properties();

    static {
        //静默读取默认的配置文件
        URL configURL = XConfigTools.class.getClassLoader().getResource("config.properties");
        if (configURL != null) {
            try {
                setConfigFiles(URLDecoder.decode(configURL.getFile(), "UTF-8"));
            } catch (Exception e) {
                System.err.println(String.format("读取默认的配置文件出错:%s", configURL));
                e.printStackTrace();
            }
        }
    }

    private XConfigTools() {
    }

    /**
     * 清除所有配置，并设置配置文件
     *
     * @param files 配置文件的classpath路径
     * @throws IOException 设置配置文件时可能会发生异常
     */
    public static void setConfigFiles(String... files) throws IOException {
        try {
            RW_LOCK.writeLock().lock();
            clearConfigs();
            addConfigFiles(files);
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    /**
     * 追加新的配置文件
     *
     * @param configs 配置文件的classpath路径
     * @throws IOException 追加配置文件时可能会发生异常
     */
    public static void addConfigFiles(String... configs) throws IOException {
        try {
            RW_LOCK.writeLock().lock();
            if (configs != null && configs.length > 0) {
                for (String config : configs) {
                    try (FileInputStream finStream = new FileInputStream(config)) {
                        CONFIGS.load(finStream);
                    }
                }
            }
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    /**
     * 清除所有配置文件
     */
    public static void clearConfigs() {
        try {
            RW_LOCK.writeLock().lock();
            CONFIGS.clear();
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    /**
     * 获取配置信息值
     *
     * @param key 配置信息键名
     * @return 配置信息值
     */
    public static String cfgGet(String key) {
        Objects.requireNonNull(key, "配置键不能为null");
        try {
            RW_LOCK.readLock().lock();
            return CONFIGS.getProperty(key);
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    /**
     * 获取或设置配置信息
     *
     * @param key 配置信息键名
     * @param def 配置信息为null时设置的默认值
     * @return 当配置信息值为null时，将def设置为配置信息的值并返回，否则返回原有的配置信息值并且不做任何更改
     */
    public static String cfgDef(String key, String def) {
        Objects.requireNonNull(key, "配置键不能为null");
        Objects.requireNonNull(def, "配置值不能为null");
        try {
            RW_LOCK.writeLock().lock();
            if (CONFIGS.getProperty(key) == null) {
                CONFIGS.setProperty(key, def);
                return def;
            } else {
                return CONFIGS.getProperty(key, def);
            }
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    /**
     * 设置配置信息
     *
     * @param key 配置信息键名
     * @param val 配置信息值
     */
    public static void cfgSet(String key, String val) {
        Objects.requireNonNull(key, "配置键不能为null");
        Objects.requireNonNull(val, "配置值不能为null");
        try {
            RW_LOCK.writeLock().lock();
            CONFIGS.setProperty(key, val);
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    public static void cfgIterate(Iteration iteration) {
        try {
            RW_LOCK.readLock().lock();
            for (Map.Entry<Object, Object> entry : CONFIGS.entrySet()) {
                iteration.action(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    public static <T> T supply(String clazzName) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            if (XSupplier.class.isAssignableFrom(clazz)) {
                return ((XSupplier<T>) clazz.newInstance()).supply();
            } else {
                return (T) clazz.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ClassCastException) {
                throw new ClassCastException(String.format("使用[ %s ]获取实例时出错：类型不匹配", clazzName));
            } else {
                throw new IllegalArgumentException(String.format("使用[ %s ]获取实例时出错：无法实例化", clazzName));
            }
        }
    }

    public interface Iteration {

        void action(String key, String value);
    }
}
