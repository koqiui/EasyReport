
-- ----------------------------
-- Records of ezrpt_member_module
-- ----------------------------
INSERT INTO `ezrpt_member_module` VALUES ('3', '40', '数据源', 'report.ds', 'icon-datasource', 'views/report/ds', '40,3', '0', '0', '', '', '2', '1', '数据源', '2014-10-30 06:41:24', '2015-12-17 18:13:28');
INSERT INTO `ezrpt_member_module` VALUES ('4', '40', '报表设计', 'report.designer', 'icon-chart', 'views/report/designer', '40,4', '0', '0', '', '', '1', '1', '报表设计', '2014-10-30 06:41:42', '2015-12-17 18:13:28');
INSERT INTO `ezrpt_member_module` VALUES ('7', '0', '用户权限', 'membership', 'icon-user', 'views/report/user', '7', '1', '0', '', '', '3', '1', '用户权限', '2014-10-30 06:45:47', '2015-12-17 18:13:28');
INSERT INTO `ezrpt_member_module` VALUES ('23', '7', '用户管理', 'membership.user', 'icon-member', 'views/member/user', '7,23', '0', '0', '', '', '1', '1', '用户管理', '2014-10-30 07:38:22', '2015-12-17 18:13:57');
INSERT INTO `ezrpt_member_module` VALUES ('24', '7', '角色管理', 'membership.role', 'icon-group', 'views/member/role', '7,24', '0', '0', '', '', '2', '1', '角色管理', '2014-10-30 07:38:44', '2015-12-17 18:13:57');
INSERT INTO `ezrpt_member_module` VALUES ('25', '7', '权限管理', 'membership.permission', 'icon-perm', 'views/member/permission', '7,25', '0', '0', '', '', '3', '1', '操作管理', '2014-10-30 07:39:03', '2015-12-17 18:13:57');
INSERT INTO `ezrpt_member_module` VALUES ('26', '32', '系统日志', 'membership.event', 'icon-event', 'views/member/event', '32,26', '0', '0', '', '', '3', '1', '系统日志', '2014-10-30 07:41:06', '2015-12-17 18:13:57');
INSERT INTO `ezrpt_member_module` VALUES ('31', '7', '模块管理', 'membership.module', 'icon-org', 'views/member/module', '7,31', '0', '0', '', '', '4', '1', '模块管理', '2014-10-31 02:21:46', '2015-12-17 18:13:57');
INSERT INTO `ezrpt_member_module` VALUES ('32', '0', '系统管理', 'sys', 'icon-settings4', 'views/sys/conf', '32', '1', '0', '', '', '5', '1', '系统管理', '2014-11-12 04:20:57', '2015-12-17 18:13:28');
INSERT INTO `ezrpt_member_module` VALUES ('39', '32', '系统配置', 'sys.conf', 'icon-settings2', 'views/sys/conf', '32,39', '0', '0', ' ', ' ', '2', '0', '系统配置', '2015-08-08 02:48:03', '2015-12-17 18:13:57');
INSERT INTO `ezrpt_member_module` VALUES ('40', '0', '报表管理', 'report', 'icon-designer1', 'views/report/designer', '40', '1', '0', '', '', '1', '1', '报表管理', '2015-12-14 03:45:36', '2015-12-17 18:13:28');
INSERT INTO `ezrpt_member_module` VALUES ('41', '40', '参数配置', 'report.conf', 'icon-settings', 'views/report/conf', '40,41', '0', '0', '', '', '3', '1', '参数配置', '2015-12-14 03:45:36', '2015-12-17 18:13:28');
INSERT INTO `ezrpt_member_module` VALUES ('42', '0', '计划任务', 'schedule', 'icon-scheduled1', 'views/schedule/task', '42', '1', '0', '', '', '2', '1', '计划任务', '2015-12-14 03:45:36', '2015-12-17 18:13:28');
INSERT INTO `ezrpt_member_module` VALUES ('54', '42', '任务管理', 'schedule.task', 'icon-task2', 'views/schedule/task', '42,54', '0', '0', ' ', ' ', '1', '1', '任备管理', '2016-08-29 09:43:08', '2016-08-29 09:43:12');


