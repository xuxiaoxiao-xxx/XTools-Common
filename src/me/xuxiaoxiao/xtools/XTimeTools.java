package me.xuxiaoxiao.xtools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XuXiaoXiao的常用的基本的关于时间的函数的集合
 */
public final class XTimeTools {
    /**
     * int dateType(Date date);函数的返回值的枚举，工作日
     */
    public static final int WORKDAY = 1;
    /**
     * int dateType(Date date);函数的返回值的枚举，公休日
     */
    public static final int RESTDAY = 2;
    /**
     * int dateType(Date date);函数的返回值的枚举，节假日
     */
    public static final int HOLIDAY = 3;

    private static final TLYMDHMS tlYMDHMS = new TLYMDHMS();
    private static final TLYMD tlYMD = new TLYMD();
    private static final TLMD tlMD = new TLMD();
    private static final TLHMS tlHMS = new TLHMS();
    private static final TLHM tlHM = new TLHM();
    private static final TLEEEE tlEEEE = new TLEEEE();

    private XTimeTools() {
    }

    /**
     * 获取yyyy-MM-dd HH:mm:ss格式的SimpleDateFormat，线程安全
     *
     * @return SimpleDateFormat对象
     */
    public static SimpleDateFormat sdfYMDHMS() {
        return tlYMDHMS.get();
    }

    /**
     * 获取yyyy-MM-dd格式的SimpleDateFormat，线程安全
     *
     * @return SimpleDateFormat对象
     */
    public static SimpleDateFormat sdfYMD() {
        return tlYMD.get();
    }

    /**
     * 获取MM-dd格式的SimpleDateFormat，线程安全
     *
     * @return SimpleDateFormat对象
     */
    public static SimpleDateFormat sdfMD() {
        return tlMD.get();
    }

    /**
     * 获取HH:mm:ss格式的SimpleDateFormat，线程安全
     *
     * @return SimpleDateFormat对象
     */
    public static SimpleDateFormat sdfHMS() {
        return tlHMS.get();
    }

    /**
     * 获取HH:mm格式的SimpleDateFormat，线程安全
     *
     * @return SimpleDateFormat对象
     */
    public static SimpleDateFormat sdfHM() {
        return tlHM.get();
    }

    /**
     * 获取EEEE格式的SimpleDateFormat，线程安全
     *
     * @return SimpleDateFormat对象
     */
    public static SimpleDateFormat sdfEEEE() {
        return tlEEEE.get();
    }

