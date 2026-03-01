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
- 通用工具类（日期、加密、校验、JSON 解析）
- 全局枚举（设备状态、工单状态、质检类型等）
- 公共 DTO/VO/Command/Query（分页、响应体、通用入参）
- 全局异常体系（业务异常、系统异常、统一异常处理器）
- 通用注解（权限、日志、数据校验）

**创建依据**：避免各业务模块重复写通用代码，降低维护成本，是开源项目的基础规范。

#### ✅ mom-infra（基础设施模块）
**核心职责**：封装技术层能力，为业务域提供 "技术工具"，隔离底层技术细节
- 数据库适配（PostgreSQL 连接池、分表分库、读写分离）
- 消息队列（EMQX/MQTT 客户端封装、领域事件发送 / 消费）
- 工业协议接入（OPC UA/Modbus/TCP 客户端封装）
- 缓存（Redis 客户端封装、缓存策略）
- 监控告警（Prometheus/Grafana 埋点、告警推送）
- 分布式事务（Seata 客户端封装）

**创建依据**：DDD 中 "基础设施层" 的落地，让业务域（如 iot/mes）无需关注技术实现，仅调用接口即可，符合 "业务与技术解耦" 的核心思想。

### 2. 核心业务域（必选，MOM 系统核心闭环）

#### ✅ mom-iot（设备物联域）
**核心职责**：覆盖设备全生命周期管理，是 MOM 系统的 "数据入口"
- 设备注册 / 上下线 / 状态监控
- 设备数据采集（实时 / 定时）、数据清洗、数据存储
- 设备故障告警、远程控制
- 设备台账管理（型号、参数、维保记录）

**创建依据**：制造企业的核心生产要素是设备，设备数据是 MES/WMS/QMS 的基础，必须独立成域。

#### ✅ mom-mes（生产执行域）
**核心职责**：MOM 系统的核心，覆盖生产全流程
- 工单管理（创建 / 下发 / 暂停 / 完成）
- 工序管理（工序拆分、工序执行、报工）
- 产线管理（产线状态、产能统计）
- 生产进度监控、生产报表

**创建依据**：生产执行是 MOM 系统的核心价值，需独立成域保证业务内聚。

#### ✅ mom-wms（仓储管理域）
**核心职责**：物料流转管理，衔接生产与供应链
- 物料入库 / 出库 / 盘点
- 库存查询、库存预警
- 物料批次管理、物料追溯
- 生产领料 / 退料管理

**创建依据**：生产离不开物料，仓储是生产执行的 "后勤保障"，独立成域避免与 MES 耦合。

#### ✅ mom-qms（质量管理域）
**核心职责**：质量管控与追溯，制造企业合规必备
- 质检任务创建 / 执行
- 质检数据采集、质量判定
- 不合格品处理（返工 / 报废）
- 质量追溯（关联工单 / 设备 / 物料）

**创建依据**：质量是制造企业的生命线，独立成域可灵活扩展质检规则，不影响核心生产流程。

### 3. 系统管理域（必选，基础信息管理）

#### ✅ mom-admin（系统管理域）
**核心职责**：用户 / 租户 / 字典等基础信息统一维护，独立成域避免与业务耦合
- 用户管理（创建、授权、认证）
- 租户管理（多租户数据隔离、租户配置）
- 字典管理（统一维护系统基础配置）
- 角色权限管理（RBAC权限模型）
- 系统日志（操作日志、审计日志）

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
- 接口路由（转发请求到对应业务域）
- 统一认证授权（JWT/OAuth2）
- 接口限流、熔断、降级（Sentinel）
- 跨域处理、请求日志、接口文档聚合

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
     - ly_factmesh_iot
     - ly_factmesh_mes
     - ly_factmesh_wms
     - ly_factmesh_qms
     - ly_factmesh_admin
   - 导入SQL脚本：执行 `sql` 目录下的对应SQL脚本

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
├── mom-gateway/        # 网关模块
├── sql/                # 数据库初始化脚本
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
