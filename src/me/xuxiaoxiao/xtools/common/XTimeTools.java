package me.xuxiaoxiao.xtools.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XTimeTools {
    public static final int WORKDAY = 1;
    public static final int RESTDAY = 2;
    public static final int HOLIDAY = 3;

    private static final String PATTERN_WORKDAY = ".+[一二三四五]";
    private static final TLYMDHMS tlYMDHMS = new TLYMDHMS();
    private static final TLYMD tlYMD = new TLYMD();
    private static final TLMD tlMD = new TLMD();
    private static final TLHMS tlHMS = new TLHMS();
    private static final TLHM tlHM = new TLHM();
    private static final TLE tlE = new TLE();

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
    public static SimpleDateFormat sdfE() {
        return tlE.get();
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
     * 获取以任意一天为基准的偏移若干天的00:00:00时刻的date对象
     *
     * @param base      基准时间的date对象
     * @param dayOffset 偏移的天数
     * @return 以参数base为基准的偏移dayOffset天的00:00:00时刻的date对象
     */
    public static Date dateOfDay(Date base, int dayOffset) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        return dateOfTime(new Date(baseTime + dayOffset * 24 * 60 * 60 * 1000));
    }

    /**
     * 获取以任意一天为基准的偏移若干周的某天00:00:00时刻的date对象
     *
     * @param base       基准时间的date对象
     * @param weekOffset 偏移的周数
     * @param dayIndex   周里的第几天，0为周一
     * @return 任意base为基准的偏移weekOffset周的第dayIndex天00:00:00时刻的date对象
     */
    public static Date dateOfWeek(Date base, int weekOffset, int dayIndex) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        for (int i = 0; i < 7; i++) {
            if (sdfE().format(new Date(baseTime - i * 24 * 60 * 60 * 1000)).contains("一")) {
                return dateOfTime(new Date(baseTime + (7 * weekOffset + dayIndex - i) * 24 * 60 * 60 * 1000));
            }
        }
        return null;
    }

    /**
     * 获取任意一天的类型，1：工作日，2：公休日，3：节假日
     *
     * @param date 要获取的date对象
     * @return date对象对应的那天的类型。1：工作日，2：公休日，3：节假日
     */
    public static int dateType(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        if (year < XHolidayDb.YEAR_MIN || year >= XHolidayDb.YEAR_MAX) {
            return Pattern.matches(PATTERN_WORKDAY, sdfE().format(date)) ? WORKDAY : RESTDAY;
        } else {
            try {
                String[][] holidays = (String[][]) XHolidayDb.class.getDeclaredField("HOLIDAY_" + year).get(null);
                String dateMD = sdfMD().format(date);
                for (int type = WORKDAY; type <= HOLIDAY; type++) {
                    for (String typeMD : holidays[type - 1]) {
                        if (typeMD.equals(dateMD)) {
                            return type;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Pattern.matches(PATTERN_WORKDAY, sdfE().format(date)) ? WORKDAY : RESTDAY;
        }
    }

    /**
     * 将农历时间转换成阳历时间
     *
     * @param lunarDate 农历时间字符串。例1：2016年八月初六。例2：2033年闰冬月廿八。
     * @return 对应的阳历的Date对象
     */
    public static Date lunarToSolar(String lunarDate) {
        // 匹配年月日闰
        Matcher matcher = Pattern.compile(XLunarDb.LUNAR_PATTERN).matcher(lunarDate);
        if (!matcher.matches()) {
            throw new RuntimeException("农历日期格式不正确，例1：2016年八月初六。例2：2033年闰冬月廿八。");
        }
        String strYear = matcher.group(1);
        String strMonth = matcher.group(4);
        String strDay = matcher.group(5);
        String strLeap = matcher.group(3);
        // 解析年月日闰
        int lunarYear = Integer.valueOf(strYear);
        int lunarMonth = 0;
        for (int i = 0; i < XLunarDb.LUNAR_MONTH.length; i++) {
            if (XLunarDb.LUNAR_MONTH[i].equals(strMonth)) {
                lunarMonth = i + 1;
                break;
            }
        }
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
        boolean lunarLeap = !XStrTools.isEmpty(strLeap);
        // 验证年月日闰
        XLunarDb.checkLunarDate(lunarYear, lunarMonth, lunarDay, lunarLeap);
        // 计算与1900-01-31相差多少天
        int dayBetween = 0;
        for (int i = XLunarDb.MIN_YEAR; i < lunarYear; i++) {
            dayBetween += XLunarDb.getYearDays(i);
        }
        for (int i = 1; i < lunarMonth; i++) {
            dayBetween += XLunarDb.getMonthDays(lunarYear, i, false);
        }
        final int leapMonth = XLunarDb.LUNAR_INFO[lunarYear - XLunarDb.MIN_YEAR] & 0xf;
        if (0 < leapMonth && leapMonth < lunarMonth) {
            dayBetween += XLunarDb.getMonthDays(lunarYear, leapMonth, true);
        } else if (leapMonth == lunarMonth && lunarLeap) {
            dayBetween += XLunarDb.getMonthDays(lunarYear, leapMonth, false);
        }
        dayBetween += lunarDay - 1;
        try {
            Date minDate = XLunarDb.sdfLunar().parse(XLunarDb.MIN_DATE);
            return new Date(minDate.getTime() + (long) dayBetween * 24L * 60L * 60L * 1000L);
        } catch (ParseException e) {
            throw new RuntimeException("解析日期出错");
        }
    }

    /**
     * 将阳历时间转化成农历字符串
     *
     * @param solarDate 阳历时间的Date对象
     * @return 对应的农历的字符串
     */
    public static String solarToLunar(Date solarDate) {
        // 验证日期是否在范围内
        Date minDate, maxDate;
        try {
            minDate = XLunarDb.sdfLunar().parse(XLunarDb.MIN_DATE);
            maxDate = XLunarDb.sdfLunar().parse(XLunarDb.MAX_DATE);
        } catch (ParseException e) {
            throw new RuntimeException("解析日期出错");
        }
        if (solarDate.before(minDate) || solarDate.after(maxDate)) {
            throw new RuntimeException("要转换的公历超出范围");
        }
        // 计算目标日期与基准日期差多少天
        int dayBetween = (int) ((solarDate.getTime() - minDate.getTime()) / 24 / 60 / 60 / 1000);
        // 确定年
        int lunarYear = 0;
        for (int i = XLunarDb.MIN_YEAR; i <= XLunarDb.MAX_YEAR; i++) {
            int yearDays = XLunarDb.getYearDays(i);
            if (dayBetween - yearDays < 0) {
                lunarYear = i;
                break;
            } else {
                dayBetween -= yearDays;
            }
        }
        // 确定年中的月和是否是闰月
        int lunarMonth = 0;
        boolean lunarLeap = false;
        final int leapMonth = XLunarDb.LUNAR_INFO[lunarYear - XLunarDb.MIN_YEAR] & 0xf;
        for (int i = 1; i <= 12; i++) {
            int monthDays = XLunarDb.getMonthDays(lunarYear, i, false);
            if (dayBetween - monthDays < 0) {
                lunarMonth = i;
                lunarLeap = false;
                break;
            } else {
                dayBetween -= monthDays;
            }
            if (i == leapMonth) {
                monthDays = XLunarDb.getMonthDays(lunarYear, leapMonth, true);
                if (dayBetween - monthDays < 0) {
                    lunarMonth = leapMonth;
                    lunarLeap = true;
                    break;
                } else {
                    dayBetween -= monthDays;
                }
            }
        }
        // 确定月中的天
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
     * yyyy-MM-dd HH:mm:ss格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLYMDHMS extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        }
    }

    /**
     * yyyy-MM-dd格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLYMD extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        }
    }

    /**
     * MM-dd格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLMD extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM-dd", Locale.CHINA);
        }
    }

    /**
     * HH:mm:ss格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLHM extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm", Locale.CHINA);
        }
    }

    /**
     * HH:mm:ss格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLHMS extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        }
    }

    /**
     * EEEE格式的SimpleDateFormat的ThreadLocal类
     */
    private static class TLE extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EEEE", Locale.CHINA);
        }
    }

    /**
     * 农历工具类，保存了农历的一些信息，和操作函数
     */
    private static class XLunarDb {
        // 可接受的最早的时间
        public static final String MIN_DATE = "1900-01-31 UTC+0800";
        // 可接受的最迟的时间
        public static final String MAX_DATE = "2050-12-31 UTC+0800";
        // 允许输入的最小年份
        public static final int MIN_YEAR = 1900;
        // 允许输入的最大年份
        public static final int MAX_YEAR = 2050;
        //农历日期格式化工具
        private static final TLLunar tlLunar = new TLLunar();
        // 农历月份
        private static final String[] LUNAR_MONTH = new String[]{"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
        // 农历日期
        private static final String[] LUNAR_DAY = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        // 农历的正则表达式
        private static final String LUNAR_PATTERN = "((19|20)\\d{2})年(闰)?(正|一|二|三|四|五|六|七|八|九|十|冬|腊)月((初|十|廿)(一|二|三|四|五|六|七|八|九)|初十|二十|三十)";
        // 1900年到2050年的阴历数据
        private static final int[] LUNAR_INFO = { //
                0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, //
                0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, //
                0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, //
                0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, //
                0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, //
                0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, //
                0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, //
                0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6, //
                0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, //
                0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, //
                0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, //
                0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, //
                0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, //
                0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, //
                0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0//
        };


        /**
         * 获取yyyy-MM-dd z格式的SimpleDateFormat，线程安全
         *
         * @return SimpleDateFormat对象
         */
        private static SimpleDateFormat sdfLunar() {
            return tlLunar.get();
        }

        /**
         * 获取农历的某年某月有多少天
         *
         * @param lunarYear  农历年份
         * @param lunarMonth 　农历月份
         * @param lunarLeap  　是否是闰月
         * @return 该年该月一共有多少天
         */
        private static int getMonthDays(int lunarYear, int lunarMonth, boolean lunarLeap) {
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
        private static int getYearDays(int lunarYear) {
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
            if (lunarYear < MIN_YEAR || lunarYear >= MAX_YEAR) {
                throw new RuntimeException("非法农历年份！");
            }
            if ((lunarMonth < 1) || (lunarMonth > 12)) {
                throw new RuntimeException("非法农历月份！");
            }
            if (lunarLeap && (LUNAR_INFO[lunarYear - MIN_YEAR] & 0xf) != lunarMonth) {
                throw new RuntimeException(String.format("农历%d年%d月不是闰月", lunarYear, lunarMonth));
            }
            if (lunarDay < 1 || lunarDay > getMonthDays(lunarYear, lunarMonth, lunarLeap)) {
                throw new RuntimeException("非法农历天数！");
            }
        }

        /**
         * yyyy-MM-dd z格式的SimpleDateFormat的ThreadLocal类
         */
        private static class TLLunar extends ThreadLocal<SimpleDateFormat> {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat sdfLunar = new SimpleDateFormat("yyyy-MM-dd z", Locale.CHINA);
                sdfLunar.setTimeZone(TimeZone.getTimeZone("UTC+0800"));
                return sdfLunar;
            }
        }
    }

    private static class XHolidayDb {
        public static final int YEAR_MIN = 2000;
        public static final int YEAR_MAX = 2018;

        public static final String[][] HOLIDAY_2000 = new String[][]{
                {"02-12", "02-13", "04-29", "04-30", "09-30", "10-08"},
                {"02-08", "02-09", "02-10", "02-11", "05-04", "05-05", "10-04", "10-05", "10-06"},
                {"02-05", "02-06", "02-07", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2001 = new String[][]{
                {"01-20", "01-21", "04-28", "04-29", "09-29", "09-30", "12-29", "12-30"},
                {"01-29", "01-30", "05-04", "05-07", "10-04", "10-05"},
                {"01-01", "01-24", "01-25", "01-26", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2002 = new String[][]{
                {"02-09", "02-10", "04-27", "04-28", "09-28", "09-29"},
                {"01-02", "01-03", "02-15", "02-18", "05-06", "05-07", "10-04", "10-07"},
                {"01-01", "02-12", "02-13", "02-14", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2003 = new String[][]{
                {"02-08", "02-09", "04-26", "04-27", "09-27", "09-28"},
                {"02-04", "02-05", "02-06", "02-07", "05-05", "05-06", "05-07", "10-06", "10-07"},
                {"01-01", "02-01", "02-02", "02-03", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2004 = new String[][]{
                {"01-17", "01-18", "05-08", "05-09", "10-09", "10-10"},
                {"01-26", "01-27", "01-28", "05-04", "05-05", "05-06", "05-07", "10-04", "10-05", "10-06", "10-07"},
                {"01-01", "01-22", "01-23", "01-24", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2005 = new String[][]{
                {"02-05", "02-06", "04-30", "05-08", "10-08", "10-09", "12-31"},
                {"01-03", "02-14", "02-15", "05-04", "05-05", "05-06", "10-04", "10-05", "10-06", "10-07"},
                {"01-01", "02-09", "02-10", "02-11", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2006 = new String[][]{
                {"01-28", "02-05", "04-29", "04-30", "09-30", "10-08", "12-30", "12-31"},
                {"01-02", "01-03", "02-01", "02-02", "02-03", "05-04", "05-05", "10-04", "10-05", "10-06"},
                {"01-01", "01-29", "01-30", "01-31", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2007 = new String[][]{
                {"02-17", "02-25", "04-28", "04-29", "09-29", "09-30", "12-29"},
                {"01-02", "01-03", "02-21", "02-22", "02-23", "05-04", "05-07", "10-04", "10-05", "12-31"},
                {"01-01", "02-18", "02-19", "02-20", "05-01", "05-02", "05-03", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2008 = new String[][]{
                {"02-02", "02-03", "05-04", "09-27", "09-28"},
                {"02-11", "02-12", "05-02", "06-09", "09-15", "09-29", "09-30"},
                {"01-01", "02-06", "02-07", "02-08", "04-04", "05-01", "06-08", "09-14", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2009 = new String[][]{
                {"01-04", "01-24", "02-01", "05-31", "09-27", "10-10"},
                {"01-02", "01-28", "01-29", "01-30", "04-06", "05-29", "10-05", "10-07", "10-08"},
                {"01-01", "01-25", "01-26", "01-27", "04-04", "05-01", "05-28", "10-01", "10-02", "10-03", "10-06"}
        };
        public static final String[][] HOLIDAY_2010 = new String[][]{
                {"02-20", "02-21", "06-12", "06-13", "09-19", "09-25", "09-26", "10-09"},
                {"02-16", "02-17", "02-18", "02-19", "05-03", "06-14", "06-15", "09-23", "09-24", "10-04", "10-05", "10-06", "10-07"},
                {"01-01", "02-13", "02-14", "02-15", "04-05", "05-01", "06-16", "09-22", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2011 = new String[][]{
                {"01-30", "02-12", "04-02", "10-08", "10-09", "12-31"},
                {"01-03", "02-07", "02-08", "05-02", "10-04", "10-05", "10-06", "10-07"},
                {"01-01", "02-02", "02-03", "02-04", "04-05", "05-01", "06-06", "09-12", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2012 = new String[][]{
                {"01-21", "01-29", "03-31", "04-01", "04-28", "09-29"},
                {"01-02", "01-03", "01-25", "01-26", "01-27", "04-02", "04-03", "04-30", "06-22", "10-04", "10-05"},
                {"01-01", "01-22", "01-23", "01-24", "04-04", "05-01", "06-23", "09-30", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2013 = new String[][]{
                {"01-05", "01-06", "02-16", "02-17", "04-07", "04-27", "04-28", "06-08", "06-09", "09-22", "09-29", "10-12"},
                {"01-02", "01-03", "02-12", "02-13", "02-14", "02-15", "04-05", "04-29", "04-30", "06-10", "06-11", "09-20", "10-04", "10-07"},
                {"01-01", "02-09", "02-10", "02-11", "04-04", "05-01", "06-12", "09-19", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2014 = new String[][]{
                {"01-26", "02-08", "05-04", "09-28", "10-11"},
                {"02-03", "02-04", "02-05", "02-06", "04-07", "05-02", "10-06", "10-07"},
                {"01-01", "01-31", "02-01", "02-02", "04-05", "05-01", "06-02", "09-08", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2015 = new String[][]{
                {"01-04", "02-15", "02-28", "09-06", "10-10"},
                {"01-02", "02-23", "02-24", "04-06", "06-22", "09-04", "10-05", "10-06", "10-07"},
                {"01-01", "02-18", "02-19", "02-20", "04-05", "05-01", "06-20", "09-03", "09-27", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2016 = new String[][]{
                {"02-06", "02-14", "06-12", "09-18", "10-08", "10-09"},
                {"02-10", "02-11", "02-12", "05-02", "06-10", "06-11", "09-16", "09-17", "10-04", "10-05", "10-06", "10-07"},
                {"01-01", "02-07", "02-08", "02-09", "04-04", "05-01", "06-09", "09-15", "10-01", "10-02", "10-03"}
        };
        public static final String[][] HOLIDAY_2017 = new String[][]{
                {"01-02", "01-22", "02-04", "04-01", "05-27", "09-30"},
                {"01-30", "01-31", "02-01", "02-02", "04-03", "05-29", "10-05", "10-06"},
                {"01-01", "01-27", "01-28", "01-29", "04-04", "05-01", "05-30", "10-01", "10-02", "10-03", "10-04"}
        };
    }
}