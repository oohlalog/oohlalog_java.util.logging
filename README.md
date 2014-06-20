oohlalog_java.util.logging
==========================

OohLaLog Handler for java.util.logging


This is handler for java.util.logging that sends logs to the Oohlalog Cloud Logging Service. 
##Usage/Configuration

This handler takes one required parameter, your OohLaLog apiKey.  For a tutorial on how to use java.util.logging, see [here][0].  For full documentation on the Logging class, see [here][1].  

An *important* thing to note is that the logging levels that are native to java.util.logging are not the same ones used by OohLaLog.  All default Level constants will be translated to their OohLaLog counterparts as shown in the table below. 
For best behavior, we recommend using the OllLevel constants instead.

OohLaLog logging levels
-------------------------

| Severity     | OohLaLog Level | Accessed Via     | Correspondence with java.util.logging |
| ------------ | -------------- | ---------------- | ------------------------------------- |
| 0            | ALL            | OllLevel.ALL     | Level.ALL                             |
| 1            | TRACE          | OllLevel.TRACE   | Level.FINEST, Level.FINER             |
| 2            | DEBUG          | OllLevel.DEBUG   | Level.FINE, Level.CONFIG              |
| 3            | INFO           | OllLevel.INFO    | Level.INFO                            |
| 4            | WARN           | OllLevel.WARN    | Level.WARNING                         |
| 5            | ERROR          | OllLevel.ERROR   | Level.SEVERE                          |
| 6            | FATAL          | OllLevel.FATAL   |  NA                                   |
| 7            | OFF            | OllLevel.OFF     | Level.OFF                             |

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
*IMPORTANT:* When finished using the OohLaLogHandler, make sure to call the .close() method on the OohLaLogHandler to release the resources that it uses.

##Dependencies

To use the OohLaLogHandler please include the following jars in your classpath:

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
