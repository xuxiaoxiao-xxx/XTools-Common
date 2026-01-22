package me.xuxiaoxiao.xtools.common;

import me.xuxiaoxiao.xtools.common.hash.XHashTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.impl.XRequest;
import me.xuxiaoxiao.xtools.common.http.impl.XResponse;
import me.xuxiaoxiao.xtools.common.reflect.XReflectTools;
import me.xuxiaoxiao.xtools.common.time.XTimeTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 常用的基本的函数
 */
public final class XTools {
    private static XHashTools hashTools = new XHashTools(new XHashTools.Config());
    private static XHttpTools httpTools = new XHttpTools(new XHttpTools.Config());
    private static XReflectTools reflectTools = new XReflectTools();

    private XTools() {
    }

    public static void setTools(XHashTools hashTools) {
        XTools.hashTools = hashTools;
    }

    public static void setTools(XHttpTools httpTools) {
        XTools.httpTools = httpTools;
    }

    public static void setTools(XReflectTools reflectTools) {
        XTools.reflectTools = reflectTools;
    }

    /**
     * 字符串MD5散列
     *
     * @param str 被散列的字符串
     * @return MD5散列结果，全小写字母
     */
    @Nonnull
    public static String md5(@Nonnull String str) {
        return hashTools.hash(XHashTools.HASH_MD5, str.getBytes());
    }

    /**
     * 文件MD5散列
     *
     * @param file 被散列的文件
     * @return MD5散列结果，全小写字母
     */
    @Nonnull
    public static String md5(@Nonnull File file) {
        return hashTools.hash(XHashTools.HASH_MD5, file);
    }

    /**
     * 字符串SHA1散列
     *
     * @param str 被散列的字符串
     * @return SHA1散列结果，全小写字母
     */
    @Nonnull
    public static String sha1(@Nonnull String str) {
        return hashTools.hash(XHashTools.HASH_SHA1, str.getBytes());
    }

    /**
     * 文件SHA1散列
     *
     * @param file 被散列的文件
     * @return SHA1散列结果，全小写字母
     */
    @Nonnull
    public static String sha1(@Nonnull File file) {
        return hashTools.hash(XHashTools.HASH_SHA1, file);
    }

    /**
     * 字符串SHA256散列
     *
     * @param str 被散列的字符串
     * @return SHA256散列结果，全小写字母
     */
    @Nonnull
    public static String sha256(@Nonnull String str) {
        return hashTools.hash(XHashTools.HASH_SHA256, str.getBytes());
    }