-- ----------------------------
-- Records of ezrpt_member_permission
-- ----------------------------
INSERT INTO `ezrpt_member_permission` VALUES ('2', '23', '删除用户', 'membership.user:delete', '2', '删除', '2014-10-30 08:25:52', '2014-10-30 08:24:54');
INSERT INTO `ezrpt_member_permission` VALUES ('4', '23', '编辑用户', 'membership.user:update', '3', '编辑', '2014-10-30 13:02:37', '2014-10-30 13:01:39');
INSERT INTO `ezrpt_member_permission` VALUES ('5', '23', '查看用户', 'membership.user:view', '4', '查看', '2014-10-30 13:08:23', '2014-10-30 13:07:25');
INSERT INTO `ezrpt_member_permission` VALUES ('6', '24', '添加角色', 'membership.role:add', '5', '添加', '2014-10-31 02:13:48', '2014-10-31 02:12:52');
INSERT INTO `ezrpt_member_permission` VALUES ('7', '24', '删除角色', 'membership.role:delete', '4', '删除', '2014-10-31 02:15:43', '2014-10-31 02:14:47');
INSERT INTO `ezrpt_member_permission` VALUES ('8', '24', '编辑角色', 'membership.role:update', '3', '编辑', '2014-10-31 02:16:07', '2014-10-31 02:15:10');
INSERT INTO `ezrpt_member_permission` VALUES ('9', '24', '查看角色', 'membership.role:view', '2', '查看', '2014-10-31 02:16:21', '2014-10-31 02:15:25');
INSERT INTO `ezrpt_member_permission` VALUES ('10', '25', '管理', 'membership.permission:*', '1', '管理所有权限', '2014-10-31 02:24:04', '2014-10-31 02:23:08');
INSERT INTO `ezrpt_member_permission` VALUES ('14', '31', '管理', 'membership.module:*', '1', '管理系统模块', '2014-10-31 02:28:43', '2014-10-31 02:27:46');
INSERT INTO `ezrpt_member_permission` VALUES ('15', '31', '编辑模块', 'membership.module.edit', '2', '编辑', '2014-10-31 02:29:17', '2014-10-31 02:28:20');
INSERT INTO `ezrpt_member_permission` VALUES ('16', '31', '删除模块', 'membership.module.remove', '3', '删除', '2014-10-31 02:29:38', '2014-10-31 02:28:42');
INSERT INTO `ezrpt_member_permission` VALUES ('18', '26', '查看日志', 'membership.event:view', '1', '查看', '2014-10-31 02:31:08', '2014-10-31 02:30:11');
INSERT INTO `ezrpt_member_permission` VALUES ('20', '23', '添加用户', 'membership.user:add', '1', '添加', '2014-10-31 10:29:22', '2014-10-31 10:28:26');
INSERT INTO `ezrpt_member_permission` VALUES ('21', '2', '查看', 'dashboard:view', '1', '查看仪表盘图表', '2014-11-12 03:01:11', '2014-11-12 03:01:11');
INSERT INTO `ezrpt_member_permission` VALUES ('37', '23', '管理', 'membership.user:*', '1', '用户管理', '2014-11-12 03:26:41', '2014-11-12 03:26:41');
INSERT INTO `ezrpt_member_permission` VALUES ('38', '24', '管理', 'membership.role:*', '1', '角色管理', '2014-11-12 03:27:49', '2014-11-12 03:27:49');
INSERT INTO `ezrpt_member_permission` VALUES ('58', '26', '日志管理', 'membership.event:*', '2', '日志管理', '2014-11-27 07:51:02', '2014-11-27 07:51:04');
INSERT INTO `ezrpt_member_permission` VALUES ('59', '3', '删除数据源', 'report.ds:delete', '1', '删除数据源', '2015-06-23 06:55:58', '2015-06-23 06:55:58');
INSERT INTO `ezrpt_member_permission` VALUES ('60', '3', '创建数据源', 'report.ds:add', '2', '创建数据源', '2015-06-23 06:56:18', '2015-06-23 06:56:18');
INSERT INTO `ezrpt_member_permission` VALUES ('61', '3', '修改数据源', 'report.ds:update', '3', '修改数据源', '2015-06-23 06:56:37', '2015-06-23 06:56:37');
INSERT INTO `ezrpt_member_permission` VALUES ('62', '3', '查看数据源', 'report.ds:view', '4', '查看数据源', '2015-06-23 06:57:36', '2015-06-23 06:57:36');
INSERT INTO `ezrpt_member_permission` VALUES ('63', '3', '管理数据源', 'report.ds:*', '5', '管理数据源', '2015-06-23 06:59:14', '2015-06-23 06:59:14');
INSERT INTO `ezrpt_member_permission` VALUES ('64', '41', '管理', 'report.conf:*', '1', '管理报表参数配置', '2015-06-23 07:01:44', '2015-06-23 07:01:44');
INSERT INTO `ezrpt_member_permission` VALUES ('65', '4', '管理', 'report.designer:*', '2', '报表设计管理', '2015-06-23 07:02:04', '2015-06-23 07:02:04');
INSERT INTO `ezrpt_member_permission` VALUES ('66', '4', '查看', 'report.designer:view', '10', '查看', '2015-12-17 13:24:08', '2015-12-17 13:24:08');
INSERT INTO `ezrpt_member_permission` VALUES ('67', '4', '增加', 'report.designer:add', '10', '添加', '2015-12-17 13:24:40', '2015-12-17 13:24:52');
INSERT INTO `ezrpt_member_permission` VALUES ('69', '4', '修改', 'report.designer:update', '10', '编辑', '2015-12-17 17:43:58', '2015-12-17 17:43:58');
INSERT INTO `ezrpt_member_permission` VALUES ('71', '39', '管理', 'sys.conf:*', '10', '管理', '2015-12-17 17:44:35', '2015-12-17 17:44:35');
INSERT INTO `ezrpt_member_permission` VALUES ('72', '42', '修改', 'schedule.task:update', '10', '修改任务', '2015-12-17 17:45:36', '2015-12-17 17:45:43');
INSERT INTO `ezrpt_member_permission` VALUES ('73', '42', '删除', 'schedule.task:delete', '10', '删除任务', '2015-12-17 17:46:00', '2015-12-17 17:46:00');
INSERT INTO `ezrpt_member_permission` VALUES ('74', '42', '查看', 'schedule.task:view', '10', '查看任务', '2015-12-17 17:46:19', '2015-12-17 17:46:19');
INSERT INTO `ezrpt_member_permission` VALUES ('75', '54', '管理', 'schedule.task:*', '10', '管理所有任务', '2015-12-17 17:46:50', '2015-12-17 17:46:50');
INSERT INTO `ezrpt_member_permission` VALUES ('77', '54', '增加', 'schedule.task:add', '10', 'add', '2016-09-18 23:57:09', '2016-09-18 23:57:09');
INSERT INTO `ezrpt_member_permission` VALUES ('78', '4', '预览', 'report.designer:preview', '10', '预览报表', '2016-09-27 11:09:14', '2016-09-27 11:09:14');
INSERT INTO `ezrpt_member_permission` VALUES ('79', '4', '导出', 'report.designer:export', '10', '导出excel,pdf等', '2016-09-27 11:09:55', '2016-09-27 11:09:55');

