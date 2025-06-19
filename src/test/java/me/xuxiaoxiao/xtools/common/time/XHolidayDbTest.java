package me.xuxiaoxiao.xtools.common.time;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.xuxiaoxiao.xtools.common.XTools;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

public class XHolidayDbTest {

    @Test
    public void generate() throws Exception {
        JsonNode jsonNode = new ObjectMapper().readTree(XTools.fileToStr(new File("src/test/resources/holiday.json"), "utf-8"));
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode yearNode = jsonNode.get(String.valueOf(2000 + i));
            Short[] yearHolidays = new Short[yearNode.size()];

            int index = 0;
            Iterator<Map.Entry<String, JsonNode>> iterator = yearNode.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();

                int type = entry.getValue().asInt();

                String monthStr = entry.getKey().substring(0, 2);
                int month = Integer.parseInt(monthStr.startsWith("0") ? monthStr.substring(1) : monthStr) - 1;

                String dayStr = entry.getKey().substring(2);
                int day = Integer.parseInt(dayStr.startsWith("0") ? dayStr.substring(1) : dayStr) - 1;

                yearHolidays[index++] = (short) (type << 12 | month << 8 | day);
            }
            StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
            for (Short yearHoliday : yearHolidays) {
                stringJoiner.add("0x" + String.format("%04x", yearHoliday));
            }

            System.out.printf("public static final short[] HOLIDAY_%d = %s;%n", (2000 + i), stringJoiner);
        }
    }
}