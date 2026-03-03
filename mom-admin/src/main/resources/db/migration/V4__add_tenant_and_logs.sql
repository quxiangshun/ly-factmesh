-- 租户表（多租户数据隔离）
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(50) NOT NULL UNIQUE,
    tenant_name VARCHAR(100) NOT NULL,
    contact VARCHAR(100),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    config TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_tenant_code ON sys_tenant(tenant_code);

-- 用户表增加租户ID
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_sys_user_tenant_id ON sys_user(tenant_id);

-- 操作日志表（谁在何时执行了哪些操作）
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    module VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(10),
    url VARCHAR(500),
    params TEXT,
    ip VARCHAR(50),
    status INT DEFAULT 1,
    error_msg TEXT,
    duration BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_op_log_user_id ON sys_operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_op_log_create_time ON sys_operation_log(create_time);

-- 审计日志表（数据变更记录）
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id VARCHAR(100),
    operation_type VARCHAR(20) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_table_record ON sys_audit_log(table_name, record_id);
CREATE INDEX IF NOT EXISTS idx_audit_create_time ON sys_audit_log(create_time);
