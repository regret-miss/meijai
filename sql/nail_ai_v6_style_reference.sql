SET NAMES utf8mb4;

-- v6：美甲风格母版库（用户选风格母版，模型据此锚定质感/光影/构图）
CREATE TABLE IF NOT EXISTS `la_nail_style_reference` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL DEFAULT '',
  `category` varchar(32) NOT NULL DEFAULT 'GENERAL',
  `uri` varchar(500) NOT NULL DEFAULT '',
  `thumb_uri` varchar(500) NOT NULL DEFAULT '',
  `mime_type` varchar(80) NOT NULL DEFAULT 'image/png',
  `prompt_enhance` varchar(500) NOT NULL DEFAULT '',
  `reference_strategy` varchar(32) NOT NULL DEFAULT 'REINTERPRET',
  `sort` int unsigned NOT NULL DEFAULT 0,
  `status` varchar(24) NOT NULL DEFAULT 'ACTIVE',
  `is_delete` tinyint unsigned NOT NULL DEFAULT 0,
  `create_time` bigint unsigned NOT NULL DEFAULT 0,
  `update_time` bigint unsigned NOT NULL DEFAULT 0,
  `delete_time` bigint unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_style_ref_status_sort` (`status`, `sort`, `id`),
  KEY `idx_style_ref_category` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美甲风格母版';

-- 生成任务关联风格母版
ALTER TABLE `la_nail_ai_task`
  ADD COLUMN `style_reference_id` int unsigned DEFAULT NULL AFTER `reference_result_id`;

-- 后台菜单：风格母版管理
INSERT IGNORE INTO `la_system_auth_menu`
(`id`,`pid`,`menu_type`,`menu_name`,`menu_icon`,`menu_sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`) VALUES
(2015,2000,'C','风格母版','el-icon-Picture',5,'nail:style-reference:list','style-reference','nail/style-reference/index','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());
