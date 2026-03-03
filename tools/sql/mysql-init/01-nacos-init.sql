-- ----------------------------
-- Nacos完整初始化脚本
-- 合并所有Nacos相关初始化文件
-- 执行顺序：表结构 -> 命名空间 -> 服务配置
-- ----------------------------

-- UTF-8编码声明
/*!40101 SET NAMES utf8mb4 */;

-- 创建并使用Nacos数据库
CREATE DATABASE IF NOT EXISTS `ly_factmesh_nacos` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ly_factmesh_nacos`;

-- ----------------------------
-- 1. 表结构创建
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(128) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) DEFAULT NULL,
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) DEFAULT NULL,
  `c_use` varchar(64) DEFAULT NULL,
  `effect` varchar(64) DEFAULT NULL,
  `type` varchar(64) DEFAULT NULL,
  `c_schema` text,
  `encrypted_data_key` text NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='config_info';

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(128) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `datum_id` varchar(128) NOT NULL COMMENT 'datum_id',
  `content` longtext NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='增加租户字段';

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(128) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
  `content` longtext NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='config_info_beta';

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(128) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='config_info_tag';

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation` (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `tag_name` varchar(128) NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(128) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`),
  UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='config_tag_relation';

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，0表示使用默认值',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群、各Group容量信息表';

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info` (
  `id` bigint(64) unsigned NOT NULL,
  `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  `op_type` char(10) DEFAULT NULL COMMENT '操作类型',
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`nid`),
  KEY `idx_gmt_create` (`gmt_create`),
  KEY `idx_gmt_modified` (`gmt_modified`),
  KEY `idx_did` (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='多租户改造';

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='租户容量信息表';

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) default '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) default '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tenant_info';

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `username` varchar(50) NOT NULL PRIMARY KEY COMMENT 'username',
  `password` varchar(500) NOT NULL COMMENT 'password',
  `enabled` boolean NOT NULL COMMENT 'enabled'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `username` varchar(50) NOT NULL COMMENT 'username',
  `role` varchar(50) NOT NULL COMMENT 'role',
  UNIQUE KEY `idx_user_role` (`username` , `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `role` varchar(50) NOT NULL COMMENT 'role',
  `resource` varchar(255) NOT NULL COMMENT 'resource',
  `action` varchar(8) NOT NULL COMMENT 'action',
  UNIQUE KEY `uk_role_permission` (`role`,`resource`,`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 2. 默认数据插入
-- ----------------------------

-- 插入默认用户
INSERT INTO `users` VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);
INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');

-- 插入命名空间
-- Insert dev namespace
INSERT INTO `tenant_info` (`kp`, `tenant_id`, `tenant_name`, `tenant_desc`, `create_source`, `gmt_create`, `gmt_modified`) 
VALUES ('1', 'dev', '开发环境', '用于开发环境的命名空间', 'nacos', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);

-- Insert test namespace
INSERT INTO `tenant_info` (`kp`, `tenant_id`, `tenant_name`, `tenant_desc`, `create_source`, `gmt_create`, `gmt_modified`) 
VALUES ('1', 'test', '测试环境', '用于测试环境的命名空间', 'nacos', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);

-- Insert prod namespace
INSERT INTO `tenant_info` (`kp`, `tenant_id`, `tenant_name`, `tenant_desc`, `create_source`, `gmt_create`, `gmt_modified`) 
VALUES ('1', 'prod', '生产环境', '用于生产环境的命名空间', 'nacos', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);

-- 插入默认容量配置
INSERT INTO `tenant_capacity` (`tenant_id`, `quota`, `usage`, `max_size`, `max_aggr_count`, `max_aggr_size`, `max_history_count`) VALUES ('', 200, 0, 20480, 100, 10240, 30);
INSERT INTO `tenant_capacity` (`tenant_id`, `quota`, `usage`, `max_size`, `max_aggr_count`, `max_aggr_size`, `max_history_count`) VALUES ('dev', 100, 0, 20480, 50, 10240, 30);
INSERT INTO `tenant_capacity` (`tenant_id`, `quota`, `usage`, `max_size`, `max_aggr_count`, `max_aggr_size`, `max_history_count`) VALUES ('test', 50, 0, 10240, 20, 5120, 50);
INSERT INTO `tenant_capacity` (`tenant_id`, `quota`, `usage`, `max_size`, `max_aggr_count`, `max_aggr_size`, `max_history_count`) VALUES ('prod', 20, 0, 5120, 10, 2048, 100);

INSERT INTO `group_capacity` (`group_id`, `quota`, `usage`, `max_size`, `max_aggr_count`, `max_aggr_size`, `max_history_count`) VALUES ('DEFAULT_GROUP', 1000, 0, 20480, 100, 10240, 30);
INSERT INTO `group_capacity` (`group_id`, `quota`, `usage`, `max_size`, `max_aggr_count`, `max_aggr_size`, `max_history_count`) VALUES ('LY_MOM_GROUP', 1000, 0, 20480, 100, 10240, 30);

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 3. 服务配置插入
-- ----------------------------

-- ----------------------------
-- 3.1 admin服务配置
-- ----------------------------

-- admin开发环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('admin', 'LY_MOM_GROUP', '# 开发环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_admin?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9091
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'dev', 'mom的系统管理配置', NULL, NULL, 'yaml', NULL, '');

-- admin测试环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('admin', 'LY_MOM_GROUP', '# 测试环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_admin?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9091
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'test', 'mom的系统管理配置', NULL, NULL, 'yaml', NULL, '');

-- admin生产环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('admin', 'LY_MOM_GROUP', '# 生产环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_admin?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9091
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'prod', 'mom的系统管理配置', NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- 3.2 gateway 服务配置（路由保留在 application.yml，此处仅 management 与 app）
-- ----------------------------

-- gateway开发环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('gateway', 'LY_MOM_GROUP', 'management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: when_authorized

app:
  jwt:
    secret: ${JWT_SECRET:ly-factmesh-admin-jwt-secret-change-in-production}
  rate-limit:
    enabled: true
    requests-per-minute: 200
    window-seconds: 60', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'dev', 'mom的网关配置(management/app)', NULL, NULL, 'yaml', NULL, '');

-- gateway测试环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('gateway', 'LY_MOM_GROUP', 'management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: when_authorized

app:
  jwt:
    secret: ${JWT_SECRET:ly-factmesh-admin-jwt-secret-change-in-production}
  rate-limit:
    enabled: true
    requests-per-minute: 200
    window-seconds: 60', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'test', 'mom的网关配置(management/app)', NULL, NULL, 'yaml', NULL, '');

-- gateway生产环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('gateway', 'LY_MOM_GROUP', 'management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: when_authorized

app:
  jwt:
    secret: ${JWT_SECRET:ly-factmesh-admin-jwt-secret-change-in-production}
  rate-limit:
    enabled: true
    requests-per-minute: 200
    window-seconds: 60', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'prod', 'mom的网关配置(management/app)', NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- 3.3 IoT服务配置
-- ----------------------------

-- IoT开发环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('iot', 'LY_MOM_GROUP', '# 开发环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_iot?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9092
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'dev', 'mom的iot配置', NULL, NULL, 'yaml', NULL, '');

-- IoT测试环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('iot', 'LY_MOM_GROUP', '# 测试环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_iot?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9092
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'test', 'mom的iot配置', NULL, NULL, 'yaml', NULL, '');

-- IoT生产环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('iot', 'LY_MOM_GROUP', '# 生产环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_iot?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9092
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'prod', 'mom的iot配置', NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- 3.4 MES服务配置
-- ----------------------------

-- MES开发环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('mes', 'LY_MOM_GROUP', '# 开发环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_mes?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9093
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'dev', 'mom的mes配置', NULL, NULL, 'yaml', NULL, '');

-- MES测试环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('mes', 'LY_MOM_GROUP', '# 测试环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_mes?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9093
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'test', 'mom的mes配置', NULL, NULL, 'yaml', NULL, '');

-- MES生产环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('mes', 'LY_MOM_GROUP', '# 生产环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_mes?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9093
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'prod', 'mom的mes配置', NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- 3.5 QMS服务配置
-- ----------------------------

-- QMS开发环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('qms', 'LY_MOM_GROUP', '# 开发环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_qms?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9095
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'dev', 'mom的qms配置', NULL, NULL, 'yaml', NULL, '');

-- QMS测试环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('qms', 'LY_MOM_GROUP', '# 测试环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_qms?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9095
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'test', 'mom的qms配置', NULL, NULL, 'yaml', NULL, '');

-- QMS生产环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('qms', 'LY_MOM_GROUP', '# 生产环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_qms?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9095
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'prod', 'mom的qms配置', NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- 3.6 WMS服务配置
-- ----------------------------

-- WMS开发环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('wms', 'LY_MOM_GROUP', '# 开发环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_wms?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9094
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'dev', 'mom的wms配置', NULL, NULL, 'yaml', NULL, '');

-- WMS测试环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('wms', 'LY_MOM_GROUP', '# 测试环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_wms?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9094
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'test', 'mom的wms配置', NULL, NULL, 'yaml', NULL, '');

-- WMS生产环境配置
INSERT INTO `config_info` (`data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`)
VALUES 
('wms', 'LY_MOM_GROUP', '# 生产环境配置
spring:
  # PostgreSQL数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/ly_factmesh_wms?useSSL=false&serverTimezone=UTC
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

# 服务器配置
server:
  port: 9094
  address: 0.0.0.0

# 日志配置
logging:
  level:
    org.springframework: INFO', 'e10adc3949ba59abbe56e057f20f883e', NOW(), NOW(), NULL, '127.0.0.1', NULL, 'prod', 'mom的wms配置', NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- Nacos完整初始化脚本结束
-- ----------------------------