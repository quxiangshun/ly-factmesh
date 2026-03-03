<template>
  <section class="page">
    <h1 class="page-title">设备告警</h1>
    <p class="page-desc">告警记录、待处理告警、阈值规则自动告警</p>
    <div class="stats-bar" v-if="pendingCount !== null && tab !== 'rules'">
      <span class="pending">待处理 {{ pendingCount }} 条</span>
    </div>
    <div class="toolbar">
      <div class="tabs">
        <button type="button" class="tab" :class="{ active: tab === 'pending' }" @click="tab = 'pending'">待处理</button>
        <button type="button" class="tab" :class="{ active: tab === 'all' }" @click="tab = 'all'">全部</button>
        <button type="button" class="tab" :class="{ active: tab === 'rules' }" @click="tab = 'rules'">告警规则</button>
      </div>
      <button v-if="tab === 'rules'" type="button" class="btn primary" @click="openCreateRule">新建规则</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else-if="tab === 'rules'">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>规则名称</th>
              <th>设备/类型</th>
              <th>测点</th>
              <th>条件</th>
              <th>告警类型</th>
              <th>级别</th>
              <th>冷却(秒)</th>
              <th>启用</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in rules" :key="r.id">
              <td>{{ r.ruleName || '-' }}</td>
              <td>{{ r.deviceId ? `设备#${r.deviceId}` : (r.deviceType || '全部') }}</td>
              <td>{{ r.fieldName }}</td>
              <td>{{ r.operator }} {{ r.thresholdValue }}</td>
              <td>{{ r.alertType }}</td>
              <td><span class="level" :class="'level-' + r.alertLevel">L{{ r.alertLevel }}</span></td>
              <td>{{ r.cooldownSeconds }}</td>
              <td>{{ r.enabled ? '是' : '否' }}</td>
              <td>
                <button type="button" class="btn small" @click="openEditRule(r)">编辑</button>
                <button type="button" class="btn small danger" @click="doDeleteRule(r.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <button type="button" class="btn small" :disabled="rulePage <= 1" @click="rulePage--">上一页</button>
        <span class="page-info">第 {{ rulePage }} 页</span>
        <button type="button" class="btn small" :disabled="rules.length < pageSize" @click="rulePage++">下一页</button>
      </div>
      <p v-if="rules.length === 0 && !loading" class="empty-msg">暂无规则，遥测数据满足阈值时将自动创建告警</p>
    </template>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>设备</th>
              <th>类型</th>
              <th>级别</th>
              <th>内容</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in alerts" :key="a.id" :class="{ 'row-pending': a.alertStatus === 0 }">
              <td>{{ a.deviceCode || a.deviceId }}</td>
              <td>{{ a.alertType }}</td>
              <td><span class="level" :class="'level-' + a.alertLevel">L{{ a.alertLevel }}</span></td>
              <td>{{ a.alertContent }}</td>
              <td>{{ a.alertStatus === 0 ? '待处理' : '已处理' }}</td>
              <td>{{ a.createTime }}</td>
              <td>
                <button v-if="a.alertStatus === 0" type="button" class="btn small" @click="resolveAlert(a.id)">处理</button>
                <span v-else class="resolved">{{ a.resolveTime }} {{ a.resolvedBy ? `by ${a.resolvedBy}` : '' }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <button type="button" class="btn small" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
        <span class="page-info">第 {{ currentPage }} 页</span>
        <button type="button" class="btn small" :disabled="alerts.length < pageSize" @click="currentPage++">下一页</button>
      </div>
      <p v-if="alerts.length === 0 && !loading" class="empty-msg">暂无告警</p>
    </template>
    <div v-if="showRuleModal" class="modal-mask" @click.self="showRuleModal = false">
      <div class="modal">
        <h3>{{ editingRuleId ? '编辑规则' : '新建规则' }}</h3>
        <form @submit.prevent="submitRule">
          <div class="form-group">
            <label>规则名称</label>
            <input v-model="ruleForm.ruleName" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>设备ID（可选，为空则按类型或全部）</label>
            <input v-model.number="ruleForm.deviceId" type="number" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>设备类型（可选）</label>
            <input v-model="ruleForm.deviceType" placeholder="如 SENSOR" />
          </div>
          <div class="form-group">
            <label>测点名称 *</label>
            <input v-model="ruleForm.fieldName" required placeholder="如 temperature" />
          </div>
          <div class="form-group">
            <label>操作符 *</label>
            <select v-model="ruleForm.operator" required>
              <option value="gt">大于 (gt)</option>
              <option value="gte">大于等于 (gte)</option>
              <option value="lt">小于 (lt)</option>
              <option value="lte">小于等于 (lte)</option>
              <option value="eq">等于 (eq)</option>
            </select>
          </div>
          <div class="form-group">
            <label>阈值 *</label>
            <input v-model.number="ruleForm.thresholdValue" type="number" step="any" required />
          </div>
          <div class="form-group">
            <label>告警类型 *</label>
            <input v-model="ruleForm.alertType" required placeholder="如 TEMPERATURE_HIGH" />
          </div>
          <div class="form-group">
            <label>告警级别 *</label>
            <input v-model.number="ruleForm.alertLevel" type="number" min="1" max="4" required />
          </div>
          <div class="form-group">
            <label>告警内容模板（支持 {value} {threshold}）</label>
            <input v-model="ruleForm.alertContentTemplate" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>冷却时间（秒）</label>
            <input v-model.number="ruleForm.cooldownSeconds" type="number" min="0" />
          </div>
          <div class="form-group">
            <label>启用</label>
            <select v-model.number="ruleForm.enabled">
              <option :value="1">是</option>
              <option :value="0">否</option>
            </select>
          </div>
          <p v-if="ruleError" class="error-msg">{{ ruleError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showRuleModal = false">取消</button>
            <button type="submit" class="btn primary" :disabled="ruleSaving">保存</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import {
  getPendingAlerts,
  getAlertsAll,
  getPendingAlertCount,
  resolveDeviceAlert,
  getAlertRules,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  type DeviceAlertDTO,
  type DeviceAlertRuleDTO,
  type DeviceAlertRuleCreateRequest
} from '@/api/devices';

const tab = ref<'pending' | 'all' | 'rules'>('pending');
const alerts = ref<DeviceAlertDTO[]>([]);
const rules = ref<DeviceAlertRuleDTO[]>([]);
const pendingCount = ref<number | null>(null);
const loading = ref(false);
const error = ref('');
const currentPage = ref(1);
const rulePage = ref(1);
const pageSize = 20;

const showRuleModal = ref(false);
const editingRuleId = ref<number | null>(null);
const ruleForm = ref<DeviceAlertRuleCreateRequest & { enabled?: number; cooldownSeconds?: number }>({
  ruleName: '',
  deviceId: undefined,
  deviceType: '',
  fieldName: '',
  operator: 'gt',
  thresholdValue: 0,
  alertType: '',
  alertLevel: 2,
  alertContentTemplate: '',
  enabled: 1,
  cooldownSeconds: 300
});
const ruleError = ref('');
const ruleSaving = ref(false);

async function loadPendingCount() {
  try {
    const res = await getPendingAlertCount();
    pendingCount.value = res.count;
  } catch {
    pendingCount.value = null;
  }
}

async function loadAlerts() {
  loading.value = true;
  error.value = '';
  try {
    if (tab.value === 'pending') {
      alerts.value = await getPendingAlerts(currentPage.value, pageSize);
    } else {
      alerts.value = await getAlertsAll(currentPage.value, pageSize);
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
    alerts.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadRules() {
  loading.value = true;
  error.value = '';
  try {
    rules.value = await getAlertRules(rulePage.value, pageSize);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
    rules.value = [];
  } finally {
    loading.value = false;
  }
}

async function resolveAlert(id: number) {
  try {
    await resolveDeviceAlert(id, 'admin');
    await loadAlerts();
    await loadPendingCount();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '处理失败';
  }
}

function openCreateRule() {
  editingRuleId.value = null;
  ruleForm.value = {
    ruleName: '',
    deviceId: undefined,
    deviceType: '',
    fieldName: '',
    operator: 'gt',
    thresholdValue: 0,
    alertType: '',
    alertLevel: 2,
    alertContentTemplate: '',
    enabled: 1,
    cooldownSeconds: 300
  };
  ruleError.value = '';
  showRuleModal.value = true;
}

function openEditRule(r: DeviceAlertRuleDTO) {
  editingRuleId.value = r.id;
  ruleForm.value = {
    ruleName: r.ruleName,
    deviceId: r.deviceId,
    deviceType: r.deviceType,
    fieldName: r.fieldName,
    operator: r.operator,
    thresholdValue: r.thresholdValue,
    alertType: r.alertType,
    alertLevel: r.alertLevel,
    alertContentTemplate: r.alertContentTemplate,
    enabled: r.enabled,
    cooldownSeconds: r.cooldownSeconds
  };
  ruleError.value = '';
  showRuleModal.value = true;
}

async function submitRule() {
  ruleError.value = '';
  ruleSaving.value = true;
  try {
    const body = {
      ruleName: ruleForm.value.ruleName || undefined,
      deviceId: ruleForm.value.deviceId || undefined,
      deviceType: ruleForm.value.deviceType || undefined,
      fieldName: ruleForm.value.fieldName,
      operator: ruleForm.value.operator,
      thresholdValue: ruleForm.value.thresholdValue,
      alertType: ruleForm.value.alertType,
      alertLevel: ruleForm.value.alertLevel,
      alertContentTemplate: ruleForm.value.alertContentTemplate || undefined,
      enabled: ruleForm.value.enabled ?? 1,
      cooldownSeconds: ruleForm.value.cooldownSeconds ?? 300
    };
    if (editingRuleId.value) {
      await updateAlertRule(editingRuleId.value, body);
    } else {
      await createAlertRule(body);
    }
    showRuleModal.value = false;
    await loadRules();
  } catch (e) {
    ruleError.value = e instanceof Error ? e.message : '保存失败';
  } finally {
    ruleSaving.value = false;
  }
}

async function doDeleteRule(id: number) {
  if (!confirm('确定删除该规则？')) return;
  try {
    await deleteAlertRule(id);
    await loadRules();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}

watch([tab, currentPage], () => {
  if (tab.value === 'rules') loadRules();
  else loadAlerts();
});

watch(rulePage, () => {
  if (tab.value === 'rules') loadRules();
});

onMounted(async () => {
  await loadPendingCount();
  if (tab.value === 'rules') await loadRules();
  else await loadAlerts();
});
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.stats-bar { margin-bottom: 1rem; font-size: 0.9rem; }
.stats-bar .pending { color: #f87171; }
.toolbar { margin-bottom: 1rem; }
.tabs { display: flex; gap: 0.5rem; }
.tab { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #94a3b8; }
.tab.active { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.danger { color: #f87171; border-color: #f87171; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.row-pending { background: rgba(248, 113, 113, 0.08); }
.level { font-weight: 600; padding: 0.1rem 0.3rem; border-radius: 4px; }
.level-1 { color: #f87171; }
.level-2 { color: #fb923c; }
.level-3 { color: #facc15; }
.level-4 { color: #94a3b8; }
.resolved { font-size: 0.8rem; color: #64748b; }
.pagination { margin-top: 1rem; display: flex; align-items: center; gap: 0.75rem; font-size: 0.9rem; color: #94a3b8; }
.empty-msg { color: #64748b; font-size: 0.9rem; margin-top: 1rem; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 360px; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
