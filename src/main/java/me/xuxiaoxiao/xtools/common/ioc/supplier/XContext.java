package me.xuxiaoxiao.xtools.common.ioc.supplier;

import java.lang.reflect.Field;

public class XContext {
    public final XContext parent;
    public final Object ctxObj;
    public final Field ctxField;

    public XContext(XContext parent, Object ctxObj, Field ctxField) {
        this.parent = parent;
        this.ctxObj = ctxObj;
        this.ctxField = ctxField;
    }
}
