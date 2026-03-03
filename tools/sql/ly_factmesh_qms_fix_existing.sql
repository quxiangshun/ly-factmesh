-- 针对已由旧版 tools/sql/ly_factmesh_qms.sql 初始化过的数据库，补充缺失列
-- 连接到 ly_factmesh_qms 后执行，若列已存在会报错，可忽略
\c ly_factmesh_qms;

-- inspection_task 补充 order_code, product_code
ALTER TABLE inspection_task ADD COLUMN IF NOT EXISTS order_code VARCHAR(50);
ALTER TABLE inspection_task ADD COLUMN IF NOT EXISTS product_code VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_inspection_task_order_code ON inspection_task(order_code);

-- inspection_result 补充 create_time
ALTER TABLE inspection_result ADD COLUMN IF NOT EXISTS create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- non_conforming_product 补充 ncr_no, task_id, remark
ALTER TABLE non_conforming_product ADD COLUMN IF NOT EXISTS ncr_no VARCHAR(50) UNIQUE;
ALTER TABLE non_conforming_product ADD COLUMN IF NOT EXISTS task_id BIGINT;
ALTER TABLE non_conforming_product ADD COLUMN IF NOT EXISTS remark TEXT;
CREATE INDEX IF NOT EXISTS idx_ncp_ncr_no ON non_conforming_product(ncr_no);
CREATE INDEX IF NOT EXISTS idx_ncp_task_id ON non_conforming_product(task_id);
