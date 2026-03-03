# LY-FactMesh

## 项目概述
LY-FactMesh是一个面向制造业的现代化运营管理系统(MOM)，采用DDD架构设计，实现了业务与技术的解耦，支持微服务部署。该系统旨在帮助制造企业实现数字化转型，提升生产效率和管理水平。

## 核心优势

- **DDD架构设计**：采用领域驱动设计，业务与技术解耦，易于扩展和维护
- **微服务架构**：支持模块化部署，各业务域独立开发、测试和部署
- **统一技术栈**：基于Spring Boot 4.x和Java 25，技术栈统一，学习成本低
- **强大的设备管理**：支持多种工业协议接入，实时监控设备状态
- **全面的生产管理**：覆盖生产计划、工单管理、工序执行、质量检验等全流程
- **灵活的扩展机制**：支持插件化扩展，易于集成第三方系统
- **完善的监控体系**：集成Prometheus和Grafana，实现全面的系统监控

## 模块结构

### 1. 基础支撑类（必选，全模块依赖）

#### ✅ mom-common（公共核心模块）
**核心职责**：存放全系统复用的通用能力，与业务无关，仅做基础支撑

| 能力 | 实现状态 | 说明 |
|------|----------|------|
| 通用工具类 | ✅ 已实现 | DateUtils（日期格式化/解析）、JsonUtils（JSON 序列化）、ValidationUtils（校验）、DigestUtils（MD5/SHA256）、SnowflakeIdGenerator |
| 全局枚举 | ✅ 已实现 | DeviceStatusEnum、WorkOrderStatusEnum、InspectionTypeEnum |
| 公共 DTO/Query | ✅ 已实现 | Result、PageResult、PageRequest；Feign 契约 DTO |
| 全局异常体系 | ✅ 已实现 | BusinessException、SystemException；GlobalExceptionHandler 统一返回 Result |
| 通用注解 | ✅ 已实现 | @OperationLog（操作日志）、@NoAuth（免认证） |

**创建依据**：避免各业务模块重复写通用代码，降低维护成本，是开源项目的基础规范。

#### ✅ mom-infra（基础设施模块）
**核心职责**：封装技术层能力，为业务域提供 "技术工具"，隔离底层技术细节

| 能力 | 实现状态 | 说明 | 配置/依赖 |
|------|----------|------|-----------|
| 数据库适配 | ✅ 已实现 | PostgreSQL + Druid 连接池、MyBatis-Plus 分页；PostgresConfig、DruidDataSourceConfig | spring.datasource.* |
| 读写分离 | ✅ 已实现 | DynamicDataSource + @ReadOnly 注解；主库写、从库读 | infra.datasource.read-write-split.enabled=true，master-url、slave-url |
| 缓存 | ✅ 已实现 | CacheService 接口 + RedisCacheServiceImpl；RedisCacheConfig 条件装配 | spring.data.redis.host |
| 消息队列 | ✅ 已实现 | MqttClientWrapper 接口、MqttProperties；EmqxMqttClientConfig 配置绑定，实现由业务域提供 | infra.mqtt.broker-url |
| 工业协议 | ✅ 接口已定义 | OpcUaClient、ModbusTcpClient 接口，业务域引入 OPC UA/Modbus 库后实现 | - |
| 监控告警 | ✅ 已实现 | Actuator + Micrometer Prometheus；PrometheusMetricsConfig 公共标签 | management.endpoints.* |
| 分布式事务 | ✅ 已实现 | Seata 2.2.0，seata-spring-boot-starter 自动装配 | seata.enabled=true，application-id，tx-service-group |

**创建依据**：DDD 中 "基础设施层" 的落地，让业务域（如 iot/mes）无需关注技术实现，仅调用接口即可，符合 "业务与技术解耦" 的核心思想。

### 2. 核心业务域（必选，MOM 系统核心闭环）

#### ✅ mom-iot（设备物联域）
**核心职责**：覆盖设备全生命周期管理，是 MOM 系统的 "数据入口"

