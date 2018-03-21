package me.xuxiaoxiao.xtools.common.ioc.factory;

import java.lang.reflect.Field;

/**
 * 创建和回收的过程的上下文，包括了父级的上下文、当前所在实例和实例的成员域。
 */
public class XContext {
    /**
     * 父级上下文
     */
    public final XContext parent;
    /**
     * 当前所在的实例
     */
    public final Object object;
    /**
     * 当前处理的成员域
     */
    public final Field field;

    public XContext(XContext parent, Object object, Field field) {
        this.parent = parent;
        this.object = object;
        this.field = field;
    }
}
