-- Redis 连接配置表（运维管理）
CREATE TABLE IF NOT EXISTS sys_redis_connection (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL DEFAULT 6379,
    password VARCHAR(255),
    database INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_redis_conn_name ON sys_redis_connection(name);
