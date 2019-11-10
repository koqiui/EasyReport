EasyReport
==========

A simple and easy to use Web Report System for java

EasyReport是一个简单易用的Web报表工具,它的主要功能是把SQL语句查询出的数据转换成报表页面，
同时支持表格的跨行(RowSpan)与跨列(ColSpan)配置。
同时它还支持报表Excel导出、图表显示及固定表头与左边列的功能。  
欢迎加入QQ群交流：（365582678）

* mvn -DskipTests package -P${env} (${env}变量说明:dev表示开发环境,prod表示生产环境)
* mvn spring-boot:run -pl easyreport-web 

然后就可以通过浏览器localhost:8080查看


## Release(发布说明)
### what's new?(ver2.1.0)
修复内容：
* 简化开发、部署、数据移植；
* 修复数据源配置js错误；
* 支持报表自定义code（并增加相应api），便于集成调用
* 支持直接获取原生结果集（api）
* 修复报表copy/edit数据污染；
* 修正布局方向写死的问题；
* 支持在指定报表的数据源上调用sql（集成应用可以处理复杂参数源，比如基于sql的下拉列表参数源）
* 参数增加hidden标记，便于集成调用过滤不应让用户接触的参数
* 其他问题修复

### what's new?(ver2.1)
* 改进图表报表图表生成并增加图表生成配置
* 定时任务功能完成
* 支持大数据产品查询引擎(Hive,Presto,HBase,Drill,Impala等）
* 提供REST API服务接口
* 增加报表权限控制

### what's new?(ver2.0)
* 界面交互调整,前端js代码全部重写,方便向AMD模块化转换
* 报表引擎查询支持CP30、Druid、DBCP2连接池
* JAVA部分代码重构
* 加入用户及权限管理模块
* 数据访问采用mybatis框架,方便二次开发
* 报表展现支持自定义生成模板

## [开发参考][]
## [入门手册][]
## [用户参考][]
## 捐助
您的热情,我的动力!开源是一种精神,也是一种生活...

![支付宝][]

[开发参考]: https://github.com/koqiui/EasyReport/blob/master/docs/EasyReport开发、部署、应用指南.pdf
[入门手册]: https://github.com/koqiui/EasyReport/blob/master/docs/manual/user-guide.md
[用户参考]: https://github.com/koqiui/EasyReport/blob/master/docs/manual/version2_0.md
[支付宝]: https://raw.githubusercontent.com/koqiui/EasyReport/master/docs/assets/imgs/alipay-code.png
