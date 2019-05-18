package me.xuxiaoxiao.xtools.common.config.configs;

import java.io.IOException;

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
     * 移除配置信息
     *
     * @param key 配置键
     */
    String cfgRmv(String key);

    /**
     * 获取或设置配置信息
     *
     * @param key 配置键
     * @param def 配置值为null时设置的默认值
     * @return 当配置值为null时，将def设置为配置值并返回，否则返回原有的配置值并且不做任何更改
     */
    String cfgDef(String key, String def);

    /**
     * 加载文件中的配置信息
     *
     * @param file 配置文件
     */
    void cfgLoad(String file) throws IOException;

    /**
     * 遍历配置信息，在迭代器中的iterate方法可以返回true，表示删除当前迭代的配置项
     *
     * @param iteration 迭代器
     */
    void cfgIterate(Iteration iteration);

    /**
     * 清除所有配置信息
     */
    void cfgClear();

    /**
     * 添加配置观察者
     *
     * @param watcher 配置观察者
     */
    void watcherAdd(String prefix, Watcher watcher);

    /**
     * 删除配置观察者
     *
     * @param watcher 配置观察者
     */
    void watcherDel(String prefix, Watcher watcher);

    /**
     * 清空配置观察者
     */
    void watcherClear();

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

    /**
     * 配置信息观察者
     */
    interface Watcher {
        /**
         * 配置信息新增
         *
         * @param configs 配置库
         * @param key     新增的配置键
         * @param val     新增的配置值
         */
        void onCfgAdd(XConfigs configs, String key, String val);

        /**
         * 配置信息被删除
         *
         * @param configs 配置库
         * @param key     删除的配置键
         * @param val     删除的配置值
         */
        void onCfgDel(XConfigs configs, String key, String val);

        /**
         * 配置信息变化
         *
         * @param configs 配置库
         * @param key     变化的配置键
         * @param valOld  变化前的配置值
         * @param valNew  变化后的配置值
         */
        void onCfgChange(XConfigs configs, String key, String valOld, String valNew);
    }
}
