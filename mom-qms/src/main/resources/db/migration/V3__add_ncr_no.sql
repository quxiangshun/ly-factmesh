-- 不合格品 NCR 编号
ALTER TABLE non_conforming_product ADD COLUMN IF NOT EXISTS ncr_no VARCHAR(50) UNIQUE;
CREATE INDEX IF NOT EXISTS idx_ncp_ncr_no ON non_conforming_product(ncr_no);
CREATE INDEX IF NOT EXISTS idx_ncp_task_id ON non_conforming_product(task_id);
