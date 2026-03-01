# LY-FactMesh 系统开发计划

## 1. 文档说明

| 项目     | 说明 |
|----------|------|
| 文档名称 | LY-FactMesh 系统开发计划 |
| 版本     | v1.0 |
| 适用范围 | 产品规划、迭代排期、里程碑与交付物管理 |
| 相关文档 | [项目设计文档](PROJECT_DESIGN.md)、[DDD 架构总览](DDD_ARCHITECTURE_OVERVIEW.md) |

---

## 2. 计划总览

本计划将系统建设分为 **五个阶段**：基础平台期、核心业务期、集成与优化期、生态与扩展期、持续运营期。各阶段有明确里程碑与交付物，便于按阶段验收与开源发布。

### 2.1 阶段一览

| 阶段 | 名称 | 核心目标 | 建议周期 |
|------|------|----------|----------|
| P0 | 基础平台期 | 微服务骨架、网关、注册中心、文档聚合、前端骨架 | 4–6 周 |
| P1 | 核心业务期 | Admin/IoT/MES/WMS/QMS 核心能力与领域模型落地 | 12–16 周 |
| P2 | 集成与优化期 | 跨域流程、统一认证、监控与性能优化 | 6–8 周 |
| P3 | 生态与扩展期 | 报表、扩展点、开放 API、文档与示例完善 | 6–8 周 |
| P4 | 持续运营期 | 版本发布、社区运营、问题修复与小版本迭代 | 持续 |

---

## 3. P0：基础平台期

### 3.1 目标

- 完成微服务骨架与基础设施（Nacos、Gateway、mom-common、mom-infra）。
- 完成接口文档统一聚合（OpenAPI3 + Nacos Discover）。
- 完成前端工程（web/）骨架与基础页面，可访问网关与文档。

### 3.2 里程碑与交付物

| 序号 | 里程碑 | 交付物 | 验收标准 |
|------|--------|--------|----------|
| M0-1 | 基础设施就绪 | Nacos 可启动（Docker/本地），各微服务可注册与发现 | 网关通过服务名访问到 mom-admin、mom-iot 等 |
| M0-2 | 网关与文档聚合 | mom-gateway 基于 Nacos Discover 聚合各服务 OpenAPI3 | 访问 `/doc.html` 可切换并查看各服务 OpenAPI3 |
| M0-3 | 统一 OpenAPI 规范 | mom-common 统一配置，各服务暴露 `/v3/api-docs` | 各业务服务无额外 Swagger 配置，文档风格一致 |
| M0-4 | 前端工程骨架 | web/（Vite+Vue3+TS+Router+Iconify） | 本地 `npm run dev` 可访问概览/微服务页，可跳转 `/doc.html` |
| M0-5 | 文档与规范文档 | docs/ 下设计文档、开发计划、OpenAPI 说明等 | 新人可仅凭 docs 理解架构与接入方式 |

### 3.3 任务清单（建议顺序）

1. 确认 Nacos 配置（bootstrap/application），移除网关对 Nacos 的排除，启用 Discovery Locator。
2. mom-gateway 配置 OpenAPI 聚合，基于 Nacos Discover 发现各服务 `/v3/api-docs`。
3. 各业务服务仅依赖 mom-common，确认 OpenAPI 配置生效，单服务 `/v3/api-docs` 可访问。
4. 创建 web/ 工程，配置 Vite 代理 `/api` → 网关，实现首页与微服务介绍页，导航可打开 `/doc.html`。
5. 编写/更新 docs：PROJECT_DESIGN.md、SYSTEM_DEVELOPMENT_PLAN.md。

### 3.4 输出

- 可一键启动的本地/Docker 基础环境（Nacos + PostgreSQL + 至少 mom-admin + mom-gateway）。
- 统一文档入口：`http://{gateway}/doc.html`。
- 前端入口：`http://localhost:5173`（或配置的端口）。

### 3.5 P0 完成情况（已落实）

