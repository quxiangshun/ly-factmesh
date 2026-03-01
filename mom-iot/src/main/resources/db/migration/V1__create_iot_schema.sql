-- mom-iot 初始表结构（Flyway 迁移，ly_factmesh_iot 独立库）
-- 设备表（设备台账+当前状态，id 由应用雪花算法生成）
CREATE TABLE IF NOT EXISTS device (
    id BIGINT PRIMARY KEY,
    device_code VARCHAR(50) NOT NULL UNIQUE,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50),
    model VARCHAR(50),
    manufacturer VARCHAR(100),
    install_location VARCHAR(200),
    online_status INT DEFAULT 0,
    running_status INT DEFAULT 0,
    temperature FLOAT,
    humidity FLOAT,
    voltage FLOAT,
    current FLOAT,
    status_last_update_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_device_device_type ON device(device_type);
CREATE INDEX IF NOT EXISTS idx_device_online_status ON device(online_status);
CREATE INDEX IF NOT EXISTS idx_device_running_status ON device(running_status);

-- 设备告警记录表（id 由应用雪花算法生成）
CREATE TABLE IF NOT EXISTS device_alert (
    id BIGINT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    device_code VARCHAR(50),
    alert_type VARCHAR(50) NOT NULL,
    alert_level INT NOT NULL,
    alert_content TEXT NOT NULL,
    alert_status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolve_time TIMESTAMP,
    resolved_by VARCHAR(100),
    remark TEXT,
    FOREIGN KEY (device_id) REFERENCES device(id)
);

CREATE INDEX IF NOT EXISTS idx_device_alert_device_id ON device_alert(device_id);
CREATE INDEX IF NOT EXISTS idx_device_alert_status ON device_alert(alert_status);
CREATE INDEX IF NOT EXISTS idx_device_alert_create_time ON device_alert(create_time);
