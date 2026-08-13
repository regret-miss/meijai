SET NAMES utf8mb4;

ALTER TABLE `la_nail_ai_task`
  ADD COLUMN `public_token` varchar(64) DEFAULT NULL AFTER `creator_id`,
  ADD UNIQUE KEY `uk_nail_task_public_token` (`public_token`);
