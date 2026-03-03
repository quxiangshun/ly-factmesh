# LY-FactMesh 项目设计文档

## 1. 文档说明

| 项目     | 说明 |
|----------|------|
| 文档名称 | LY-FactMesh 项目设计文档 |
| 版本     | v1.0 |
| 适用范围 | 后端微服务、前端 Web、部署与运维 |
| 相关文档 | [DDD 架构总览](DDD_ARCHITECTURE_OVERVIEW.md)、[系统开发计划](SYSTEM_DEVELOPMENT_PLAN.md) |

---

## 2. 项目背景与目标

### 2.1 项目定位

LY-FactMesh 是面向制造业的 **制造运营管理（MOM）** 开源系统，采用领域驱动设计（DDD）与微服务架构，实现设备物联、生产执行、仓储、质量等核心能力的统一管理与数据贯通。

### 2.2 设计目标

- **业务与技术解耦**：通过 DDD 分层与限界上下文，使业务规则清晰、可演进。
- **微服务可独立部署**：各业务域（admin/iot/mes/wms/qms）可独立开发、测试、上线。
- **统一入口与文档**：前端与外部系统仅对接网关；接口文档通过 OpenAPI3 在网关层聚合、统一访问。
- **企业级与开源友好**：提供完整文档、规范与开发计划，便于二次开发与社区贡献。

### 2.3 业务范围概要

| 域         | 英文标识 | 核心能力简述 |
|------------|----------|--------------|
| 系统管理   | mom-admin | 用户、角色、权限、字典、租户、操作日志 |
| 设备物联   | mom-iot   | 设备注册、状态监控、数据采集、告警、远程控制 |
| 生产执行   | mom-mes   | 工单、工序、产线、报工、生产进度与报表 |
| 仓储管理   | mom-wms   | 入库、出库、盘点、库存预警、批次与追溯 |
| 质量管理   | mom-qms   | 质检任务、检验执行、不合格处理、质量追溯 |
| 网关与聚合 | mom-gateway | 路由、鉴权、限流、OpenAPI 文档聚合 |

---

## 3. 系统架构

### 3.1 总体架构图

```
                    ┌─────────────────────────────────────────────────┐
                    │                    前端 (web/)                    │
                    │              Vite + Vue3 + TypeScript             │
                    │         Iconify 图标 / 统一调用网关 /api          │
                    └─────────────────────────┬───────────────────────┘
                                              │
                    ┌─────────────────────────▼───────────────────────┐
                    │              Spring Cloud Gateway                │
                    │   路由 / 鉴权 / 限流 / OpenAPI 文档聚合 /doc.html  │
                    └─────────────────────────┬───────────────────────┘
                                              │
        ┌─────────────────────────────────────┼─────────────────────────────────────┐
        │                 Nacos（服务注册与发现 + 配置管理）                          │
        └─────────────────────────────────────┬─────────────────────────────────────┘
                    │                         │                         │
    ┌───────────────▼───────────┐ ┌───────────▼───────────┐ ┌───────────▼───────────┐
    │       mom-admin           │ │       mom-iot        │ │       mom-mes        │
    │  用户/角色/权限/字典      │ │  设备/采集/告警       │ │  工单/工序/报工      │
    │  PostgreSQL (admin)      │ │  PostgreSQL (iot)    │ │  PostgreSQL (mes)    │
    └───────────────────────────┘ └──────────────────────┘ └───────────────────────┘
    ┌───────────────────────────┐ ┌───────────────────────┐
    │       mom-wms             │ │       mom-qms         │
    │  入库/出库/库存/批次      │ │  质检/不合格/追溯     │
    │  PostgreSQL (wms)         │ │  PostgreSQL (qms)     │
    └───────────────────────────┘ └───────────────────────┘
```

### 3.2 技术架构

