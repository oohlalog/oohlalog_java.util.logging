oohlalog_java.util.logging
==========================

OohLaLog Handler for java.util.logging


This is handler for java.util.logging that sends logs to the Oohlalog Cloud Logging Service. 
##Usage/Configuration

This is a handler, just like any other you might use for java.util.logging.  The only difference is that this handler takes one required parameter, your OohLaLog
apiKey.  For a tutorial on how to use java.util.logging, see [here][0].  For full documentation on the Logging class, see [here][1].  An *important* thing to note is that the logging levels that are native to java.util.logging are not the same ones used by OohLaLog.  

OohLaLog logging levels
-------------------------

0: ALL   = OllLevel.ALL
1: TRACE = OllLevel.TRACE
2: DEBUG = OllLevel.DEBUG
3: INFO  = OllLevel.INFO
4: WARN  = OllLevel.WARN
5: ERROR = OllLevel.ERROR
6: FATAL = OllLevel.FATAL
7: OFF   = OllLevel.OFF

For best behavior, we recommend using the static logging levels provided by the OllLevel class.

Here is a small example, for starters.
```
String apiKey = <your apiKey>
Logger logger = Logger.getLogger(MyExample.class.getName());
logger.setLevel(OllLevel.INFO);

OohLaLogHandler handler = new OohLaLogHandler(apiKey);
logger.addHandler(handler);

logger.log(OllLevel.TRACE, "msg1"); // Will not be logged because TRACE is lower severity than the INFO level of the logger
logger.log(OllLevel.INFO, "msg2");  // Will be logged

handler.close();
```


##Dependencies

To use the OohLaLog Log4J Appender please include the following jars in your classpath:

###1. OohLaLog Jar
```
TODO
```


### 2. GSON Jar
```
gson-2.2.4.jar
```
Repository Info:

Maven Central

Maven info:
```
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.2.4</version>
</dependency>
```
[0]:http://www.vogella.com/tutorials/Logging/article.html
[1]:http://docs.oracle.com/javase/7/docs/api/java/util/logging/package-summary.html
