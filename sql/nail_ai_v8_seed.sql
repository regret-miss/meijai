SET NAMES utf8mb4;

-- v8：AI 任务固定随机种子（同参数可复现构图与光影）
ALTER TABLE `la_nail_ai_task`
  ADD COLUMN `seed` bigint NOT NULL DEFAULT 0 COMMENT '生成随机种子（0=旧任务未记录）' AFTER `template_version`;