| 能力 | 实现状态 | 说明 |
|------|----------|------|
| 设备注册 / 上下线 / 状态监控 | ✅ 已实现 | 设备 CRUD、POST /api/devices/{id}/online、/offline、/start、/stop、/fault；GET /api/devices/stats 统计 |
| 设备数据采集（实时） | ✅ 已实现 | POST /api/devices/telemetry 遥测上报，写入 InfluxDB 时序库 |
| 设备数据存储 | ✅ 已实现 | InfluxDB 存储遥测数据，支持按设备/时间范围查询 |
| 设备故障告警 | ✅ 已实现 | 告警规则引擎 /api/devices/alert-rules；阈值触发自动告警；告警处理 /api/devices/alerts |
| 远程控制 | ✅ 已实现 | POST /api/devices/{id}/start、/stop 启停控制 |
| 设备台账（型号、参数） | ✅ 已实现 | Device 含 model、manufacturer、installLocation；PATCH /api/devices/{id}/status 更新采集数据 |
| 设备台账（维保记录） | ✅ 已实现 | POST /api/devices/maintenance 新增；GET /api/devices/maintenance/device/{id} 按设备查询 |
| 定时采集 | ✅ 已实现 | ScheduledTelemetryCollectJob 每 5 分钟将在线设备 Device 表状态同步到 InfluxDB；iot.scheduled-collect.enabled=true 开启 |
| 数据清洗 | ✅ 已实现 | TelemetryDataCleaner 过滤 NaN/Inf、范围校验（temperature/humidity/voltage/current）；iot.telemetry.cleaner.enabled 可关闭 |

**创建依据**：制造企业的核心生产要素是设备，设备数据是 MES/WMS/QMS 的基础，必须独立成域。

#### ✅ mom-mes（生产执行域）
**核心职责**：MOM 系统的核心，覆盖生产全流程

| 能力 | 实现状态 | 说明 |
|------|----------|------|
| 工单管理 | ✅ 已实现 | 创建、下发、开始、暂停、恢复、完成；POST /api/work-orders/{id}/pause、/resume |
| 工序管理 | ✅ 已实现 | 工序 CRUD /api/processes；工序执行通过报工体现 |
| 产线管理 | ✅ 已实现 | 产线 CRUD /api/lines；编码唯一、排序 |
| 报工 | ✅ 已实现 | POST /api/work-reports 报工录入；自动累加工单 actualQuantity、已下发→进行中 |
| 生产进度监控 | ✅ 已实现 | GET /api/work-orders/stats 各状态数量；工单 planQuantity/actualQuantity 跟踪 |
| 生产报表 | ✅ 已实现 | GET /api/work-orders/summary?date= 按日统计完成工单数、产量、进行中/暂停数 |
| 产线状态 | ✅ 已实现 | production_line.status：0-空闲 1-运行 2-检修；PUT /api/lines/{id}/status |
| 产线产能统计 | ✅ 已实现 | work_order.line_id 关联产线；GET /api/lines/capacity-summary?date= 按产线汇总完成工单数、产量 |

**创建依据**：生产执行是 MOM 系统的核心价值，需独立成域保证业务内聚。

#### ✅ mom-wms（仓储管理域）
**核心职责**：物料流转管理，衔接生产与供应链

| 能力 | 实现状态 | 说明 |
|------|----------|------|
| 物料入库 / 出库 | ✅ 已实现 | POST /api/inventory/adjust 正数入库、负数出库；领料完成自动扣库存、退料完成自动加库存 |
| 盘点 | ✅ 已实现 | POST /api/inventory/count 录入实盘数量，系统自动计算差异并调整库存，记录 TYPE_ADJUST 事务 |
| 库存查询 | ✅ 已实现 | GET /api/inventory 分页查询，支持按物料/仓库/批次号筛选；GET /api/inventory/material/{id} 按物料 ID 查全部库位 |
| 库存预警 | ✅ 已实现 | PUT /api/inventory/{id}/safe-stock 设置安全库存；GET /api/inventory/below-safe-stock 低于安全库存预警 |
| 生产领料 / 退料 | ✅ 已实现 | 领料单 /api/requisitions CRUD；MES 工单下发 Feign 触发创建；POST /{id}/complete 完成领料扣库存、退料加库存 |
| 物料批次管理 | ✅ 已实现 | 库存/出入库/领料明细表支持 batch_no；adjust 请求可指定 batchNo 入库；库存分页支持 batchNo 筛选 |
| 物料追溯 | ✅ 已实现 | GET /api/inventory/trace 按物料/批次/工单/领料单多条件查询；领料完成自动记录 order_id、req_id 到出入库事务 |