    /**
     * 文件SHA256散列
     *
     * @param file 被散列的文件
     * @return SHA256散列结果，全小写字母
     */
    @Nonnull
    public static String sha256(@Nonnull File file) {
        return hashTools.hash(XHashTools.HASH_SHA256, file);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 要判断的字符串
     * @return str == null || str.isEmpty()
     */
    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断数组是否为空
     *
     * @param array 要判断的数组
     * @return collection == null || collection.isEmpty()
     */
    public static <T> boolean isEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断集合是否为空
     *
     * @param collection 要判断的集合
     * @return collection == null || collection.isEmpty()
     */
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断映射是否为空
     *
     * @param map 要判断的字映射
     * @return map == null || map.isEmpty()
     */
    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断字符串是否为空或全是空白符
     *
     * @param str 要判断的字符串
     * @return str == null || str.trim().isEmpty()
     */
    public static boolean isBlank(@Nullable String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断数组是否为空或全是null元素
     *
     * @param array 要判断的数组
     * @return true: 数组里没有有意义的元素；false: 数组中至少有一个非null的元素
     */
    public static <T> boolean isBlank(@Nullable T[] array) {
        if (!XTools.isEmpty(array)) {
            for (T t : array) {
                if (t != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断集合是否为空或全为null元素
     *
     * @param collection 要判断的集合
     * @return true: 集合里没有有意义的元素；false: 集合中至少有一个非null的元素
     */
    public static boolean isBlank(@Nullable Collection<?> collection) {
        if (!XTools.isEmpty(collection)) {
            for (Object o : collection) {
                if (o != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断映射是否为空或全为null键值对
     *
     * @param map 要判断的字映射
     * @return true: 映射里没有有意义的键值对；false: 映射中至少有一个非null的键或值
     */
    public static boolean isBlank(@Nullable Map<?, ?> map) {
        if (!XTools.isEmpty(map)) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null || entry.getValue() != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 去除字符串前后的空白符，如果为null则返回null
     *
     * @param str 需要去除前后空白符的字符串
     * @return 去除前后空白符的字符串，如果传入的字符串为null则返回null
     */
    @Nullable
    public static String trim(@Nullable String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除字符串数组中每个元素的前后的空白符，如果为null则返回null
     *
     * @param strs 需要去除前后空白符的字符串数组
     * @return 去除每个元素前后空白符的字符串数组，如果传入的字符串数组为null则返回null，字符串中元素为null的返回的数据中也是null
     */
    @Nullable
    public static String[] trim(@Nullable String[] strs) {
        if (strs == null) {
            return null;
        } else {
            for (int i = 0, len = strs.length; i < len; i++) {
                strs[i] = (strs[i] == null ? null : strs[i].trim());
            }
            return strs;
        }
    }

    /**
     * 将数组用一个固定的字符串连接起来
     *
     * @param array 数组
     * @param glue  用于连接的字符串
     * @return 连接后的字符串
     */
    @Nonnull
    public static <T> String join(@Nonnull T[] array, @Nonnull String glue) {
        StringJoiner joiner = new StringJoiner(glue);
        for (T t : array) {
            joiner.add(String.valueOf(t));
        }
        return joiner.toString();
    }

    /**
     * 将集合元素用一个固定的字符串连接起来
     *
     * @param collection 需要连接的集合
     * @param glue       用于连接的字符串
     * @return 连接后的字符串
     */
    @Nonnull
    public static String join(@Nonnull Collection<?> collection, @Nonnull String glue) {
        StringJoiner joiner = new StringJoiner(glue);
        for (Object obj : collection) {
            joiner.add(String.valueOf(obj));
        }
        return joiner.toString();
    }

    /**
     * 将映射用固定的字符串连接起来
     *
     * @param map       键值对集合
     * @param glueInner 连接键和值的字符串
     * @param glueOuter 连接键值对之间的字符串
     * @return 拼接后的字符串
     */
    @Nonnull
    public static String join(@Nonnull Map<?, ?> map, @Nonnull String glueInner, @Nonnull String glueOuter) {
        StringJoiner joiner = new StringJoiner(glueOuter);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            joiner.add(entry.getKey() + glueInner + entry.getValue());
        }
        return joiner.toString();
    }

    /**
     * 将字符串保存成文件
     *
     * @param str     要保存成文件的字符串
     * @param path    保存文件的位置
     * @param charset 字符串的编码格式
     * @return 保存后的文件
     * @throws IOException 在保存时可能会发生IO异常
     */
    @Nonnull
    public static File strToFile(@Nullable String str, @Nonnull String path, @Nonnull String charset) throws IOException {
        File file = new File(path);
        try (BufferedOutputStream outStream = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            outStream.write(str == null ? new byte[0] : str.getBytes(charset));
            outStream.flush();
        }
        return file;
    }

    /**
     * 将文件读取成字符串
     *
     * @param file    要读取成字符串的文件
     * @param charset 字符串的编码格式
     * @return 读取出的字符串
     * @throws IOException 在读取时可能会发生IO异常
     */
    @Nonnull
    public static String fileToStr(@Nonnull File file, @Nonnull String charset) throws IOException {
        try (FileInputStream fInStream = new FileInputStream(file)) {
            return streamToStr(fInStream, charset);
        }
    }

    /**
     * 将文件复制到另外一个文件
     *
     * @param file 源文件
     * @param path 目标文件的地址
     * @return 目标文件
     * @throws IOException 在复制时可能会发生IO异常
     */
    @Nonnull
    public static File fileToFile(@Nonnull File file, @Nonnull String path) throws IOException {
        File fileTo = new File(path);
        try (FileInputStream fInStream = new FileInputStream(file); FileOutputStream fOutStream = new FileOutputStream(fileTo)) {
            streamToStream(fInStream, fOutStream);
            return fileTo;
        }
    }

    /**
     * 将输入流中的全部数据读取成字符串
     *
     * @param inStream 要读取的输入流，不会关闭该输入流
     * @param charset  字符串的编码格式
     * @return 读取出的字符串
     * @throws IOException 在读取时可能会发生IO异常
     */
    @Nonnull
    public static String streamToStr(@Nonnull InputStream inStream, @Nonnull String charset) throws IOException {
        int count;
        char[] buffer = new char[1024];
        StringBuilder sbStr = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inStream, charset));
        while ((count = bufReader.read(buffer)) > 0) {
            sbStr.append(buffer, 0, count);
        }
        return sbStr.toString();
    }

    /**
     * 将输入流中的全部数据读取成文件
     *
     * @param inStream 要读取的输入流，不会关闭该输入流
     * @param path     要保存的文件的位置
     * @return 保存后的文件
     * @throws IOException 在读取时可能会发生IO异常
     */
    @Nonnull
    public static File streamToFile(@Nonnull InputStream inStream, @Nonnull String path) throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        File file = new File(path);
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        try (BufferedOutputStream outStream = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            while ((count = bufInStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, count);
            }
            outStream.flush();
        }
        return file;
    }

    /**
     * 将输入流中的全部数据读取到输出流
     *
     * @param inStream  要读取的输入流，不会关闭该输入流
     * @param outStream 要写入的输出流，不会关闭该输出流
     * @throws IOException 输入输出时可能会发生IO异常
     */
    public static void streamToStream(@Nonnull InputStream inStream, @Nonnull OutputStream outStream) throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        BufferedOutputStream bufOutStream = new BufferedOutputStream(outStream);
        while ((count = bufInStream.read(buffer)) > 0) {
            bufOutStream.write(buffer, 0, count);
        }
        bufOutStream.flush();
    }

    /**
     * 获取任意一天的类型
     *
     * @param date 任意一天的date对象
     * @return 任意一天的类型。1：工作日(XTimeTools.WORKDAY)，2：公休日(XTimeTools.RESTDAY)，3：节假日(XTimeTools.HOLIDAY)
     */
    public static int dateType(@Nonnull Date date) {
        return XTimeTools.dateType(date);
    }

    /**
     * 将date对象转换成相应格式的字符串，线程安全
     *
     * @param date date对象
     * @return 相应格式的字符串
     */
    @Nonnull
    public static String dateFormat(@Nonnull Date date) {
        return XTimeTools.dateFormat(XTimeTools.FORMAT_YMDHMS, date);
    }

    /**
     * 将date对象转换成相应格式的字符串，线程安全
     *
     * @param format 格式字符串
     * @param date   date对象
     * @return 相应格式的字符串
     */
    @Nonnull
    public static String dateFormat(@Nonnull String format, @Nonnull Date date) {
        return XTimeTools.dateFormat(format, date);
    }

    /**
     * 将日期字符串转换成相应的date对象，线程安全
     *
     * @param dateStr 日期字符串
     * @return 相应的date对象
     */
    @Nonnull
    public static Date dateParse(@Nonnull String dateStr) {
        return XTimeTools.dateParse(XTimeTools.FORMAT_YMDHMS, dateStr);
    }

    /**
     * 将日期字符串转换成相应的date对象，线程安全
     *
     * @param format  格式字符串
     * @param dateStr 日期字符串
     * @return 相应的date对象
     */
    @Nonnull
    public static Date dateParse(@Nonnull String format, @Nonnull String dateStr) {
        return XTimeTools.dateParse(format, dateStr);
    }

    /**
     * 获取以任意时间所在的那一天为基准偏移若干天的00:00:00时刻的date对象
     *
     * @param base   任意时间的date对象，如果为null则以当前时间为基准
     * @param offset 偏移的天数
     * @return 以base所在的那一天为基准偏移offset天00:00:00时刻的date对象
     */
    @Nonnull
    public static Date date(@Nullable Date base, int offset) {
        return XTimeTools.date(base, offset);
    }

    /**
     * 获取以某天所在的那一周为基准偏移若干周的某天00:00:00时刻的date对象
     *
     * @param base       基准时间的date对象，如果为null则以当前时间为基准
     * @param weekOffset 偏移的周数
     * @param dayIndex   那一周的第几天（每周的第一天是周一，周一为0）
     * @return 以base所在的那一周为基准偏移weekOffset周的dayIndex天00:00:00时刻的date对象
     */
    @Nonnull
    public static Date dateByWeek(@Nullable Date base, int weekOffset, int dayIndex) {
        return XTimeTools.dateByWeek(base, weekOffset, dayIndex);
    }

    /**
     * 获取以某天所在的那一月为基准偏移若干月的某天00:00:00时刻的date对象
     *
     * @param base        基准时间的date对象，如果为null则以当前时间为基准
     * @param monthOffset 偏移的月数
     * @param dayIndex    那一月的第几天（一号为0）
     * @return 以base所在的那一月为基准偏移monthOffset月的dayIndex天00:00:00时刻的date对象
     */
    @Nonnull
    public static Date dateByMonth(@Nullable Date base, int monthOffset, int dayIndex) {
        return XTimeTools.dateByMonth(base, monthOffset, dayIndex);
    }

    /**
     * 获取以某天所在的那一季度为基准偏移若干季度的某天00:00:00时刻的date对象
     *
     * @param base         基准时间的date对象，如果为null则以当前时间为基准
     * @param seasonOffset 偏移的季度数
     * @param dayIndex     那一季度的第几天（一号为0）
     * @return 以base所在的那一季度为基准偏移seasonOffset季度的dayIndex天00:00:00时刻的date对象
     */
    @Nonnull
    public static Date dateBySeason(@Nullable Date base, int seasonOffset, int dayIndex) {
        return XTimeTools.dateBySeason(base, seasonOffset, dayIndex);
    }

    /**
     * 获取以某天所在的那一年为基准偏移若干年的某天00:00:00时刻的date对象
     *
     * @param base       基准时间的date对象，如果为null则以当前时间为基准
     * @param yearOffset 偏移的年数
     * @param dayIndex   那一年的第几天（第一天为0）
     * @return 以base所在的那一年为基准偏移yearOffset年的dayIndex天00:00:00时刻的date对象
     */
    @Nonnull
    public static Date dateByYear(@Nullable Date base, int yearOffset, int dayIndex) {
        return XTimeTools.dateByYear(base, yearOffset, dayIndex);
    }

    /**
     * 获取以某天所在的那一月为基准偏移若干月的某周的周一00:00:00时刻的date对象
     *
     * @param base        基准时间的date对象，如果为null则以当前时间为基准
     * @param monthOffset 偏移的月数
     * @param weekIndex   那一月的第几周（每周的第一天是周一，每月的第一周是第一个周一所在的那一周，第一周为0）
     * @return 以base所在的那一月为基准偏移monthOffset月的weekIndex周的周一00:00:00时刻的date对象
     */
    @Nonnull
    public static Date weekByMonth(@Nullable Date base, int monthOffset, int weekIndex) {
        return XTimeTools.weekByMonth(base, monthOffset, weekIndex);
    }

    /**
     * 获取以某天所在的那一季度为基准偏移若干季度的某周的周一00:00:00时刻的date对象
     *
     * @param base         基准时间的date对象，如果为null则以当前时间为基准
     * @param seasonOffset 偏移的季度数
     * @param weekIndex    那一季度的第几周（每周的第一天是周一，每季度的第一周是第一个周一所在的那一周，第一周为0）
     * @return 以base所在的那一季度为基准偏移seasonOffset季度的weekIndex周的周一00:00:00时刻的date对象
     */
    @Nonnull
    public static Date weekBySeason(@Nullable Date base, int seasonOffset, int weekIndex) {
        return XTimeTools.weekBySeason(base, seasonOffset, weekIndex);
    }

    /**
     * 获取以某天所在的那一年为基准偏移若干年的某周的周一00:00:00时刻的date对象
     *
     * @param base       基准时间的date对象，如果为null则以当前时间为基准
     * @param yearOffset 偏移的年数
     * @param weekIndex  那一年的第几周（每周的第一天是周一，每年的第一周是第一个周一所在的那一周，第一周为0）
     * @return 以base所在的那一年为基准偏移yearOffset年的monthIndex月的weekIndex周的周一00:00:00时刻的date对象
     */
    @Nonnull
    public static Date weekByYear(@Nullable Date base, int yearOffset, int weekIndex) {
        return XTimeTools.weekByYear(base, yearOffset, weekIndex);
    }

    /**
     * 获取以某天所在的那一季度为基准偏移若干季度的某月的一号00:00:00时刻的date对象
     *
     * @param base         基准时间的date对象，如果为null则以当前时间为基准
     * @param seasonOffset 偏移的季度数
     * @param monthIndex   那一季度的第几个月（第一个月为0）
     * @return 以base所在的那一季度为基准偏移seasonOffset季度的monthIndex月的一号00:00:00时刻的date对象
     */
    @Nonnull
    public static Date monthBySeason(@Nullable Date base, int seasonOffset, int monthIndex) {
        return XTimeTools.monthBySeason(base, seasonOffset, monthIndex);
    }

    /**
     * 获取以某天所在的那一年为基准偏移若干年的某月的一号00:00:00时刻的date对象
     *
     * @param base       基准时间的date对象，如果为null则以当前时间为基准
     * @param yearOffset 偏移的年数
     * @param monthIndex 那一年的第几个月（一月为0）
     * @return 以base所在的那一年为基准偏移yearOffset年的monthIndex月的weekIndex周的周一00:00:00时刻的date对象
     */
    @Nonnull
    public static Date monthByYear(@Nullable Date base, int yearOffset, int monthIndex) {
        return XTimeTools.monthByYear(base, yearOffset, monthIndex);
    }

    /**
     * 获取以某天所在的那一年为基准偏移若干年的某季度的第一天00:00:00时刻的date对象
     *
     * @param base        基准时间的date对象，如果为null则以当前时间为基准
     * @param yearOffset  偏移的年数
     * @param seasonIndex 那一年的第几个季度（第一个季度为0）
     * @return 以base所在的那一年为基准偏移yearOffset年的seasonIndex月的第一天00:00:00时刻的date对象
     */
    @Nonnull
    public static Date seasonByYear(@Nullable Date base, int yearOffset, int seasonIndex) {
        return XTimeTools.seasonByYear(base, yearOffset, seasonIndex);
    }

    /**
     * 获取任意一天是一周中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那周中的第几天（每周的第一天是周一，周一为0）
     */
    public static int dateInWeek(@Nullable Date base) {
        return XTimeTools.dateInWeek(base);
    }

    /**
     * 获取任意一天是一个月中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那个月中的第几天（一号为0）
     */
    public static int dateInMonth(@Nullable Date base) {
        return XTimeTools.dateInMonth(base);
    }

    /**
     * 获取任意一天是一个季度中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那个季度中的第几天（第一天为0）
     */
    public static int dateInSeason(@Nullable Date base) {
        return XTimeTools.dateInSeason(base);
    }

    /**
     * 获取任意一天是一年中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那一年中的第几天（第一天为0）
     */
    public static int dateInYear(@Nullable Date base) {
        return XTimeTools.dateInYear(base);
    }

    /**
     * 获取任意一天所在的周是哪个月的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪个月的第几周（正数表示本月第几周，负数表示上月第几周,例：1=本月第二周，-3=上月第四周）
     */
    public static int weekInMonth(@Nullable Date base) {
        return XTimeTools.weekInMonth(base);
    }

    /**
     * 获取任意一天所在的周是哪一季度的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪一季度的第几周（正数表示本季度第几周，负数表示上季度第几周,例：1=本季第第二周，-12=上季度第13周）
     */
    public static int weekInSeason(@Nullable Date base) {
        return XTimeTools.weekInSeason(base);
    }

    /**
     * 获取任意一天所在的周是哪一年的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪一年的第几周（正数表示今年第几周，负数表示去年第几周,例：1=今年第二周，-51=去年第52周）
     */
    public static int weekInYear(@Nullable Date base) {
        return XTimeTools.weekInYear(base);
    }

    /**
     * 获取任意一天所在的月是那一季度的第几月
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的月是那一季度的第几月，第一个月为0
     */
    public static int monthInSeason(@Nullable Date base) {
        return XTimeTools.monthInSeason(base);
    }

    /**
     * 获取任意一天所在的月是那一年的第几月
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的月是那一年的第几月
     */
    public static int monthInYear(@Nullable Date base) {
        return XTimeTools.monthInYear(base);
    }

    /**
     * 获取任意一天所在的季度是那一年的第几个季度
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的季度是那一年的第几季度，第一个季度为0
     */
    public static int seasonInYear(@Nullable Date base) {
        return XTimeTools.seasonInYear(base);
    }

    /**
     * 将阳历时间转化成农历时间
     *
     * @param solarDate 阳历时间的Date对象
     * @return 对应的农历时间的字符串，例1：1992年八月初六。例2：2033年闰冬月廿八
     */
    @Nonnull
    public static String solarToLunar(@Nonnull Date solarDate) {
        return XTimeTools.solarToLunar(solarDate);
    }

    /**
     * 将农历时间转换成阳历时间
     *
     * @param lunarDate 农历时间字符串。例1：1992年八月初六。例2：2033年闰冬月廿八
     * @return 阳历时间的Date对象
     */
    @Nonnull
    public static Date lunarToSolar(@Nonnull String lunarDate) {
        return XTimeTools.lunarToSolar(lunarDate);
    }

    /**
     * 判断是否是windows系统
     *
     * @return 是否是windows系统
     */
    public static boolean sysWindows() {
        return System.getProperties().getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 判断是否是MacOS系统
     *
     * @return 是否是MacOS系统
     */
    public static boolean sysMacOS() {
        String osName = System.getProperties().getProperty("os.name").toLowerCase();
        return osName.contains("mac") && !osName.contains("x");
    }

    /**
     * 判断是否是MacOSX系统
     *
     * @return 是否是MacOSX系统
     */
    public static boolean sysMacOSX() {
        String osName = System.getProperties().getProperty("os.name").toLowerCase();
        return osName.contains("mac") && osName.contains("x");
    }

    /**
     * 判断是否是Linux系统
     *
     * @return 是否是Linux系统
     */
    public static boolean sysLinux() {
        String osName = System.getProperties().getProperty("os.name").toLowerCase();
        return osName.contains("linux");
    }

    /**
     * 使用默认的请求执行器进行HTTP请求，
     * 如需对HTTP请求进行更复杂的配置，请移步XTools.http(XOption option, XRequest request);方法
     *
     * @param request http请求
     * @return 请求的响应体
     */
    @Nonnull
    public static XResponse http(@Nonnull XRequest request) {
        return httpTools.http(request);
    }

    @Nullable
    public static <T> T getFieldValue(@Nonnull Object obj, @Nonnull String field) {
        return reflectTools.getFieldValue(obj, field);
    }

    @Nullable
    public static <T> T getFieldValue(@Nonnull Class<?> clazz, @Nonnull String field) {
        return reflectTools.getFieldValue(clazz, field);
    }

    public static void setFieldValue(@Nonnull Object obj, @Nonnull String field, @Nullable Object value) {
        reflectTools.setFieldValue(obj, field, value);
    }

    public static void setFieldValue(@Nonnull Class<?> clazz, @Nonnull String field, @Nullable Object value) {
        reflectTools.setFieldValue(clazz, field, value);
    }

    @Nullable
    public static <T> T invokeMethod(@Nonnull Object obj, @Nonnull String method, @Nullable Object... args) {
        return reflectTools.invokeMethod(obj, method, args);
    }

    @Nullable
    public static <T> T invokeMethod(@Nonnull Class<?> clazz, @Nonnull String method, @Nullable Object... args) {
        return reflectTools.invokeMethod(clazz, method, args);
    }
}