| 任务 | 状态 | 说明 |
|------|------|------|
| Nacos 配置与网关 Discovery | ✅ | 网关与各业务服务（admin/iot/mes/wms/qms）均已引入 `spring-cloud-starter-alibaba-nacos-discovery`，网关启用 Discovery Locator，bootstrap 中 Nacos 配置已存在 |
| 网关 OpenAPI 聚合 | ✅ | mom-gateway 基于 Nacos Discover 聚合各服务 OpenAPI3 |
| 各业务服务 OpenAPI | ✅ | 各服务依赖 mom-common，统一暴露 `/v3/api-docs` |
| 前端 web/ 骨架 | ✅ | Vite+Vue3+TS+Router+Iconify，首页/微服务页，代理 `/api` 与 `/doc.html` 等至网关 |
| 文档 | ✅ | PROJECT_DESIGN.md、SYSTEM_DEVELOPMENT_PLAN.md、web/README.md |
| mom-mes/wms/qms mainClass | ✅ | 已修正为 MesApplication、WmsApplication、QmsApplication |

**验收建议**：本地需 JDK 25（与 build.gradle 一致），先启动 Nacos（如 Docker），再启动 mom-admin、mom-gateway；访问 `http://localhost:8080/doc.html` 应能看到 OpenAPI 聚合；前端 `cd web && npm run dev` 后访问 `http://localhost:5173` 与 `http://localhost:5173/doc.html`。

---

## 4. P1：核心业务期

### 4.1 目标

- mom-admin：用户、角色、权限、字典、配置等 CRUD 与 RBAC 模型可用。
- mom-iot：设备注册、状态、采集/告警等核心领域模型与 API 可用。
- mom-mes：工单、工序、产线、报工等核心领域模型与 API 可用。
- mom-wms：仓库、库存、出入库、批次等核心领域模型与 API 可用。
- mom-qms：质检任务、检验执行、不合格处理、追溯等核心领域模型与 API 可用。

### 4.2 里程碑与交付物

| 序号 | 里程碑 | 交付物 | 验收标准 |
|------|--------|--------|----------|
| M1-1 | Admin 核心能力 | 用户/角色/权限/字典/配置的领域模型、应用服务、REST API | 可通过 OpenAPI 调试，前端可对接（若已做登录页） |
| M1-2 | IoT 核心能力 | 设备实体、状态、注册/上下线、基础采集或告警 API | 设备列表与状态可查询，可扩展采集与告警 |
| M1-3 | MES 核心能力 | 工单、工序、产线、报工等领域模型与 API | 工单创建与状态流转、报工可演示 |
| M1-4 | WMS 核心能力 | 仓库、库存、出入库单、批次等领域模型与 API | 入库/出库/盘点流程可演示 |
| M1-5 | QMS 核心能力 | 检验任务、检验项、不合格处理等领域模型与 API | 质检任务创建与执行可演示 |
| M1-6 | 领域事件与跨域 | 关键跨域事件（如工单下发触发领料、报工触发质检） | 至少 1–2 条跨域流程打通（事件或 Feign 调用） |

### 4.3 任务清单（按域）

- **mom-admin**：完善 User/Role/Permission/Dict/Config 的仓储与 API；统一分页与响应体（mom-common）；可选：登录接口与 JWT 签发。
- **mom-iot**：完善 Device 聚合与仓储；设备状态变更、采集配置、告警规则等 API；与 mom-infra 对接（若已封装 MQTT/OPC 等）。
- **mom-mes**：工单聚合、工序、产线、报工；与 mom-iot 设备、mom-wms 领料等通过事件或 Feign 衔接。
- **mom-wms**：仓库、库位、物料、库存、出入库单、批次；与 mom-mes 领料/退料接口对接。
- **mom-qms**：检验计划、检验项、检验结果、不合格处理；与 mom-mes 报工/工单关联，支持追溯。

### 4.4 输出

- 各域核心 API 在 `/doc.html` 中可查阅与调试。
- 至少一条端到端业务流程可演示（如：工单创建 → 领料 → 报工 → 质检）。
- 各域数据库表结构与初始数据脚本在 `sql/` 下可复现。

