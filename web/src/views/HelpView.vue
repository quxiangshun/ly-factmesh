<template>
  <section class="help-page">
    <h1 class="help-title">LY-FactMesh MOM 帮助文档</h1>
    <p class="help-desc">业务逻辑说明与技术实现要点，按菜单结构组织</p>

    <nav class="help-toc">
      <div v-for="group in toc" :key="group.id" class="toc-group">
        <a :href="'#' + group.id" class="toc-link">{{ group.name }}</a>
        <template v-if="group.children?.length">
          <a v-for="c in group.children" :key="c.id" :href="'#' + c.id" class="toc-child">{{ c.name }}</a>
        </template>
      </div>
    </nav>

    <article class="help-content">
      <section id="intro" class="help-section">
        <h2>系统概述</h2>
        <p>LY-FactMesh 是面向制造业的制造运营管理（MOM）系统，采用 DDD 与微服务架构，实现设备物联、生产执行、仓储、质量等核心能力的统一管理。</p>

        <h3 id="intro-swimlane">系统泳道流程图</h3>
        <p>下图按泳道展示用户进入、设备上报、告警触发、工单流转等核心流程及跨域触发条件。下方按流程拆解业务逻辑、步骤与触发条件。</p>
        <SwimlaneDiagram />
        <div class="flow-legend">
          <h4>图例说明</h4>
          <ul>
            <li><strong>泳道</strong>：用户/设备、网关、Admin、IoT、MES、WMS、QMS、基础设施（mom-infra），每列表示一个参与方或微服务。</li>
            <li><strong>实线箭头</strong>：同步调用或顺序执行。</li>
            <li><strong>虚线箭头</strong>：Feign 跨域调用（如工单下发→WMS 领料、工单完成→QMS 质检）。</li>
            <li><strong>菱形</strong>：条件判断（如告警规则匹配）。</li>
          </ul>

          <h4 id="flow-user">一、用户进入</h4>
          <p><strong>业务逻辑</strong>：用户访问前端，输入账号密码发起登录；请求经网关 Path 匹配 /api/auth/** 转发至 Admin；Admin 校验用户存在、BCrypt 比对密码，通过后签发 JWT；前端将 Token 存 localStorage，后续请求携带 Authorization: Bearer &lt;token&gt; 访问各业务接口。</p>
          <p><strong>步骤</strong>：访问前端 → 输入账号密码 → POST /api/auth/login → 网关 lb 转发 → AuthService.login → 校验用户存在 → BCrypt.matches 校验密码 → 签发 JWT → 返回 token/userId/nickname → 前端存 Token，跳转业务页。</p>
          <p><strong>前置条件</strong>：用户已创建且未禁用；密码正确。</p>

          <h4 id="flow-device">二、设备上报</h4>
          <p><strong>业务逻辑</strong>：设备或采集端通过 POST /api/devices/{id}/telemetry 上报遥测数据；请求经网关转发至 IoT；DeviceTelemetryService 校验设备存在后，将 data（测点名→数值）写入 InfluxDB 时序库；写入完成后同步调用 AlertRuleEngineService.evaluate() 触发规则评估。</p>
          <p><strong>请求体</strong>：{ deviceId, deviceCode?, data: { "temperature": 85.2, "humidity": 60 }, collectTime? }；data 为 key-value，key 为测点名称。</p>
          <p><strong>步骤</strong>：设备上报 → 网关 Path /api/devices/** → IoT reportTelemetry → 校验设备存在 → 批量写入 InfluxDB（device_id、field、value、time）→ 调用 AlertRuleEngineService.evaluate(deviceId, deviceCode, data)。</p>

          <h4 id="flow-alert">三、告警触发</h4>
          <p><strong>业务逻辑</strong>：遥测上报后，规则引擎遍历所有启用规则，逐条评估；满足条件且非冷却期时创建告警；人工在告警列表点击「处理」调用 resolve 更新状态。</p>
          <p><strong>规则匹配条件</strong>：① 设备匹配：rule.deviceId 为空或等于上报设备；rule.deviceType 为空或等于设备类型；② 测点匹配：data 中存在 rule.fieldName（支持大小写不敏感）；③ 阈值判断：value 与 rule.thresholdValue（及 thresholdValueHigh）满足 operator；④ 冷却期：rule.cooldown_seconds 内该规则+设备无待处理告警。</p>
          <p><strong>操作符</strong>：gt（大于）、gte（大于等于）、lt（小于）、lte（小于等于）、eq（等于）、ne（不等于）、between（区间内 low≤value≤high，需 thresholdValueHigh）、outside（区间外 value&lt;low 或 value&gt;high）。规则列表缓存 60 秒，规则增删改后自动失效。</p>
          <p><strong>步骤</strong>：evaluate 入参 (deviceId, deviceCode, data) → 查设备类型 → 遍历 listEnabledRules → matchesDevice → 取 data[fieldName] → evaluateCondition(operator, value, threshold) → 冷却期检查 hasRecentUnresolvedAlert → createAlertFromRule → 人工 resolve。</p>

          <h4 id="flow-workorder">四、工单流转</h4>
          <p><strong>业务逻辑</strong>：用户创建工单（草稿）→ 下发（release）→ 报工录入 → 完成（complete）。下发时 MES 通过 Feign 调用 WMS 创建领料单；完成时 MES 通过 Feign 调用 QMS 创建质检任务。报工后工单 actualQuantity 累加，已下发自动转进行中。</p>
          <p><strong>状态机</strong>：草稿(0) → release → 已下发(1) → 报工 → 进行中(2) → complete → 已完成(3)。</p>
          <p><strong>步骤</strong>：创建工单（orderCode、productCode、planQuantity）→ release(id) 校验状态=草稿 → 更新 status=已下发 → Feign WmsFeignClient.createRequisition(workOrderId, productCode, planQuantity) → 报工（校验工单已下发或进行中、工序存在）→ actualQuantity += reportQuantity - scrapQuantity，若原状态=已下发则置为进行中 → complete(id) 校验状态=进行中 → 更新 status=已完成 → Feign QmsFeignClient.createInspectionTask(orderId, taskCode, inspectionType)。</p>

          <h4 id="flow-cross">五、跨域触发条件</h4>
          <table class="help-table">
            <thead>
              <tr><th>触发方</th><th>触发动作</th><th>触发条件</th><th>被调方</th><th>调用接口</th><th>入参</th></tr>
            </thead>
            <tbody>
              <tr><td>MES</td><td>工单下发</td><td>工单 status=草稿，调用 release(id) 成功</td><td>WMS</td><td>WmsFeignClient.createRequisition</td><td>workOrderId, workOrderNo, materialCode, materialName, quantity</td></tr>
              <tr><td>MES</td><td>工单完成</td><td>工单 status=进行中，调用 complete(id) 成功</td><td>QMS</td><td>QmsFeignClient.createInspectionTask</td><td>taskCode, orderId, inspectionType</td></tr>
            </tbody>
          </table>

          <h4>触发条件速查</h4>
          <table class="help-table">
            <thead>
              <tr><th>流程</th><th>触发条件</th><th>结果</th></tr>
            </thead>
            <tbody>
              <tr><td>用户登录</td><td>POST /api/auth/login，用户名+密码</td><td>BCrypt 校验通过 → 签发 JWT</td></tr>
              <tr><td>设备遥测</td><td>POST /api/devices/{id}/telemetry，deviceId + data</td><td>写入 InfluxDB → 触发规则引擎</td></tr>
              <tr><td>告警创建</td><td>规则匹配：deviceId/type、field、operator、threshold 满足；且非冷却期</td><td>创建 DeviceAlert，待人工 resolve</td></tr>
              <tr><td>工单下发</td><td>工单状态=草稿，调用 release(id)</td><td>状态→已下发；Feign 调用 WMS 创建领料单</td></tr>
              <tr><td>工单完成</td><td>工单状态=进行中，调用 complete(id)</td><td>状态→已完成；Feign 调用 QMS 创建质检任务</td></tr>
            </tbody>
          </table>
        </div>

        <h3 id="intro-flow">系统运转流程</h3>
        <p><strong>业务逻辑</strong>：生产执行从基础配置到工单完成的闭环流程。先维护工序与产线主数据，再创建工单并下发，现场通过报工录入产量，系统自动累加实际数量；当实际数量达到计划数量时工单完成，否则继续报工。</p>
        <p><strong>解决方案</strong>：采用「配置先行、工单驱动、报工闭环」模式。工序与产线支持 CRUD 与编辑（编码不可改）；工单状态机控制流转；报工时校验工单/工序存在且工单已下发或进行中，报工后自动更新工单 actualQuantity 与 status，实现生产进度实时反馈。</p>
        <p><strong>流程图描述</strong>：从左到右依次为「工序管理」→「产线管理」→「工单创建」→「工单下发」→「报工录入」→「实际数量累加」→「工单完成」。虚线箭头表示反馈回路：当实际数量未达计划时，需继续执行报工录入，直至实际≥计划后工单完成。</p>
        <div class="flow-diagram">
          <svg viewBox="0 0 720 260" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <marker id="flow-arrow" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto">
                <path d="M0,0 L0,6 L9,3 z" fill="#64748b" />
              </marker>
              <linearGradient id="flow-box" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:#38bdf8;stop-opacity:0.25" />
                <stop offset="100%" style="stop-color:#1e293b" />
              </linearGradient>
            </defs>
            <!-- 节点 -->
            <rect x="15" y="95" width="85" height="40" rx="6" fill="url(#flow-box)" stroke="#38bdf8" stroke-width="1.5" />
            <text x="57" y="120" text-anchor="middle" fill="#e5e7eb" font-size="12">工序管理</text>
            <rect x="120" y="95" width="85" height="40" rx="6" fill="url(#flow-box)" stroke="#38bdf8" stroke-width="1.5" />
            <text x="162" y="120" text-anchor="middle" fill="#e5e7eb" font-size="12">产线管理</text>
            <rect x="225" y="95" width="85" height="40" rx="6" fill="url(#flow-box)" stroke="#38bdf8" stroke-width="1.5" />
            <text x="267" y="120" text-anchor="middle" fill="#e5e7eb" font-size="12">工单创建</text>
            <rect x="330" y="95" width="85" height="40" rx="6" fill="url(#flow-box)" stroke="#38bdf8" stroke-width="1.5" />
            <text x="372" y="120" text-anchor="middle" fill="#e5e7eb" font-size="12">工单下发</text>
            <rect x="435" y="95" width="85" height="40" rx="6" fill="url(#flow-box)" stroke="#38bdf8" stroke-width="1.5" />
            <text x="477" y="120" text-anchor="middle" fill="#e5e7eb" font-size="12">报工录入</text>
            <rect x="540" y="95" width="95" height="40" rx="6" fill="url(#flow-box)" stroke="#38bdf8" stroke-width="1.5" />
            <text x="587" y="120" text-anchor="middle" fill="#e5e7eb" font-size="11">实际数量累加</text>
            <rect x="655" y="95" width="70" height="40" rx="6" fill="url(#flow-box)" stroke="#38bdf8" stroke-width="1.5" />
            <text x="690" y="120" text-anchor="middle" fill="#e5e7eb" font-size="12">工单完成</text>
            <!-- 主流程箭头 -->
            <path d="M100 115 L120 115" stroke="#64748b" stroke-width="2" marker-end="url(#flow-arrow)" />
            <path d="M205 115 L225 115" stroke="#64748b" stroke-width="2" marker-end="url(#flow-arrow)" />
            <path d="M310 115 L330 115" stroke="#64748b" stroke-width="2" marker-end="url(#flow-arrow)" />
            <path d="M415 115 L435 115" stroke="#64748b" stroke-width="2" marker-end="url(#flow-arrow)" />
            <path d="M520 115 L540 115" stroke="#64748b" stroke-width="2" marker-end="url(#flow-arrow)" />
            <path d="M635 115 L655 115" stroke="#64748b" stroke-width="2" marker-end="url(#flow-arrow)" />
            <!-- 反馈回路：未达计划则继续报工 -->
            <path d="M587 135 L587 185 L435 185 L435 135" stroke="#64748b" stroke-width="1.5" stroke-dasharray="5 4" fill="none" marker-end="url(#flow-arrow)" />
            <text x="510" y="205" fill="#94a3b8" font-size="10">未达计划则继续报工</text>
          </svg>
        </div>

        <h3>技术架构</h3>
        <ul>
          <li><strong>前端</strong>：Vite + Vue 3 + TypeScript + Vue Router + Iconify</li>
          <li><strong>网关</strong>：Spring Cloud Gateway，统一入口、路由、鉴权</li>
          <li><strong>基础设施</strong>：mom-infra 封装数据库、缓存、MQTT、工业协议、监控等，业务域依赖即可使用</li>
          <li><strong>后端</strong>：Spring Boot 4.x、JDK 25，各域独立微服务</li>
          <li><strong>持久化</strong>：PostgreSQL 16.x、MyBatis-Plus，一域一库</li>
          <li><strong>ID 生成</strong>：Snowflake 雪花算法，分布式唯一</li>
        </ul>
      </section>

      <section id="infra" class="help-section">
        <h2>基础设施（Infra）</h2>
        <p>mom-infra 模块，封装技术层能力，为业务域（admin/iot/mes/wms/qms）提供「技术工具」，隔离底层实现。无独立数据库，被各业务模块依赖。</p>

        <h3 id="infra-overview">能力概览</h3>
        <p><strong>业务逻辑</strong>：DDD 基础设施层落地，业务域通过接口调用技术能力，无需关心 Redis/MQTT/OPC UA 等具体实现。</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">业务域依赖 mom-infra 能力:
  mom-admin ──┬──► DB/Druid ──┬──► PostgreSQL
  mom-iot   ──┤              ├──► CacheService(Redis)
  mom-mes   ──┤              ├──► MqttClientWrapper(EMQX)
  mom-wms   ──┤              ├──► OpcUaClient / ModbusTcpClient
  mom-qms   ──┘              ├──► Prometheus/Micrometer
                             ├──► 读写分离(DynamicDataSource + @ReadOnly)
                             └──► Seata(分布式事务)</pre>
        </div>
        <table class="help-table">
          <thead>
            <tr><th>能力</th><th>说明</th><th>配置/接口</th></tr>
          </thead>
          <tbody>
            <tr><td>数据库适配</td><td>PostgreSQL + Druid 连接池、MyBatis-Plus 分页</td><td>spring.datasource.*</td></tr>
            <tr><td>读写分离</td><td>DynamicDataSource 主从路由；@ReadOnly 注解标注读方法路由到从库</td><td>infra.datasource.read-write-split.enabled=true，master-url、slave-url</td></tr>
            <tr><td>缓存</td><td>Redis 客户端封装，CacheService 接口（set/get/delete/expire）</td><td>spring.data.redis.host</td></tr>
            <tr><td>消息队列</td><td>MQTT 客户端接口 MqttClientWrapper（publish/subscribe）、MqttProperties</td><td>infra.mqtt.broker-url</td></tr>
            <tr><td>工业协议</td><td>OPC UA、Modbus TCP 客户端接口（业务域实现）</td><td>OpcUaClient、ModbusTcpClient</td></tr>
            <tr><td>监控告警</td><td>Actuator + Micrometer Prometheus 埋点</td><td>management.endpoints.*</td></tr>
            <tr><td>分布式事务</td><td>Seata 2.2.0，seata-spring-boot-starter 自动装配</td><td>seata.enabled=true，application-id，tx-service-group</td></tr>
          </tbody>
        </table>

        <h3 id="infra-read-write-split">读写分离</h3>
        <p><strong>使用方式</strong>：配置 infra.datasource.read-write-split.enabled=true，并填写 master-url、slave-url。在 Service 或 Mapper 的读方法上标注 @ReadOnly，执行时自动路由到从库。</p>
        <p><strong>配置示例</strong>：infra.datasource.read-write-split.master-url、slave-url、username、password；未配置时从 spring.datasource 继承。</p>

        <h3 id="infra-seata">分布式事务（Seata）</h3>
        <p><strong>使用方式</strong>：配置 seata.enabled=true，部署 Seata Server；业务方法加 @GlobalTransactional 即可参与分布式事务。</p>
        <p><strong>配置示例</strong>：seata.application-id、tx-service-group、service.vgroup-mapping、service.grouplist。</p>

        <h3 id="infra-cache">缓存（Redis）</h3>
        <p><strong>使用方式</strong>：注入 CacheService，配置 spring.data.redis.host 后自动生效。</p>
        <p><strong>接口</strong>：set(key, value)、set(key, value, timeout, unit)、get(key, clazz)、delete(key)、hasKey(key)、expire(key, timeout, unit)。</p>

        <h3 id="infra-mqtt">消息队列（MQTT）</h3>
        <p><strong>使用方式</strong>：实现 MqttClientWrapper 接口，对接 EMQX；配置 infra.mqtt.broker-url 启用。</p>
        <p><strong>接口</strong>：publish(topic, payload)、subscribe(topic, callback)、unsubscribe(topic)、isConnected()、disconnect()。</p>

        <h3 id="infra-protocol">工业协议</h3>
        <p><strong>OPC UA</strong>：OpcUaClient 接口，connect、readValue、writeValue、readValues；业务域（如 mom-iot）引入 OPC UA 库后实现。</p>
        <p><strong>Modbus TCP</strong>：ModbusTcpClient 接口，connect、readHoldingRegisters、readInputRegisters、writeSingleRegister、writeMultipleRegisters。</p>
      </section>

      <section id="gateway" class="help-section">
        <h2>网关（Gateway）</h2>
        <p>mom-gateway 模块，统一入口，基于 Nacos 服务发现路由至各微服务。无独立数据库，仅依赖 mom-common。</p>

        <h3 id="gateway-overview">网关概览</h3>
        <p><strong>业务逻辑</strong>：所有前端与外部请求统一通过网关访问；网关负责路由匹配、鉴权（可选）、限流（可选）、OpenAPI 文档聚合；按路径前缀将请求转发至对应微服务。</p>
        <p><strong>解决方案</strong>：采用 Spring Cloud Gateway + Nacos Discovery；路由配置由 application.yml 或 Nacos 配置中心 gateway.yaml 提供；支持 lb:// 负载均衡转发；认证接口 /api/auth/** 与用户/角色/权限等接口转发至 mom-admin；设备、工单、物料、质检等按路径前缀转发至对应服务。</p>
        <p><strong>流程图描述</strong>：请求 → 网关入口 → 路径匹配（Predicate）→ 路由选择（admin/iot/mes/wms/qms）→ 负载均衡转发（lb://）→ 下游微服务返回。若路径为 /v3/api-docs/all，则走本地聚合控制器，返回统一 OpenAPI 文档。</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">  ┌────────────┐  Path匹配  ┌────────────────┐  lb转发  ┌────────────────────────┐
  │ 前端请求   │ ────────► │  mom-gateway   │ ──────► │ mom-admin/iot/mes/wms  │
  │ /api/*     │           │  路由/鉴权     │         │ /qms (均依赖mom-infra) │
  └────────────┘           └───────┬───────┘         └────────────────────────┘
                                   │
                                   │ /v3/api-docs/all
                                   ▼
                           ┌────────────────┐
                           │ OpenAPI 聚合文档 │
                           └────────────────┘</pre>
        </div>

        <h3 id="gateway-routes">路由规则</h3>
        <p><strong>业务逻辑</strong>：按路径前缀将请求转发至对应服务。</p>
        <p><strong>解决方案</strong>：/api/auth/**、/api/users/**、/api/roles/**、/api/permissions/**、/api/dicts/**、/api/configs/** → mom-admin；/api/devices/** → mom-iot；/api/work-orders/**、/api/processes/**、/api/lines/**、/api/work-reports/** → mom-mes；/api/materials/**、/api/requisitions/**、/api/inventory/** → mom-wms；/api/inspection-tasks/**、/api/inspection-results/**、/api/ncr/** → mom-qms。</p>
        <p><strong>技术点</strong>：Spring Cloud Gateway routes；Nacos Discovery Locator；lb:// 服务名负载均衡；connect-timeout 3s、response-timeout 30s。</p>
      </section>

      <section id="dashboard" class="help-section">
        <h2>工作台</h2>
        <h3 id="dashboard-home">首页概览</h3>
        <p><strong>业务逻辑</strong>：展示平台概览、快捷入口、领域能力卡片。</p>
        <p><strong>解决方案</strong>：静态展示页，通过 RouterLink 跳转各模块；无后端接口，轻量快速。</p>
        <p><strong>技术点</strong>：静态展示页，无后端接口；通过 RouterLink 跳转各模块。</p>

        <h3 id="dashboard-todos">待办任务</h3>
        <p><strong>业务逻辑</strong>：聚合工单待办、质检待办、设备告警待办，统一提醒。</p>
        <p><strong>解决方案</strong>：占位页，后续通过 Feign 或 BFF 聚合 mom-mes 待检工单、mom-qms 待检任务、mom-iot 待处理告警，统一展示与跳转。</p>
        <p><strong>技术点</strong>：占位页，后续需跨域聚合 mom-mes、mom-qms、mom-iot 的待办数据。</p>
      </section>

      <section id="admin" class="help-section">
        <h2>系统管理（Admin）</h2>
        <p>mom-admin 模块，RBAC 权限模型，独立 PostgreSQL 库 ly_factmesh_admin。</p>

        <h3 id="admin-overview">Admin 业务流转概览</h3>
        <p><strong>业务逻辑</strong>：系统管理以 RBAC 为核心，用户通过登录获取 JWT，携带 Token 访问受保护接口；权限数据由用户→角色→权限三级关联维护；字典与配置为全局选项与参数提供支撑。</p>
        <p><strong>解决方案</strong>：AuthService 登录校验用户名密码、BCrypt 比对、签发 JWT；前端将 Token 存 localStorage，请求头 Authorization: Bearer &lt;token&gt;；User-Role-Permission 多对多关联，分配时先删后插批量更新。</p>
        <p><strong>流程图描述</strong>：登录流程：用户输入账号密码 → POST /api/auth/login → 校验用户存在且密码正确 → 签发 JWT 返回；RBAC 关联：User ↔ UserRole ↔ Role ↔ RolePermission ↔ Permission，分配角色/权限时先清空关联再批量插入。</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">登录: 用户名/密码 ──► AuthService.login ──► BCrypt校验 ──► 签发JWT ──► 返回token/userId

RBAC: User ──UserRole──► Role ──RolePermission──► Permission
      │                    │
      ├─ 分配角色: 清空UserRole, 批量插入
      └─ 分配权限: 清空RolePermission, 批量插入</pre>
        </div>

        <h3 id="admin-users">用户管理</h3>
        <p><strong>业务逻辑</strong>：用户 CRUD、分页查询、用户-角色关联（多对多）。</p>
        <p><strong>解决方案</strong>：User 与 Role 通过 UserRole 多对多关联；分配角色时先清空再批量插入；密码 BCrypt 加密存储；登录时校验并签发 JWT。</p>
        <p><strong>技术点</strong>：MyBatis-Plus 分页 Page；UserRole 关联表；密码加密存储。</p>

        <h3 id="admin-roles">角色管理</h3>
        <p><strong>业务逻辑</strong>：角色 CRUD、分配权限（勾选权限树）。</p>
        <p><strong>解决方案</strong>：Role 与 Permission 通过 RolePermission 多对多关联；getPermissionTree 递归构建树形结构；assignRolePermissions 先删后插批量更新。</p>
        <p><strong>技术点</strong>：RolePermission 关联表；getPermissionTree 返回树形结构；assignRolePermissions 批量更新。</p>

        <h3 id="admin-permissions">权限管理</h3>
        <p><strong>业务逻辑</strong>：权限 CRUD、树形展示（父子结构）。</p>
        <p><strong>解决方案</strong>：parent_id 自关联构建树；列表/树形 Tab 切换；递归构建树供角色分配时勾选。</p>
        <p><strong>技术点</strong>：parent_id 自关联；列表/树形 Tab 切换；递归构建树。</p>

        <h3 id="admin-dicts">字典管理</h3>
        <p><strong>业务逻辑</strong>：数据字典维护，按类型（dict_type）分组，用于下拉选项等。</p>
        <p><strong>解决方案</strong>：dict_type 分组；getDictsByType 按类型查询返回键值对列表；前端下拉、表单选项统一从字典获取。</p>
        <p><strong>技术点</strong>：按 type 筛选；getDictsByType 按类型查询。</p>

        <h3 id="admin-configs">系统配置</h3>
        <p><strong>业务逻辑</strong>：键值对配置，支持按 config_key 筛选。</p>
        <p><strong>解决方案</strong>：config_key 唯一；getConfigByKey 单键查询；用于系统参数、开关等可配置项。</p>
        <p><strong>技术点</strong>：config_key 唯一；getConfigByKey 单键查询。</p>

        <h3 id="admin-logs">操作日志</h3>
        <p><strong>业务逻辑</strong>：操作审计日志，记录谁在何时做了什么。</p>
        <p><strong>解决方案</strong>：占位页，后续可通过 AOP 切面拦截 Controller 方法，记录用户、时间、操作、参数、结果。</p>
        <p><strong>技术点</strong>：占位页，后端待实现；可通过 AOP 切面记录 Controller 调用。</p>
      </section>

      <section id="iot" class="help-section">
        <h2>设备物联（IoT）</h2>
        <p>mom-iot 模块，DDD 分层，独立库 ly_factmesh_iot；遥测数据存 InfluxDB。</p>

        <h3 id="iot-overview">IoT 业务流转概览</h3>
        <p><strong>业务逻辑</strong>：设备从注册到运行的全生命周期管理；遥测数据上报写入 InfluxDB，同时触发告警规则引擎评估；满足阈值的规则自动创建告警，待处理告警由人工 resolve。</p>
        <p><strong>解决方案</strong>：DeviceAggregate 聚合根管理设备状态（在线/离线/运行/故障）；遥测上报时 DeviceTelemetryService 写入 InfluxDB 并调用 AlertRuleEngineService.evaluate()；规则匹配 deviceId/deviceType、field、operator、threshold，cooldown 防重复。</p>
        <p><strong>流程图描述</strong>：设备生命周期：注册 → 上线 → 运行/停止 → 故障/离线；遥测+告警联动：遥测上报 → 写入 InfluxDB → 规则引擎评估 → 满足条件且非冷却期 → 创建告警 → 人工处理 resolve。</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">设备生命周期:
  注册 ──► 上线(online) ──► 运行(running)/停止(idle) ──► 故障(fault)/离线(offline)

遥测+告警联动:
  遥测上报 ──► 写入InfluxDB ──► AlertRuleEngineService.evaluate()
      │                              │
      │                              ├─ 规则缓存(60s, 增删改失效) / 测点大小写不敏感
      │                              ├─ 匹配规则(deviceId/type, field, operator, threshold)
      │                              │   operator: gt/gte/lt/lte/eq/ne/between/outside
      │                              ├─ 冷却期检查(cooldown_seconds)
      │                              └─ 创建告警 ──► 人工resolve</pre>
        </div>

        <h3 id="iot-devices">设备管理</h3>
        <p><strong>业务逻辑</strong>：设备注册、上下线、启停、故障、编辑、删除；展示温度/湿度等实时状态。</p>
        <p><strong>解决方案</strong>：DeviceAggregate 聚合根统一管理设备生命周期；状态机控制 onlineStatus、runningStatus 流转；遥测上报时更新 statusLastUpdateTime 与测点值；GET /stats 聚合统计。</p>
        <p><strong>技术点</strong>：DeviceAggregate 聚合根；DeviceRepository；状态机（在线/离线/运行/故障）；GET /stats 统计总数/在线/故障。</p>

        <h3 id="iot-telemetry">设备遥测</h3>
        <p><strong>业务逻辑</strong>：按设备、测点、时间范围查询历史采集数据；支持遥测上报写入。</p>
        <p><strong>解决方案</strong>：遥测数据存 InfluxDB 时序库，按 deviceId、field、时间范围 Flux 查询；reportTelemetry 支持批量测点写入；queryTelemetry 支持 start/end/limit 分页。</p>
        <p><strong>技术点</strong>：InfluxDB 时序库；Flux 查询；reportTelemetry 写入；queryTelemetry 按 deviceId/field/start/end 查询。</p>

        <h3 id="iot-alerts">设备告警</h3>
        <p><strong>业务逻辑</strong>：告警记录（待处理/全部）、告警规则（阈值自动告警）、处理告警。</p>
        <p><strong>解决方案</strong>：告警规则配置 field/operator/threshold，遥测上报时 AlertRuleEngineService 评估，满足条件自动创建告警；cooldown_seconds 防重复告警；resolve 更新告警状态并记录处理人。</p>
        <p><strong>技术点</strong>：</p>
        <ul>
          <li><strong>告警记录</strong>：DeviceAlert 表，rule_id 关联规则；分页查询；resolve 更新状态。</li>
          <li><strong>告警规则</strong>：DeviceAlertRule 表，field/operator/threshold（between/outside 需 thresholdValueHigh）；遥测上报时 AlertRuleEngineService 评估，满足条件自动创建告警；cooldown_seconds 防重复；规则缓存 60s；测点名称大小写不敏感。</li>
        </ul>

        <h3 id="iot-rule-engine">规则引擎</h3>
        <p><strong>业务逻辑</strong>：遥测上报后，AlertRuleEngineService.evaluate() 遍历启用规则，按设备匹配、测点匹配、阈值判断、冷却期检查逐条评估，满足条件则创建告警。</p>
        <p><strong>操作符</strong>：</p>
        <table class="help-table">
          <thead>
            <tr><th>操作符</th><th>说明</th><th>阈值要求</th><th>示例</th></tr>
          </thead>
          <tbody>
            <tr><td>gt</td><td>大于</td><td>thresholdValue</td><td>temperature &gt; 80</td></tr>
            <tr><td>gte</td><td>大于等于</td><td>thresholdValue</td><td>temperature ≥ 80</td></tr>
            <tr><td>lt</td><td>小于</td><td>thresholdValue</td><td>humidity &lt; 20</td></tr>
            <tr><td>lte</td><td>小于等于</td><td>thresholdValue</td><td>humidity ≤ 20</td></tr>
            <tr><td>eq</td><td>等于</td><td>thresholdValue</td><td>status == 0</td></tr>
            <tr><td>ne</td><td>不等于</td><td>thresholdValue</td><td>status ≠ 0</td></tr>
            <tr><td>between</td><td>区间内</td><td>thresholdValue + thresholdValueHigh</td><td>20 ≤ temperature ≤ 80</td></tr>
            <tr><td>outside</td><td>区间外</td><td>thresholdValue + thresholdValueHigh</td><td>temperature &lt; 20 或 &gt; 80</td></tr>
          </tbody>
        </table>
        <p><strong>其他特性</strong>：① 测点名称大小写不敏感（Temperature 与 temperature 等价）；② 规则列表缓存 60 秒，规则增删改后自动失效；③ 告警内容模板支持 {value}、{threshold} 占位符；④ between/outside 创建时需填写 thresholdValueHigh，否则校验失败。</p>
      </section>

      <section id="mes" class="help-section">
        <h2>生产执行（MES）</h2>
        <p>mom-mes 模块，独立库 ly_factmesh_mes，Flyway 管理表结构。核心能力：工序、产线、工单、报工，形成「配置→工单→报工→完成」闭环。</p>

        <h3 id="mes-overview">MES 业务流转概览</h3>
        <p><strong>业务逻辑</strong>：生产执行从基础配置到工单完成的闭环流程。先维护工序与产线主数据，再创建工单并下发，现场通过报工录入产量，系统自动累加实际数量；当实际数量达到计划数量时工单完成，否则继续报工。工单下发触发 WMS 领料单创建，工单完成触发 QMS 质检任务创建。</p>
        <p><strong>解决方案</strong>：采用「配置先行、工单驱动、报工闭环」模式。工序与产线支持 CRUD 与编辑（编码不可改）；工单状态机控制流转；报工时校验工单/工序存在且工单已下发或进行中，报工后自动更新工单 actualQuantity 与 status，实现生产进度实时反馈。跨域通过 Feign 调用 WmsFeignClient、QmsFeignClient。</p>
        <p><strong>流程图描述</strong>：主流程：工序管理 → 产线管理 → 工单创建 → 工单下发（触发 WMS 领料）→ 报工录入 → 实际数量累加 → 工单完成（触发 QMS 质检）。工单状态：草稿(0) → 已下发(1) → 进行中(2) ⇄ 暂停(5) → 已完成(3)；报工后已下发自动转进行中；支持暂停/恢复；实际≥计划时可完成。</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">工单状态流转:
  草稿(0) ──release──► 已下发(1) ──报工──► 进行中(2) ──complete──► 已完成(3)
      │                    │                    │
      │                    ├─ 触发WMS领料单      ├─ pause ⇄ resume
      │                    │                    └─ 触发QMS质检任务
      │
跨域联动: 工单下发→WmsFeignClient.createRequisition(); 工单完成→QmsFeignClient.createInspectionTask()</pre>
        </div>

        <h3 id="mes-work-orders">工单管理</h3>
        <p><strong>业务逻辑</strong>：工单 CRUD、状态流转（草稿→已下发→进行中⇄暂停→已完成）、分页查询、计划数量与实际数量跟踪；支持暂停/恢复、手动完成并填写实际数量。</p>
        <p><strong>解决方案</strong>：WorkOrder 实体维护 status、plannedQuantity、actualQuantity；下发后状态变为已下发；报工自动累加 actualQuantity 并将已下发转为进行中；完成时可手动确认实际数量。</p>
        <p><strong>技术点</strong>：WorkOrder 实体；MyBatis-Plus 分页；GET /stats 统计各状态数量；POST /{id}/pause、/{id}/resume 暂停/恢复；GET /summary?date= 生产汇总；完成弹窗支持填写 actualQuantity。</p>

        <h3 id="mes-processes">工序管理</h3>
        <p><strong>业务逻辑</strong>：工序 CRUD、编码唯一、名称、排序号、工作中心；编辑时工序编码不可改；分页、创建时间展示、空状态提示。</p>
        <p><strong>解决方案</strong>：Process 实体；ProcessUpdateRequest 仅含 processName、sequence、workCenter，编码不可更新；前端编辑弹窗将 processCode 设为 readonly，提交时调用 PUT /api/processes/{id}。</p>
        <p><strong>技术点</strong>：Process 实体；ProcessApplicationService.update()；PUT /api/processes/{id}；前端编辑弹窗、formatTime 格式化时间、readonly 编码字段。</p>

        <h3 id="mes-lines">产线管理</h3>
        <p><strong>业务逻辑</strong>：产线 CRUD、编码唯一、名称、描述、排序号；编辑时产线编码不可改；分页、创建时间展示、空状态提示。</p>
        <p><strong>解决方案</strong>：ProductionLine 实体；ProductionLineUpdateRequest 仅含 lineName、description、sequence，编码不可更新；前端编辑弹窗将 lineCode 设为 readonly，提交时调用 PUT /api/lines/{id}。</p>
        <p><strong>技术点</strong>：ProductionLine 实体；ProductionLineApplicationService.update()；PUT /api/lines/{id}；前端编辑弹窗、formatTime、readonly 编码字段。</p>

        <h3 id="mes-reports">报工管理</h3>
        <p><strong>业务逻辑</strong>：报工录入（工单、工序、设备、报工数量、报废数量、操作员）；按工单筛选；设备名称展示（deviceCode - deviceName）；删除报工；空状态提示。报工后自动更新工单实际数量（累加报工减报废），工单由「已下发」自动转为「进行中」。</p>
        <p><strong>解决方案</strong>：创建时校验工单存在且状态为已下发或进行中、工序存在；报工后累加 actualQuantity = 原值 + reportQuantity - scrapQuantity；若工单为已下发则自动置为进行中并记录 startTime；设备名称通过 getDeviceList() 构建设备 ID→名称映射，前端展示 deviceCode - deviceName。</p>
        <p><strong>技术点</strong>：WorkReport 实体；WorkReportApplicationService 创建时校验；getDeviceList() 构建设备映射；orderId 筛选；工单 actualQuantity 累加、status 自动流转；@Transactional 保证报工与工单更新原子性。</p>
      </section>

      <section id="wms" class="help-section">
        <h2>仓储管理（WMS）</h2>
        <p>mom-wms 模块，独立库 ly_factmesh_wms，Flyway 管理表结构。核心能力：物料主数据、领料单、库存与出入库记录。</p>

        <h3 id="wms-overview">WMS 业务流转概览</h3>
        <div class="flow-diagram">
          <pre class="flow-mermaid">物料管理 ──► 库存初始化(调整) ──► 领料单创建 ──► 领料完成(扣库存) ──► 出入库记录
      │              │                    │                    │
      │              │                    ├─ MES工单下发Feign触发
      │              └── 安全库存预警 ◄─────┴────────────────────┘
      └── 删除前校验: 无库存、无领料引用</pre>
        </div>

        <h3 id="wms-materials">物料管理</h3>
        <p><strong>业务逻辑</strong>：</p>
        <ul>
          <li>物料 CRUD：创建（编码唯一）、更新（名称/类型/规格/单位）、删除</li>
          <li>分页查询：支持按物料编码、名称（模糊）、类型筛选</li>
          <li>删除校验：若物料存在库存记录或已被领料单引用，禁止删除</li>
        </ul>
        <p><strong>解决方案</strong>：MaterialApplicationService 注入 InventoryRepository、MaterialRequisitionRepository，删除前调用 existsDetailByMaterialId 与 findByMaterialId 校验。</p>
        <p><strong>技术点</strong>：Material 实体；MaterialUpdateRequest；MyBatis-Plus LambdaQueryWrapper 动态条件；GlobalExceptionHandler 统一 400 响应。</p>

        <h3 id="wms-requisitions">领料管理</h3>
        <p><strong>业务逻辑</strong>：</p>
        <ul>
          <li><strong>创建方式</strong>：① MES 工单发布时 Feign 调用创建（已提交）；② 手动创建草稿</li>
          <li><strong>状态流转</strong>：草稿(0) → 已提交(1) → 已完成(2)；支持取消(3)</li>
          <li><strong>完成领料</strong>：提交实发数量，校验实发≤申请、库存充足后扣减库存并记录出入库</li>
          <li><strong>退料</strong>：类型为退料时，完成时增加库存</li>
        </ul>
        <p><strong>领料单状态流转图</strong>：</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">  ┌─────────┐  submit   ┌─────────┐  complete  ┌─────────┐
  │  草稿   │ ────────► │ 已提交  │ ─────────► │ 已完成  │
  │  (0)    │           │  (1)    │            │  (2)    │
  └────┬────┘           └────┬────┘            └─────────┘
       │                     │
       │ cancel              │ cancel
       ▼                     ▼
  ┌─────────┐           ┌─────────┐
  │ 已取消  │ ◄─────────│ 已取消  │
  │  (3)    │           │  (3)    │
  └─────────┘           └─────────┘</pre>
        </div>
        <p><strong>MES 联动流程</strong>：工单发布 → WorkOrderApplicationService 调用 WmsFeignClient.createRequisition() → mom-wms 创建领料单（物料不存在则自动创建）→ 返回领料单 ID。</p>
        <p><strong>技术点</strong>：MaterialRequisition、MaterialRequisitionDetail；RequisitionManualCreateRequest；complete 前 getTotalQuantityByMaterialId 预校验；@Transactional 保证库存扣减与状态更新原子性。</p>

        <h3 id="wms-inventory">库存管理</h3>
        <p><strong>业务逻辑</strong>：</p>
        <ul>
          <li><strong>库存查询</strong>：按物料、仓库、批次号分页；按物料 ID 查全部库位</li>
          <li><strong>库存调整</strong>：正数入库、负数出库，支持 batchNo 指定批次；自动创建 inventory 记录并写入 inventory_transaction</li>
          <li><strong>盘点</strong>：POST /count 录入实盘数量，系统自动计算差异并调整库存，记录 TYPE_ADJUST 事务</li>
          <li><strong>安全库存</strong>：可设置 safe_stock；低于安全库存查询接口用于预警</li>
          <li><strong>出入库记录</strong>：分页查询某物料的出入库流水，含 total；记录 orderId/reqId 用于追溯</li>
          <li><strong>物料追溯</strong>：GET /trace 按物料、批次、工单、领料单多条件组合查询出入库记录</li>
        </ul>
        <p><strong>库存调整流程图</strong>：</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">POST /adjust (quantity) ──► 查找/创建 inventory(materialId,warehouse,location)
        │
        ├─ quantity &gt; 0 ──► 入库: quantity+=qty, 记录TYPE_INBOUND
        │
        └─ quantity &lt; 0 ──► 出库: 校验quantity+qty≥0, 扣减, 记录TYPE_OUTBOUND
                            │
                            └─ 不足则抛「库存不足」

POST /count (inventoryId, actualQuantity) ──► 计算 diff=实盘-账面
        │
        └─ diff≠0 ──► 更新 inventory.quantity=actualQuantity, 记录 TYPE_ADJUST</pre>
        </div>
        <p><strong>技术点</strong>：Inventory、InventoryTransaction 实体；findByMaterialAndLocation 支持空仓库/库位；findAllBelowSafeStock 条件 safe_stock&gt;0 AND quantity&lt;safe_stock；Page 分页返回；count 盘点确认自动生成调整事务。</p>
      </section>

      <section id="qms" class="help-section">
        <h2>质量管理（QMS）</h2>
        <p>mom-qms 模块，独立库 ly_factmesh_qms，Flyway 管理表结构。核心能力：质检任务、检验结果、不合格品（NCR）登记与处置。</p>

        <h3 id="qms-overview">QMS 业务流转概览</h3>
        <div class="flow-diagram">
          <pre class="flow-mermaid">质检任务创建 ──► 开始检验 ──► 录入检验结果(合格/不合格) ──► 完成质检
      │                    │                    │
      │ MES工单完成Feign触发│                    ├─ 存在不合格项 ──► 创建NCR ──► 选择处置方式 ──► 处置完成
      │                    │                    │   (返工/报废/让步接收/退货)
      │                    │                    └─ 无不合格项 ──► 直接完成
      │                    │
      │                    └─ 完成时: 填写检验员; 存在不合格项需勾选「强制完成」或先创建NCR</pre>
        </div>

        <h3 id="qms-inspection-tasks">质检任务</h3>
        <p><strong>业务逻辑</strong>：</p>
        <ul>
          <li><strong>创建</strong>：任务编码、工单 ID、物料 ID、检验类型（来料/过程/成品/出货）</li>
          <li><strong>状态流转</strong>：待检(0) → 检验中(1) → 已完成(2)；支持已关闭(3)</li>
          <li><strong>检验结果</strong>：检验中状态下可添加检验项（检验项、标准值、实际值、判定、检验员）；判定为不合格时需创建 NCR 或勾选强制完成才能完成任务</li>
          <li><strong>完成</strong>：填写检验员；存在不合格项时需勾选「强制完成」或先创建不合格品</li>
          <li><strong>统计</strong>：GET /stats 返回总数、待检、检验中、已完成数量</li>
        </ul>
        <p><strong>质检任务状态流转图</strong>：</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">  ┌─────────┐  start   ┌─────────┐  complete  ┌─────────┐
  │  待检   │ ───────► │ 检验中  │ ─────────► │ 已完成  │
  │  (0)    │          │  (1)    │             │  (2)    │
  └─────────┘          └────┬────┘             └─────────┘
                            │
                            │ 录入检验结果(合格/不合格)
                            │ 存在不合格 → 创建NCR或强制完成
                            ▼
                      ┌──────────┐
                      │ 检验结果  │
                      │ 弹窗录入  │
                      └──────────┘</pre>
        </div>
        <p><strong>解决方案</strong>：完成时校验检验结果中是否存在不合格项；若存在且未创建 NCR，则需传入 forceComplete=true 方可完成；支持从检验结果弹窗一键跳转创建 NCR（预填任务上下文）。</p>
        <p><strong>技术点</strong>：InspectionTask、InspectionResult 实体；InspectionTaskApplicationService.complete() 校验；getInspectionTaskStats、getNcrContext；前端完成弹窗、统计条、创建 NCR 跳转。</p>

        <h3 id="qms-ncr">不合格品（NCR）</h3>
        <p><strong>业务逻辑</strong>：</p>
        <ul>
          <li><strong>创建</strong>：产品编码、批次号、数量、不合格原因、处置方式（可选）、关联质检任务 ID；NCR 编号自动生成（NCR-{yyyyMMddHHmmss}）</li>
          <li><strong>从任务创建</strong>：质检任务存在不合格项时，可一键跳转 NCR 页面，预填产品编码、不合格原因、关联任务 ID</li>
          <li><strong>处置</strong>：待处理状态下点击「处置完成」，必须选择处置方式（返工/报废/让步接收/退货），可填写处置说明</li>
          <li><strong>分页</strong>：支持按处置状态、关联任务 ID 筛选</li>
        </ul>
        <p><strong>不合格品处置流程图</strong>：</p>
        <div class="flow-diagram">
          <pre class="flow-mermaid">创建NCR ──► 待处理(disposalResult=0)
    │
    │ 点击「处置完成」
    ▼
选择处置方式 ──► 返工(1)/报废(2)/让步接收(3)/退货(4)
    │
    ▼
填写处置说明(可选) ──► 提交 ──► 已处理(disposalResult=1), 记录dispose_time</pre>
        </div>
        <p><strong>解决方案</strong>：NCR 编号采用时间戳前缀保证唯一；处置时 NcrDisposeRequest 必填 disposalMethod，后端更新实体并记录处置时间；前端支持 ?fromTask=id 预填创建表单。</p>
        <p><strong>技术点</strong>：NonConformingProduct 实体；ncr_no 唯一索引；NonConformingProductApplicationService.dispose(id, request)；getInspectionTaskNcrContext 预填；前端处置弹窗、NCR 编号列展示。</p>
      </section>

      <section id="reports" class="help-section">
        <h2>报表看板</h2>

        <h3 id="reports-production">生产日报</h3>
        <p><strong>业务逻辑</strong>：按日统计产量、良品率等。</p>
        <p><strong>解决方案</strong>：占位页；后续聚合 MES 报工数据，按日期、工单、工序维度汇总 reportQuantity、scrapQuantity，计算良品率。</p>
        <p><strong>技术点</strong>：占位页；需聚合 MES 报工数据。</p>

        <h3 id="reports-oee">设备 OEE</h3>
        <p><strong>业务逻辑</strong>：设备综合效率 = 可用率 × 性能率 × 良品率。</p>
        <p><strong>解决方案</strong>：占位页；后续聚合 IoT 设备运行时长、故障时长与 MES 产量，计算 OEE 三要素并展示趋势。</p>
        <p><strong>技术点</strong>：占位页；需聚合 IoT 设备运行数据与 MES 产量。</p>
      </section>

      <section id="monitor" class="help-section">
        <h2>系统监控</h2>

        <h3 id="monitor-services">服务状态</h3>
        <p><strong>业务逻辑</strong>：展示各微服务健康状态、实例列表。</p>
        <p><strong>解决方案</strong>：通过 Nacos 服务发现获取各服务实例；调用 /actuator/health 获取健康状态；前端展示 UP/DOWN 与实例详情。</p>
        <p><strong>技术点</strong>：通过 Nacos 服务发现获取实例；调用各服务 /actuator/health。</p>

        <h3 id="monitor-docs">接口文档</h3>
        <p><strong>业务逻辑</strong>：OpenAPI 聚合文档，统一调试接口。</p>
        <p><strong>解决方案</strong>：各服务暴露 /v3/api-docs；网关聚合至 /doc.html；Knife4j/Swagger UI 展示，支持按服务切换查看。</p>
        <p><strong>技术点</strong>：各服务暴露 /v3/api-docs；网关聚合至 /doc.html；Knife4j/Swagger UI 展示。</p>
      </section>

      <section id="tech" class="help-section">
        <h2>通用技术要点</h2>
        <ul>
          <li><strong>分页</strong>：统一格式 { records, total, current, size }，与 MyBatis-Plus Page 一致。</li>
          <li><strong>鉴权</strong>：JWT Token 存 localStorage；请求头 Authorization: Bearer &lt;token&gt;。</li>
          <li><strong>网关路由</strong>：/api/auth/**、/api/users/** 等按路径前缀转发至对应服务。</li>
          <li><strong>跨域</strong>：前端 Vite 代理 /api 到网关；网关到各服务通过 lb:// 负载均衡。</li>
          <li><strong>Flyway</strong>：各模块 db/migration 下 SQL 自动执行，版本号递增。</li>
          <li><strong>基础设施</strong>：mom-infra 提供 CacheService、MqttClientWrapper、OpcUaClient、ModbusTcpClient 等接口；配置对应属性后按需启用。</li>
        </ul>
      </section>
    </article>
  </section>
</template>

<script setup lang="ts">
import { menuConfig } from '@/config/menu';
import SwimlaneDiagram from '@/components/SwimlaneDiagram.vue';

const toc = [
  {
    id: 'intro',
    name: '系统概述',
    children: [
      { id: 'intro-swimlane', name: '系统泳道流程图' },
      { id: 'flow-user', name: '一、用户进入' },
      { id: 'flow-device', name: '二、设备上报' },
      { id: 'flow-alert', name: '三、告警触发' },
      { id: 'flow-workorder', name: '四、工单流转' },
      { id: 'flow-cross', name: '五、跨域触发条件' },
      { id: 'intro-flow', name: '系统运转流程' }
    ]
  },
  { id: 'infra', name: '基础设施（Infra）', children: [{ id: 'infra-overview', name: '能力概览' }, { id: 'infra-cache', name: '缓存' }, { id: 'infra-mqtt', name: '消息队列' }, { id: 'infra-protocol', name: '工业协议' }] },
  { id: 'gateway', name: '网关（Gateway）', children: [{ id: 'gateway-overview', name: '网关概览' }, { id: 'gateway-routes', name: '路由规则' }] },
  ...menuConfig.map((g) => {
    const base = { id: g.id, name: g.name, children: g.children?.map((c) => ({ id: c.id, name: c.name })) };
    if (g.id === 'admin') {
      base.children = [
        { id: 'admin-overview', name: 'Admin 业务流转概览' },
        ...(base.children || [])
      ];
    }
    if (g.id === 'iot') {
      base.children = [
        { id: 'iot-overview', name: 'IoT 业务流转概览' },
        ...(base.children || []),
        { id: 'iot-rule-engine', name: '规则引擎' }
      ];
    }
    if (g.id === 'wms') {
      base.children = [
        { id: 'wms-overview', name: 'WMS 业务流转概览' },
        ...(base.children || []),
        { id: 'wms-api', name: 'WMS API 汇总' }
      ];
    }
    if (g.id === 'mes') {
      base.children = [
        { id: 'mes-overview', name: 'MES 业务流转概览' },
        ...(base.children || [])
      ];
    }
    return base;
  }),
  { id: 'tech', name: '通用技术要点', children: null }
];
</script>

<style scoped>
.help-page { max-width: 900px; margin: 0 auto; }
.help-title { font-size: 1.75rem; margin: 0 0 0.25rem; color: #e5e7eb; }
.help-desc { font-size: 0.95rem; color: #94a3b8; margin-bottom: 2rem; }

.help-toc {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  margin-bottom: 2rem;
}
.toc-group { margin-bottom: 0.5rem; }
.toc-link { display: block; color: #38bdf8; text-decoration: none; font-weight: 500; margin-bottom: 0.25rem; }
.toc-link:hover { text-decoration: underline; }
.toc-child { display: block; color: #94a3b8; text-decoration: none; font-size: 0.9rem; margin-left: 1rem; margin-bottom: 0.2rem; }
.toc-child:hover { color: #e5e7eb; }

.flow-diagram {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 1.5rem;
  margin: 1rem 0;
  overflow-x: auto;
}
.flow-diagram :deep(svg) { max-width: 100%; height: auto; }
.flow-mermaid {
  margin: 0;
  padding: 1rem;
  font-family: "Sarasa Mono SC", "Cascadia Code", "JetBrains Mono", "Consolas", "Monaco", ui-monospace, monospace;
  font-size: 0.85rem;
  color: #94a3b8;
  line-height: 1.6;
  white-space: pre;
  overflow-x: auto;
}

.help-content { color: #e5e7eb; line-height: 1.6; }
.help-section { margin-bottom: 2.5rem; }
.help-section h2 {
  font-size: 1.35rem;
  color: #38bdf8;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #334155;
}
.help-section h3 {
  font-size: 1.1rem;
  color: #e5e7eb;
  margin: 1.25rem 0 0.5rem;
  scroll-margin-top: 1rem;
}
.help-section p { margin: 0.5rem 0; color: #cbd5e1; }
.help-section ul { margin: 0.5rem 0; padding-left: 1.5rem; }
.help-section li { margin: 0.25rem 0; }

.help-table-wrap { overflow-x: auto; margin: 1rem 0; }
.help-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}
.help-table th, .help-table td {
  padding: 0.5rem 0.75rem;
  text-align: left;
  border: 1px solid #334155;
}
.help-table th { background: #1e293b; color: #38bdf8; }
.help-table td { color: #cbd5e1; }

.flow-legend {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #1e293b;
  border-radius: 8px;
  border: 1px solid #334155;
}
.flow-legend h4 { font-size: 1rem; color: #38bdf8; margin: 1rem 0 0.5rem; }
.flow-legend h4:first-child { margin-top: 0; }
.flow-legend ul { margin: 0.5rem 0; padding-left: 1.5rem; }
.flow-legend li { margin: 0.25rem 0; color: #cbd5e1; }
.flow-legend .help-table { margin-top: 0.5rem; }
</style>
