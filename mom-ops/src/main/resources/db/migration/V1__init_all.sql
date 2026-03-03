-- mom-ops 初始化（Flyway 迁移，ly_factmesh_ops 独立库）
-- id 由应用雪花算法生成

-- 全局日志表
CREATE TABLE IF NOT EXISTS global_log (
    id BIGINT PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL,
    log_type INT NOT NULL,
    log_level VARCHAR(20),
    log_content TEXT NOT NULL,
    request_id VARCHAR(100),
    client_ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_global_log_service ON global_log(service_name);
CREATE INDEX IF NOT EXISTS idx_global_log_create_time ON global_log(create_time);

-- 审计记录表（跨服务审计）
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL,
    user_id BIGINT,
    username VARCHAR(50),
    operation_type VARCHAR(50) NOT NULL,
    operation_content TEXT NOT NULL,
    operation_result INT NOT NULL,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    client_ip VARCHAR(50),
    request_params TEXT
);

CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_create_time ON audit_log(operation_time);

-- 系统事件表
CREATE TABLE IF NOT EXISTS system_event (
    id BIGINT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    event_level INT NOT NULL,
    event_content TEXT NOT NULL,
    related_service VARCHAR(50),
    related_id VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed INT DEFAULT 0,
    process_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_event_type ON system_event(event_type);
CREATE INDEX IF NOT EXISTS idx_system_event_create_time ON system_event(create_time);
