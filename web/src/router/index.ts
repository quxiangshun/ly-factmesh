import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { getToken } from '@/api/auth';
import LayoutWithSidebar from '@/components/LayoutWithSidebar.vue';

const Placeholder = () => import('../views/dashboard/PlaceholderView.vue');

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: LayoutWithSidebar,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/HomeView.vue'), meta: { title: '首页概览' } },
      { path: 'dashboard/bigscreen', name: 'DashboardBigScreen', component: () => import('../views/dashboard/BigScreenView.vue'), meta: { title: '可视化大屏', desc: '系统数据大屏展示' } },
      { path: 'dashboard/todos', name: 'DashboardTodos', component: Placeholder, meta: { title: '待办任务', desc: '工单待办、质检待办、告警待办' } },
      { path: 'admin/users', name: 'AdminUsers', component: () => import('../views/admin/UsersView.vue'), meta: { title: '用户管理' } },
      { path: 'admin/roles', name: 'AdminRoles', component: () => import('../views/admin/RolesView.vue'), meta: { title: '角色管理' } },
      { path: 'admin/permissions', name: 'AdminPermissions', component: () => import('../views/admin/PermissionsView.vue'), meta: { title: '权限管理' } },
      { path: 'admin/dicts', name: 'AdminDicts', component: () => import('../views/admin/DictsView.vue'), meta: { title: '字典管理' } },
      { path: 'admin/configs', name: 'AdminConfigs', component: () => import('../views/admin/ConfigsView.vue'), meta: { title: '系统配置' } },
      { path: 'admin/tenants', name: 'AdminTenants', component: () => import('../views/admin/TenantsView.vue'), meta: { title: '租户管理' } },
      { path: 'admin/operation-logs', name: 'AdminOperationLogs', component: () => import('../views/admin/OperationLogsView.vue'), meta: { title: '操作日志' } },
      { path: 'admin/audit-logs', name: 'AdminAuditLogs', component: () => import('../views/admin/AuditLogsView.vue'), meta: { title: '审计日志' } },
      { path: 'iot/devices', name: 'IotDevices', component: () => import('../views/iot/DevicesView.vue'), meta: { title: '设备管理' } },
      { path: 'iot/telemetry', name: 'IotTelemetry', component: () => import('../views/iot/TelemetryView.vue'), meta: { title: '设备遥测' } },
      { path: 'iot/alerts', name: 'IotAlerts', component: () => import('../views/iot/AlertsView.vue'), meta: { title: '设备告警' } },
      { path: 'mes/work-orders', name: 'MesWorkOrders', component: () => import('../views/mes/WorkOrdersView.vue'), meta: { title: '工单管理' } },
      { path: 'mes/processes', name: 'MesProcesses', component: () => import('../views/mes/ProcessesView.vue'), meta: { title: '工序管理' } },
      { path: 'mes/lines', name: 'MesLines', component: () => import('../views/mes/LinesView.vue'), meta: { title: '产线管理' } },
      { path: 'mes/reports', name: 'MesReports', component: () => import('../views/mes/ReportsView.vue'), meta: { title: '报工管理' } },
      { path: 'wms/materials', name: 'WmsMaterials', component: () => import('../views/wms/MaterialsView.vue'), meta: { title: '物料管理' } },
      { path: 'wms/requisitions', name: 'WmsRequisitions', component: () => import('../views/wms/RequisitionsView.vue'), meta: { title: '领料单', desc: '生产领料/退料（MES 联动）' } },
      { path: 'wms/inventory', name: 'WmsInventory', component: () => import('../views/wms/InventoryView.vue'), meta: { title: '库存管理', desc: '库存查询、预警、盘点' } },
      { path: 'wms/trace', name: 'WmsTrace', component: () => import('../views/wms/WmsTraceView.vue'), meta: { title: '物料追溯', desc: '按物料/批次/工单/领料单追溯' } },
      { path: 'qms/inspection-tasks', name: 'QmsInspectionTasks', component: () => import('../views/qms/InspectionTasksView.vue'), meta: { title: '质检任务' } },
      { path: 'qms/inspection-results', name: 'QmsInspectionResults', component: () => import('../views/qms/InspectionResultsView.vue'), meta: { title: '质检结果', desc: '检验项录入与质量判定' } },
      { path: 'qms/ncr', name: 'QmsNcr', component: () => import('../views/qms/NcrView.vue'), meta: { title: '不合格品处理' } },
      { path: 'qms/quality-trace', name: 'QmsQualityTrace', component: () => import('../views/qms/QualityTraceView.vue'), meta: { title: '质量追溯', desc: '关联工单/设备/物料' } },
      { path: 'ops/mqtt', name: 'OpsMqtt', component: () => import('../views/ops/MqttClientView.vue'), meta: { title: 'MQTT 客户端', desc: '订阅、发布、接收 MQTT 消息' } },
      { path: 'ops/redis', name: 'OpsRedis', component: () => import('../views/ops/RedisClientView.vue'), meta: { title: 'Redis 客户端', desc: '连接任意 Redis，执行命令' } },
      { path: 'ops/influxdb', name: 'OpsInfluxDb', component: () => import('../views/ops/InfluxDbClientView.vue'), meta: { title: 'InfluxDB 管理', desc: '连接 InfluxDB 2.x，执行 Flux 查询' } },
      { path: 'ops/pg', name: 'OpsPg', component: () => import('../views/ops/PgAdminView.vue'), meta: { title: 'PG 管理', desc: 'PostgreSQL 数据库查询' } },
      { path: 'ops/global-logs', name: 'OpsGlobalLogs', component: () => import('../views/ops/GlobalLogsView.vue'), meta: { title: '全局日志', desc: '系统级日志' } },
      { path: 'ops/audit-logs', name: 'OpsAuditLogs', component: () => import('../views/ops/OpsAuditLogsView.vue'), meta: { title: '运维审计', desc: '运维审计记录' } },
      { path: 'ops/system-events', name: 'OpsSystemEvents', component: () => import('../views/ops/OpsSystemEventsView.vue'), meta: { title: '系统事件', desc: '系统级事件' } },
      { path: 'reports/production-daily', name: 'ReportsProduction', component: () => import('../views/reports/ProductionDailyView.vue'), meta: { title: '生产日报', desc: '生产统计' } },
      { path: 'reports/equipment-oee', name: 'ReportsOee', component: () => import('../views/reports/EquipmentOeeView.vue'), meta: { title: '设备 OEE', desc: '设备综合效率' } },
      { path: 'monitor/services', name: 'MonitorServices', component: () => import('../views/monitor/ServicesView.vue'), meta: { title: '服务状态' } },
      { path: 'simulator/data', name: 'SimulatorData', component: () => import('../views/simulator/SimulatorView.vue'), meta: { title: '工业协议模拟', desc: 'OPC UA、Modbus TCP 模拟数据，仅开发环境' } },
      { path: 'help', name: 'Help', component: () => import('../views/HelpView.vue'), meta: { title: '帮助文档' } }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  const hasToken = !!getToken();
  if (to.meta.public) {
    if (hasToken && to.name === 'Login') {
      next({ path: '/dashboard' });
    } else {
      next();
    }
  } else {
    if (!hasToken) {
      next({ name: 'Login', query: to.path !== '/' ? { redirect: to.fullPath } : {} });
    } else {
      next();
    }
  }
});

export default router;

