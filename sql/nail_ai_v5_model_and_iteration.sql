SET NAMES utf8mb4;

-- v5：支持前端切换模型 + 基于上次生成结果图迭代
ALTER TABLE `la_nail_ai_task`
  ADD COLUMN `reference_result_id` bigint unsigned DEFAULT NULL AFTER `reference_asset_id`;
