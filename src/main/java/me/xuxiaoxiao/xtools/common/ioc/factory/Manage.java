package me.xuxiaoxiao.xtools.common.ioc.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动管理，该注解标注的成员域会被ioc模块的创建和回收方法处理
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Manage {
}
