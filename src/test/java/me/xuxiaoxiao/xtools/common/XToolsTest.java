package me.xuxiaoxiao.xtools.common;

import me.xuxiaoxiao.xtools.common.hash.XHashTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.impl.XRequest;
import me.xuxiaoxiao.xtools.common.http.impl.XResponse;
import me.xuxiaoxiao.xtools.common.reflect.XReflectTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class XToolsTest {

    public void mockHashTools(XHashTools mockTools, Runnable runnable) {
        XHashTools origin = XTools.getFieldValue(XTools.class, "hashTools");
        XTools.setTools(mockTools);
        runnable.run();
        XTools.setTools(origin);
    }

    public void mockHttpTools(XHttpTools mockTools, Runnable runnable) {
        XHttpTools origin = XTools.getFieldValue(XTools.class, "httpTools");
        XTools.setTools(mockTools);
        runnable.run();
        XTools.setTools(origin);
    }

    public void mockReflectTools(XReflectTools mockTools, Runnable runnable) {
        XReflectTools origin = XTools.getFieldValue(XTools.class, "reflectTools");
        XTools.setTools(mockTools);
        runnable.run();
        XTools.setTools(origin);
    }

    @Test
    public void testHashTools() {
        XHashTools mockTools = Mockito.mock(XHashTools.class);
        mockHashTools(mockTools, () -> {
            try {
                String hashStr = "hashStr";
                File hashFile = File.createTempFile("test", ".txt");
                Files.write(hashFile.toPath(), Collections.singletonList("filecontent"));

                Mockito.reset(mockTools);
                XTools.md5(hashStr);
                Mockito.verify(mockTools, Mockito.times(1)).hash("MD5", hashStr.getBytes());

                Mockito.reset(mockTools);
                XTools.md5(hashFile);
                Mockito.verify(mockTools, Mockito.times(1)).hash("MD5", hashFile);

                Mockito.reset(mockTools);
                XTools.sha1(hashStr);
                Mockito.verify(mockTools, Mockito.times(1)).hash("SHA-1", hashStr.getBytes());

                Mockito.reset(mockTools);
                XTools.sha1(hashFile);
                Mockito.verify(mockTools, Mockito.times(1)).hash("SHA-1", hashFile);

                Mockito.reset(mockTools);
                XTools.sha256(hashStr);
                Mockito.verify(mockTools, Mockito.times(1)).hash("SHA-256", hashStr.getBytes());

                Mockito.reset(mockTools);
                XTools.sha256(hashFile);
                Mockito.verify(mockTools, Mockito.times(1)).hash("SHA-256", hashFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testEmptyAndBlank() {
        String strEmpty = "";
        String strBlank = "\t\n";
        String strExist = "\ta\n";
        List<String> collectEmpty = new ArrayList<>();
        List<String> collectBlank = Arrays.asList(null, null);
        List<String> collectExist = Arrays.asList(null, "b", null);
        Map<String, Object> mapEmpty = new HashMap<>();
        Map<String, Object> mapBlank = new HashMap<>();
        mapBlank.put(null, null);
        Map<String, Object> mapExist = new HashMap<>();
        mapExist.put("c", null);

        Assertions.assertTrue(XTools.isEmpty(strEmpty));
        Assertions.assertFalse(XTools.isEmpty(strBlank));
        Assertions.assertFalse(XTools.isEmpty(strExist));
        Assertions.assertTrue(XTools.isBlank(strEmpty));
        Assertions.assertTrue(XTools.isBlank(strBlank));
        Assertions.assertFalse(XTools.isBlank(strExist));

        Assertions.assertTrue(XTools.isEmpty(collectEmpty));
        Assertions.assertFalse(XTools.isEmpty(collectBlank));
        Assertions.assertFalse(XTools.isEmpty(collectExist));
        Assertions.assertTrue(XTools.isBlank(collectEmpty));
        Assertions.assertTrue(XTools.isBlank(collectBlank));
        Assertions.assertFalse(XTools.isBlank(collectExist));

        Assertions.assertTrue(XTools.isEmpty(mapEmpty));
        Assertions.assertFalse(XTools.isEmpty(mapBlank));
        Assertions.assertFalse(XTools.isEmpty(mapExist));
        Assertions.assertTrue(XTools.isBlank(mapEmpty));
        Assertions.assertTrue(XTools.isBlank(mapBlank));
        Assertions.assertFalse(XTools.isBlank(mapExist));
    }

    @Test
    public void testStrTrim() {
        String nullStr = null;
        String emptyStr = "";
        String blankStr = "    ";
        String existStr = "  a b c  ";

        Assertions.assertNull(XTools.trim(nullStr));
        Assertions.assertEquals("", XTools.trim(emptyStr));
        Assertions.assertEquals("", XTools.trim(blankStr));
        Assertions.assertEquals("a b c", XTools.trim(existStr));

        String[] strArr = new String[]{nullStr, emptyStr, blankStr, existStr};
        String[] strRet = XTools.trim(strArr);

        Assertions.assertSame(strArr, strRet);
        Assertions.assertNull(strRet[0]);
        Assertions.assertEquals("", strRet[1]);
        Assertions.assertEquals("", strRet[2]);
        Assertions.assertEquals("a b c", strRet[3]);

        Assertions.assertNull(XTools.trim((String[]) null));
    }

    @Test
    public void testJoin() {
        Object[] objArr = new Object[]{1, (short) 2, "3", Long.valueOf("4"), null};
        Assertions.assertEquals("1,2,3,4,null", XTools.join(objArr, ","));

        String[] strArr = new String[]{"a", "b", "c", "d", "e"};
        Assertions.assertEquals("a+b+c+d+e", XTools.join(strArr, "+"));

        List<String> strList = Arrays.asList("A", "B", "C", "D", null);
        Assertions.assertEquals("A B C D null", XTools.join(strList, " "));

        Map<String, Object> strMap = new HashMap<>();
        strMap.put("a", 1);
        strMap.put("b", "2");
        strMap.put("c", null);
        strMap.put("d", "D");
        Assertions.assertEquals("a=1&b=2&c=null&d=D", XTools.join(strMap, "=", "&"));
    }

    @Test
    public void testStrToFileToStream() throws IOException {
        File file1 = XTools.strToFile("hello world", "./testFile1", StandardCharsets.UTF_8.name());
        File file2 = XTools.fileToFile(file1, "./testFile2");
        File file3 = XTools.streamToFile(Files.newInputStream(file2.toPath()), "./testFile3");
        Assertions.assertEquals("hello world", XTools.fileToStr(file3, StandardCharsets.UTF_8.name()));
        file1.deleteOnExit();
        file2.deleteOnExit();
        file3.deleteOnExit();
    }

    @Test
    public void testHttp() {
        XHttpTools mockTools = Mockito.mock(XHttpTools.class);
        mockHttpTools(mockTools, () -> {
            XRequest request = XRequest.GET("http://a.com/api");
            try (XResponse ignore = XTools.http(request)) {
                Mockito.verify(mockTools, Mockito.times(1)).http(request);
            }
        });
    }

    public static class TestReflect {
        private static String sf = "static value";

        private String of = "object value";

        private static String sm(String arg1) {
            return "hello " + arg1;
        }

        private String om(String arg1, int arg2) {
            return "hello " + arg1 + arg2;
        }
    }

    @Test
    public void testReflectTools() {
        XReflectTools mockTools = Mockito.mock(XReflectTools.class);
        mockReflectTools(mockTools, () -> {
            TestReflect testReflect = new TestReflect();

            Mockito.reset(mockTools);
            XTools.getFieldValue(TestReflect.class, "sf");
            Mockito.verify(mockTools, Mockito.times(1)).getFieldValue(TestReflect.class, "sf");

            Mockito.reset(mockTools);
            XTools.getFieldValue(testReflect, "of");
            Mockito.verify(mockTools, Mockito.times(1)).getFieldValue(testReflect, "of");

            Mockito.reset(mockTools);
            XTools.setFieldValue(TestReflect.class, "sf", "set value");
            Mockito.verify(mockTools, Mockito.times(1)).setFieldValue(TestReflect.class, "sf", "set value");

            Mockito.reset(mockTools);
            XTools.setFieldValue(testReflect, "of", "set value");
            Mockito.verify(mockTools, Mockito.times(1)).setFieldValue(testReflect, "of", "set value");

            Mockito.reset(mockTools);
            XTools.invokeMethod(TestReflect.class, "sm", "world");
            Mockito.verify(mockTools, Mockito.times(1)).invokeMethod(TestReflect.class, "sm", "world");

            Mockito.reset(mockTools);
            XTools.invokeMethod(testReflect, "sm", "world");
            Mockito.verify(mockTools, Mockito.times(1)).invokeMethod(testReflect, "sm", "world");
        });
    }
}