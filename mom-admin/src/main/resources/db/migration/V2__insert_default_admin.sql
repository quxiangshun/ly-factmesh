-- 默认管理员账号：admin / 123456（BCrypt 加密）
-- 若已存在 admin 用户则跳过（显式指定 id，兼容无自增的旧表结构）
INSERT INTO sys_user (id, username, password, nickname, status)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 1)
ON CONFLICT (username) DO NOTHING;
