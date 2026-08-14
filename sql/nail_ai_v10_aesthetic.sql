SET NAMES utf8mb4;

-- v10：生成结果美学评分（启发式或云美学打分，0=未评分）
ALTER TABLE `la_nail_ai_result`
  ADD COLUMN `score` decimal(5,3) NOT NULL DEFAULT 0 COMMENT '美学评分 0-10（0=未评分）' AFTER `sort`;
