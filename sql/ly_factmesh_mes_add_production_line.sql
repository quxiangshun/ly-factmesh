-- 增量迁移：为已有 ly_factmesh_mes 数据库添加产线表
-- 若通过 ly_factmesh_mes.sql 初始化则无需执行
\c ly_factmesh_mes;

CREATE TABLE IF NOT EXISTS production_line (
    id BIGINT PRIMARY KEY,
    line_code VARCHAR(50) NOT NULL UNIQUE,
    line_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sequence INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
