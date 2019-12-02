# XTools-Common
Java开发基础工具集，陆续收录常用的java代码，令代码更加简洁美观

## 1.4.X 正式发布
#### 强大的配置管理工具
* XTools默认加载classpath下所有的config.properties配置文件
* XTools支持使用代码读取和修改配置信息
* XTools提供了监听配置配置变化的功能

各工具支持的配置的项和默认值都在各个工具类或实现类中以"CFG_"开头的常量定义。
一般格式为：**groupId$artifactId$configKey=configValue**

#### 全新设计的日志工具
* 支持统一设置日志记录等级
* 支持处理器根据tag决定是否处理日志，可以用于正则匹配处理等。使日志功能使用起来更加简单易懂。

#### 支持装饰者的HTTP工具
* HTTP请求工具支持设置装饰者，以统一预处理或后处理http请求

## 静态工具方法目录
#### 编码解码
* 字符串和文件的MD5散列
* 字符串和文件的SHA1散列

#### HTTP相关
* http get请求
* http post请求
* http put请求
* http delete请求

#### 字符串相关
* 判断字符串是否为null或空
* 判断字符串是否为空白串
* 连接数组中的字符串
* 连接集合中的字符串
* 连接映射中的字符串
* 将字符串写入文件

#### 文件相关
* 将文件读取成字符串
* 将文件复制到另一文件

#### 流相关
* 将流读取成字符串
* 将流读取成文件
* 将流读取到另一个流中

#### 日期相关
* 获取某天00:00:00时刻的Date对象
* 判断某天是工作日、公休日还是节假日
* 获取日期格式化类
* 以周为基准计算某日
* 以月为基准计算某日
* 以年为基准计算某日
* 以月为基准计算某周
* 以年为基准计算某周
* 以年为基准计算某月
* 判断某天是一周的第几天
* 判断某天是一月的第几天
* 判断某天是一年的第几天
* 判断某周是一月的第几周
* 判断某周是一年的第几周
* 判断某月是一年的第几月
* 阳历转农历
* 农历转阳历

#### 环境相关
* 判断是否是Windows
* 判断是否是Linux
* 判断是否是MacOS
* 判断是否是MacOSX

#### 配置相关
* 设置配置信息
* 读取配置信息
* 监听配置信息

#### 日志相关
* 记录详细日志
* 记录提醒日志
* 记录警告日志
* 记录错误日志

### Demo
* maven依赖
```xml
<dependency>
    <groupId>me.xuxiaoxiao</groupId>
    <artifactId>xtools-common</artifactId>
    <version>1.4.0</version>
</dependency>
```
* gradle依赖
```gradle
implementation 'me.xuxiaoxiao:xtools-common:1.4.0'
```
使用示例
```java
public class Test {
    public static void main(String[] args) {
        
        //获取字符串的MD5值
        String strMD5 = XTools.md5("XTools-Common");
        
        //获取文件的SHA1值
        String fileSHA1 = XTools.sha1(new File("test.txt"));
        
        //
        //将github首页保存成字符串
        String githubToStr = XTools.http(XRequest.GET("https://github.com")).string();
        
        //将github首页保存成文件
        File githubToFile = XTools.http(XRequest.GET("https://github.com")).file("github.txt");
        
        //
        //判断字符串是否为空
        boolean isStrEmpty = XTools.strEmpty("");
        
        //判断字符串是否为空白
        boolean isStrBlank = XTools.strBlank("\n");
        
        //将集合中的字符串以逗号连接
        String joinStrWithComma = XTools.strJoin(new String[]{"A","B","C"}, ",");
        
        //
        //判断今天是否是节假日（仅支持2000年-今年）
        boolean isTodayHoliday = XTools.dateType(new Date()) == XTimeTools.HOLIDAY;
        
        //获取下周一的Date对象
        Date nextMonday = XTools.dateByWeek(null, 1, 0);
        
        //获取1901-2100年间的阳历日期对应的农历日期
        String lunarToday = XTools.solarToLunar(new Date());
    }
}
```
