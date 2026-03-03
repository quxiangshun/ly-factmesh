-- 针对已由旧版 tools/sql/ly_factmesh_mes.sql 初始化过的数据库，补充缺失列
\c ly_factmesh_mes;

-- work_order 补充 line_id
ALTER TABLE work_order ADD COLUMN IF NOT EXISTS line_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_work_order_line_id ON work_order(line_id);

-- production_line 补充 status（0-空闲 1-运行 2-检修）
ALTER TABLE production_line ADD COLUMN IF NOT EXISTS status INT DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_production_line_status ON production_line(status);
