-- 创建数据库
CREATE DATABASE ly_factmesh_mes WITH ENCODING='UTF8' OWNER=postgres;

-- 切换到创建的数据库
\c ly_factmesh_mes;

-- 创建工单表（与 Flyway V1 一致）
CREATE TABLE IF NOT EXISTS work_order (
    id BIGINT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    plan_quantity INT NOT NULL,
    actual_quantity INT DEFAULT 0,
    status INT DEFAULT 1,
    line_id BIGINT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_work_order_line_id ON work_order(line_id);

-- 创建工序表
CREATE TABLE process (
    id BIGINT PRIMARY KEY,
    process_code VARCHAR(50) NOT NULL UNIQUE,
    process_name VARCHAR(100) NOT NULL,
    sequence INT DEFAULT 0,
    work_center VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建报工记录表
CREATE TABLE work_report (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    process_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    report_quantity INT NOT NULL,
    scrap_quantity INT DEFAULT 0,
    report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    operator VARCHAR(50),
    FOREIGN KEY (order_id) REFERENCES work_order(id),
    FOREIGN KEY (process_id) REFERENCES process(id)
);

-- 创建产线表（status: 0-空闲 1-运行 2-检修）
CREATE TABLE IF NOT EXISTS production_line (
    id BIGINT PRIMARY KEY,
    line_code VARCHAR(50) NOT NULL UNIQUE,
    line_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sequence INT DEFAULT 0,
    status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_production_line_status ON production_line(status);

-- 创建设备告警记录表
CREATE TABLE device_alert (
    id BIGINT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    alert_level INT NOT NULL,
    alert_content TEXT NOT NULL,
    alert_status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolve_time TIMESTAMP
);
