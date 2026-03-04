-- InfluxDB 连接配置表（运维管理）
CREATE TABLE IF NOT EXISTS sys_influxdb_connection (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    token VARCHAR(500),
    org VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_influxdb_conn_name ON sys_influxdb_connection(name);
