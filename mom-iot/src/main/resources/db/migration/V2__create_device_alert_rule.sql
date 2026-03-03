-- 告警规则表（阈值自动告警）
CREATE TABLE IF NOT EXISTS device_alert_rule (
    id BIGINT PRIMARY KEY,
    rule_name VARCHAR(100),
    device_id BIGINT,
    device_type VARCHAR(50),
    field_name VARCHAR(50) NOT NULL,
    operator VARCHAR(10) NOT NULL,
    threshold_value DOUBLE PRECISION NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    alert_level INT NOT NULL DEFAULT 2,
    alert_content_template VARCHAR(500),
    enabled INT DEFAULT 1,
    cooldown_seconds INT DEFAULT 300,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES device(id)
);

CREATE INDEX IF NOT EXISTS idx_alert_rule_device_id ON device_alert_rule(device_id);
CREATE INDEX IF NOT EXISTS idx_alert_rule_device_type ON device_alert_rule(device_type);
CREATE INDEX IF NOT EXISTS idx_alert_rule_enabled ON device_alert_rule(enabled);

-- 告警记录增加 rule_id 关联
ALTER TABLE device_alert ADD COLUMN IF NOT EXISTS rule_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_device_alert_rule_id ON device_alert(rule_id);