**创建依据**：生产离不开物料，仓储是生产执行的 "后勤保障"，独立成域避免与 MES 耦合。

#### ✅ mom-qms（质量管理域）
**核心职责**：质量管控与追溯，制造企业合规必备

| 能力 | 实现状态 | 说明 |
|------|----------|------|
| 质检任务创建 / 执行 | ✅ 已实现 | POST /api/inspection-tasks 创建；POST /{id}/start 开始、/{id}/complete 完成；MES 工单完成 Feign 触发创建 |
| 质检数据采集、质量判定 | ✅ 已实现 | POST /api/inspection-results 录入检验项、标准值、实际值、判定（合格/不合格） |
| 不合格品处理（返工/报废） | ✅ 已实现 | POST /api/ncr 创建；POST /{id}/dispose 处置（返工/报废/让步接收/退货） |
| 质量追溯（关联工单/设备/物料） | ✅ 已实现 | GET /api/quality-trace?productCode=&batchNo=&productionOrder= 多条件查询；质检不合格、NCR 创建时自动写入追溯 |

**创建依据**：质量是制造企业的生命线，独立成域可灵活扩展质检规则，不影响核心生产流程。

### 3. 系统管理域（必选，基础信息管理）

#### ✅ mom-admin（系统管理域）
**核心职责**：用户 / 租户 / 字典等基础信息统一维护，独立成域避免与业务耦合

| 能力 | 实现状态 | 说明 |
|------|----------|------|
| 用户管理（创建、授权、认证） | ✅ 已实现 | POST /api/users CRUD；PUT /{id}/roles 分配角色；POST /api/auth/login、GET /api/auth/me |
| 租户管理（多租户数据隔离、租户配置） | ✅ 已实现 | POST /api/tenants CRUD；sys_user.tenant_id 关联租户 |
| 字典管理（统一维护系统基础配置） | ✅ 已实现 | POST /api/dicts CRUD；GET /api/dicts/type/{dictType} 按类型获取 |
| 角色权限管理（RBAC权限模型） | ✅ 已实现 | 角色 CRUD、权限 CRUD、角色权限关联、用户角色分配 |
| 系统日志（操作日志、审计日志） | ✅ 已实现 | POST /api/operation-logs 记录；GET 分页查询；POST /api/audit-logs 记录；GET 分页查询 |

**设计原则**：
- mom-admin 仅依赖基础支撑层
- 业务域通过接口调用其能力
- 网关层依赖其做权限校验

**隔离规则**：
- 通过租户 ID 隔离多租户数据
- 字典数据缓存化保证性能
- 禁止业务域硬编码基础配置

**创建依据**：将系统管理能力独立成域，符合 DDD 领域划分原则，避免与业务逻辑耦合，提高系统可维护性和扩展性。

### 4. 网关 & 注册配置（可选，微服务部署必选）

#### ✅ mom-gateway（网关模块）
**核心职责**：系统统一入口，处理跨域、认证、路由、限流

| 能力 | 实现状态 | 说明 |
|------|----------|------|
| 接口路由 | ✅ 已实现 | 按 Path 转发到 mom-admin/iot/mes/wms/qms；lb 负载均衡 |
| 统一认证授权（JWT） | ✅ 已实现 | JwtAuthGlobalFilter 校验 Bearer Token；app.jwt.gateway-auth-enabled=true 开启 |
| 接口限流 | ✅ 已实现 | RateLimitGlobalFilter 按 IP 内存限流；app.rate-limit.enabled=true 开启 |
| 熔断、降级（Sentinel） | ✅ 已实现 | 引入 spring-cloud-starter-alibaba-sentinel；配置 Sentinel Dashboard 后生效 |
| 跨域处理 | ✅ 已实现 | globalcors 配置 + GatewayCorsConfig |
| 请求日志 | ✅ 已实现 | RequestLoggingGlobalFilter 记录 method、path、IP、耗时、status |
| 接口文档聚合 | ✅ 已实现 | GET /v3/api-docs/all 聚合各服务 OpenAPI |

**创建依据**：微服务部署时，前端 / 外部系统只需对接网关，无需感知后端多模块，降低对接成本。

## 技术栈

