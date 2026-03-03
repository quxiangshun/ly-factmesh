-- 产线状态：0-空闲 1-运行 2-检修
ALTER TABLE production_line ADD COLUMN IF NOT EXISTS status INT DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_production_line_status ON production_line(status);

-- 工单关联产线（用于产线产能统计）
ALTER TABLE work_order ADD COLUMN IF NOT EXISTS line_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_work_order_line_id ON work_order(line_id);
