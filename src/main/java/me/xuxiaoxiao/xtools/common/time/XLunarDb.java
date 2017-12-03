package me.xuxiaoxiao.xtools.common.time;


import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 农历数据库，保存了农历的一些信息，和操作函数
 */
class XLunarDb {
    // 可接受的最早的时间
    public static final String MIN_DATE = "1901-02-19";
    // 可接受的最迟的时间
    public static final String MAX_DATE = "2101-01-29";
    // 允许输入的最小年份
    public static final int MIN_YEAR = 1901;
    // 允许输入的最大年份
    public static final int MAX_YEAR = 2101;
    // 农历日期格式化工具
    public static final TLLunar TL_LUNAR = new TLLunar();
    // 农历月份
    public static final String[] LUNAR_MONTH = new String[]{"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
    // 农历日期
    public static final String[] LUNAR_DAY = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九"};
    // 农历的正则表达式
    public static final String LUNAR_PATTERN = "((19|20|21)\\d{2})年(闰)?([正二三四五六七八九十冬腊])月(([初十廿])([一二三四五六七八九])|初十|二十|三十)";
    // 1901年到2100年的阴历数据，来源于香港天文台http://data.weather.gov.hk/gts/time/conversion1_text_c.htm
    public static final int[] LUNAR_INFO = {
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
    public static int lunarMonthDays(int lunarYear, int lunarMonth, boolean lunarLeap) {
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
    public static int lunarYearDays(int lunarYear) {
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
    public static void checkLunarDate(int lunarYear, int lunarMonth, int lunarDay, boolean lunarLeap) {
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
    public static class TLLunar extends ThreadLocal<SimpleDateFormat> {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdfLunar = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            sdfLunar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return sdfLunar;
        }
    }
}
