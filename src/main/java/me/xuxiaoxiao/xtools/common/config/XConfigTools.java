package me.xuxiaoxiao.xtools.common.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Properties;

public class XConfigTools {
    private static final Properties CONFIGS = new Properties();

    static {
        //静默读取默认的配置文件
        URL configURL = XConfigTools.class.getClassLoader().getResource("config.properties");
        if (configURL != null) {
            try {
                setConfigs(URLDecoder.decode(configURL.getFile(), "UTF-8"));
            } catch (Exception e) {
                System.err.println(String.format("读取默认的配置文件出错:%s", configURL));
            }
        }
    }

    private XConfigTools() {
    }

    /**
     * 清除所有配置，并设置配置文件
     *
     * @param configs 配置文件的classpath路径
     */
    public static void setConfigs(String... configs) {
        clearConfigs();
        addConfigs(configs);
    }

    /**
     * 追加新的配置文件
     *
     * @param configs 配置文件的classpath路径
     */
    public static void addConfigs(String... configs) {
        try {
            if (configs != null && configs.length > 0) {
                for (String config : configs) {
                    try (FileInputStream finStream = new FileInputStream(config)) {
                        CONFIGS.load(finStream);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("配置文件%s读取失败", Arrays.toString(configs)));
        }
    }

    /**
     * 清除所有配置文件
     */
    public static void clearConfigs() {
        CONFIGS.clear();
    }

    /**
     * 获取配置信息值
     *
     * @param key 配置信息键名
     * @return 配置信息值
     */
    public static String confGet(String key) {
        return CONFIGS.getProperty(key);
    }

    /**
     * 获取或设置配置信息
     *
     * @param key 配置信息键名
     * @param def 配置信息为null时设置的默认值
     * @return 当配置信息值为null时，将def设置为配置信息的值并返回，否则返回原有的配置信息值
     */
    public static String confDef(String key, String def) {
        if (CONFIGS.getProperty(key) == null) {
            CONFIGS.setProperty(key, def);
            return def;
        } else {
            return CONFIGS.getProperty(key, def);
        }
    }

    /**
     * 设置配置信息
     *
     * @param key 配置信息键名
     * @param val 配置信息值
     */
    public static void confSet(String key, String val) {
        CONFIGS.setProperty(key, val);
    }
}