- **Java Version**: 25
- **Build Tool**: Gradle 9.2.1+
- **Framework**: Spring Boot 3.5.3
- **Architecture**: DDD (Domain-Driven Design)
- **Database**: PostgreSQL 16.4+
- **Message Queue**: EMQX/MQTT
- **Cache**: Redis
- **Monitoring**: Prometheus/Grafana
- **Distributed Transaction**: Seata
- **Service Registry**: Nacos 2.3.2
- **API Gateway**: Spring Cloud Gateway
- **ORM Framework**: Spring Data JPA
- **Connection Pool**: Druid 1.2.22

## 快速开始

### 环境要求

- JDK 25
- Gradle 9.2.1+
- PostgreSQL 16.4+
- Docker 20.10.0+ (可选，用于容器化部署)

### 本地开发

1. **克隆代码**
   ```bash
   git clone https://github.com/your-username/ly-factmesh.git
   cd ly-factmesh
   ```

2. **配置数据库**
   - 创建数据库：为每个模块创建独立的数据库
     - ly_factmesh_admin
     - ly_factmesh_iot
     - ly_factmesh_mes
     - ly_factmesh_wms
     - ly_factmesh_qms
     - ly_factmesh_ops（运维库：全局日志、审计、系统事件）
   - 导入SQL脚本：执行 `tools/sql` 目录下的对应SQL脚本

3. **构建项目**
   ```bash
   ./gradlew build
   ```

4. **启动服务**
   - 启动系统管理模块
     ```bash
     ./gradlew :mom-admin:bootRun
     ```
   - 启动业务域模块（根据需要选择）
     ```bash
     ./gradlew :mom-iot:bootRun
     ./gradlew :mom-mes:bootRun
     ./gradlew :mom-wms:bootRun
     ./gradlew :mom-qms:bootRun
     ```
   - 启动网关
     ```bash
     ./gradlew :mom-gateway:bootRun
     ```

5. **访问系统**
   - 网关地址：http://localhost:8080
   - API 文档：http://localhost:8080/swagger-ui.html（网关聚合）/ http://localhost:8081/swagger-ui.html（admin 单服务）
   - 聚合 OpenAPI JSON：http://localhost:8080/swagger-ui.html
   - 注册中心地址：http://localhost:8848

6. **常见问题**
   - **端口 8080 被占用**：`Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force`
   - **swagger-ui.html 访问失败**：需先启动 Nacos 和 mom-admin，再启动网关；或直接访问 http://localhost:8080/v3/api-docs/all 查看聚合文档

### Docker部署

1. **构建镜像**
   ```bash
   ./gradlew bootBuildImage
   ```

2. **启动服务**
   ```bash
   docker-compose up -d
   ```

3. **访问系统**
   - 网关地址：http://localhost:8080
   - 注册中心地址：http://localhost:8848
   - PostgreSQL地址：localhost:5432

### Nacos + MySQL（可选，供微服务注册与配置使用）

使用 `tools/docker-compose-nacos.yml` 启动 MySQL（Nacos 存储）、Nacos：

```bash
cd tools && docker compose -f docker-compose-nacos.yml up -d
```

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | Nacos 配置持久化 |
| Nacos | 8848 | 服务注册与配置中心 |

### InfluxDB（可选，供 mom-iot 遥测数据存储）

使用 `tools/docker-compose-influxdb.yml` 启动 InfluxDB：

```bash
cd tools && docker compose -f docker-compose-influxdb.yml up -d
```

| 服务 | 端口 | 说明 |
|------|------|------|
| InfluxDB | 8086 | IoT 遥测时序数据 |

### MQTT 服务端（可选，供 mom-iot 遥测、事件等使用）

使用 `tools/mqtt` 目录下的 docker-compose 启动 EMQX：

```bash
docker compose -f tools/mqtt/docker-compose.yml up -d
```

| 端口  | 协议         | 说明                          |
|-------|--------------|-------------------------------|
| 1883  | MQTT         | 默认连接，配置 `infra.mqtt.broker-url=tcp://localhost:1883` |
| 8883  | MQTT over TLS | 加密连接                      |
| 8083  | WebSocket    | 浏览器端 MQTT                 |
| 18083 | HTTP         | 控制台，默认账号 admin/public |

### Seata Server（可选，供跨库分布式事务使用）

