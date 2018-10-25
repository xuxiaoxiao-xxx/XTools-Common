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

    public static void setConfigs(String... configs) {
        clearConfigs();
        addConfigs(configs);
    }

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

    public static void clearConfigs() {
        CONFIGS.clear();
    }

    public static void confSet(String key, String val) {
        CONFIGS.setProperty(key, val);
    }

    public static String confGet(String key) {
        return CONFIGS.getProperty(key);
    }

    public static String confGet(String key, String defaultValue) {
        return CONFIGS.getProperty(key, defaultValue);
    }
}
