package me.xuxiaoxiao.xtools.common.time;

import me.xuxiaoxiao.xtools.common.XTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用的基本的关于时间的函数的集合
 */
public class XTimeTools {
    /**
     * 周一
     */
    public final static int MONDAY = 0;
    /**
     * 周二
     */
    public final static int TUESDAY = 1;
    /**
     * 周三
     */
    public final static int WEDNESDAY = 2;
    /**
     * 周四
     */
    public final static int THURSDAY = 3;
    /**
     * 周五
     */
    public final static int FRIDAY = 4;
    /**
     * 周六
     */
    public final static int SATURDAY = 5;
    /**
     * 周日
     */
    public final static int SUNDAY = 6;

    /**
     * int dateType(Date date);函数的返回值的枚举，工作日，补班日
     */
    public static final int WORKDAY = 1;
    /**
     * int dateType(Date date);函数的返回值的枚举，公休日，调休日
     */
    public static final int RESTDAY = 2;
    /**
     * int dateType(Date date);函数的返回值的枚举，法定节假日
     */
    public static final int HOLIDAY = 3;
    /**
     * 一小时的毫秒数
     */
    public static final long HOUR_MILLIS = 60 * 60 * 1000;
    /**
     * 一天的毫秒数
     */
    public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    /**
     * 一周的毫秒数
     */
    public static final long WEEK_MILLIS = 7 * DAY_MILLIS;
    /**
     * yyyy-MM-dd HH:mm:ss.SSS格式的日期
     */
    public static final String FORMAT_YMDHMSSSS = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * yyyy-MM-dd HH:mm:ss格式的日期
     */
    public static final String FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    /**
     * yyyy-MM-dd格式的日期
     */
    public static final String FORMAT_YMD = "yyyy-MM-dd";
    /**
     * yyyy-MM格式的日期
     */
    public static final String FORMAT_YM = "yyyy-MM";
    /**
     * MM-dd格式的日期
     */
    public static final String FORMAT_MD = "MM-dd";
    /**
     * HH:mm:ss格式的日期
     */
    public static final String FORMAT_HMS = "HH:mm:ss";
    /**
     * EEEE格式的日期
     */
    public static final String FORMAT_EEEE = "EEEE";

    /**
     * 将date对象转换成相应格式的字符串
     *
     * @param format 格式字符串
     * @param date   date对象
     * @return 相应格式的字符串
     */
    @Nonnull
    public static String dateFormat(@Nonnull String format, @Nonnull Date date) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(date);

        return new SimpleDateFormat(format).format(date);
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
        Objects.requireNonNull(format);
        Objects.requireNonNull(dateStr);

