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
> 加强集成调用支持
* 简化开发、部署、数据移植；
* 支持报表自定义code（并增加相应api），便于集成调用
* 修复只返回一行数据的重大问题
* 支持直接获取原生结果集（api）
* 修复报表copy/edit数据污染
* 修正布局方向写死的问题（放开让用户可调）
* 支持在指定报表的数据源上调用sql（集成应用可以处理复杂参数源，比如基于sql的下拉列表参数源）
* 参数增加hidden标记，便于集成调用过滤不应让用户接触的参数
* 支持从主体查询Sql中提取查询参数并追加到参数列表中（减少人为失误和工作量）
* 支持把隐藏+非必须（但是必须有默认值)的参数当作基于sql的参数的数据源的变量（注意：这样的参数为参数sql中专用，不会出现在查询界面）
* 加强参数合理性检查，优化错误提示，更加具体
* 加强对用于in的（下拉多选“,”分割）列表数据的支持
* 增加对strng、date类型值特殊字符转义（减少错误）
* 简化checkbox选择，提供就地刷新数据源下拉列表
* 去掉内建的startTime、endTime等时间参数（以免影响集成api对参数的检查和过滤）
* sql分析后得到的列名不再转为小写，而是原封不动地返回（保持大小写的习惯）
* 放开之前被隐藏的图表功能
* 隐藏冗余的checkbox条件控件类型（原生特性：不选中不会回传值）
* 修正报表结果导出Excel的条件错误和不一致问题
* 修正后端DataGridPager参数（分页信息）接收无效问题
* 增加对自定义对齐和统计列色阶样式的支持
* 用日期、时间、数值定制格式化增强和替换掉原来的（小数点精度）
* 调整报表数据结果html样式（支持双击标记）
* 提高列名称兼容性、优化界面和操作
* 支持计算列色阶样式
* 支持修改报表后列表刷新焦点恢复到那一行
* 在有自定义代码(ucode)的报表行ID上显示小旗标
* 结果列支持BOOLEAN类型
* 查询参数支持布尔型(bool)
* 报表导出不导出隐藏参数
* 修正复制/新增时没有id报错的问题（select key问题）
* 支持查看报表json和从json复制新建报表功能

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
