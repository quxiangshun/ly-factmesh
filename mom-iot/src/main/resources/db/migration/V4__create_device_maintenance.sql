-- 设备维保记录表（设备台账扩展）
CREATE TABLE IF NOT EXISTS device_maintenance (
    id BIGINT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    maintenance_date DATE NOT NULL,
    content TEXT,
    operator_name VARCHAR(100),
    cost DECIMAL(12, 2),
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES device(id)
);

CREATE INDEX IF NOT EXISTS idx_device_maintenance_device_id ON device_maintenance(device_id);
CREATE INDEX IF NOT EXISTS idx_device_maintenance_date ON device_maintenance(maintenance_date);
CREATE INDEX IF NOT EXISTS idx_device_maintenance_type ON device_maintenance(maintenance_type);
