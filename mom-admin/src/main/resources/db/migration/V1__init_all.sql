-- mom-admin 初始化（Flyway 迁移，ly_factmesh_admin 独立库）

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    tenant_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_user_tenant_id ON sys_user(tenant_id);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGSERIAL PRIMARY KEY,
    perm_name VARCHAR(50) NOT NULL,
    perm_code VARCHAR(50) NOT NULL UNIQUE,
    url VARCHAR(200),
    method VARCHAR(20),
    parent_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关系表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

-- 角色权限关系表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, perm_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id),
    FOREIGN KEY (perm_id) REFERENCES sys_permission(id)
);

-- 数据字典表
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGSERIAL PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL,
    dict_code VARCHAR(50) NOT NULL,
    dict_name VARCHAR(100) NOT NULL,
    dict_value VARCHAR(200) NOT NULL,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_desc TEXT,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

-- 操作日志表
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

-- 审计日志表
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

-- 默认管理员账号：admin / 123456（BCrypt 加密）
INSERT INTO sys_user (id, username, password, nickname, status)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 1)
ON CONFLICT (username) DO NOTHING;

-- 默认管理员角色
INSERT INTO sys_role (id, role_name, role_code, description)
VALUES (1, '管理员', 'ADMIN', '系统管理员，拥有所有权限')
ON CONFLICT (id) DO NOTHING;

-- 默认权限（系统管理相关）
INSERT INTO sys_permission (id, perm_name, perm_code, url, method, parent_id)
VALUES
  (1, '用户管理', 'user:list', '/api/users', 'GET', NULL),
  (2, '角色管理', 'role:list', '/api/roles', 'GET', NULL),
  (3, '权限管理', 'permission:list', '/api/permissions', 'GET', NULL),
  (4, '字典管理', 'dict:list', '/api/dicts', 'GET', NULL),
  (5, '配置管理', 'config:list', '/api/configs', 'GET', NULL)
ON CONFLICT (id) DO NOTHING;

-- 管理员用户关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 管理员角色关联所有默认权限
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 5)
ON CONFLICT (role_id, perm_id) DO NOTHING;
