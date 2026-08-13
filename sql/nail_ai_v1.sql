SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `la_nail_asset` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(160) NOT NULL DEFAULT '',
  `uri` varchar(500) NOT NULL DEFAULT '',
  `mime_type` varchar(80) NOT NULL DEFAULT '',
  `file_size` bigint unsigned NOT NULL DEFAULT 0,
  `width` int unsigned NOT NULL DEFAULT 0,
  `height` int unsigned NOT NULL DEFAULT 0,
  `source` varchar(32) NOT NULL DEFAULT 'UPLOAD',
  `copyright_status` varchar(32) NOT NULL DEFAULT 'ORIGINAL',
  `ai_usable` tinyint unsigned NOT NULL DEFAULT 1,
  `status` varchar(24) NOT NULL DEFAULT 'ACTIVE',
  `prompt` varchar(2000) NOT NULL DEFAULT '',
  `creator_id` int unsigned NOT NULL DEFAULT 0,
  `is_delete` tinyint unsigned NOT NULL DEFAULT 0,
  `create_time` bigint unsigned NOT NULL DEFAULT 0,
  `update_time` bigint unsigned NOT NULL DEFAULT 0,
  `delete_time` bigint unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_nail_asset_status_time` (`status`,`is_delete`,`create_time`),
  KEY `idx_nail_asset_source` (`source`,`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美甲图片资产';

CREATE TABLE IF NOT EXISTS `la_nail_ai_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `task_type` varchar(32) NOT NULL,
  `status` varchar(24) NOT NULL DEFAULT 'QUEUED',
  `provider` varchar(32) NOT NULL DEFAULT 'VOLCENGINE',
  `model_code` varchar(160) NOT NULL DEFAULT '',
  `prompt_raw` varchar(2000) NOT NULL,
  `prompt_compiled` varchar(4000) NOT NULL,
  `negative_prompt` varchar(1200) NOT NULL DEFAULT '',
  `aspect_ratio` varchar(12) NOT NULL DEFAULT '1:1',
  `resolution` varchar(12) NOT NULL DEFAULT '2K',
  `output_count` tinyint unsigned NOT NULL DEFAULT 1,
  `reference_asset_id` int unsigned DEFAULT NULL,
  `template_version` varchar(64) NOT NULL DEFAULT '',
  `error_message` varchar(1000) NOT NULL DEFAULT '',
  `creator_id` int unsigned NOT NULL DEFAULT 0,
  `started_time` bigint unsigned NOT NULL DEFAULT 0,
  `finished_time` bigint unsigned NOT NULL DEFAULT 0,
  `create_time` bigint unsigned NOT NULL DEFAULT 0,
  `update_time` bigint unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_nail_task_creator_time` (`creator_id`,`create_time`),
  KEY `idx_nail_task_status` (`status`,`create_time`),
  CONSTRAINT `fk_nail_task_reference` FOREIGN KEY (`reference_asset_id`) REFERENCES `la_nail_asset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美甲AI生成任务';

CREATE TABLE IF NOT EXISTS `la_nail_ai_result` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `task_id` bigint unsigned NOT NULL,
  `uri` varchar(500) NOT NULL DEFAULT '',
  `mime_type` varchar(80) NOT NULL DEFAULT 'image/png',
  `width` int unsigned NOT NULL DEFAULT 0,
  `height` int unsigned NOT NULL DEFAULT 0,
  `review_status` varchar(24) NOT NULL DEFAULT 'PENDING',
  `adopted_asset_id` int unsigned DEFAULT NULL,
  `sort` int unsigned NOT NULL DEFAULT 0,
  `create_time` bigint unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_nail_result_task` (`task_id`,`sort`),
  CONSTRAINT `fk_nail_result_task` FOREIGN KEY (`task_id`) REFERENCES `la_nail_ai_task` (`id`),
  CONSTRAINT `fk_nail_result_asset` FOREIGN KEY (`adopted_asset_id`) REFERENCES `la_nail_asset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美甲AI逐图结果';

INSERT IGNORE INTO `la_system_auth_menu` (`id`,`pid`,`menu_type`,`menu_name`,`menu_icon`,`menu_sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`) VALUES
(2000,0,'M','AI美甲','el-icon-MagicStick',48,'','nail','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2001,2000,'C','AI创作台','el-icon-PictureRounded',20,'nail:ai:task:list','ai','nail/ai/index','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2002,2000,'C','美甲资产','el-icon-FolderOpened',10,'nail:asset:list','assets','nail/assets/index','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2003,2001,'A','创建生成任务','',0,'nail:ai:task:create','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2004,2001,'A','采纳生成结果','',0,'nail:ai:result:adopt','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2005,2002,'A','上传美甲资产','',0,'nail:asset:upload','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()),
(2006,2002,'A','删除美甲资产','',0,'nail:asset:delete','','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());
