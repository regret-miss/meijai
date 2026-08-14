SET NAMES utf8mb4;

-- v7：设计记录 / 生成结果删除权限（任务与审阅、AI创作台共用）
-- 删除会级联回收派生资产并解除外键关联，详见 NailAiTaskService#cascadeDeleteTask。

INSERT IGNORE INTO `la_system_auth_menu`
(`id`,`pid`,`menu_type`,`menu_name`,`menu_icon`,`menu_sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`) VALUES
(2016,2001,'A','删除设计记录','',0,'nail:ai:task:delete','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2017,2008,'A','删除设计记录','',0,'nail:ai:task:delete','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2018,2001,'A','删除生成结果','',0,'nail:ai:result:delete','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2019,2008,'A','删除生成结果','',0,'nail:ai:result:delete','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());