### 4.5 P1 完成情况（已落实）

| 里程碑 | 状态 | 说明 |
|--------|------|------|
| M1-1 Admin 核心能力 | ✅ 已有 | 用户/角色/权限/字典/配置 CRUD 与 RBAC 已存在，可通过 OpenAPI 调试 |
| 统一分页与响应体 | ✅ | mom-common 新增 `Result<T>`、`PageResult<T>`，供各域统一使用 |
| M1-2 IoT 核心能力 | ✅ | 新增 DeviceController、DeviceRegisterRequest；设备注册、上下线、状态、列表、删除等 API |
| M1-3 MES 核心能力 | ✅ | 工单领域：WorkOrder 实体、WorkOrderRepository、WorkOrderApplicationService、WorkOrderController；创建/分页/下发/开始/完成/删除工单 API |
| M1-4 WMS 核心能力 | ✅ | 物料领域：Material 实体与仓储、MaterialController；创建/分页/查询/删除物料 API |
| M1-5 QMS 核心能力 | ✅ | 质检任务领域：InspectionTask 实体与仓储、InspectionTaskController；创建/分页/完成/删除质检任务 API |
| M1-6 领域事件与跨域 | ✅ | mom-common 新增 WmsFeignClient、QmsFeignClient 及 DTO；WMS 领料单（MaterialRequisition）与 POST /api/requisitions；MES 工单发布调用 WMS 创建领料单、工单完成调用 QMS 创建质检任务 |

**验收建议**：启动 Nacos、mom-admin、mom-iot、mom-mes、mom-gateway 后，访问 `http://localhost:8080/doc.html`，应能看到 mom-admin、mom-iot、mom-mes 的接口；可调试设备注册、工单创建与状态流转。

---

## 5. P2：集成与优化期

### 5.1 目标

- 统一认证与鉴权（JWT/OAuth2）在网关层落地，与 mom-admin 权限校验打通。
- 前端登录、鉴权、路由守卫与各业务模块基础页面。
- 监控与可观测性（Actuator、Prometheus/Grafana 或等价方案）。
- 性能与稳定性：连接池、慢 SQL、接口超时与限流策略。

### 5.2 里程碑与交付物

| 序号 | 里程碑 | 交付物 | 验收标准 |
|------|--------|--------|----------|
| M2-1 | 统一认证 | 网关校验 Token，转发用户/租户信息；mom-admin 提供登录与刷新 | 前端携带 Token 可访问受保护接口 |
| M2-2 | 前端鉴权与布局 | 登录页、路由守卫、侧边栏/菜单与权限联动 | 未登录跳转登录页；按权限展示菜单 |
| M2-3 | 监控与健康 | 各服务暴露 Actuator，对接 Prometheus/Grafana（或文档说明） | 核心指标与健康检查可观测 |
| M2-4 | 性能与限流 | 关键接口限流与超时配置，Druid/慢 SQL 监控 | 压测或人工验收无明显瓶颈与雪崩 |

### 5.3 任务清单

1. 网关：JWT 校验过滤器、从 Token 解析 userId/tenantId 并写入请求头。
2. mom-admin：登录接口、刷新接口、权限校验接口（供网关或 BFF 调用）。
3. 前端：登录页、Token 存储与请求头注入、路由守卫、菜单与权限数据对接。
4. 各服务：Actuator 暴露与 Prometheus 埋点（或文档中写明接入方式）。
5. 网关/服务：限流与超时配置；mom-infra/Druid 慢 SQL 与连接池监控。

### 5.4 输出

- 完整“登录 → 访问业务 → 登出”流程可用。
- 运维可通过监控查看服务健康与关键指标。
- 文档中补充安全与监控章节（如 docs/SECURITY_AND_AUTH.md、docs/MONITORING.md）。

---

## 6. P3：生态与扩展期

### 6.1 目标