| 层次       | 技术选型 | 说明 |
|------------|----------|------|
| 前端       | Vite、Vue 3、TypeScript、Vue Router、Iconify | 单页应用，通过 `/api` 代理访问网关 |
| 网关       | Spring Cloud Gateway | 统一入口，基于 Nacos 服务发现路由 |
| 注册与配置 | Nacos 2.3.x | 服务注册发现、配置中心（namespace/group 区分环境） |
| 后端框架   | Spring Boot 4.x、JDK 25 | 各微服务统一技术栈 |
| 文档与规范 | OpenAPI 3 | 各服务暴露 `/v3/api-docs`，网关聚合至 `/doc.html` |
| 持久化     | PostgreSQL 16.x、MyBatis-Plus | 按域独立库（ly_factmesh_admin/iot/mes/wms/qms） |
| 连接池     | Druid | 由 mom-infra 统一封装 |
| ID 生成    | Snowflake（mom-common） | 全局唯一、有序、分布式友好 |

### 3.3 部署架构

- **开发环境**：本地起 Nacos（或 Docker）、PostgreSQL、各微服务及网关；前端 `web/` 通过 Vite 代理访问网关。
- **Docker 编排**：`tools/nacos/`（Nacos+MySQL）、`tools/docker-compose-influxdb.yml`（InfluxDB）、`tools/pgsql`（PostgreSQL 主从）；根目录 `docker-compose.yml` 为完整编排。
- **生产建议**：网关 + 各微服务无状态水平扩展；Nacos 集群；PostgreSQL 主从/读写分离（由 mom-infra 与配置统一）。

---

## 4. 模块设计

### 4.1 模块依赖关系

```
mom-gateway     → mom-common
mom-admin       → mom-infra → mom-common
mom-iot         → mom-infra → mom-common
mom-mes         → mom-infra → mom-common
mom-wms         → mom-infra → mom-common
mom-qms         → mom-infra → mom-common
```

- **mom-common**：工具类、统一响应/异常、OpenAPI 配置、Feign 客户端接口等，无业务表。
- **mom-infra**：数据源与 MyBatis-Plus 等基础设施封装，供各业务域依赖，不直接对外暴露接口。
- **mom-gateway**：仅依赖 mom-common，通过 Nacos 发现并路由到各微服务；不依赖 mom-admin 代码，鉴权可通过 Feign 调用 admin 或网关内过滤器实现。

### 4.2 各模块职责与边界

| 模块 | 职责摘要 | 对外接口形式 |
|------|----------|--------------|
| mom-common | 公共配置、工具、统一 DTO/异常、OpenAPI 配置 | 被其他模块依赖，无 HTTP 端点 |
| mom-infra | 数据源、连接池、MyBatis-Plus 等基础设施 | 被业务模块依赖，无 HTTP 端点 |
| mom-admin | 用户/角色/权限/字典/租户/日志 | REST，路径建议 `/api/admin/**` 或由网关前缀转发 |
| mom-iot | 设备、采集、告警、控制 | REST，路径建议 `/api/iot/**` |
| mom-mes | 工单、工序、产线、报工 | REST，路径建议 `/api/mes/**` |
| mom-wms | 仓库、库存、出入库、批次 | REST，路径建议 `/api/wms/**` |
| mom-qms | 质检、不合格、追溯 | REST，路径建议 `/api/qms/**` |
| mom-gateway | 路由、鉴权、限流、OpenAPI 聚合 | `/doc.html`、`/api/**` 转发、健康检查等 |

### 4.3 领域模型概要（按域）

- **mom-admin**：用户(User)、角色(Role)、权限(Permission)、字典(Dict)、配置(Config) 等实体与值对象；RBAC 模型。
- **mom-iot**：设备(Device)、设备状态(DeviceStatus)、采集任务、告警规则等；详见 mom-iot 内 DDD 文档。
- **mom-mes**：工单(WorkOrder)、工序、产线、报工记录等。
- **mom-wms**：仓库、库存、物料、批次、出入库单等。
- **mom-qms**：检验任务、检验项、不合格处理、追溯关系等。

各业务域在代码结构上遵循 DDD 分层：`domain`（实体/值对象/聚合/领域服务/仓储接口）→ `application`（应用服务/DTO/Command/Query）→ `infrastructure`（仓储实现/Mapper）→ `presentation`（Controller）。

