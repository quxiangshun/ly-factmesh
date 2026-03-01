# 企业级 MOM 菜单结构

根据微服务划分的完整菜单树，用于前端侧边栏、权限配置与导航。

---

## 一级菜单总览

| 序号 | 菜单名称 | 微服务 | 路由 | 图标建议 |
|------|----------|--------|------|----------|
| 1 | 工作台 | - | /dashboard | mdi:view-dashboard |
| 2 | 系统管理 | mom-admin | /admin | mdi:cog |
| 3 | 设备物联 | mom-iot | /iot | mdi:factory |
| 4 | 生产执行 | mom-mes | /mes | mdi:clipboard-list |
| 5 | 仓储管理 | mom-wms | /wms | mdi:warehouse |
| 6 | 质量管理 | mom-qms | /qms | mdi:quality-high |
| 7 | 报表看板 | 跨域 | /reports | mdi:chart-box |
| 8 | 系统监控 | 跨域 | /monitor | mdi:monitor-dashboard |

---

## 1. 工作台 /dashboard

| 菜单名称 | 路由 | 说明 |
|----------|------|------|
| 首页概览 | /dashboard | 生产概况、待办、快捷入口 |
| 待办任务 | /dashboard/todos | 工单待办、质检待办、告警待办 |

---

## 2. 系统管理 /admin（mom-admin）

| 二级菜单 | 路由 | API 路径 | 说明 |
|----------|------|----------|------|
| 用户管理 | /admin/users | /api/users | 用户 CRUD、分配角色 |
| 角色管理 | /admin/roles | /api/roles | 角色 CRUD、分配权限 |
| 权限管理 | /admin/permissions | /api/permissions | 权限 CRUD、权限树 |
| 字典管理 | /admin/dicts | /api/dicts | 数据字典维护 |
| 系统配置 | /admin/configs | /api/configs | 系统参数配置 |
| 操作日志 | /admin/logs | /api/logs | 操作审计日志（待实现） |
| 组织架构 | /admin/org | /api/org | 组织/部门/岗位（待实现） |
| 租户管理 | /admin/tenants | /api/tenants | 多租户（待实现） |

---

## 3. 设备物联 /iot（mom-iot）

| 二级菜单 | 路由 | API 路径 | 说明 |
|----------|------|----------|------|
| 设备管理 | /iot/devices | /api/devices | 设备注册、上下线、状态 |
| 设备遥测 | /iot/telemetry | /api/devices/telemetry | 采集数据、历史曲线 |
| 设备告警 | /iot/alerts | /api/devices/alerts | 告警规则、告警记录 |
| 采集配置 | /iot/collect | /api/devices/collect | 采集任务配置（待实现） |
| 远程控制 | /iot/control | /api/devices/control | 设备指令下发（待实现） |

---

## 4. 生产执行 /mes（mom-mes）

| 二级菜单 | 路由 | API 路径 | 说明 |
|----------|------|----------|------|
| 工单管理 | /mes/work-orders | /api/work-orders | 工单创建、下发、状态流转 |
| 工序管理 | /mes/processes | /api/processes | 工序定义（待实现） |
| 产线管理 | /mes/lines | /api/lines | 产线、工位（待实现） |
| 报工管理 | /mes/reports | /api/work-reports | 报工录入（待实现） |
| 生产进度 | /mes/progress | /api/progress | 进度看板（待实现） |
| 排产计划 | /mes/scheduling | /api/scheduling | 排产（待实现） |

---

## 5. 仓储管理 /wms（mom-wms）

| 二级菜单 | 路由 | API 路径 | 说明 |
|----------|------|----------|------|
| 物料管理 | /wms/materials | /api/materials | 物料主数据 |
| 仓库管理 | /wms/warehouses | /api/warehouses | 仓库、库位（待实现） |
| 库存管理 | /wms/inventory | /api/inventory | 库存查询、预警（待实现） |
| 入库管理 | /wms/inbound | /api/inbound | 入库单（待实现） |
| 出库管理 | /wms/outbound | /api/outbound | 出库单（待实现） |
| 领料单 | /wms/requisitions | /api/requisitions | 领料单（MES 联动） |
| 盘点管理 | /wms/stocktaking | /api/stocktaking | 盘点（待实现） |
| 批次追溯 | /wms/batches | /api/batches | 批次追溯（待实现） |

---

## 6. 质量管理 /qms（mom-qms）

| 二级菜单 | 路由 | API 路径 | 说明 |
|----------|------|----------|------|
| 质检任务 | /qms/inspection-tasks | /api/inspection-tasks | 质检任务创建、执行 |
| 检验计划 | /qms/inspection-plans | /api/inspection-plans | 检验项、标准（待实现） |
| 不合格处理 | /qms/ncr | /api/ncr | 不合格品处理（待实现） |
| 质量追溯 | /qms/traceability | /api/traceability | 质量追溯（待实现） |

