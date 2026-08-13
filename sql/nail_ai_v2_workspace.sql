SET NAMES utf8mb4;

ALTER TABLE `la_nail_ai_task`
  ADD COLUMN `title` varchar(80) NOT NULL DEFAULT '' AFTER `task_type`,
  ADD COLUMN `creative_mode` varchar(32) NOT NULL DEFAULT 'ON_HAND' AFTER `reference_asset_id`,
  ADD COLUMN `nail_shape` varchar(32) NOT NULL DEFAULT 'SHORT_ALMOND' AFTER `creative_mode`,
  ADD COLUMN `finish` varchar(32) NOT NULL DEFAULT 'VELVET_CAT_EYE' AFTER `nail_shape`,
  ADD COLUMN `design_style` varchar(32) NOT NULL DEFAULT 'QUIET_LUXURY' AFTER `finish`,
  ADD COLUMN `layout_style` varchar(32) NOT NULL DEFAULT 'TWO_ACCENTS' AFTER `design_style`,
  ADD COLUMN `trend_preset` varchar(32) NOT NULL DEFAULT 'CUSTOM' AFTER `layout_style`,
  ADD COLUMN `reference_strategy` varchar(32) NOT NULL DEFAULT 'REINTERPRET' AFTER `trend_preset`,
  ADD COLUMN `color_palette` varchar(120) NOT NULL DEFAULT '' AFTER `reference_strategy`;

UPDATE `la_nail_ai_task`
SET `title` = CONCAT(LEFT(REPLACE(REPLACE(`prompt_raw`, '\r', ' '), '\n', ' '), 30), IF(CHAR_LENGTH(`prompt_raw`) > 30, '…', ''))
WHERE `title` = '';

ALTER TABLE `la_nail_ai_result`
  ADD COLUMN `review_note` varchar(500) NOT NULL DEFAULT '' AFTER `review_status`,
  ADD COLUMN `reviewer_id` int unsigned NOT NULL DEFAULT 0 AFTER `review_note`,
  ADD COLUMN `review_time` bigint unsigned NOT NULL DEFAULT 0 AFTER `reviewer_id`;

ALTER TABLE `la_nail_asset`
  ADD COLUMN `source_task_id` bigint unsigned DEFAULT NULL AFTER `prompt`,
  ADD COLUMN `source_result_id` bigint unsigned DEFAULT NULL AFTER `source_task_id`,
  ADD KEY `idx_nail_asset_source_task` (`source_task_id`),
  ADD KEY `idx_nail_asset_source_result` (`source_result_id`),
  ADD CONSTRAINT `fk_nail_asset_source_task` FOREIGN KEY (`source_task_id`) REFERENCES `la_nail_ai_task` (`id`),
  ADD CONSTRAINT `fk_nail_asset_source_result` FOREIGN KEY (`source_result_id`) REFERENCES `la_nail_ai_result` (`id`);

CREATE TABLE `la_nail_ai_task_reference` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `task_id` bigint unsigned NOT NULL,
  `asset_id` int unsigned NOT NULL,
  `uri_snapshot` varchar(500) NOT NULL DEFAULT '',
  `copyright_status_snapshot` varchar(32) NOT NULL DEFAULT '',
  `reference_strategy` varchar(32) NOT NULL DEFAULT 'REINTERPRET',
  `sort` int unsigned NOT NULL DEFAULT 0,
  `create_time` bigint unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_nail_task_reference_task` (`task_id`,`sort`),
  KEY `idx_nail_task_reference_asset` (`asset_id`),
  CONSTRAINT `fk_nail_task_reference_task` FOREIGN KEY (`task_id`) REFERENCES `la_nail_ai_task` (`id`),
  CONSTRAINT `fk_nail_task_reference_asset` FOREIGN KEY (`asset_id`) REFERENCES `la_nail_asset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美甲AI任务参考资产快照';

INSERT INTO `la_nail_ai_task_reference`
(`task_id`,`asset_id`,`uri_snapshot`,`copyright_status_snapshot`,`reference_strategy`,`sort`,`create_time`)
SELECT t.id, a.id, a.uri, a.copyright_status, t.reference_strategy, 0, t.create_time
FROM `la_nail_ai_task` t
JOIN `la_nail_asset` a ON a.id = t.reference_asset_id
LEFT JOIN `la_nail_ai_task_reference` r ON r.task_id = t.id AND r.asset_id = a.id
WHERE r.id IS NULL;

INSERT IGNORE INTO `la_system_auth_menu`
(`id`,`pid`,`menu_type`,`menu_name`,`menu_icon`,`menu_sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`) VALUES
(2007,2000,'C','设计详情','el-icon-PictureRounded',19,'nail:ai:task:list','ai/detail','nail/ai/detail','/nail/ai','',0,0,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2008,2000,'C','任务与审阅','el-icon-List',15,'nail:ai:task:list','tasks','nail/tasks/index','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2009,2001,'A','重命名设计记录','',0,'nail:ai:task:rename','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2010,2008,'A','驳回生成结果','',0,'nail:ai:result:reject','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2011,2002,'A','编辑美甲资产','',0,'nail:asset:update','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());
