-- mom-mes 初始化（Flyway 迁移，ly_factmesh_mes 独立库）
-- id 由应用雪花算法生成

-- 工单表
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

CREATE INDEX IF NOT EXISTS idx_work_order_status ON work_order(status);
CREATE INDEX IF NOT EXISTS idx_work_order_create_time ON work_order(create_time);
CREATE INDEX IF NOT EXISTS idx_work_order_line_id ON work_order(line_id);

-- 工序表
CREATE TABLE IF NOT EXISTS process (
    id BIGINT PRIMARY KEY,
    process_code VARCHAR(50) NOT NULL UNIQUE,
    process_name VARCHAR(100) NOT NULL,
    sequence INT DEFAULT 0,
    work_center VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_process_sequence ON process(sequence);

-- 产线表（status: 0-空闲 1-运行 2-检修）
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

CREATE INDEX IF NOT EXISTS idx_production_line_sequence ON production_line(sequence);
CREATE INDEX IF NOT EXISTS idx_production_line_status ON production_line(status);

-- 报工记录表（device_id 关联 IoT 设备，跨库不建 FK）
CREATE TABLE IF NOT EXISTS work_report (
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

CREATE INDEX IF NOT EXISTS idx_work_report_order_id ON work_report(order_id);
CREATE INDEX IF NOT EXISTS idx_work_report_report_time ON work_report(report_time);

-- 设备告警记录表（MES 侧缓存，device_id 关联 IoT 设备，跨库不建 FK）
CREATE TABLE IF NOT EXISTS device_alert (
    id BIGINT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    alert_level INT NOT NULL,
    alert_content TEXT NOT NULL,
    alert_status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolve_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_device_alert_device_id ON device_alert(device_id);
CREATE INDEX IF NOT EXISTS idx_device_alert_status ON device_alert(alert_status);
