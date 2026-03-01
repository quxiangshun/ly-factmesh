-- 创建数据库
CREATE DATABASE ly_factmesh_common WITH ENCODING='UTF8' OWNER=postgres;

-- 切换到创建的数据库
\c ly_factmesh_common;

-- 创建全局日志表
CREATE TABLE global_log (
    id BIGINT PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL,
    log_type INT NOT NULL,
    log_level VARCHAR(20),
    log_content TEXT NOT NULL,
    request_id VARCHAR(100),
    client_ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建审计记录表
CREATE TABLE audit_log (
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

-- 创建系统事件表
CREATE TABLE system_event (
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
