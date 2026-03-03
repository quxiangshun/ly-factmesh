-- 告警规则增加阈值上限（用于 between/outside 操作符）
ALTER TABLE device_alert_rule ADD COLUMN IF NOT EXISTS threshold_value_high DOUBLE PRECISION;
