package me.xuxiaoxiao.xtools.common;

import java.util.Collection;
import java.util.Map;

/**
 * 字符串处理工具类
 *
 * @author XXX
 */
public final class XStrTools {

    private XStrTools() {
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 要判断的字符串
     * @return str == null || str.length() == 0
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
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
}
