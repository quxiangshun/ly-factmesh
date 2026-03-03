-- 创建数据库
CREATE DATABASE ly_factmesh_qms WITH ENCODING='UTF8' OWNER=postgres;

-- 切换到创建的数据库
\c ly_factmesh_qms;

-- 创建质检任务表
CREATE TABLE inspection_task (
    id BIGINT PRIMARY KEY,
    task_code VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT,
    material_id BIGINT,
    device_id BIGINT,
    inspection_type INT NOT NULL,
    inspection_time TIMESTAMP,
    status INT DEFAULT 1,
    operator VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建质检结果表
CREATE TABLE inspection_result (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    inspection_item VARCHAR(100) NOT NULL,
    standard_value VARCHAR(50),
    actual_value VARCHAR(50),
    judgment INT NOT NULL,
    inspector VARCHAR(50),
    inspection_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES inspection_task(id)
);

-- 创建不合格品记录表
CREATE TABLE non_conforming_product (
    id BIGINT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL,
    batch_no VARCHAR(50),
    quantity INT NOT NULL,
    reason TEXT NOT NULL,
    disposal_method INT,
    disposal_result INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dispose_time TIMESTAMP
);

-- 创建质量追溯信息表
CREATE TABLE quality_traceability (
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