使用 `tools/seata` 目录下的 docker-compose 启动 Seata 服务端：

```bash
docker compose -f tools/seata/docker-compose.yml up -d
```

| 端口  | 说明                          |
|-------|-------------------------------|
| 8091  | Seata TC RPC，配置 `seata.service.grouplist.default=127.0.0.1:8091` |

启用后在各业务模块配置 `seata.enabled=true`，业务方法加 `@GlobalTransactional` 即可参与分布式事务。

### PostgreSQL 主从集群（可选，供读写分离使用）

使用 `tools/pgsql` 目录下的 docker-compose 启动 PostgreSQL 主从集群：

```bash
cd tools/pgsql && docker compose up -d
```

| 端口  | 说明                              |
|-------|-----------------------------------|
| 5432  | 主库（写）                        |
| 5433  | 从库（读）                        |

在各业务模块中启用读写分离：`infra.datasource.read-write-split.enabled=true`，配置 `master-url`、`slave-url`。在 Service/Mapper 的读方法上标注 `@ReadOnly` 即可自动路由到从库。详见 [tools/pgsql/README.md](tools/pgsql/README.md)。

## 项目结构

```
ly-factmesh/
├── mom-common/         # 公共核心模块
├── mom-infra/          # 基础设施模块
├── mom-admin/          # 系统管理域
├── mom-iot/            # 设备物联域
├── mom-mes/            # 生产执行域
├── mom-wms/            # 仓储管理域
├── mom-qms/            # 质量管理域
├── mom-ops/            # 运维模块（全局日志、审计、系统事件）
├── mom-gateway/        # 网关模块
├── tools/              # 辅助工具
│   ├── mqtt/           # MQTT 服务端 (EMQX) docker-compose
│   ├── pgsql/          # PostgreSQL 主从集群（读写分离）
│   ├── seata/          # Seata Server 分布式事务 docker-compose
│   ├── sql/            # 数据库初始化脚本
│   ├── docker-compose-nacos.yml  # Nacos+MySQL 编排
│   └── docker-compose-influxdb.yml  # InfluxDB 编排
├── DDD_ARCHITECTURE_OVERVIEW.md  # DDD架构文档
├── DOCKER_README.md    # Docker部署文档
├── docker-compose.yml  # Docker Compose配置
├── build.gradle        # 项目构建配置
├── settings.gradle     # 模块配置
├── gradlew             # Gradle包装器
└── README.md           # 项目说明文档
```

## 依赖关系

```
mom-iot → mom-infra → mom-common
mom-mes → mom-infra → mom-common
mom-wms → mom-infra → mom-common
mom-qms → mom-infra → mom-common
mom-admin → mom-infra → mom-common
mom-ops → mom-infra → mom-common
mom-gateway → mom-admin, mom-common
```

## 贡献指南

### 开发流程

1. **Fork 仓库**
2. **创建分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **提交代码**
   ```bash
   git add .
   git commit -m "Add your commit message"
   ```
4. **推送代码**
   ```bash
   git push origin feature/your-feature-name
   ```
5. **创建 Pull Request**

### 代码规范

- 遵循 Java 编码规范
- 使用 4 个空格进行缩进
- 类名使用 PascalCase
- 方法名和变量名使用 camelCase
- 常量名使用全大写，单词间用下划线分隔
- 每行代码长度不超过 120 个字符
- 方法注释使用 Javadoc 格式

### 测试要求

- 新增功能必须编写单元测试
- 单元测试覆盖率不低于 80%
- 提交代码前必须通过所有测试

## 许可证

本项目采用 MIT 许可证，详情请查看 [LICENSE](LICENSE) 文件。

## 联系方式

- **项目地址**: https://github.com/your-username/ly-factmesh
- **问题反馈**: https://github.com/your-username/ly-factmesh/issues
- **邮件联系**: your-email@example.com

## 发展规划

- **v1.0.0**: 完成核心功能开发，包括设备管理、工单管理、库存管理和质量管理
- **v1.1.0**: 增加报表统计功能，支持自定义报表
- **v1.2.0**: 集成 AI 预测分析功能，实现设备故障预测
- **v2.0.0**: 支持多云部署，实现跨地域协同管理

## 致谢

感谢所有为项目做出贡献的开发者和社区成员！
