package me.xuxiaoxiao.xtools.common.config.configs;

public interface XConfigs {

    /**
     * 获取配置信息
     *
     * @param key 配置键
     * @return 配置值
     */
    String cfgGet(String key);

    /**
     * 设置配置信息
     *
     * @param key 配置键
     * @param val 配置值
     */
    void cfgSet(String key, String val);

    /**
     * 获取或设置配置信息
     *
     * @param key 配置键
     * @param def 配置值为null时设置的默认值
     * @return 当配置值为null时，将def设置为配置值并返回，否则返回原有的配置值并且不做任何更改
     */
    String cfgDef(String key, String def);

    /**
     * 遍历配置信息，在迭代器中的iterate方法可以返回true，表示删除当前迭代的配置项
     *
     * @param iteration 迭代器
     */
    void cfgItr(Iteration iteration);

    /**
     * 配置迭代操作类
     */
    interface Iteration {

        /**
         * 迭代操作方法
         *
         * @param key   当前配置键
         * @param value 当前配置值
         * @return 是否需要删除当前配置
         */
        boolean iterate(String key, String value);
    }
}
