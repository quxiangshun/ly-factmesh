-- mom-qms 初始化（Flyway 迁移，ly_factmesh_qms 独立库）
-- id 由应用雪花算法生成

-- 质检任务表
CREATE TABLE IF NOT EXISTS inspection_task (
    id BIGINT PRIMARY KEY,
    task_code VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT,
    order_code VARCHAR(50),
    material_id BIGINT,
    product_code VARCHAR(50),
    device_id BIGINT,
    inspection_type INT NOT NULL DEFAULT 0,
    inspection_time TIMESTAMP,
    status INT DEFAULT 0,
    operator VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inspection_task_status ON inspection_task(status);
CREATE INDEX IF NOT EXISTS idx_inspection_task_order_id ON inspection_task(order_id);
CREATE INDEX IF NOT EXISTS idx_inspection_task_create_time ON inspection_task(create_time);
CREATE INDEX IF NOT EXISTS idx_inspection_task_order_code ON inspection_task(order_code);

-- 质检结果表
CREATE TABLE IF NOT EXISTS inspection_result (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    inspection_item VARCHAR(100) NOT NULL,
    standard_value VARCHAR(50),
    actual_value VARCHAR(50),
    judgment INT NOT NULL,
    inspector VARCHAR(50),
    inspection_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES inspection_task(id)
);

CREATE INDEX IF NOT EXISTS idx_inspection_result_task_id ON inspection_result(task_id);

-- 不合格品记录表
CREATE TABLE IF NOT EXISTS non_conforming_product (
    id BIGINT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL,
    batch_no VARCHAR(50),
    ncr_no VARCHAR(50) UNIQUE,
    quantity INT NOT NULL DEFAULT 1,
    task_id BIGINT,
    reason TEXT NOT NULL,
    disposal_method INT,
    disposal_result INT DEFAULT 0,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dispose_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ncp_product_code ON non_conforming_product(product_code);
CREATE INDEX IF NOT EXISTS idx_ncp_create_time ON non_conforming_product(create_time);
CREATE INDEX IF NOT EXISTS idx_ncp_ncr_no ON non_conforming_product(ncr_no);
CREATE INDEX IF NOT EXISTS idx_ncp_task_id ON non_conforming_product(task_id);

-- 质量追溯信息表
CREATE TABLE IF NOT EXISTS quality_traceability (
    id BIGINT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL,
    batch_no VARCHAR(50),
    material_batch VARCHAR(50),
    production_order VARCHAR(50),
    inspection_record_id BIGINT,
    non_conforming_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inspection_record_id) REFERENCES inspection_result(id),
    FOREIGN KEY (non_conforming_id) REFERENCES non_conforming_product(id)
);

CREATE INDEX IF NOT EXISTS idx_quality_traceability_product ON quality_traceability(product_code);
