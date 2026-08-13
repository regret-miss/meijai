SET NAMES utf8mb4;

ALTER TABLE `la_nail_asset`
  ADD COLUMN `category` varchar(32) NOT NULL DEFAULT 'INSPIRATION' AFTER `height`,
  ADD COLUMN `style` varchar(32) NOT NULL DEFAULT 'QUIET_LUXURY' AFTER `category`,
  ADD COLUMN `color_family` varchar(32) NOT NULL DEFAULT 'NEUTRAL' AFTER `style`,
  ADD COLUMN `nail_shape` varchar(32) NOT NULL DEFAULT 'SHORT_ALMOND' AFTER `color_family`,
  ADD COLUMN `craft` varchar(32) NOT NULL DEFAULT 'GLOSSY_GEL' AFTER `nail_shape`,
  ADD COLUMN `tags_json` json DEFAULT NULL AFTER `craft`,
  ADD COLUMN `original_filename` varchar(255) NOT NULL DEFAULT '' AFTER `tags_json`,
  ADD COLUMN `sha256` char(64) NOT NULL DEFAULT '' AFTER `original_filename`,
  ADD COLUMN `thumb_200_uri` varchar(500) NOT NULL DEFAULT '' AFTER `sha256`,
  ADD COLUMN `thumb_600_uri` varchar(500) NOT NULL DEFAULT '' AFTER `thumb_200_uri`,
  ADD COLUMN `failure_reason` varchar(500) NOT NULL DEFAULT '' AFTER `status`,
  ADD KEY `idx_nail_asset_library_cursor` (`is_delete`,`status`,`id`),
  ADD KEY `idx_nail_asset_category_style` (`category`,`style`,`id`),
  ADD KEY `idx_nail_asset_color_shape` (`color_family`,`nail_shape`,`id`),
  ADD KEY `idx_nail_asset_craft` (`craft`,`id`),
  ADD KEY `idx_nail_asset_copyright` (`copyright_status`,`id`),
  ADD KEY `idx_nail_asset_ai_usable` (`ai_usable`,`id`),
  ADD KEY `idx_nail_asset_sha256` (`sha256`,`is_delete`);

UPDATE `la_nail_asset`
SET `tags_json` = JSON_ARRAY(),
    `category` = CASE WHEN `source` = 'AI' THEN 'AI_WORK' ELSE 'INSPIRATION' END,
    `style` = 'QUIET_LUXURY',
    `color_family` = 'NEUTRAL',
    `nail_shape` = 'SHORT_ALMOND',
    `craft` = 'GLOSSY_GEL'
WHERE `tags_json` IS NULL;

ALTER TABLE `la_nail_asset`
  MODIFY COLUMN `tags_json` json NOT NULL;

CREATE TABLE IF NOT EXISTS `la_nail_asset_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `asset_id` int unsigned NOT NULL,
  `action` varchar(32) NOT NULL,
  `detail` varchar(1000) NOT NULL DEFAULT '',
  `operator_id` int unsigned NOT NULL DEFAULT 0,
  `create_time` bigint unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_nail_asset_audit_asset` (`asset_id`,`create_time`),
  KEY `idx_nail_asset_audit_operator` (`operator_id`,`create_time`),
  CONSTRAINT `fk_nail_asset_audit_asset` FOREIGN KEY (`asset_id`) REFERENCES `la_nail_asset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美甲资产操作审计';

INSERT IGNORE INTO `la_system_auth_menu`
(`id`,`pid`,`menu_type`,`menu_name`,`menu_icon`,`menu_sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`) VALUES
(2012,2002,'A','下载美甲资产','',0,'nail:asset:download','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());

INSERT IGNORE INTO `la_system_auth_menu`
(`id`,`pid`,`menu_type`,`menu_name`,`menu_icon`,`menu_sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`) VALUES
(2013,2002,'A','批量删除美甲资产','',0,'nail:asset:batch-delete','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());

INSERT IGNORE INTO `la_system_auth_menu`
(`id`,`pid`,`menu_type`,`menu_name`,`menu_icon`,`menu_sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`) VALUES
(2014,2002,'A','查看美甲资产详情','',0,'nail:asset:detail','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());
