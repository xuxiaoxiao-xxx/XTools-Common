package me.xuxiaoxiao.xtools.common;

import me.xuxiaoxiao.xtools.common.code.XCodeTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.log.XLogTools;
import me.xuxiaoxiao.xtools.common.time.XTimeTools;

import java.io.*;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 常用的基本的函数的集合和索引
 */
public final class XTools {

    private XTools() {
    }

    /**
     * 字符串MD5散列
     *
     * @param str 被散列的字符串
     * @return 散列结果，全小写字母
     */
    public static String md5(String str) {
        try {
            return XCodeTools.hash(XCodeTools.HASH_MD5, str.getBytes("UTF-8"));
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
    public static String md5(File file) {
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
    public static String sha1(String str) {
        try {
            return XCodeTools.hash(XCodeTools.HASH_SHA1, str.getBytes("UTF-8"));
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
    public static String sha1(File file) {
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
    public static XResponse http(XRequest request) {
        return XHttpTools.http(XHttpTools.EXECUTOR, request);
    }

    /**
     * 使用自定义的请求执行器进行HTTP请求
     *
     * @param executor http请求执行器
     * @param request  http请求
     * @return 请求的响应体
     */
    public static XResponse http(XHttpExecutor executor, XRequest request) {
        return XHttpTools.http(executor, request);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 要判断的字符串
     * @return str == null || str.length() == 0
     */
    public static boolean strEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否没有可见字符
     *
     * @param str 要判断的字符串
     * @return str == null || str.trim().length() == 0
     */
    public static boolean strBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 将几个字符串用一个固定的字符串连接起来
     *
     * @param strSet 字符串集合
     * @param glue   用于连接的字符串
     * @return 连接后的字符串
     */
    public static String strJoin(Collection<String> strSet, String glue) {
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
    public static String strJoin(Map<?, ?> strMap, String glueInner, String glueOuter) {
        StringBuilder sbStr = new StringBuilder();
        for (Object key : strMap.keySet()) {
            if (sbStr.length() > 0) {
                sbStr.append(glueOuter);
            }
            sbStr.append(String.valueOf(key)).append(glueInner).append(String.valueOf(strMap.get(key)));
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
    public static File strToFile(String str, String path, String charset) throws IOException {
        File file = new File(path);
        try (BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outStream.write(str.getBytes(charset));
            outStream.flush();
            return file;
        }
    }

    /**
     * 将文件读取成字符串
     *
     * @param file    要读取成字符串的文件
     * @param charset 字符串的编码格式
     * @return 读取出的字符串
     * @throws IOException 在读取时可能会发生IO异常
     */
    public static String fileToStr(File file, String charset) throws IOException {
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
    public static File fileToFile(File file, String path) throws IOException {
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
    public static String streamToStr(InputStream inStream, String charset) throws IOException {
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
    public static File streamToFile(InputStream inStream, String path) throws IOException {
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
    public static void streamToStream(InputStream inStream, OutputStream outStream) throws IOException {
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
    public static int dateType(Date date) {
        return XTimeTools.dateType(date);
    }

    /**
     * 将date对象转换成相应格式的字符串，线程安全
     *
     * @param format 格式字符串
     * @param date   date对象
     * @return 相应格式的字符串
     */
    public static String dateFormat(String format, Date date) {
        return XTimeTools.dateFormat(format, date);
    }

    /**
     * 将日期字符串转换成相应的date对象，线程安全
     *
     * @param format  格式字符串
     * @param dateStr 日期字符串
     * @return 相应的date对象
     */
    public static Date dateParse(String format, String dateStr) {
        return XTimeTools.dateParse(format, dateStr);
    }

    /**
     * 获取以任意时间所在的那一天为基准偏移若干天的00:00:00时刻的date对象
     *
     * @param base   任意时间的date对象，如果为null则以当前时间为基准
     * @param offset 偏移的天数
     * @return 以base所在的那一天为基准偏移offset天00:00:00时刻的date对象
     */
    public static Date date(Date base, int offset) {
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
    public static Date dateByWeek(Date base, int weekOffset, int dayIndex) {
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
    public static Date dateByMonth(Date base, int monthOffset, int dayIndex) {
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
    public static Date dateBySeason(Date base, int seasonOffset, int dayIndex) {
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
    public static Date dateByYear(Date base, int yearOffset, int dayIndex) {
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
    public static Date weekByMonth(Date base, int monthOffset, int weekIndex) {
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
    public static Date weekBySeason(Date base, int seasonOffset, int weekIndex) {
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
    public static Date weekByYear(Date base, int yearOffset, int weekIndex) {
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
    public static Date monthBySeason(Date base, int seasonOffset, int monthIndex) {
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
    public static Date monthByYear(Date base, int yearOffset, int monthIndex) {
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
    public static Date seasonByYear(Date base, int yearOffset, int seasonIndex) {
        return XTimeTools.seasonByYear(base, yearOffset, seasonIndex);
    }

    /**
     * 获取任意一天是一周中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那周中的第几天（每周的第一天是周一，周一为0）
     */
    public static int dateInWeek(Date base) {
        return XTimeTools.dateInWeek(base);
    }

    /**
     * 获取任意一天是一个月中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那个月中的第几天（一号为0）
     */
    public static int dateInMonth(Date base) {
        return XTimeTools.dateInMonth(base);
    }

    /**
     * 获取任意一天是一个季度中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那个季度中的第几天（第一天为0）
     */
    public static int dateInSeason(Date base) {
        return XTimeTools.dateInSeason(base);
    }

    /**
     * 获取任意一天是一年中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那一年中的第几天（第一天为0）
     */
    public static int dateInYear(Date base) {
        return XTimeTools.dateInYear(base);
    }

    /**
     * 获取任意一天所在的周是哪个月的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪个月的第几周（正数表示本月第几周，负数表示上月第几周,例：1=本月第二周，-3=上月第四周）
     */
    public static int weekInMonth(Date base) {
        return XTimeTools.weekInMonth(base);
    }

    /**
     * 获取任意一天所在的周是哪一季度的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪一季度的第几周（正数表示本季度第几周，负数表示上季度第几周,例：1=本季第第二周，-12=上季度第13周）
     */
    public static int weekInSeason(Date base) {
        return XTimeTools.weekInSeason(base);
    }

    /**
     * 获取任意一天所在的周是哪一年的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪一年的第几周（正数表示今年第几周，负数表示去年第几周,例：1=今年第二周，-51=去年第52周）
     */
    public static int weekInYear(Date base) {
        return XTimeTools.weekInYear(base);
    }

    /**
     * 获取任意一天所在的月是那一季度的第几月
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的月是那一季度的第几月，第一个月为0
     */
    public static int monthInSeason(Date base) {
        return XTimeTools.monthInSeason(base);
    }

    /**
     * 获取任意一天所在的月是那一年的第几月
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的月是那一年的第几月
     */
    public static int monthInYear(Date base) {
        return XTimeTools.monthInYear(base);
    }

    /**
     * 获取任意一天所在的季度是那一年的第几个季度
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的季度是那一年的第几季度，第一个季度为0
     */
    public static int seasonInYear(Date base) {
        return XTimeTools.seasonInYear(base);
    }

    /**
     * 将阳历时间转化成农历时间
     *
     * @param solarDate 阳历时间的Date对象
     * @return 对应的农历时间的字符串，例1：1992年八月初六。例2：2033年闰冬月廿八
     */
    public static String solarToLunar(Date solarDate) {
        return XTimeTools.solarToLunar(solarDate);
    }

    /**
     * 将农历时间转换成阳历时间
     *
     * @param lunarDate 农历时间字符串。例1：1992年八月初六。例2：2033年闰冬月廿八
     * @return 阳历时间的Date对象
     */
    public static Date lunarToSolar(String lunarDate) {
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
     * 获取配置信息值
     *
     * @param key 配置信息键名
     * @return 配置信息值
     */
    public static String cfgGet(String key) {
        return XConfigTools.cfgGet(key);
    }

    /**
     * 获取或设置配置信息
     *
     * @param key 配置信息键名
     * @param def 配置信息为null时设置的默认值
     * @return 当配置信息值为null时，将def设置为配置信息的值并返回，否则返回原有的配置信息值并且不做任何更改
     */
    public static String cfgDef(String key, String def) {
        return XConfigTools.cfgDef(key, def);
    }

    /**
     * 设置配置信息
     *
     * @param key 配置信息键名
     * @param val 配置信息值
     */
    public static void cfgSet(String key, String val) {
        XConfigTools.cfgSet(key, val);
    }

    /**
     * 打印错误日志
     *
     * @param error 错误日志信息
     * @param args  信息的参数
     */
    public static void logE(String error, Object... args) {
        XLogTools.LOGGER.logE(error, args);
    }

    /**
     * 打印警告日志
     *
     * @param warning 警告日志信息
     * @param args    信息的参数
     */
    public static void logW(String warning, Object... args) {
        XLogTools.LOGGER.logW(warning, args);
    }

    /**
     * 打印提示日志
     *
     * @param notice 提示日志信息
     * @param args   信息的参数
     */
    public static void logN(String notice, Object... args) {
        XLogTools.LOGGER.logN(notice, args);
    }

    /**
     * 打印详细日志
     *
     * @param detail 详细日志信息
     * @param args   信息的参数
     */
    public static void logD(String detail, Object... args) {
        XLogTools.LOGGER.logD(detail, args);
    }
}
