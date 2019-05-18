package me.xuxiaoxiao.xtools.common.config.configs.impl;

import me.xuxiaoxiao.xtools.common.config.configs.XConfigs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class XConfigsImpl extends Observable implements XConfigs {
    private final Properties configs = new Properties();
    private final Lock lock = new ReentrantLock();

    @Override
    public String cfgGet(String key) {
        return configs.getProperty(key);
    }

    @Override
    public void cfgSet(String key, String val) {
        Objects.requireNonNull(key, "配置键不能为null");
        Objects.requireNonNull(val, "配置值不能为null");
        lock.lock();
        try {
            Object valOld = configs.setProperty(key, val);
            if (!val.equals(valOld)) {
                setChanged();
                notifyObservers(new String[]{key, valOld == null ? null : String.valueOf(valOld), val});
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String cfgRmv(String key) {
        Objects.requireNonNull(key, "配置键不能为null");
        lock.lock();
        try {
            Object value = configs.remove(key);
            if (value != null) {
                setChanged();
                notifyObservers(new String[]{key, String.valueOf(value), null});
                return String.valueOf(value);
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String cfgDef(String key, String def) {
        Objects.requireNonNull(key, "配置键不能为null");
        Objects.requireNonNull(def, "配置值不能为null");
        lock.lock();
        try {
            if (configs.getProperty(key) == null) {
                configs.setProperty(key, def);
                setChanged();
                notifyObservers(new String[]{key, null, def});
                return def;
            } else {
                return configs.getProperty(key, def);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cfgLoad(String file) throws IOException {
        lock.lock();
        try {
            Enumeration<URL> urls = XConfigsImpl.class.getClassLoader().getResources(file);
            while (urls != null && urls.hasMoreElements()) {
                Properties properties = new Properties();
                properties.load(urls.nextElement().openStream());
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    cfgSet(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
            File config = new File(file);
            if (config.exists() && !config.isDirectory() && config.canRead()) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(config));
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    cfgSet(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cfgIterate(Iteration iteration) {
        lock.lock();
        try {
            Iterator<Map.Entry<Object, Object>> iterator = configs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Object, Object> entry = iterator.next();
                if (iteration.iterate(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()))) {
                    //迭代器返回true，表示删除该配置
                    iterator.remove();
                    setChanged();
                    notifyObservers(new String[]{String.valueOf(entry.getKey()), String.valueOf(entry.getValue()), null});
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cfgClear() {
        cfgIterate(new Iteration() {
            @Override
            public boolean iterate(String key, String value) {
                return true;
            }
        });
    }

    @Override
    public void watcherAdd(String prefix, Watcher watcher) {
        addObserver(new WObserver(prefix, watcher));
    }

    @Override
    public void watcherDel(String prefix, Watcher watcher) {
        deleteObserver(new WObserver(prefix, watcher));
    }

    @Override
    public void watcherClear() {
        deleteObservers();
    }

    private static class WObserver implements Observer {
        public static final int IDX_KEY = 0;
        public static final int IDX_VAL_OLD = 1;
        public static final int IDX_VAL_NEW = 2;

        private final String prefix;
        private final Watcher watcher;

        public WObserver(String prefix, Watcher watcher) {
            this.prefix = prefix.replace("\\.+$", "");
            this.watcher = watcher;
        }

        @Override
        public void update(Observable o, Object arg) {
            String[] update = (String[]) arg;
            if (update[IDX_KEY].equals(prefix) || update[IDX_KEY].startsWith(prefix + ".")) {
                if (update[IDX_VAL_OLD] == null && update[IDX_VAL_NEW] != null) {
                    watcher.onCfgAdd((XConfigs) o, update[IDX_KEY], update[IDX_VAL_NEW]);
                } else if (update[IDX_VAL_OLD] != null && update[IDX_VAL_NEW] == null) {
                    watcher.onCfgDel((XConfigs) o, update[IDX_KEY], update[IDX_VAL_OLD]);
                } else {
                    watcher.onCfgChange((XConfigs) o, update[IDX_KEY], update[IDX_VAL_OLD], update[IDX_VAL_NEW]);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WObserver wObserver = (WObserver) o;
            return Objects.equals(prefix, wObserver.prefix) &&
                    Objects.equals(watcher, wObserver.watcher);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prefix, watcher);
        }
    }
}