        try {
            return new SimpleDateFormat(format).parse(dateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("日期:%s 不符合格式:%s", dateStr, format), e);
        }
    }

    /**
     * 获取任意一天的类型
     *
     * @param date 任意一天的date对象
     * @return 任意一天的类型。1：工作日(XTimeTools.WORKDAY)，2：公休日(XTimeTools.RESTDAY)，3：节假日(XTimeTools.HOLIDAY)
     * @see #WORKDAY
     * @see #RESTDAY
     * @see #HOLIDAY
     */
    public static int dateType(@Nonnull Date date) {
        Objects.requireNonNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE) - 1;
        try {
            short[] holidays = (short[]) XHolidayDb.class.getDeclaredField("HOLIDAY_" + year).get(null);
            for (int typeMD : holidays) {
                if ((month << 8 | day) == (typeMD & 0xfff)) {
                    return typeMD >> 12;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateInWeek(date) < SATURDAY ? WORKDAY : RESTDAY;
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
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        return dateParse(FORMAT_YMD, dateFormat(FORMAT_YMD, new Date(baseTime + offset * DAY_MILLIS)));
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
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime + weekOffset * WEEK_MILLIS);
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return XTimeTools.date(calendar.getTime(), dayIndex - 6);
        } else {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            return XTimeTools.date(calendar.getTime(), dayIndex);
        }
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
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.add(Calendar.MONTH, monthOffset);
        calendar.set(Calendar.DAY_OF_MONTH, dayIndex + 1);
        return XTimeTools.date(calendar.getTime(), 0);
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
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) / 3 + seasonOffset) * 3);
        calendar.set(Calendar.DAY_OF_MONTH, dayIndex + 1);
        return XTimeTools.date(calendar.getTime(), 0);
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
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.add(Calendar.YEAR, yearOffset);
        calendar.set(Calendar.DAY_OF_YEAR, dayIndex + 1);
        return XTimeTools.date(calendar.getTime(), 0);
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
        Date date = dateByMonth(base, monthOffset, 0);
        while (dateInWeek(date) != MONDAY) {
            date = new Date(date.getTime() + DAY_MILLIS);
        }
        return new Date(date.getTime() + weekIndex * WEEK_MILLIS);
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
        Date date = dateBySeason(base, seasonOffset, 0);
        while (dateInWeek(date) != MONDAY) {
            date = new Date(date.getTime() + DAY_MILLIS);
        }
        return new Date(date.getTime() + weekIndex * WEEK_MILLIS);
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
        Date date = dateByYear(base, yearOffset, 0);
        while (dateInWeek(date) != MONDAY) {
            date = new Date(date.getTime() + DAY_MILLIS);
        }
        return new Date(date.getTime() + weekIndex * WEEK_MILLIS);
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
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) / 3 + seasonOffset) * 3 + monthIndex);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return XTimeTools.date(calendar.getTime(), 0);
    }

    /**
     * 获取以某天所在的那一年为基准偏移若干年的某月的一号00:00:00时刻的date对象
     *
     * @param base       基准时间的date对象，如果为null则以当前时间为基准
     * @param yearOffset 偏移的年数
     * @param monthIndex 那一年的第几个月（一月为0）
     * @return 以base所在的那一年为基准偏移yearOffset年的monthIndex月的一号00:00:00时刻的date对象
     */
    @Nonnull
    public static Date monthByYear(@Nullable Date base, int yearOffset, int monthIndex) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.add(Calendar.YEAR, yearOffset);
        calendar.set(Calendar.MONTH, monthIndex);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return XTimeTools.date(calendar.getTime(), 0);
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
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.add(Calendar.YEAR, yearOffset);
        calendar.set(Calendar.MONTH, seasonIndex * 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return XTimeTools.date(calendar.getTime(), 0);
    }

    /**
     * 获取任意一天是一周中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那周中的第几天（每周的第一天是周一，周一为0）
     */
    public static int dateInWeek(@Nullable Date base) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return MONDAY;
            case Calendar.TUESDAY:
                return TUESDAY;
            case Calendar.WEDNESDAY:
                return WEDNESDAY;
            case Calendar.THURSDAY:
                return THURSDAY;
            case Calendar.FRIDAY:
                return FRIDAY;
            case Calendar.SATURDAY:
                return SATURDAY;
            case Calendar.SUNDAY:
                return SUNDAY;
            default:
                return -1;
        }
    }

    /**
     * 获取任意一天是一个月中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那个月中的第几天（一号为0）
     */
    public static int dateInMonth(@Nullable Date base) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        return calendar.get(Calendar.DAY_OF_MONTH) - 1;
    }

    /**
     * 获取任意一天是一个季度中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那个季度中的第几天（第一天为0）
     */
    public static int dateInSeason(@Nullable Date base) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) / 3) * 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return (int) ((XTimeTools.date(new Date(baseTime), 0).getTime() - XTimeTools.date(calendar.getTime(), 0).getTime()) / DAY_MILLIS);
    }

    /**
     * 获取任意一天是一年中的第几天
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天是那一年中的第几天（第一天为0）
     */
    public static int dateInYear(@Nullable Date base) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        return calendar.get(Calendar.DAY_OF_YEAR) - 1;
    }

    /**
     * 获取任意一天所在的周是哪个月的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪个月的第几周（正数表示本月第几周，负数表示上月第几周,例：1=本月第二周，-3=上月第四周）
     */
    public static int weekInMonth(@Nullable Date base) {
        Date weekMonday = dateByWeek(base, 0, MONDAY);

        Date monthMonday = dateByMonth(base, 0, 0);
        while (dateInWeek(monthMonday) != MONDAY) {
            monthMonday = new Date(monthMonday.getTime() + DAY_MILLIS);
        }

        if (weekMonday.compareTo(monthMonday) >= 0) {
            return (int) ((weekMonday.getTime() - monthMonday.getTime()) / WEEK_MILLIS);
        } else {
            Date lastMonthMonday = dateByMonth(base, -1, 0);
            while (dateInWeek(lastMonthMonday) != MONDAY) {
                lastMonthMonday = new Date(lastMonthMonday.getTime() + DAY_MILLIS);
            }

            return (int) ((lastMonthMonday.getTime() - weekMonday.getTime()) / WEEK_MILLIS);
        }
    }

    /**
     * 获取任意一天所在的周是哪一季度的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪一季度的第几周（正数表示本季度第几周，负数表示上季度第几周,例：1=本季第第二周，-12=上季度第13周）
     */
    public static int weekInSeason(@Nullable Date base) {
        Date weekMonday = dateByWeek(base, 0, MONDAY);

        Date seasonMonday = dateBySeason(base, 0, 0);
        while (dateInWeek(seasonMonday) != MONDAY) {
            seasonMonday = new Date(seasonMonday.getTime() + DAY_MILLIS);
        }

        if (weekMonday.compareTo(seasonMonday) >= 0) {
            return (int) ((weekMonday.getTime() - seasonMonday.getTime()) / WEEK_MILLIS);
        } else {
            Date lastSeasonMonday = dateBySeason(base, -1, 0);
            while (dateInWeek(lastSeasonMonday) != MONDAY) {
                lastSeasonMonday = new Date(lastSeasonMonday.getTime() + DAY_MILLIS);
            }
            return (int) ((lastSeasonMonday.getTime() - weekMonday.getTime()) / WEEK_MILLIS);
        }
    }

    /**
     * 获取任意一天所在的周是哪一年的第几周
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的周是哪一年的第几周（正数表示今年第几周，负数表示去年第几周,例：1=今年第二周，-51=去年第52周）
     */
    public static int weekInYear(@Nullable Date base) {
        Date weekMonday = dateByWeek(base, 0, MONDAY);

        Date yearMonday = dateByYear(base, 0, 0);
        while (dateInWeek(yearMonday) != MONDAY) {
            yearMonday = new Date(yearMonday.getTime() + DAY_MILLIS);
        }

        if (weekMonday.compareTo(yearMonday) >= 0) {
            return (int) ((weekMonday.getTime() - yearMonday.getTime()) / WEEK_MILLIS);
        } else {
            Date lastYearMonday = dateByYear(base, -1, 0);
            while (dateInWeek(lastYearMonday) != MONDAY) {
                lastYearMonday = new Date(lastYearMonday.getTime() + DAY_MILLIS);
            }

            return (int) ((lastYearMonday.getTime() - weekMonday.getTime()) / WEEK_MILLIS);
        }
    }

    /**
     * 获取任意一天所在的月是那一季度的第几月
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的月是那一季度的第几月，第一个月为0
     */
    public static int monthInSeason(@Nullable Date base) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        return calendar.get(Calendar.MONTH) - (calendar.get(Calendar.MONTH) / 3) * 3;
    }

    /**
     * 获取任意一天所在的月是那一年的第几月
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的月是那一年的第几月，第一个月为0
     */
    public static int monthInYear(@Nullable Date base) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        return calendar.get(Calendar.MONTH);
    }

    /**
     * 获取任意一天所在的季度是那一年的第几个季度
     *
     * @param base 基准时间的date对象，如果为null则以当前时间为基准
     * @return base那天所在的季度是那一年的第几季度，第一个季度为0
     */
    public static int seasonInYear(@Nullable Date base) {
        long baseTime = base != null ? base.getTime() : System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTime);
        return calendar.get(Calendar.MONTH) / 3;
    }

    /**
     * 将阳历时间转化成农历时间
     *
     * @param solarDate 阳历时间的Date对象
     * @return 对应的农历时间的字符串，例1：1992年八月初六。例2：2033年闰冬月廿八
     */
    @Nonnull
    public static String solarToLunar(@Nonnull Date solarDate) {
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
        int dayBetween = (int) ((solarDate.getTime() - minDate.getTime()) / DAY_MILLIS);
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
                default:
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
    @Nonnull
    public static Date lunarToSolar(@Nonnull String lunarDate) {
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
        int lunarYear = Integer.parseInt(strYear);
        // 解析农历月，1代表正月
        int lunarMonth = 0;
        for (int i = 0; i < XLunarDb.LUNAR_MONTH.length; i++) {
            if (XLunarDb.LUNAR_MONTH[i].equals(strMonth)) {
                lunarMonth = i + 1;
                break;
            }
        }
        // 解析农历日，1代表初一
        int lunarDay;
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
                default:
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
        boolean lunarLeap = !XTools.isEmpty(strLeap);
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

            return new Date(minDate.getTime() + dayBetween * DAY_MILLIS);
        } catch (ParseException e) {
            throw new RuntimeException("解析日期出错");
        }
    }
}
