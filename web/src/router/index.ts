import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { getToken } from '@/api/auth';
import LayoutWithSidebar from '@/components/LayoutWithSidebar.vue';

const Placeholder = () => import('../views/PlaceholderView.vue');

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
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/HomeView.vue'), meta: { title: '首页概览' } },
      { path: 'dashboard/todos', name: 'DashboardTodos', component: Placeholder, meta: { title: '待办任务', desc: '工单待办、质检待办、告警待办' } },
      { path: 'admin/users', name: 'AdminUsers', component: () => import('../views/UsersView.vue'), meta: { title: '用户管理' } },
      { path: 'admin/roles', name: 'AdminRoles', component: () => import('../views/RolesView.vue'), meta: { title: '角色管理' } },
      { path: 'admin/permissions', name: 'AdminPermissions', component: () => import('../views/PermissionsView.vue'), meta: { title: '权限管理' } },
      { path: 'admin/dicts', name: 'AdminDicts', component: () => import('../views/DictsView.vue'), meta: { title: '字典管理' } },
      { path: 'admin/configs', name: 'AdminConfigs', component: () => import('../views/ConfigsView.vue'), meta: { title: '系统配置' } },
      { path: 'admin/logs', name: 'AdminLogs', component: Placeholder, meta: { title: '操作日志', desc: '操作审计日志' } },
      { path: 'iot/devices', name: 'IotDevices', component: () => import('../views/DevicesView.vue'), meta: { title: '设备管理' } },
      { path: 'iot/telemetry', name: 'IotTelemetry', component: Placeholder, meta: { title: '设备遥测', desc: '采集数据、历史曲线' } },
      { path: 'iot/alerts', name: 'IotAlerts', component: Placeholder, meta: { title: '设备告警', desc: '告警规则、告警记录' } },
      { path: 'mes/work-orders', name: 'MesWorkOrders', component: () => import('../views/WorkOrdersView.vue'), meta: { title: '工单管理' } },
      { path: 'mes/processes', name: 'MesProcesses', component: Placeholder, meta: { title: '工序管理', desc: '工序定义' } },
      { path: 'mes/lines', name: 'MesLines', component: Placeholder, meta: { title: '产线管理', desc: '产线、工位' } },
      { path: 'mes/reports', name: 'MesReports', component: Placeholder, meta: { title: '报工管理', desc: '报工录入' } },
      { path: 'wms/materials', name: 'WmsMaterials', component: () => import('../views/MaterialsView.vue'), meta: { title: '物料管理' } },
      { path: 'wms/requisitions', name: 'WmsRequisitions', component: Placeholder, meta: { title: '领料单', desc: '领料单（MES 联动）' } },
      { path: 'wms/inventory', name: 'WmsInventory', component: Placeholder, meta: { title: '库存管理', desc: '库存查询、预警' } },
      { path: 'qms/inspection-tasks', name: 'QmsInspectionTasks', component: () => import('../views/InspectionTasksView.vue'), meta: { title: '质检任务' } },
      { path: 'reports/production-daily', name: 'ReportsProduction', component: Placeholder, meta: { title: '生产日报', desc: '生产统计' } },
      { path: 'reports/equipment-oee', name: 'ReportsOee', component: Placeholder, meta: { title: '设备 OEE', desc: '设备综合效率' } },
      { path: 'monitor/services', name: 'MonitorServices', component: () => import('../views/ServicesView.vue'), meta: { title: '服务状态' } }
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

