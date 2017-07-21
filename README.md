# XTools-Common
Java开发基础工具集，陆续收录常用的java代码，令你的代码更加简洁美观

### 第一层封装
例如XCodeTools、XHttpTools、XTimeTools等，对常用代码进行第一层封装。
使用起来比较复杂，但是功能相对强大。

### 第二层封装
XTools类对XCodeTools、XHttpTools、XTimeTools等进行了再一次封装。
使用起来非常便捷，常用的就是这一层封装。

### 目录
##### 编码解码
* 字符串和文件的MD5散列
* 字符串和文件的SHA1散列
##### HTTP相关
* http get请求
* http post请求
##### 字符串相关
* 判断字符串是否为null或空
* 判断字符串是否为空白串
* 连接集合中的字符串
* 连接映射中的字符串
* 将字符串写入文件
##### 文件相关
* 将文件读取成字符串
* 将文件复制到另一文件
##### 流相关
* 将流读取成字符串
* 将流读取成文件
* 将流读取到另一个流中
##### 日期相关
* 判断某天是周几
* 判断某天是工作日、公休日还是节假日
* 以天为基准计算日期
* 以周为基准计算日期
* 以月为基准计算日期
* 阳历转农历
* 农历转阳历

### Demo
```java
public class Test {
    public static void main(String[] args) {
        //
        //获取字符串的MD5值
        String strMD5 = XTools.md5("XTools-Common");
        //获取文件的SHA1值
        String fileSHA1 = XTools.sha1(new File("test.txt"));
        //
        //将github首页保存成字符串
        String githubToStr = XTools.http(XHttpTools.XUrl.base("https://github.com")).string();
        //将github首页保存成文件
        File githubToFile = XTools.http(XHttpTools.XUrl.base("https://github.com")).file("github.txt");
        //
        //判断字符串是否为空
        boolean isStrEmpty = XTools.strEmpty("");
        //判断字符串是否为空白
        boolean isStrBlank = XTools.strBlank("\n");
        //将集合中的字符串以逗号连接
        String joinStrWithComma = XTools.strJoin(Arrays.asList("A", "B", "C"), ",");
        //
        //判断今天是否是节假日（农历节日仅支持2000年-今年）
        boolean isTodayHoliday = XTools.dateType(new Date()) == XTimeTools.HOLIDAY;
        //获取下周一的Date对象
        Date nextMonday = XTools.dateByWeek(null, 1, 0);
        //获取1901-2100年间的阳历日期对应的农历日期
        String lunarToday = XTools.solarToLunar(new Date());
    }
}
```