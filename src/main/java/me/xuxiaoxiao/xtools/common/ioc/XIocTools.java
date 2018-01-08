package me.xuxiaoxiao.xtools.common.ioc;

import me.xuxiaoxiao.xtools.common.ioc.injector.XInjector;
import me.xuxiaoxiao.xtools.common.ioc.supplier.Supply;
import me.xuxiaoxiao.xtools.common.ioc.supplier.XContext;
import me.xuxiaoxiao.xtools.common.ioc.supplier.XSupplier;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 依赖注入工具类
 */
@SuppressWarnings("unchecked")
public final class XIocTools {

    private static final ReentrantReadWriteLock SUPPLIERS_LOCK = new ReentrantReadWriteLock();

    private static final TreeMap<Class<?>, XSupplier> SUPPLIERS = new TreeMap<>(new ClassComparator());

    private XIocTools() {
    }

    public static <T> void register(Class<T> clazz, XSupplier supplier) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(supplier);
        SUPPLIERS_LOCK.writeLock().lock();
        try {
            if (SUPPLIERS.containsKey(clazz)) {
                throw new IllegalStateException(String.format("%s类已经注册生成器", clazz.getName()));
            } else {
                SUPPLIERS.put(clazz, supplier);
            }
        } finally {
            SUPPLIERS_LOCK.writeLock().unlock();
        }
    }

    public static <T> void unregister(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        SUPPLIERS_LOCK.writeLock().lock();
        try {
            SUPPLIERS.remove(clazz);
        } finally {
            SUPPLIERS_LOCK.writeLock().unlock();
        }
    }

    public static <T> T supply(Class<T> clazz, XContext context, XInjector... injectors) throws Exception {
        T target = null;
        SUPPLIERS_LOCK.readLock().lock();
        try {
            for (Class<?> keyClass : SUPPLIERS.keySet()) {
                if (keyClass.isAssignableFrom(clazz)) {
                    target = SUPPLIERS.get(keyClass).supply(clazz, context);
                    if (target == null) {
                        throw new RuntimeException(String.format("未能实例化类%s", clazz.getName()));
                    } else {
                        for (Class<?> targetClass = target.getClass(); !targetClass.equals(Object.class); targetClass = targetClass.getSuperclass()) {
                            for (Field field : targetClass.getDeclaredFields()) {
                                if (field.isAnnotationPresent(Resource.class) || field.isAnnotationPresent(Supply.class)) {
                                    field.setAccessible(true);
                                    if (field.get(target) == null) {
                                        field.set(target, supply(field.getType(), new XContext(context, target, field)));
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } finally {
            SUPPLIERS_LOCK.readLock().unlock();
        }
        return XIocTools.inject(target, injectors);
    }

    public static <T> T inject(T target, XInjector... injectors) throws Exception {
        if (injectors != null && injectors.length > 0) {
            for (XInjector injector : injectors) {
                target = injector.inject(target);
            }
        }
        return target;
    }

    private static final class ClassComparator implements Comparator<Class<?>> {

        @Override
        public int compare(Class<?> class1, Class<?> class2) {
            if (class1.equals(class2)) {
                return 0;
            } else if (class1.isAssignableFrom(class2)) {
                return 1;
            } else if (class2.isAssignableFrom(class1)) {
                return -1;
            } else {
                return class1.hashCode() - class2.hashCode();
            }
        }
    }
}