-- ----------------------------
-- Records of ezrpt_member_role
-- ----------------------------
INSERT INTO `ezrpt_member_role` VALUES ('4', '39,38,42,41,40,23,24,25,26,3,7,32,31,4', '76,75,67,66,77,64,65,74,73,72,59,60,61,62,63,69,37,20,2,4,5,38,9,8,7,6,10,14,15,16,71,70,18,58', '超级管理员', 'superAdmin', '1', '1', '1', '管理员', 'admin', '2014-10-31 14:38:09', '2015-12-18 01:48:00');
INSERT INTO `ezrpt_member_role` VALUES ('22', '39,38,42,41,40,23,24,25,26,3,7,32,31,4', '76,75,67,66,77,64,65,74,73,72,59,60,61,62,63,69,37,20,2,4,5,38,9,8,7,6,10,14,15,16,71,70,18,58', '开发人员', 'developer', '1', '1', '2', '开发人员', 'admin', '2014-11-15 12:56:56', '2015-12-18 01:47:45');
INSERT INTO `ezrpt_member_role` VALUES ('23', '3,4,40,41,42,54', '59,60,61,62,63,65,66,67,69,64,75,77,72,73,74', '测试人员', 'test', '1', '1', '10', '测试人员', 'admin', '2015-12-17 21:35:50', '2015-12-20 18:05:27');


-- ----------------------------
-- Records of ezrpt_member_user
-- ----------------------------
INSERT INTO `ezrpt_member_user` VALUES ('1', '4', 'admin', '436a5530ff7436c546dc2047d24fff46', 'c1d69267a3fd2e207408b68f8662cf27', '管理员', 'koqiui@163.com', '123456789', '1', 'sa', '2015-01-05 17:38:50', '2015-01-05 17:38:50');
INSERT INTO `ezrpt_member_user` VALUES ('5', '23', 'test', 'c2b57a2b72ec4f289c8daa68aa7fb3a6', '283facc4dc9896ddb303a736be9530ea', 'tester', 'tester@qq.com', '12343423423', '1', '1', '2015-12-20 18:06:59', '2015-08-14 10:04:00');
