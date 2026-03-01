-- 创建数据库
CREATE DATABASE ly_factmesh_wms WITH ENCODING='UTF8' OWNER=postgres;

-- 切换到创建的数据库
\c ly_factmesh_wms;

-- 创建物料信息表
CREATE TABLE material_info (
    id BIGINT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL UNIQUE,
    material_name VARCHAR(100) NOT NULL,
    material_type VARCHAR(50),
    specification VARCHAR(100),
    unit VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建库存表
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY,
    material_id BIGINT NOT NULL,
    warehouse VARCHAR(100),
    location VARCHAR(100),
    quantity INT DEFAULT 0,
    safe_stock INT DEFAULT 0,
    last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (material_id) REFERENCES material_info(id)
);

-- 创建出入库记录表
CREATE TABLE inventory_transaction (
    id BIGINT PRIMARY KEY,
    material_id BIGINT NOT NULL,
    transaction_type INT NOT NULL,
    quantity INT NOT NULL,
    warehouse VARCHAR(100),
    location VARCHAR(100),
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    operator VARCHAR(50),
    reference_no VARCHAR(50),
    FOREIGN KEY (material_id) REFERENCES material_info(id)
);

-- 创建领料/退料单表
CREATE TABLE material_requisition (
    id BIGINT PRIMARY KEY,
    req_no VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT,
    req_type INT NOT NULL,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建领料/退料单明细表
CREATE TABLE material_requisition_detail (
    id BIGINT PRIMARY KEY,
    req_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    actual_quantity INT DEFAULT 0,
    FOREIGN KEY (req_id) REFERENCES material_requisition(id),
    FOREIGN KEY (material_id) REFERENCES material_info(id)
);