---

## 5. 数据设计原则

- **一域一库**：每个业务域使用独立 PostgreSQL 数据库（ly_factmesh_admin / iot / mes / wms / qms），库结构在 `tools/sql/` 下按域维护。
- **主键**：统一使用 Snowflake 生成的 BIGINT 主键，避免跨库主键冲突与可读性依赖。
- **跨域关联**：仅通过业务主键 ID 引用，不做跨库外键；跨域一致性通过应用层与事件/任务队列保证。
- **租户隔离**：多租户场景下在 mom-admin 及业务表中通过 `tenant_id` 隔离，由网关或应用层注入租户上下文。

---

## 6. API 设计规范

- **协议与格式**：REST over HTTP/HTTPS，JSON 请求与响应；统一使用 UTF-8。
- **路径风格**：`/api/{域或上下文}/{资源}`，例如 `/api/admin/users`、`/api/iot/devices`。
- **HTTP 动词**：GET 查询、POST 创建、PUT/PATCH 更新、DELETE 删除。
- **统一响应体**：建议 `{ "code": 0, "message": "ok", "data": { ... } }`；列表与分页格式在 mom-common 中统一定义。
- **文档**：所有接口在各自服务内通过 OpenAPI 3 暴露，网关聚合，统一在 `/doc.html` 访问与调试。

---

## 7. 安全与权限

- **认证**：计划采用 JWT 或 OAuth2，在网关层校验 Token，并将用户/租户信息传递给下游服务。
- **授权**：以 RBAC 为主，权限数据由 mom-admin 维护，网关或 BFF 层按需调用 admin 接口做权限校验。
- **接口文档**：生产环境建议关闭或仅内网开放 `/doc.html`，或对文档接口做统一认证。

---

## 8. 非功能需求

- **可用性**：核心服务目标 99.9%，通过无状态多实例与健康检查保障。
- **性能**：关键查询与列表接口需控制响应时间与分页大小；可依赖索引与 mom-infra 连接池调优。
- **可观测性**：各服务暴露 Actuator（health/metrics 等），后续可接入 Prometheus/Grafana 与日志聚合。
- **扩展性**：新业务域可新增独立微服务与数据库，仅需在 Nacos 注册并在网关暴露路由；前端与 OpenAPI 可随新服务自动扩展。

---

## 9. 技术选型与版本（当前）

| 类别     | 技术 | 版本/说明 |
|----------|------|------------|
| 后端语言 | Java | 25（build.gradle 中 sourceCompatibility） |
| 构建     | Gradle | 项目 wrapper 约定 |
| 框架     | Spring Boot | 4.0.1 |
| 云组件   | Spring Cloud / Alibaba | 与 Spring Boot 4 兼容的 BOM 版本 |
| 注册中心 | Nacos | 2.3.2（见 tools/nacos/） |
| 网关     | Spring Cloud Gateway | 由 BOM 管理 |
| 文档     | OpenAPI 3 | 各服务暴露 `/v3/api-docs` |
| 数据库   | PostgreSQL | 16.4 |
| ORM     | MyBatis-Plus | 3.5.x |
| 前端     | Vite + Vue3 + TypeScript | 见 web/package.json |
| 图标     | Iconify | 通过 https://icon-sets.iconify.design/ 选用，@iconify/vue 集成 |

---

## 10. 文档索引

- [DDD 架构总览](DDD_ARCHITECTURE_OVERVIEW.md)：分层与各域领域对象说明。
- [系统开发计划](SYSTEM_DEVELOPMENT_PLAN.md)：阶段划分、里程碑与交付物。
- [Docker 部署](DOCKER_README.md)：本地与容器化部署说明。
- [Nacos 安装配置](nacos/Nacos%20安装配置指南.md)：注册中心与配置中心搭建。

以上构成 LY-FactMesh 项目设计文档 v1.0，后续随架构演进在 `docs/` 下更新并保持索引一致。