    /**
     * 获取任意date对象当天00:00:00的date对象
     *
     * @param date 任意时间的date对象
     * @return 参数中的date对象所对应的当天的00:00:00时刻
     */
    public static Date dateOfTime(Date date) {
        try {
            return sdfYMD().parse(sdfYMD().format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取任意一天的类型，1：工作日(XTimeTools.WORKDAY)，2：公休日(XTimeTools.RESTDAY)，3：节假日(XTimeTools.HOLIDAY)
     *
     * @param date date对象
     * @return date对象对应的那天的类型。1：工作日(XTimeTools.WORKDAY)，2：公休日(XTimeTools.RESTDAY)，3：节假日(XTimeTools.HOLIDAY)
     * @see #WORKDAY
     * @see #RESTDAY
     * @see #HOLIDAY
     */
    public static int dateType(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        try {
            int[] holidays = (int[]) XHolidayDb.class.getDeclaredField("HOLIDAY_" + year).get(null);
            for (int typeMD : holidays) {
                if ((month << 8 | day) == (typeMD & 0xfff)) {
                    return typeMD >> 12;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weekIndex(date) < 5 ? WORKDAY : RESTDAY;
    }

    /**
     * 获取任意一天是一周中的第几天，0：周一，1：周二...6：周日
     *
     * @param date date对象
     * @return date对象是一周中的第几天
     */
    public static int weekIndex(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return -1;
        }
    }

    /**
     * 将阳历时间转化成农历时间
     *
     * @param solarDate 阳历时间的Date对象
     * @return 对应的农历时间的字符串，例1：1992年八月初六。例2：2033年闰冬月廿八
     */
    public static String solarToLunar(Date solarDate) {
        // 验证日期是否在范围内
        Date minDate, maxDate;
        try {
            minDate = XLunarDb.TL_LUNAR.get().parse(XLunarDb.MIN_DATE);
            maxDate = XLunarDb.TL_LUNAR.get().parse(XLunarDb.MAX_DATE);
        } catch (ParseException e) {
            throw new RuntimeException("解析日期出错");
        }
        if (solarDate.before(minDate) || !solarDate.before(maxDate)) {
            throw new IllegalStateException("要转换的公历超出范围");
        }
        // 计算目标日期与基准日期差多少天。开头那天算，结尾那天不算
        int dayBetween = (int) ((solarDate.getTime() - minDate.getTime()) / 24 / 60 / 60 / 1000);
        // 确定农历年
        int lunarYear = 0;
        for (int i = XLunarDb.MIN_YEAR; i < XLunarDb.MAX_YEAR; i++) {
            int yearDays = XLunarDb.lunarYearDays(i);
            if (dayBetween - yearDays < 0) {
                lunarYear = i;
                break;
            } else {
                dayBetween -= yearDays;
            }
        }
        // 确定农历年中的月和是否是闰月
        int lunarMonth = 0;
        boolean lunarLeap = false;
        final int leapMonth = XLunarDb.LUNAR_INFO[lunarYear - XLunarDb.MIN_YEAR] & 0xf;
        for (int i = 1; i <= 12; i++) {
            int monthDays = XLunarDb.lunarMonthDays(lunarYear, i, false);
            if (dayBetween - monthDays < 0) {
                lunarMonth = i;
                lunarLeap = false;
                break;
            } else {
                dayBetween -= monthDays;
            }
            if (i == leapMonth) {
                monthDays = XLunarDb.lunarMonthDays(lunarYear, leapMonth, true);
                if (dayBetween - monthDays < 0) {
                    lunarMonth = leapMonth;
                    lunarLeap = true;
                    break;
                } else {
                    dayBetween -= monthDays;
                }
            }
        }
        // 确定农历月中的天，由于本来0表示初一，故这里加上1便于理解
        int lunarDay = dayBetween + 1;
        // 生成字符串
        StringBuilder sbLunar = new StringBuilder();
        sbLunar.append(lunarYear).append("年");
        if (lunarLeap) {
            sbLunar.append("闰");
        }
        sbLunar.append(XLunarDb.LUNAR_MONTH[lunarMonth - 1]).append("月");
        if (lunarDay == 10) {
            sbLunar.append("初十");
        } else if (lunarDay == 20) {
            sbLunar.append("二十");
        } else if (lunarDay == 30) {
            sbLunar.append("三十");
        } else {
            switch (lunarDay / 10) {
                case 0:
                    sbLunar.append("初");
                    break;
                case 1:
                    sbLunar.append("十");
                    break;
                case 2:
                    sbLunar.append("廿");
                    break;
            }
            sbLunar.append(XLunarDb.LUNAR_DAY[lunarDay % 10 - 1]);
        }
        return sbLunar.toString();
    }

    /**
     * 将农历时间转换成阳历时间
     *
     * @param lunarDate 农历时间字符串。例1：1992年八月初六。例2：2033年闰冬月廿八
     * @return 阳历时间的Date对象
     */
    public static Date lunarToSolar(String lunarDate) {
        // 匹配年月日闰
        Matcher matcher = Pattern.compile(XLunarDb.LUNAR_PATTERN).matcher(lunarDate);
        if (!matcher.matches()) {
            throw new RuntimeException("农历日期格式不正确，例1：1992年八月初六。例2：2033年闰冬月廿八。");
        }
        String strYear = matcher.group(1);
        String strMonth = matcher.group(4);
        String strDay = matcher.group(5);
        String strLeap = matcher.group(3);
        // 解析农历年
        int lunarYear = Integer.valueOf(strYear);
        // 解析农历月，1代表正月
        int lunarMonth = 0;
        for (int i = 0; i < XLunarDb.LUNAR_MONTH.length; i++) {
            if (XLunarDb.LUNAR_MONTH[i].equals(strMonth)) {
                lunarMonth = i + 1;
                break;
            }
        }
        // 解析农历日，1代表初一
        int lunarDay = 0;
        if ("初十".equals(strDay)) {
            lunarDay = 10;
        } else if ("二十".equals(strDay)) {
            lunarDay = 20;
        } else if ("三十".equals(strDay)) {
            lunarDay = 30;
        } else {
            String strTen = String.valueOf(strDay.charAt(0));
            String strOne = String.valueOf(strDay.charAt(1));
            switch (strTen) {
                case "初":
                    lunarDay = 0;
                    break;
                case "十":
                    lunarDay = 10;
                    break;
                case "廿":
                    lunarDay = 20;
                    break;
            }
            for (int i = 0; i < XLunarDb.LUNAR_DAY.length; i++) {
                if (XLunarDb.LUNAR_DAY[i].equals(strOne)) {
                    lunarDay = lunarDay + i + 1;
                    break;
                }
            }
        }
        boolean lunarLeap = !XTools.strEmpty(strLeap);
        // 验证年月日闰
        XLunarDb.checkLunarDate(lunarYear, lunarMonth, lunarDay, lunarLeap);
        // 计算与基准日期相差多少天
        int dayBetween = 0;
        for (int i = XLunarDb.MIN_YEAR; i < lunarYear; i++) {
            dayBetween += XLunarDb.lunarYearDays(i);
        }
        final int leapMonth = XLunarDb.LUNAR_INFO[lunarYear - XLunarDb.MIN_YEAR] & 0xf;
        for (int i = 1; i < lunarMonth; i++) {
            dayBetween += XLunarDb.lunarMonthDays(lunarYear, i, false);
            if (i == leapMonth) {
                dayBetween += XLunarDb.lunarMonthDays(lunarYear, leapMonth, true);
            }
        }
        if (leapMonth == lunarMonth && lunarLeap) {
            dayBetween += XLunarDb.lunarMonthDays(lunarYear, leapMonth, false);
        }
        dayBetween += lunarDay - 1;
        try {
            Date minDate = XLunarDb.TL_LUNAR.get().parse(XLunarDb.MIN_DATE);
            System.out.println(minDate);
            System.out.println(minDate.getTime());

            return new Date(minDate.getTime() + ((long) dayBetween) * 24L * 60L * 60L * 1000L);
        } catch (ParseException e) {
            throw new RuntimeException("解析日期出错");
        }
    }

    /**
     * yyyy-MM-dd HH:mm:ss格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLYMDHMS extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * yyyy-MM-dd格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLYMD extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    }

    /**
     * MM-dd格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLMD extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM-dd");
        }
    }

    /**
     * HH:mm:ss格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLHMS extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss");
        }
    }

    /**
     * HH:mm:ss格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLHM extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    }

    /**
     * EEEE格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLEEEE extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EEEE");
        }
    }

    /**
     * 农历数据库，保存了农历的一些信息，和操作函数
     */
    private static class XLunarDb {
        // 可接受的最早的时间
        public static final String MIN_DATE = "1901-02-19";
        // 可接受的最迟的时间
        public static final String MAX_DATE = "2101-01-29";
        // 允许输入的最小年份
        public static final int MIN_YEAR = 1901;
        // 允许输入的最大年份
        public static final int MAX_YEAR = 2101;
        // 农历日期格式化工具
        private static final TLLunar TL_LUNAR = new TLLunar();
        // 农历月份
        private static final String[] LUNAR_MONTH = new String[]{"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
        // 农历日期
        private static final String[] LUNAR_DAY = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        // 农历的正则表达式
        private static final String LUNAR_PATTERN = "((19|20|21)\\d{2})年(闰)?([正二三四五六七八九十冬腊])月(([初十廿])([一二三四五六七八九])|初十|二十|三十)";
        // 1901年到2100年的阴历数据，来源于香港天文台http://data.weather.gov.hk/gts/time/conversion1_text_c.htm
        private static final int[] LUNAR_INFO = {
                0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, 0x04ae0,//1901-1910
                0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970,//1911-1920
                0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,//1921-1930
                0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0,//1931-1940
                0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0,//1941-1950
                0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6,//1951-1960
                0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0,//1961-1970
                0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, 0x095b0,//1971-1980
                0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5,//1981-1990
                0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960,//1991-2000
                0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950,//2001-2010
                0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954,//2011-2020
                0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0,//2021-2030
                0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0,//2031-2040
                0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, 0x14b63,//2041-2050
                0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, 0x0a2e0,//2051-2060
                0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, 0x052d0,//2061-2070
                0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, 0x0b273,//2071-2080
                0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, 0x0e968,//2081-2090
                0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, 0x0d520 //2091-2100
        };

        /**
         * 获取农历的某年某月有多少天
         *
         * @param lunarYear  农历年份
         * @param lunarMonth 　农历月份
         * @param lunarLeap  　是否是闰月
         * @return 该年该月一共有多少天
         */
        private static int lunarMonthDays(int lunarYear, int lunarMonth, boolean lunarLeap) {
            if (lunarLeap) {
                return (LUNAR_INFO[lunarYear - MIN_YEAR] & 0x10000) == 0 ? 29 : 30;
            } else {
                return (LUNAR_INFO[lunarYear - MIN_YEAR] & (1 << (16 - lunarMonth))) == 0 ? 29 : 30;
            }
        }

        /**
         * 获取农历某年有多少天
         *
         * @param lunarYear 　农历年份
         * @return 该农历年份一共有多少天
         */
        private static int lunarYearDays(int lunarYear) {
            if ((LUNAR_INFO[lunarYear - MIN_YEAR] & 0xf) == 0) {
                return 29 * 12 + Integer.bitCount(LUNAR_INFO[lunarYear - MIN_YEAR] & 0xfff0);
            } else {
                return 29 * 13 + Integer.bitCount(LUNAR_INFO[lunarYear - MIN_YEAR] & 0x1fff0);
            }
        }

        /**
         * 检查农历年月日和是否闰月是否合法
         *
         * @param lunarYear  农历年份
         * @param lunarMonth 农历月份
         * @param lunarDay   农历日
         * @param lunarLeap  是否是闰月
         */
        private static void checkLunarDate(int lunarYear, int lunarMonth, int lunarDay, boolean lunarLeap) {
            if (lunarYear < MIN_YEAR || MAX_YEAR <= lunarYear) {
                throw new RuntimeException("非法农历年份！");
            }
            if (lunarMonth < 1 || 12 < lunarMonth) {
                throw new RuntimeException("非法农历月份！");
            }
            if (lunarLeap && (LUNAR_INFO[lunarYear - MIN_YEAR] & 0xf) != lunarMonth) {
                throw new RuntimeException(String.format("农历%d年%d月不是闰月", lunarYear, lunarMonth));
            }
            if (lunarDay < 1 || lunarDay > lunarMonthDays(lunarYear, lunarMonth, lunarLeap)) {
                throw new RuntimeException("非法农历天数！");
            }
        }

        /**
         * yyyy-MM-dd格式的SimpleDateFormat的ThreadLocal类，将时区设为上海
         */
        private static class TLLunar extends ThreadLocal<SimpleDateFormat> {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat sdfLunar = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                sdfLunar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                return sdfLunar;
            }
        }
    }

    /**
     * 节假日数据库，保存了节假日，调休的一些数据
     */
    private static class XHolidayDb {
        public static final int[] HOLIDAY_2000 = {0x120c, 0x120d, 0x141d, 0x141e, 0x191e, 0x1a08, 0x2208, 0x2209, 0x220a, 0x220b, 0x2504, 0x2505, 0x2a04, 0x2a05, 0x2a06, 0x3205, 0x3206, 0x3207, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2001 = {0x1114, 0x1115, 0x141c, 0x141d, 0x191d, 0x191e, 0x1c1d, 0x1c1e, 0x211d, 0x211e, 0x2504, 0x2507, 0x2a04, 0x2a05, 0x3101, 0x3118, 0x3119, 0x311a, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2002 = {0x1209, 0x120a, 0x141b, 0x141c, 0x191c, 0x191d, 0x2102, 0x2103, 0x220f, 0x2212, 0x2506, 0x2507, 0x2a04, 0x2a07, 0x3101, 0x320c, 0x320d, 0x320e, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2003 = {0x1208, 0x1209, 0x141a, 0x141b, 0x191b, 0x191c, 0x2204, 0x2205, 0x2206, 0x2207, 0x2505, 0x2506, 0x2507, 0x2a06, 0x2a07, 0x3101, 0x3201, 0x3202, 0x3203, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2004 = {0x1111, 0x1112, 0x1508, 0x1509, 0x1a09, 0x1a0a, 0x211a, 0x211b, 0x211c, 0x2504, 0x2505, 0x2506, 0x2507, 0x2a04, 0x2a05, 0x2a06, 0x2a07, 0x3101, 0x3116, 0x3117, 0x3118, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2005 = {0x1205, 0x1206, 0x141e, 0x1508, 0x1a08, 0x1a09, 0x1c1f, 0x2103, 0x220e, 0x220f, 0x2504, 0x2505, 0x2506, 0x2a04, 0x2a05, 0x2a06, 0x2a07, 0x3101, 0x3209, 0x320a, 0x320b, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2006 = {0x111c, 0x1205, 0x141d, 0x141e, 0x191e, 0x1a08, 0x1c1e, 0x1c1f, 0x2102, 0x2103, 0x2201, 0x2202, 0x2203, 0x2504, 0x2505, 0x2a04, 0x2a05, 0x2a06, 0x3101, 0x311d, 0x311e, 0x311f, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2007 = {0x1211, 0x1219, 0x141c, 0x141d, 0x191d, 0x191e, 0x1c1d, 0x2102, 0x2103, 0x2215, 0x2216, 0x2217, 0x2504, 0x2507, 0x2a04, 0x2a05, 0x2c1f, 0x3101, 0x3212, 0x3213, 0x3214, 0x3501, 0x3502, 0x3503, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2008 = {0x1202, 0x1203, 0x1504, 0x191b, 0x191c, 0x220b, 0x220c, 0x2502, 0x2609, 0x290f, 0x291d, 0x291e, 0x3101, 0x3206, 0x3207, 0x3208, 0x3404, 0x3501, 0x3608, 0x390e, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2009 = {0x1104, 0x1118, 0x1201, 0x151f, 0x191b, 0x1a0a, 0x2102, 0x211c, 0x211d, 0x211e, 0x2406, 0x251d, 0x2a05, 0x2a07, 0x2a08, 0x3101, 0x3119, 0x311a, 0x311b, 0x3404, 0x3501, 0x351c, 0x3a01, 0x3a02, 0x3a03, 0x3a06};
        public static final int[] HOLIDAY_2010 = {0x1214, 0x1215, 0x160c, 0x160d, 0x1913, 0x1919, 0x191a, 0x1a09, 0x2210, 0x2211, 0x2212, 0x2213, 0x2503, 0x260e, 0x260f, 0x2917, 0x2918, 0x2a04, 0x2a05, 0x2a06, 0x2a07, 0x3101, 0x320d, 0x320e, 0x320f, 0x3405, 0x3501, 0x3610, 0x3916, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2011 = {0x111e, 0x120c, 0x1402, 0x1a08, 0x1a09, 0x1c1f, 0x2103, 0x2207, 0x2208, 0x2502, 0x2a04, 0x2a05, 0x2a06, 0x2a07, 0x3101, 0x3202, 0x3203, 0x3204, 0x3405, 0x3501, 0x3606, 0x390c, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2012 = {0x1115, 0x111d, 0x131f, 0x1401, 0x141c, 0x191d, 0x2102, 0x2103, 0x2119, 0x211a, 0x211b, 0x2402, 0x2403, 0x241e, 0x2616, 0x2a04, 0x2a05, 0x3101, 0x3116, 0x3117, 0x3118, 0x3404, 0x3501, 0x3617, 0x391e, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2013 = {0x1105, 0x1106, 0x1210, 0x1211, 0x1407, 0x141b, 0x141c, 0x1608, 0x1609, 0x1916, 0x191d, 0x1a0c, 0x2102, 0x2103, 0x220c, 0x220d, 0x220e, 0x220f, 0x2405, 0x241d, 0x241e, 0x260a, 0x260b, 0x2914, 0x2a04, 0x2a07, 0x3101, 0x3209, 0x320a, 0x320b, 0x3404, 0x3501, 0x360c, 0x3913, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2014 = {0x111a, 0x1208, 0x1504, 0x191c, 0x1a0b, 0x2203, 0x2204, 0x2205, 0x2206, 0x2407, 0x2502, 0x2a06, 0x2a07, 0x3101, 0x311f, 0x3201, 0x3202, 0x3405, 0x3501, 0x3602, 0x3908, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2015 = {0x1104, 0x120f, 0x121c, 0x1906, 0x1a0a, 0x2102, 0x2217, 0x2218, 0x2406, 0x2616, 0x2904, 0x2a05, 0x2a06, 0x2a07, 0x3101, 0x3212, 0x3213, 0x3214, 0x3405, 0x3501, 0x3614, 0x3903, 0x391b, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2016 = {0x1206, 0x120e, 0x160c, 0x1912, 0x1a08, 0x1a09, 0x220a, 0x220b, 0x220c, 0x2502, 0x260a, 0x260b, 0x2910, 0x2911, 0x2a04, 0x2a05, 0x2a06, 0x2a07, 0x3101, 0x3207, 0x3208, 0x3209, 0x3404, 0x3501, 0x3609, 0x390f, 0x3a01, 0x3a02, 0x3a03};
        public static final int[] HOLIDAY_2017 = {0x1102, 0x1116, 0x1204, 0x1401, 0x151b, 0x191e, 0x211e, 0x211f, 0x2201, 0x2202, 0x2403, 0x251d, 0x2a05, 0x2a06, 0x3101, 0x311b, 0x311c, 0x311d, 0x3404, 0x3501, 0x351e, 0x3a01, 0x3a02, 0x3a03, 0x3a04};
    }
}
