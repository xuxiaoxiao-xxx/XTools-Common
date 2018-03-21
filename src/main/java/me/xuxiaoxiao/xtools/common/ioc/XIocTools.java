package me.xuxiaoxiao.xtools.common.ioc;

import me.xuxiaoxiao.xtools.common.ioc.factory.Manage;
import me.xuxiaoxiao.xtools.common.ioc.factory.XContext;
import me.xuxiaoxiao.xtools.common.ioc.factory.XFactory;
import me.xuxiaoxiao.xtools.common.ioc.injector.XInjector;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 依赖注入工具类
 */
public final class XIocTools {

    private static final ReentrantReadWriteLock FACTORY_LOCK = new ReentrantReadWriteLock();

    private static final TreeMap<Class<?>, XFactory> FACTORY_TREE = new TreeMap<>(new ClassComparator());

    private XIocTools() {
    }

    /**
     * 注册工厂，注册某个类及其子类的实例工厂
     *
     * @param clazz   基准类，注册的工厂将会为这个类或者其子类管理实例
     * @param factory 工厂，基准类的实例管理器
     */
    public static void register(Class<?> clazz, XFactory factory) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(factory);
        FACTORY_LOCK.writeLock().lock();
        try {
            FACTORY_TREE.put(clazz, factory);
        } finally {
            FACTORY_LOCK.writeLock().unlock();
        }
    }

    /**
     * 取消注册工厂，取消注册某个类及其子类的实例工厂
     *
     * @param clazz 基准类
     */
    public static void unregister(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        FACTORY_LOCK.writeLock().lock();
        try {
            FACTORY_TREE.remove(clazz);
        } finally {
            FACTORY_LOCK.writeLock().unlock();
        }
    }

    /**
     * 根据子类优先的顺序查找注册的工厂，提供某个类的实例
     *
     * @param clazz     类对象
     * @param context   实例的上下文
     * @param injectors 注入器，实例化后的对象将会由注入器注入一些数据
     * @param <T>       类模板
     * @return 类实例
     * @throws Exception 实例化时可能抛出异常
     */
    public static <T> T supply(Class<T> clazz, XContext context, XInjector... injectors) throws Exception {
        T target = null;
        FACTORY_LOCK.readLock().lock();
        try {
            for (Map.Entry<Class<?>, XFactory> entry : FACTORY_TREE.entrySet()) {
                if (entry.getKey().isAssignableFrom(clazz)) {
                    target = entry.getValue().supply(clazz, context);
                    Objects.requireNonNull(target, String.format("未能实例化类%s", clazz.getName()));
                    for (Class<?> targetClass = target.getClass(); !Object.class.equals(targetClass); targetClass = targetClass.getSuperclass()) {
                        for (Field field : targetClass.getDeclaredFields()) {
                            if (field.isAnnotationPresent(Manage.class)) {
                                field.setAccessible(true);
                                if (field.get(target) == null) {
                                    field.set(target, supply(field.getType(), new XContext(context, target, field)));
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } finally {
            FACTORY_LOCK.readLock().unlock();
        }
        Objects.requireNonNull(target, String.format("未能实例化类%s", clazz.getName()));
        return XIocTools.inject(target, injectors);
    }

    /**
     * 根据子类优先的顺序查找注册的工厂，回收类的实例
     *
     * @param target  待回收的类的实例
     * @param context 实例的上下文
     * @param <T>     类模板
     * @return 被回收后的类的实例
     * @throws Exception 回收时可能会出现异常
     */
    public static <T> T recycle(T target, XContext context) throws Exception {
        for (Class<?> targetClass = target.getClass(); !Object.class.equals(targetClass); targetClass = targetClass.getSuperclass()) {
            for (Field field : targetClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Manage.class)) {
                    field.setAccessible(true);
                    if (field.get(target) != null) {
                        field.set(target, recycle(field.get(target), new XContext(context, target, field)));
                    }
                }
            }
        }
        FACTORY_LOCK.readLock().lock();
        try {
            for (Map.Entry<Class<?>, XFactory> entry : FACTORY_TREE.entrySet()) {
                if (entry.getKey().isAssignableFrom(target.getClass())) {
                    return entry.getValue().recycle(target, null);
                }
            }
        } finally {
            FACTORY_LOCK.readLock().unlock();
        }
        return target;
    }

    /**
     * 为类的实例注入数据
     *
     * @param target    类的实例
     * @param injectors 注入器
     * @param <T>       类模板
     * @return 注入数据后的类的实例
     * @throws Exception 注入数据时可能发生异常
     */
    public static <T> T inject(T target, XInjector... injectors) throws Exception {
        if (injectors != null && injectors.length > 0) {
            for (XInjector injector : injectors) {
                target = injector.inject(target);
            }
        }
        return target;
    }

    /**
     * 类比较器，子类在前，父类在后
     */
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
