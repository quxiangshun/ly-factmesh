-- mom-wms 初始化（Flyway 迁移，ly_factmesh_wms 独立库）
-- id 由应用雪花算法生成

-- 物料信息表
CREATE TABLE IF NOT EXISTS material_info (
    id BIGINT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL UNIQUE,
    material_name VARCHAR(100) NOT NULL,
    material_type VARCHAR(50),
    specification VARCHAR(100),
    unit VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_material_info_code ON material_info(material_code);

-- 库存表
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY,
    material_id BIGINT NOT NULL,
    warehouse VARCHAR(100),
    location VARCHAR(100),
    quantity INT DEFAULT 0,
    safe_stock INT DEFAULT 0,
    batch_no VARCHAR(50),
    last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (material_id) REFERENCES material_info(id)
);

CREATE INDEX IF NOT EXISTS idx_inventory_material_id ON inventory(material_id);
CREATE INDEX IF NOT EXISTS idx_inventory_batch ON inventory(material_id, batch_no) WHERE batch_no IS NOT NULL;

-- 出入库记录表
CREATE TABLE IF NOT EXISTS inventory_transaction (
    id BIGINT PRIMARY KEY,
    material_id BIGINT NOT NULL,
    transaction_type INT NOT NULL,
    quantity INT NOT NULL,
    warehouse VARCHAR(100),
    location VARCHAR(100),
    batch_no VARCHAR(50),
    order_id BIGINT,
    req_id BIGINT,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    operator VARCHAR(50),
    reference_no VARCHAR(50),
    FOREIGN KEY (material_id) REFERENCES material_info(id)
);

CREATE INDEX IF NOT EXISTS idx_inv_trans_material ON inventory_transaction(material_id);
CREATE INDEX IF NOT EXISTS idx_inv_trans_order ON inventory_transaction(order_id) WHERE order_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_inv_trans_req ON inventory_transaction(req_id) WHERE req_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_inv_trans_batch ON inventory_transaction(material_id, batch_no) WHERE batch_no IS NOT NULL;

-- 领料/退料单表
CREATE TABLE IF NOT EXISTS material_requisition (
    id BIGINT PRIMARY KEY,
    req_no VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT,
    req_type INT NOT NULL,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mr_req_no ON material_requisition(req_no);
CREATE INDEX IF NOT EXISTS idx_mr_order_id ON material_requisition(order_id);

-- 领料/退料单明细表
CREATE TABLE IF NOT EXISTS material_requisition_detail (
    id BIGINT PRIMARY KEY,
    req_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    actual_quantity INT DEFAULT 0,
    batch_no VARCHAR(50),
    FOREIGN KEY (req_id) REFERENCES material_requisition(id),
    FOREIGN KEY (material_id) REFERENCES material_info(id)
);

CREATE INDEX IF NOT EXISTS idx_mrd_req_id ON material_requisition_detail(req_id);
