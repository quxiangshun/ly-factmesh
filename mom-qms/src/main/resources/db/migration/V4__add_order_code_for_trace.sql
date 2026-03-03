-- 质检任务增加工单编码、产品编码（用于质量追溯关联工单/物料）
ALTER TABLE inspection_task ADD COLUMN IF NOT EXISTS order_code VARCHAR(50);
ALTER TABLE inspection_task ADD COLUMN IF NOT EXISTS product_code VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_inspection_task_order_code ON inspection_task(order_code);
