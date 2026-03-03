-- 扩展 QMS 表结构（可选列，用于增强业务）
-- 若表已存在且无这些列则添加

ALTER TABLE inspection_result ADD COLUMN IF NOT EXISTS create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE non_conforming_product ADD COLUMN IF NOT EXISTS task_id BIGINT;
ALTER TABLE non_conforming_product ADD COLUMN IF NOT EXISTS remark TEXT;
