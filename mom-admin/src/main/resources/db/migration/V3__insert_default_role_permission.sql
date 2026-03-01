-- 默认角色与权限，以及 admin 用户与管理员角色的关联
-- 若已存在则跳过

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
