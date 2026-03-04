<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="告警记录、待处理告警、阈值规则自动告警" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <div v-if="pendingCount !== null && tab !== 'rules'" class="stats-bar">
          <span class="pending">待处理 {{ pendingCount }} 条</span>
        </div>
        <el-tabs v-model="tab" class="tabs-compact">
          <el-tab-pane label="待处理" name="pending" />
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="告警规则" name="rules" />
        </el-tabs>
        <el-button v-if="tab === 'rules'" type="primary" @click="openCreateRule">新建规则</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
    <template v-if="tab === 'rules'">
      <el-table :data="rules" class="table-wrap">
        <el-table-column prop="ruleName" label="规则名称">
          <template #default="{ row }">{{ row.ruleName || '-' }}</template>
        </el-table-column>
        <el-table-column label="设备/类型">
          <template #default="{ row }">{{ row.deviceId ? `设备#${row.deviceId}` : (row.deviceType || '全部') }}</template>
        </el-table-column>
        <el-table-column prop="fieldName" label="测点" />
        <el-table-column label="条件">
          <template #default="{ row }">{{ row.operator }} {{ row.thresholdValue }}</template>
        </el-table-column>
        <el-table-column prop="alertType" label="告警类型" />
        <el-table-column label="级别">
          <template #default="{ row }"><span class="level" :class="'level-' + row.alertLevel">L{{ row.alertLevel }}</span></template>
        </el-table-column>
        <el-table-column prop="cooldownSeconds" label="冷却(秒)" />
        <el-table-column label="启用">
          <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="openEditRule(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="doDeleteRule(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-button size="small" :disabled="rulePage <= 1" @click="rulePage--">上一页</el-button>
        <span class="page-info">第 {{ rulePage }} 页</span>
        <el-button size="small" :disabled="rules.length < pageSize" @click="rulePage++">下一页</el-button>
      </div>
      <el-empty v-if="rules.length === 0" description="暂无规则，遥测数据满足阈值时将自动创建告警" />
    </template>
    <template v-else>
      <el-table :data="alerts" class="table-wrap" :row-class-name="({ row }) => row.alertStatus === 0 ? 'row-pending' : ''">
        <el-table-column label="设备">
          <template #default="{ row }">{{ row.deviceCode || row.deviceId }}</template>
        </el-table-column>
        <el-table-column prop="alertType" label="类型" />
        <el-table-column label="级别">
          <template #default="{ row }"><span class="level" :class="'level-' + row.alertLevel">L{{ row.alertLevel }}</span></template>
        </el-table-column>
        <el-table-column prop="alertContent" label="内容" />
        <el-table-column label="状态">
          <template #default="{ row }">{{ row.alertStatus === 0 ? '待处理' : '已处理' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="row.alertStatus === 0" size="small" @click="resolveAlert(row.id)">处理</el-button>
            <span v-else class="resolved">{{ row.resolveTime }} {{ row.resolvedBy ? `by ${row.resolvedBy}` : '' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-button size="small" :disabled="currentPage <= 1" @click="currentPage--">上一页</el-button>
        <span class="page-info">第 {{ currentPage }} 页</span>
        <el-button size="small" :disabled="alerts.length < pageSize" @click="currentPage++">下一页</el-button>
      </div>
      <el-empty v-if="alerts.length === 0" description="暂无告警" />
    </template>
    </template>

    <el-dialog v-model="showRuleModal" :title="editingRuleId ? '编辑规则' : '新建规则'" width="400px" :close-on-click-modal="false">
      <el-form :model="ruleForm" @submit.prevent="submitRule">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.ruleName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="设备ID（可选，为空则按类型或全部）">
          <el-input v-model.number="ruleForm.deviceId" type="number" placeholder="可选" />
        </el-form-item>
        <el-form-item label="设备类型（可选）">
          <el-input v-model="ruleForm.deviceType" placeholder="如 SENSOR" />
        </el-form-item>
        <el-form-item label="测点名称" required>
          <el-input v-model="ruleForm.fieldName" placeholder="如 temperature" />
        </el-form-item>
        <el-form-item label="操作符" required>
          <el-select v-model="ruleForm.operator" placeholder="选择操作符">
            <el-option value="gt" label="大于 (gt)" />
            <el-option value="gte" label="大于等于 (gte)" />
            <el-option value="lt" label="小于 (lt)" />
            <el-option value="lte" label="小于等于 (lte)" />
            <el-option value="eq" label="等于 (eq)" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" required>
          <el-input-number v-model="ruleForm.thresholdValue" :step="0.1" placeholder="阈值" />
        </el-form-item>
        <el-form-item label="告警类型" required>
          <el-input v-model="ruleForm.alertType" placeholder="如 TEMPERATURE_HIGH" />
        </el-form-item>
        <el-form-item label="告警级别" required>
          <el-input-number v-model="ruleForm.alertLevel" :min="1" :max="4" />
        </el-form-item>
        <el-form-item label="告警内容模板（支持 {value} {threshold}）">
          <el-input v-model="ruleForm.alertContentTemplate" placeholder="可选" />
        </el-form-item>
        <el-form-item label="冷却时间（秒）">
          <el-input-number v-model="ruleForm.cooldownSeconds" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-select v-model="ruleForm.enabled" placeholder="启用">
            <el-option :value="1" label="是" />
            <el-option :value="0" label="否" />
          </el-select>
        </el-form-item>
        <el-alert v-if="ruleError" type="error" :title="ruleError" show-icon class="error-alert" />
        <template #footer>
          <el-button @click="showRuleModal = false">取消</el-button>
          <el-button type="primary" :disabled="ruleSaving" @click="submitRule">保存</el-button>
        </template>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
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
  try {
    await ElMessageBox.confirm('确定删除该规则？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteAlertRule(id);
    await loadRules();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
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
.toolbar-actions { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.stats-bar { font-size: 0.9rem; }
.stats-bar .pending { color: #f87171; }
.toolbar { margin-bottom: 1rem; }
.tabs-compact :deep(.el-tabs__header) { margin: 0; }
.tabs-compact :deep(.el-tabs__content) { display: none; }
.tabs-compact :deep(.el-tabs__item) { padding: 0 0.5rem; }
.page-info { margin: 0 0.75rem; font-size: 0.9rem; color: #94a3b8; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
:deep(.row-pending) { background: rgba(248, 113, 113, 0.08); }
.level { font-weight: 600; padding: 0.1rem 0.3rem; border-radius: 4px; }
.level-1 { color: #f87171; }
.level-2 { color: #fb923c; }
.level-3 { color: #facc15; }
.level-4 { color: #94a3b8; }
.resolved { font-size: 0.8rem; color: #64748b; }
</style>