- 报表与看板：至少 1–2 个跨域统计或看板（如生产日报、设备 OEE、质量趋势）。
- 扩展机制：插件化或开放 API 设计，便于二次开发。
- 文档与示例：部署手册、API 使用示例、二次开发指南。
- 开源友好：README、CONTRIBUTING、License、版本号与 Changelog 规范。

### 6.2 里程碑与交付物

| 序号 | 里程碑 | 交付物 | 验收标准 |
|------|--------|--------|----------|
| M3-1 | 报表/看板 | 生产/设备/质量等 1–2 个统计接口与前端图表 | 数据来源于各域聚合，可演示 |
| M3-2 | 扩展与开放 API | 扩展点设计文档与示例（或 OpenAPI 导出与示例代码） | 第三方可基于文档对接或扩展 |
| M3-3 | 部署与运维文档 | 生产部署、配置项、监控与日志收集说明 | 可按文档完成一次生产级部署 |
| M3-4 | 开源规范 | README、CONTRIBUTING、Changelog、版本标签 | 符合常见开源项目规范 |

### 6.3 任务清单

1. 定义报表/看板所需聚合数据，在各域提供统计接口或单独报表服务聚合多域。
2. 前端：图表组件（如 ECharts）与看板页。
3. 编写扩展点或开放 API 设计文档，并提供一个最小示例。
4. 编写生产部署文档（环境、Nacos/DB/网关配置、监控与日志）。
5. 整理 README（架构图、快速开始、模块说明、文档索引）、CONTRIBUTING、Changelog 与版本策略。

### 6.4 输出

- 可演示的报表/看板页面。
- 外部开发者可依据文档进行扩展或集成。
- 首个正式版本（如 v1.0.0）可发布，并附带 Release Notes。

---

## 7. P4：持续运营期

### 7.1 目标

- 按版本计划发布 Release，维护 Changelog 与兼容性说明。
- 处理 Issue 与 PR，修复缺陷与文档问题。
- 小版本迭代：性能优化、依赖升级、安全补丁。

### 7.2 建议实践

- 版本号：主版本.次版本.修订号（如 1.0.0、1.1.0、1.0.1），遵循语义化版本。
- 分支策略：main 为稳定发布分支，develop 或 feature 分支开发，通过 PR 合并。
- 发布节奏：大功能合并后发布次版本；仅修复与文档发布修订版本。
- 文档：每个大版本更新 PROJECT_DESIGN.md、SYSTEM_DEVELOPMENT_PLAN.md 中的版本与阶段状态。

---

## 8. 依赖关系与优先级

- **P0 必须优先完成**：无稳定网关与文档聚合时，后续联调与协作成本高。
- **P1 各域可并行**：Admin 建议略超前（认证与权限依赖），IoT/MES/WMS/QMS 可按人力并行。
- **P2 依赖 P1 核心 API**：认证与前端鉴权依赖 mom-admin 登录与权限接口。
- **P3 依赖 P1/P2**：报表依赖各域 API 与前端鉴权；扩展文档依赖核心能力稳定。

---

## 9. 风险与应对

| 风险 | 应对 |
|------|------|
| Nacos/网关与业务服务版本不兼容 | 锁定 Spring Cloud Alibaba 与 Spring Boot BOM 版本，在 CI 中做集成测试 |
| 跨域事务与一致性 | 优先采用事件与最终一致性，明确记录在设计文档；复杂场景再考虑 Saga 或补偿 |
| 前端与后端接口不同步 | 以 OpenAPI3 为契约；有条件的可生成前端类型或 Mock |
| 人力不足导致周期拉长 | 优先保证 P0+P1 的 Admin/IoT/MES，WMS/QMS 可做最小闭环后迭代 |

---

## 10. 文档更新说明

- 本计划随项目推进在 `docs/SYSTEM_DEVELOPMENT_PLAN.md` 中更新阶段状态与完成情况。
- 重大架构变更时同步更新 [项目设计文档](PROJECT_DESIGN.md)。
- 各阶段验收标准可作为 Issue/Project 看板中的 Checklist 使用。

以上为 LY-FactMesh 系统开发计划 v1.0。
