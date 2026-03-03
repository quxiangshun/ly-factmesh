-- 物料批次管理、物料追溯
-- inventory: 批次号，支持按批次管理库存
-- inventory_transaction: 批次号、工单ID、领料单ID，支持追溯

ALTER TABLE inventory ADD COLUMN IF NOT EXISTS batch_no VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_inventory_batch ON inventory(material_id, batch_no) WHERE batch_no IS NOT NULL;

ALTER TABLE inventory_transaction ADD COLUMN IF NOT EXISTS batch_no VARCHAR(50);
ALTER TABLE inventory_transaction ADD COLUMN IF NOT EXISTS order_id BIGINT;
ALTER TABLE inventory_transaction ADD COLUMN IF NOT EXISTS req_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_inv_trans_order ON inventory_transaction(order_id) WHERE order_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_inv_trans_req ON inventory_transaction(req_id) WHERE req_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_inv_trans_batch ON inventory_transaction(material_id, batch_no) WHERE batch_no IS NOT NULL;

-- 领料单明细支持批次（领料时指定从哪批次出库）
ALTER TABLE material_requisition_detail ADD COLUMN IF NOT EXISTS batch_no VARCHAR(50);