---

## 7. 报表看板 /reports（跨域聚合）

| 二级菜单 | 路由 | 说明 |
|----------|------|------|
| 生产日报 | /reports/production-daily | 生产统计 |
| 设备 OEE | /reports/equipment-oee | 设备综合效率 |
| 质量趋势 | /reports/quality-trend | 质量指标趋势 |
| 库存报表 | /reports/inventory | 库存统计 |

---

## 8. 系统监控 /monitor（跨域）

| 二级菜单 | 路由 | 说明 |
|----------|------|------|
| 服务状态 | /monitor/services | 微服务健康、Nacos 注册 |
| 接口文档 | /monitor/docs | 跳转 /doc.html |
| 日志查询 | /monitor/logs | 集中日志（待实现） |

---

## 菜单 JSON 结构示例（用于前端/权限表）

```json
[
  { "id": 1, "name": "工作台", "path": "/dashboard", "icon": "mdi:view-dashboard", "children": [
    { "id": 11, "name": "首页概览", "path": "/dashboard" },
    { "id": 12, "name": "待办任务", "path": "/dashboard/todos" }
  ]},
  { "id": 2, "name": "系统管理", "path": "/admin", "icon": "mdi:cog", "children": [
    { "id": 21, "name": "用户管理", "path": "/admin/users", "perm": "user:list" },
    { "id": 22, "name": "角色管理", "path": "/admin/roles", "perm": "role:list" },
    { "id": 23, "name": "权限管理", "path": "/admin/permissions", "perm": "permission:list" },
    { "id": 24, "name": "字典管理", "path": "/admin/dicts", "perm": "dict:list" },
    { "id": 25, "name": "系统配置", "path": "/admin/configs", "perm": "config:list" }
  ]},
  { "id": 3, "name": "设备物联", "path": "/iot", "icon": "mdi:factory", "children": [
    { "id": 31, "name": "设备管理", "path": "/iot/devices", "perm": "device:list" },
    { "id": 32, "name": "设备遥测", "path": "/iot/telemetry", "perm": "device:telemetry" },
    { "id": 33, "name": "设备告警", "path": "/iot/alerts", "perm": "device:alert" }
  ]},
  { "id": 4, "name": "生产执行", "path": "/mes", "icon": "mdi:clipboard-list", "children": [
    { "id": 41, "name": "工单管理", "path": "/mes/work-orders", "perm": "workorder:list" }
  ]},
  { "id": 5, "name": "仓储管理", "path": "/wms", "icon": "mdi:warehouse", "children": [
    { "id": 51, "name": "物料管理", "path": "/wms/materials", "perm": "material:list" },
    { "id": 52, "name": "领料单", "path": "/wms/requisitions", "perm": "requisition:list" }
  ]},
  { "id": 6, "name": "质量管理", "path": "/qms", "icon": "mdi:quality-high", "children": [
    { "id": 61, "name": "质检任务", "path": "/qms/inspection-tasks", "perm": "inspection:list" }
  ]},
  { "id": 7, "name": "报表看板", "path": "/reports", "icon": "mdi:chart-box", "children": [
    { "id": 71, "name": "生产日报", "path": "/reports/production-daily" },
    { "id": 72, "name": "设备 OEE", "path": "/reports/equipment-oee" }
  ]},
  { "id": 8, "name": "系统监控", "path": "/monitor", "icon": "mdi:monitor-dashboard", "children": [
    { "id": 81, "name": "服务状态", "path": "/monitor/services" },
    { "id": 82, "name": "接口文档", "path": "/doc.html", "external": true }
  ]}
]
```

---

## 微服务与菜单对应关系

| 微服务 | 一级菜单 | 已实现 API | 待实现 |
|--------|----------|------------|--------|
| mom-admin | 系统管理 | users, roles, permissions, dicts, configs, auth | logs, org, tenants |
| mom-iot | 设备物联 | devices, telemetry, alerts | collect, control |
| mom-mes | 生产执行 | work-orders | processes, lines, reports, progress, scheduling |
| mom-wms | 仓储管理 | materials, requisitions | warehouses, inventory, inbound, outbound, stocktaking, batches |
| mom-qms | 质量管理 | inspection-tasks | inspection-plans, ncr, traceability |
| 跨域 | 报表看板 | - | 全部 |
| 跨域 | 系统监控 | - | services, logs |

---

*文档版本：v1.0，随功能迭代更新*
