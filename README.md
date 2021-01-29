## HSDB Plugin For XPocket
![XPocket](resourse/xpocket.jpg)

### HSDB Plugin For XPocket
#### 简介
hotspot JVM官方debug工具插件，获取hotspot的运行时数据，hotspot运行时状态分析工具。目前该插件支持jdk8及以上版本，依赖JDK，无法在JRE环境使用。
支持Linux/MacOS/Windows操作系统。

#### 操作指南
使用
``` shell
use jhsdb@JDK
```
进入jhsdb插件域
然后使用
``` shell
clhsdb
```
启动clhsdb，命令详情可以使用h
``` shell
help
```
或者
``` shell
system.help jhsdb@JDK
system.help jhsdb.attach@JDK
```
来获取详细帮助信息

更多操作以及介绍请参考[官方介绍](https://docs.oracle.com/javase/jp/8/docs/serviceabilityagent/sun/jvm/hotspot/HSDB.html)

[XPocket主框架](https://github.com/perfma/xpocket)

[插件下载](https://plugin.xpocket.perfma.com/plugin/54)