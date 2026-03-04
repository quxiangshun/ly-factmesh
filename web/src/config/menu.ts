/**
 * 企业级 MOM 菜单配置（与 README 功能对应）
 */
export interface MenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  children?: MenuItem[];
  external?: boolean;
}

export const menuConfig: MenuItem[] = [
  {
    id: 'dashboard',
    name: '工作台',
    path: '/dashboard',
    icon: 'mdi:view-dashboard',
    children: [
      { id: 'dashboard-home', name: '首页概览', path: '/dashboard' },
      { id: 'dashboard-todos', name: '待办任务', path: '/dashboard/todos' }
    ]
  },
  {
    id: 'admin',
    name: '系统管理 (Admin)',
    path: '/admin',
    icon: 'mdi:cog',
    children: [
      { id: 'admin-users', name: '用户管理', path: '/admin/users' },
      { id: 'admin-tenants', name: '租户管理', path: '/admin/tenants' },
      { id: 'admin-roles', name: '角色管理', path: '/admin/roles' },
      { id: 'admin-permissions', name: '权限管理', path: '/admin/permissions' },
      { id: 'admin-dicts', name: '字典管理', path: '/admin/dicts' },
      { id: 'admin-configs', name: '系统配置', path: '/admin/configs' },
      { id: 'admin-operation-logs', name: '操作日志', path: '/admin/operation-logs' },
      { id: 'admin-audit-logs', name: '审计日志', path: '/admin/audit-logs' }
    ]
  },
  {
    id: 'iot',
    name: '设备物联 (IoT)',
    path: '/iot',
    icon: 'mdi:factory',
    children: [
      { id: 'iot-devices', name: '设备管理', path: '/iot/devices' },
      { id: 'iot-telemetry', name: '设备遥测', path: '/iot/telemetry' },
      { id: 'iot-alerts', name: '设备告警', path: '/iot/alerts' }
    ]
  },
  {
    id: 'mes',
    name: '生产执行 (MES)',
    path: '/mes',
    icon: 'mdi:clipboard-list',
    children: [
      { id: 'mes-work-orders', name: '工单管理', path: '/mes/work-orders' },
      { id: 'mes-processes', name: '工序管理', path: '/mes/processes' },
      { id: 'mes-lines', name: '产线管理', path: '/mes/lines' },
      { id: 'mes-reports', name: '报工管理', path: '/mes/reports' }
    ]
  },
  {
    id: 'wms',
    name: '仓储管理 (WMS)',
    path: '/wms',
    icon: 'mdi:warehouse',
    children: [
      { id: 'wms-materials', name: '物料管理', path: '/wms/materials' },
      { id: 'wms-requisitions', name: '领料单', path: '/wms/requisitions' },
      { id: 'wms-inventory', name: '库存管理', path: '/wms/inventory' },
      { id: 'wms-trace', name: '物料追溯', path: '/wms/trace' }
    ]
  },
  {
    id: 'qms',
    name: '质量管理 (QMS)',
    path: '/qms',
    icon: 'mdi:quality-high',
    children: [
      { id: 'qms-inspection-tasks', name: '质检任务', path: '/qms/inspection-tasks' },
      { id: 'qms-inspection-results', name: '质检结果', path: '/qms/inspection-results' },
      { id: 'qms-ncr', name: '不合格品处理 (NCR)', path: '/qms/ncr' },
      { id: 'qms-quality-trace', name: '质量追溯', path: '/qms/quality-trace' }
    ]
  },
  {
    id: 'reports',
    name: '报表看板',
    path: '/reports',
    icon: 'mdi:chart-box',
    children: [
      { id: 'reports-production', name: '生产日报', path: '/reports/production-daily' },
      { id: 'reports-oee', name: '设备 OEE', path: '/reports/equipment-oee' }
    ]
  },
  {
    id: 'ops',
    name: '运维管理 (Ops)',
    path: '/ops',
    icon: 'mdi:server',
    children: [
      { id: 'ops-mqtt', name: 'MQTT 客户端', path: '/ops/mqtt' },
      { id: 'ops-redis', name: 'Redis 客户端', path: '/ops/redis' },
      { id: 'ops-influxdb', name: 'InfluxDB 管理', path: '/ops/influxdb' },
      { id: 'ops-pg', name: 'PG 管理', path: '/ops/pg' },
      { id: 'ops-global-logs', name: '全局日志', path: '/ops/global-logs' },
      { id: 'ops-audit-logs', name: '运维审计', path: '/ops/audit-logs' },
      { id: 'ops-system-events', name: '系统事件', path: '/ops/system-events' }
    ]
  },
  {
    id: 'monitor',
    name: '系统监控 (Monitor)',
    path: '/monitor',
    icon: 'mdi:monitor-dashboard',
    children: [
      { id: 'monitor-services', name: '服务状态', path: '/monitor/services' },
      { id: 'monitor-docs', name: '接口文档', path: '/doc.html', external: true }
    ]
  }
];
