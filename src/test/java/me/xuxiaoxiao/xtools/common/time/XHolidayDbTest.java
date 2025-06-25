package me.xuxiaoxiao.xtools.common.time;

import me.xuxiaoxiao.xtools.common.XTools;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.StringJoiner;

public class XHolidayDbTest {

    @Test
    public void generate() throws Exception {
        String allYearHolidays = XTools.fileToStr(new File("src/test/resources/holiday.txt"), "utf-8");
        for (String yearHolidaysStr : allYearHolidays.split("\n")) {
            if (!XTools.isBlank(yearHolidaysStr)) {
                int year = Integer.parseInt(yearHolidaysStr.substring(yearHolidaysStr.indexOf("[") + 1, yearHolidaysStr.indexOf("]")).trim());

                String[] dateTypeStrArr = yearHolidaysStr.substring(yearHolidaysStr.indexOf("]") + 1).split(",");
                Short[] holidaysArr = new Short[dateTypeStrArr.length];
                for (int i = 0; i < dateTypeStrArr.length; i++) {
                    String dateTypeStr = dateTypeStrArr[i];

                    String dateStr = dateTypeStr.substring(0, dateTypeStr.indexOf("=")).trim();
                    String typeStr = dateTypeStr.substring(dateTypeStr.indexOf("=") + 1).trim();

                    int month = Integer.parseInt(dateStr.startsWith("0") ? dateStr.substring(1, 2) : dateStr.substring(0, 2));
                    int date = Integer.parseInt(dateStr.substring(2).startsWith("0") ? dateStr.substring(3) : dateStr.substring(2));
                    int type = Integer.parseInt(typeStr);

                    holidaysArr[i] = (short) (type << 12 | (month - 1) << 8 | (date - 1));
                }
                StringJoiner joiner = new StringJoiner(",", "{", "}");
                for (Short yh : holidaysArr) {
                    joiner.add("0x" + String.format("%04x", yh));
                }

                System.out.printf("public static final short[] HOLIDAY_%d = %s;%n", year, joiner);
            }
        }
    }
}