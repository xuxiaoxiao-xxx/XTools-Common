package me.xuxiaoxiao.xtools.common;

import me.xuxiaoxiao.xtools.common.code.XCodeTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.config.configs.XConfigs;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.log.XLogTools;
import me.xuxiaoxiao.xtools.common.time.XTimeTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 常用的基本的函数的集合和索引
 */
public final class XTools {
    public static final String CFG_PREFIX = "me.xuxiaoxiao$xtools-common$";

    private XTools() {
    }

    /**
     * 字符串MD5散列
     *
     * @param str 被散列的字符串
     * @return 散列结果，全小写字母
     */
    @Nullable
    public static String md5(@Nonnull String str) {
        try {
            return XCodeTools.hash(XCodeTools.HASH_MD5, str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文件MD5散列
     *
     * @param file 被散列的文件
     * @return 散列结果，全小写字母
     */
    @Nullable
    public static String md5(@Nonnull File file) {
        try {
            return XCodeTools.hash(XCodeTools.HASH_MD5, file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 字符串SHA1散列
     *
     * @param str 被散列的字符串
     * @return 散列结果，全小写字母
     */
    @Nullable
    public static String sha1(@Nonnull String str) {
        try {
            return XCodeTools.hash(XCodeTools.HASH_SHA1, str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文件SHA1散列
     *
     * @param file 被散列的文件
     * @return 散列结果，全小写字母
     */
    @Nullable
    public static String sha1(@Nonnull File file) {
        try {
            return XCodeTools.hash(XCodeTools.HASH_SHA1, file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用默认的请求执行器进行HTTP请求，
     * 如需对HTTP请求进行更复杂的配置，请移步XTools.http(XOption option, XRequest request);方法
     *
     * @param request http请求
     * @return 请求的响应体
     */
    @Nonnull
    public static XHttpExecutor.Response http(@Nonnull XHttpExecutor.Request request) {
        return XHttpTools.http(XHttpTools.EXECUTOR, request);
    }

    /**
     * 使用自定义的请求执行器进行HTTP请求
     *
     * @param executor http请求执行器
     * @param request  http请求
     * @return 请求的响应体
     */
    @Nonnull
    public static XHttpExecutor.Response http(@Nonnull XHttpExecutor executor, @Nonnull XHttpExecutor.Request request) {
        return XHttpTools.http(executor, request);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 要判断的字符串
     * @return str == null || str.length() == 0
     */
    public static boolean strEmpty(@Nullable String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否没有可见字符
     *
     * @param str 要判断的字符串
     * @return str == null || str.trim().length() == 0
     */
    public static boolean strBlank(@Nullable String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 去除字符串前后的空白符，如果为null则返回null
     *
     * @param str 需要去除前后空白符的字符串
     * @return 去除前后空白符的字符串，如果传入的字符串为null则返回null
     */
    @Nullable
    public static String strTrim(@Nullable String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除字符串数组中每个元素的前后的空白符，如果为null则返回null
     *
     * @param strs 需要去除前后空白符的字符串数组
     * @return 去除每个元素前后空白符的字符串数组，如果传入的字符串数组为null则返回null，字符串中元素为null的返回的数据中也是null
     */
    @Nullable
    public static String[] strTrim(@Nullable String[] strs) {
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
     * 将字符串数组用一个固定的字符串连接起来
     *
     * @param strArr 字符串集合
     * @param glue   用于连接的字符串
     * @return 连接后的字符串
     */
    @Nonnull
    public static String strJoin(@Nonnull String[] strArr, @Nonnull String glue) {
        StringBuilder sbStr = new StringBuilder();
        for (String str : strArr) {
            if (sbStr.length() > 0) {
                sbStr.append(glue);
            }
            sbStr.append(str);
        }
        return sbStr.toString();
    }

    /**
     * 将字符串集合用一个固定的字符串连接起来
     *
     * @param strSet 字符串集合
     * @param glue   用于连接的字符串
     * @return 连接后的字符串
     */
    @Nonnull
    public static String strJoin(@Nonnull Collection<String> strSet, @Nonnull String glue) {
        StringBuilder sbStr = new StringBuilder();
        for (String str : strSet) {
            if (sbStr.length() > 0) {
                sbStr.append(glue);
            }
            sbStr.append(str);
        }
        return sbStr.toString();
    }

    /**
     * 将键值对集合用固定的字符串连接起来
     *
     * @param strMap    键值对集合
     * @param glueInner 连接键和值的字符串
     * @param glueOuter 连接键值对之间的字符串
     * @return 拼接后的字符串
     */
    @Nonnull
    public static String strJoin(@Nonnull Map<?, ?> strMap, @Nonnull String glueInner, @Nonnull String glueOuter) {
        StringBuilder sbStr = new StringBuilder();
        for (Object key : strMap.keySet()) {
            if (sbStr.length() > 0) {
                sbStr.append(glueOuter);
            }
            sbStr.append(key).append(glueInner).append(strMap.get(key));
        }
        return sbStr.toString();
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
        try (BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(file))) {
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
        try (BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(file))) {
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
     * 设置配置信息
     *
     * @param key 配置键
     * @param val 配置值
     */
    public static void cfgSet(@Nonnull String key, @Nonnull String val) {
        XConfigTools.X_CONFIGS.cfgSet(key, val);
    }

    /**
     * 获取配置信息值
     *
     * @param key 配置信息键名
     * @return 配置信息值
     */
    @Nullable
    public static String cfgGet(@Nonnull String key) {
        return XConfigTools.X_CONFIGS.cfgGet(key);
    }

    /**
     * 获取或设置配置信息
     *
     * @param key 配置键
     * @param def 配置值为null时设置的默认值
     * @return 当配置值为null时，将def设置为配置值并返回，否则返回原有的配置值并且不做任何更改
     */
    @Nonnull
    public static String cfgDef(@Nonnull String key, @Nonnull String def) {
        return XConfigTools.X_CONFIGS.cfgDef(key, def);
    }

    /**
     * 监听配置信息
     *
     * @param prefix  配置键前缀
     * @param watcher 配置信息监听器
     */
    public static void cfgWatch(@Nonnull String prefix, @Nonnull XConfigs.Watcher watcher) {
        XConfigTools.X_CONFIGS.watcherAdd(prefix, watcher);
    }

    /**
     * 记录错误信息的日志
     *
     * @param tag   日志tag
     * @param error 错误信息
     */
    public static void logE(@Nonnull String tag, @Nonnull String error) {
        XLogTools.LOGGER.logE(tag, error);
    }

    /**
     * 记录错误信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param error     错误信息
     */
    public static void logE(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String error) {
        XLogTools.LOGGER.logE(tag, throwable, error);
    }

    /**
     * 记录警告信息的日志
     *
     * @param tag     日志tag
     * @param warning 警告信息
     */
    public static void logW(@Nonnull String tag, @Nonnull String warning) {
        XLogTools.LOGGER.logW(tag, warning);
    }

    /**
     * 记录警告信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param warning   告警信息
     */
    public static void logW(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String warning) {
        XLogTools.LOGGER.logW(tag, throwable, warning);
    }

    /**
     * 记录提示信息的日志
     *
     * @param tag    日志tag
     * @param notice 提示信息
     */
    public static void logN(@Nonnull String tag, @Nonnull String notice) {
        XLogTools.LOGGER.logN(tag, notice);
    }

    /**
     * 记录详细信息的日志
     *
     * @param tag    日志tag
     * @param detail 详细信息
     */
    public static void logD(@Nonnull String tag, @Nonnull String detail) {
        XLogTools.LOGGER.logD(tag, detail);
    }
}
