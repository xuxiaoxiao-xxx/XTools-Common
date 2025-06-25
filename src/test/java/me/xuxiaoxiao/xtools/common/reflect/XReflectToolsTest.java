package me.xuxiaoxiao.xtools.common.reflect;

import me.xuxiaoxiao.xtools.common.XToolsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XReflectToolsTest {

    @Test
    public void testFieldValue() {
        XReflectTools reflectTools = new XReflectTools();

        XToolsTest.TestReflect testReflect = new XToolsTest.TestReflect();
        Assertions.assertEquals("static value", reflectTools.getFieldValue(XToolsTest.TestReflect.class, "sf"));
        Assertions.assertEquals("object value", reflectTools.getFieldValue(testReflect, "of"));
        try {
            reflectTools.getFieldValue(testReflect, "nf");
            throw new RuntimeException("not reachable");
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof IllegalArgumentException);
        }

        reflectTools.setFieldValue(XToolsTest.TestReflect.class, "sf", "set value");
        reflectTools.setFieldValue(testReflect, "of", "set value");
        Assertions.assertEquals("set value", reflectTools.getFieldValue(XToolsTest.TestReflect.class, "sf"));
        Assertions.assertEquals("set value", reflectTools.getFieldValue(testReflect, "of"));
    }

    @Test
    public void testMethod() {
        XReflectTools reflectTools = new XReflectTools();

        XToolsTest.TestReflect testReflect = new XToolsTest.TestReflect();
        Assertions.assertEquals("hello world", reflectTools.invokeMethod(XToolsTest.TestReflect.class, "sm", "world"));
        Assertions.assertEquals("hello world1", reflectTools.invokeMethod(testReflect, "om", "world", 1));
        try {
            reflectTools.invokeMethod(testReflect, "nm");
            throw new RuntimeException("not reachable");
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof IllegalArgumentException);
        }
    }
}