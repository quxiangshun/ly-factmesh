<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="IoT 设备列表，支持上下线、启停、故障、遥测、告警" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-button type="primary" @click="showCreate = true">手动注册</el-button>
        <el-button :disabled="importing" @click="triggerFileInput">Excel 批量导入</el-button>
        <el-button type="primary" link @click="downloadTemplate">下载模板</el-button>
      </div>
      <input ref="fileInputRef" type="file" accept=".xlsx,.xls" style="display:none" @change="onFileSelected" />
      <div v-if="stats" class="stats-bar">
        <span>总数 {{ stats.total }}</span>
        <span class="online">在线 {{ stats.online }}</span>
        <span class="fault">故障 {{ stats.fault }}</span>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <div v-if="importResult" class="import-result-block">
      <el-alert
        :type="importResult.successCount > 0 ? 'success' : 'warning'"
        :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
        show-icon
        class="import-result-alert"
      >
        <ul v-if="importResult.errors?.length" class="import-errors">
          <li v-for="(e, i) in importResult.errors" :key="i">第 {{ e.row }} 行 {{ e.deviceCode || '-' }}：{{ e.message }}</li>
        </ul>
      </el-alert>
      <el-button size="small" @click="importResult = null">关闭</el-button>
    </div>
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="list" class="table-wrap">
        <el-table-column prop="deviceCode" label="编码" min-width="120" />
        <el-table-column prop="deviceName" label="名称" min-width="140" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ row.deviceType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="制造商" min-width="120">
          <template #default="{ row }">{{ row.manufacturer || '-' }}</template>
        </el-table-column>
        <el-table-column label="在线" width="72">
          <template #default="{ row }">{{ onlineText(row.onlineStatus) }}</template>
        </el-table-column>
        <el-table-column label="运行" width="72">
          <template #default="{ row }">{{ runningText(row.runningStatus) }}</template>
        </el-table-column>
        <el-table-column label="温度/湿度" min-width="120">
          <template #default="{ row }">{{ row.temperature != null ? row.temperature + '°C' : '-' }} / {{ row.humidity != null ? row.humidity + '%' : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleDeviceCommand(cmd, row)">
              <el-button size="small">更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <template v-if="row.onlineStatus !== 1">
                    <el-dropdown-item command="online">上线</el-dropdown-item>
                  </template>
                  <template v-else>
                    <el-dropdown-item command="offline">离线</el-dropdown-item>
                    <el-dropdown-item command="start">{{ row.runningStatus === 2 ? '恢复' : '启动' }}</el-dropdown-item>
                    <el-dropdown-item v-if="row.runningStatus === 1" command="stop">停止</el-dropdown-item>
                    <el-dropdown-item command="fault" divided>故障</el-dropdown-item>
                  </template>
                  <el-dropdown-item command="telemetry">遥测</el-dropdown-item>
                  <el-dropdown-item command="alerts">告警</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 导入预览弹窗 -->
    <el-dialog v-model="showImportPreview" title="导入预览" width="720px" :close-on-click-modal="false">
      <p class="preview-desc">请确认以下数据无误后点击「确认导入」执行导入</p>
      <el-alert v-if="previewErrors?.length" type="warning" show-icon class="preview-errors">
        <template #title>以下行校验失败，将跳过：</template>
        <ul>
          <li v-for="(e, i) in previewErrors" :key="i">第 {{ e.row }} 行 {{ e.deviceCode || '-' }}：{{ e.message }}</li>
        </ul>
      </el-alert>
      <el-table v-if="previewRows?.length" :data="previewRows" max-height="320">
        <el-table-column prop="row" label="行号" width="70" />
        <el-table-column prop="deviceCode" label="设备编码" />
        <el-table-column prop="deviceName" label="设备名称" />
        <el-table-column prop="deviceType" label="设备类型">
          <template #default="{ row }">{{ row.deviceType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="model" label="型号">
          <template #default="{ row }">{{ row.model || '-' }}</template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="制造商">
          <template #default="{ row }">{{ row.manufacturer || '-' }}</template>
        </el-table-column>
        <el-table-column prop="installLocation" label="安装位置">
          <template #default="{ row }">{{ row.installLocation || '-' }}</template>
        </el-table-column>
      </el-table>
      <p v-else-if="!previewLoading" class="no-preview-data">没有可导入的有效数据</p>
      <el-skeleton v-if="previewLoading" :rows="3" animated />
      <template #footer>
        <el-button @click="closeImportPreview">取消</el-button>
        <el-button type="primary" :disabled="!previewRows?.length || importing" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEdit" title="编辑设备" width="420px" :close-on-click-modal="false">
      <el-form v-if="editForm" :model="editForm" @submit.prevent="submitEdit">
        <el-form-item label="设备编码">
          <el-input :model-value="editForm.deviceCode" disabled />
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model="editForm.deviceName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="editForm.deviceType" placeholder="可选" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="editForm.model" placeholder="可选" />
        </el-form-item>
        <el-form-item label="制造商">
          <el-input v-model="editForm.manufacturer" placeholder="可选" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="editForm.installLocation" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="editError" type="error" :title="editError" show-icon class="error-alert" />
        <template #footer>
          <el-button @click="showEdit = false">取消</el-button>
          <el-button type="primary" :disabled="editing" @click="submitEdit">保存</el-button>
        </template>
      </el-form>
    </el-dialog>

    <el-dialog v-model="showCreate" title="注册设备" width="420px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="设备编码" required>
          <el-input v-model="createForm.deviceCode" placeholder="如 DEV-001" />
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model="createForm.deviceName" placeholder="设备名称" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="createForm.deviceType" placeholder="可选" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="createForm.model" placeholder="可选" />
        </el-form-item>
        <el-form-item label="制造商">
          <el-input v-model="createForm.manufacturer" placeholder="可选" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="createForm.installLocation" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <template #footer>
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" :disabled="creating" @click="submitCreate">确定</el-button>
        </template>
      </el-form>
    </el-dialog>

    <el-dialog v-model="showTelemetry" :title="`遥测数据 - ${selectedDevice?.deviceName}`" width="540px" :close-on-click-modal="false">
      <div class="telemetry-section">
        <h4>查询历史</h4>
        <div class="form-row form-row-wrap">
          <el-form-item label="测点">
            <el-input v-model="telemetryField" placeholder="如 temperature、voltage" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="telemetryStart"
              type="datetime"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择开始时间"
              clearable
            />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="telemetryEnd"
              type="datetime"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择结束时间"
              clearable
            />
          </el-form-item>
          <div class="telemetry-quick">
            <el-button size="small" @click="setTelemetryRange(1)">最近1小时</el-button>
            <el-button size="small" @click="setTelemetryRange(24)">最近24小时</el-button>
            <el-button size="small" @click="setTelemetryRange(24 * 7)">最近7天</el-button>
          </div>
          <el-button type="primary" @click="loadTelemetry">查询</el-button>
        </div>
      </div>
      <div class="telemetry-section">
        <h4>模拟上报</h4>
        <div class="form-row form-row-wrap">
          <el-form-item label="测点">
            <el-input v-model="reportField" placeholder="如 temperature" />
          </el-form-item>
          <el-form-item label="数值">
            <el-input v-model.number="reportValue" type="number" placeholder="如 25.5" />
          </el-form-item>
          <el-button :disabled="reporting || !reportField || reportValue === ''" @click="doReportTelemetry">上报</el-button>
        </div>
      </div>
      <el-skeleton v-if="telemetryLoading" :rows="3" animated />
      <div v-else class="telemetry-list">
        <div v-for="p in telemetryData" :key="p.time + p.field" class="telemetry-item">
          {{ p.field }}: {{ p.value }} @ {{ formatTime(p.time) }}
        </div>
        <el-empty v-if="telemetryData.length === 0" description="暂无数据" />
      </div>
      <template #footer>
        <el-button @click="showTelemetry = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAlerts" :title="`告警列表 - ${selectedDevice?.deviceName}`" width="540px" :close-on-click-modal="false">
      <el-button type="primary" class="mb-1" @click="openCreateAlert">新建告警</el-button>
      <el-skeleton v-if="alertsLoading" :rows="3" animated />
      <div v-else class="alerts-list">
        <div v-for="a in deviceAlerts" :key="a.id" class="alert-item" :class="{ pending: a.alertStatus === 0 }">
          <span class="alert-level">L{{ a.alertLevel }}</span>
          <span>{{ a.alertType }}: {{ a.alertContent }}</span>
          <span class="alert-time">{{ a.createTime }}</span>
          <el-button v-if="a.alertStatus === 0" size="small" @click="resolveAlert(a.id)">处理</el-button>
        </div>
        <el-empty v-if="deviceAlerts.length === 0" description="暂无告警" />
      </div>
      <template #footer>
        <el-button @click="showAlerts = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCreateAlert" title="新建告警" width="420px" :close-on-click-modal="false">
      <el-form v-if="selectedDevice" :model="alertForm" @submit.prevent="submitCreateAlert">
        <el-form-item label="告警类型" required>
          <el-input v-model="alertForm.alertType" placeholder="如 TEMPERATURE_HIGH" />
        </el-form-item>
        <el-form-item label="告警级别 (1-4)" required>
          <el-input-number v-model="alertForm.alertLevel" :min="1" :max="4" />
        </el-form-item>
        <el-form-item label="告警内容" required>
          <el-input v-model="alertForm.alertContent" type="textarea" :rows="3" />
        </el-form-item>
        <el-alert v-if="alertError" type="error" :title="alertError" show-icon class="error-alert" />
        <template #footer>
          <el-button @click="showCreateAlert = false">取消</el-button>
          <el-button type="primary" :disabled="alertCreating" @click="submitCreateAlert">确定</el-button>
        </template>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { InfoFilled, ArrowDown } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import {
  getDeviceList,
  getDeviceStats,
  registerDevice,
  batchPreviewDevices,
  batchImportDevices,
  downloadDeviceImportTemplate,
  updateDevice,
  updateDeviceStatus,
  deviceOnline,
  deviceOffline,
  deviceStart,
  deviceStop,
  deviceFault,
  deleteDevice,
  queryTelemetry,
  reportTelemetry,
  getDeviceAlerts,
  createDeviceAlert,
  resolveDeviceAlert,
  type DeviceRegisterRequest,
  type DeviceUpdateRequest,
  type DeviceDTO,
  type DeviceStatsDTO,
  type DeviceTelemetryPoint,
  type DeviceAlertDTO,
  type DeviceBatchImportResult,
  type DeviceImportRow
} from '@/api/devices';

const list = ref<DeviceDTO[]>([]);
const stats = ref<DeviceStatsDTO | null>(null);
const loading = ref(true);
const error = ref('');
const showCreate = ref(false);
const createForm = ref<DeviceRegisterRequest>({
  deviceCode: '',
  deviceName: '',
  deviceType: '',
  model: '',
  manufacturer: '',
  installLocation: ''
});
const createError = ref('');
const creating = ref(false);
const showEdit = ref(false);
const editForm = ref<(DeviceDTO & { deviceCode: string }) | null>(null);
const editError = ref('');
const editing = ref(false);
const showTelemetry = ref(false);
const showAlerts = ref(false);
const showCreateAlert = ref(false);
const selectedDevice = ref<DeviceDTO | null>(null);
const telemetryData = ref<DeviceTelemetryPoint[]>([]);
const telemetryField = ref('');
const telemetryStart = ref<string | ''>('');
const telemetryEnd = ref<string | ''>('');
const reportField = ref('');
const reportValue = ref<number | ''>('');
const reporting = ref(false);
const telemetryLoading = ref(false);
const deviceAlerts = ref<DeviceAlertDTO[]>([]);
const alertsLoading = ref(false);
const alertForm = ref({ alertType: '', alertLevel: 2, alertContent: '' });
const alertError = ref('');
const alertCreating = ref(false);
const fileInputRef = ref<HTMLInputElement | null>(null);
const importResult = ref<DeviceBatchImportResult | null>(null);
const importing = ref(false);
const showImportPreview = ref(false);
const previewRows = ref<DeviceImportRow[]>([]);
const previewErrors = ref<Array<{ row: number; deviceCode: string; message: string }>>([]);
const previewLoading = ref(false);

function onlineText(s: number) {
  const map: Record<number, string> = { 0: '离线', 1: '在线' };
  return map[s] ?? String(s);
}

function runningText(s: number) {
  const map: Record<number, string> = { 0: '停止', 1: '运行', 2: '故障' };
  return map[s] ?? String(s);
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    list.value = await getDeviceList();
    stats.value = await getDeviceStats();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

function formatTime(t?: string) {
  if (!t) return '-';
  try {
    return new Date(t).toLocaleString();
  } catch {
    return t;
  }
}

function openTelemetry(row: DeviceDTO) {
  selectedDevice.value = row;
  showTelemetry.value = true;
  telemetryData.value = [];
  telemetryField.value = '';
  telemetryStart.value = '';
  telemetryEnd.value = '';
  reportField.value = '';
  reportValue.value = '';
  loadTelemetry();
}

function setTelemetryRange(hours: number) {
  const end = new Date();
  const start = new Date(end.getTime() - hours * 60 * 60 * 1000);
  const fmt = (d: Date) =>
    d.getFullYear() + '-' +
    String(d.getMonth() + 1).padStart(2, '0') + '-' +
    String(d.getDate()).padStart(2, '0') + 'T' +
    String(d.getHours()).padStart(2, '0') + ':' +
    String(d.getMinutes()).padStart(2, '0') + ':' +
    String(d.getSeconds()).padStart(2, '0');
  telemetryStart.value = fmt(start);
  telemetryEnd.value = fmt(end);
}

async function loadTelemetry() {
  if (!selectedDevice.value) return;
  telemetryLoading.value = true;
  try {
    const start = telemetryStart.value || undefined;
    const end = telemetryEnd.value || undefined;
    telemetryData.value = await queryTelemetry(selectedDevice.value.id, {
      field: telemetryField.value || undefined,
      start,
      end,
      limit: 200
    });
  } catch {
    telemetryData.value = [];
  } finally {
    telemetryLoading.value = false;
  }
}

async function doReportTelemetry() {
  if (!selectedDevice.value || !reportField.value || reportValue.value === '') return;
  reporting.value = true;
  try {
    const numVal = Number(reportValue.value);
    if (isNaN(numVal)) throw new Error('请输入有效数值');
    await reportTelemetry({
      deviceId: selectedDevice.value.id,
      deviceCode: selectedDevice.value.deviceCode,
      data: { [reportField.value]: numVal }
    });
    const field = reportField.value.toLowerCase();
    if (['temperature', 'humidity', 'voltage', 'current'].includes(field)) {
      const params: Record<string, number> = {};
      params[field as keyof typeof params] = numVal;
      await updateDeviceStatus(selectedDevice.value.id, params);
    }
    await loadTelemetry();
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '上报失败';
  } finally {
    reporting.value = false;
  }
}

function openAlerts(row: DeviceDTO) {
  selectedDevice.value = row;
  showAlerts.value = true;
  deviceAlerts.value = [];
  loadDeviceAlerts();
}

async function loadDeviceAlerts() {
  if (!selectedDevice.value) return;
  alertsLoading.value = true;
  try {
    deviceAlerts.value = await getDeviceAlerts(selectedDevice.value.id);
  } catch {
    deviceAlerts.value = [];
  } finally {
    alertsLoading.value = false;
  }
}

function openCreateAlert() {
  alertForm.value = { alertType: '', alertLevel: 2, alertContent: '' };
  alertError.value = '';
  showCreateAlert.value = true;
}

async function submitCreateAlert() {
  if (!selectedDevice.value) return;
  alertError.value = '';
  alertCreating.value = true;
  try {
    await createDeviceAlert({
      deviceId: selectedDevice.value.id,
      deviceCode: selectedDevice.value.deviceCode,
      alertType: alertForm.value.alertType,
      alertLevel: alertForm.value.alertLevel,
      alertContent: alertForm.value.alertContent
    });
    showCreateAlert.value = false;
    loadDeviceAlerts();
  } catch (e) {
    alertError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    alertCreating.value = false;
  }
}

async function resolveAlert(id: number) {
  try {
    await resolveDeviceAlert(id, 'admin');
    loadDeviceAlerts();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '处理失败';
  }
}

function triggerFileInput() {
  fileInputRef.value?.click();
}

async function onFileSelected(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  input.value = '';
  previewLoading.value = true;
  previewRows.value = [];
  previewErrors.value = [];
  showImportPreview.value = true;
  importResult.value = null;
  try {
    const res = await batchPreviewDevices(file);
    previewRows.value = res.rows ?? [];
    previewErrors.value = res.errors ?? [];
  } catch (err) {
    previewErrors.value = [{ row: 0, deviceCode: '', message: err instanceof Error ? err.message : '预览失败' }];
  } finally {
    previewLoading.value = false;
  }
}

function closeImportPreview() {
  showImportPreview.value = false;
  previewRows.value = [];
  previewErrors.value = [];
}

async function confirmImport() {
  if (!previewRows.value?.length) return;
  importing.value = true;
  importResult.value = null;
  try {
    const res = await batchImportDevices(previewRows.value);
    importResult.value = res;
    if (res.successCount > 0) await load();
    closeImportPreview();
  } catch (err) {
    importResult.value = { successCount: 0, failCount: 1, errors: [{ row: 0, deviceCode: '', message: err instanceof Error ? err.message : '导入失败' }] };
    closeImportPreview();
  } finally {
    importing.value = false;
  }
}

async function downloadTemplate() {
  try {
    await downloadDeviceImportTemplate();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '下载失败';
  }
}

function openEdit(row: DeviceDTO) {
  editForm.value = { ...row, deviceCode: row.deviceCode };
  editError.value = '';
  showEdit.value = true;
}

async function submitEdit() {
  if (!editForm.value) return;
  editError.value = '';
  editing.value = true;
  try {
    const body: DeviceUpdateRequest = {
      deviceName: editForm.value.deviceName,
      deviceType: editForm.value.deviceType,
      model: editForm.value.model,
      manufacturer: editForm.value.manufacturer,
      installLocation: editForm.value.installLocation
    };
    await updateDevice(editForm.value.id, body);
    showEdit.value = false;
    editForm.value = null;
    await load();
  } catch (e) {
    editError.value = e instanceof Error ? e.message : '保存失败';
  } finally {
    editing.value = false;
  }
}

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await registerDevice(createForm.value);
    showCreate.value = false;
    createForm.value = { deviceCode: '', deviceName: '', deviceType: '', model: '', manufacturer: '', installLocation: '' };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '注册失败';
  } finally {
    creating.value = false;
  }
}

function updateRowFromResponse(dto: DeviceDTO) {
  const idx = list.value.findIndex((d) => String(d.id) === String(dto.id));
  if (idx >= 0) {
    list.value[idx] = { ...dto };
  }
  if (stats.value) {
    stats.value = {
      total: list.value.length,
      online: list.value.filter((d) => d.onlineStatus === 1).length,
      fault: list.value.filter((d) => d.runningStatus === 2).length
    };
  }
}

function handleDeviceCommand(cmd: string, row: DeviceDTO) {
  switch (cmd) {
    case 'online': doOnline(row.id); break;
    case 'offline': doOffline(row.id); break;
    case 'start': doStart(row.id); break;
    case 'stop': doStop(row.id); break;
    case 'fault': doFault(row.id); break;
    case 'telemetry': openTelemetry(row); break;
    case 'alerts': openAlerts(row); break;
    case 'delete': doDelete(row.id); break;
  }
}

async function doOnline(id: string | number) {
  try {
    const dto = await deviceOnline(id);
    updateRowFromResponse(dto);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doOffline(id: string | number) {
  try {
    const dto = await deviceOffline(id);
    updateRowFromResponse(dto);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doStart(id: string | number) {
  try {
    const dto = await deviceStart(id);
    updateRowFromResponse(dto);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doStop(id: string | number) {
  try {
    const dto = await deviceStop(id);
    updateRowFromResponse(dto);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doFault(id: string | number) {
  try {
    const dto = await deviceFault(id);
    updateRowFromResponse(dto);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doDelete(id: string | number) {
  try {
    await ElMessageBox.confirm('确定删除该设备？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteDevice(id);
    const idx = list.value.findIndex((d) => String(d.id) === String(id));
    if (idx >= 0) {
      list.value.splice(idx, 1);
    }
    if (stats.value) {
      stats.value = {
        total: list.value.length,
        online: list.value.filter((d) => d.onlineStatus === 1).length,
        fault: list.value.filter((d) => d.runningStatus === 2).length
      };
    }
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}

onMounted(() => load());
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 1rem; flex-wrap: wrap; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.stats-bar { display: flex; gap: 1rem; font-size: 0.9rem; color: #94a3b8; margin-left: auto; }
.stats-bar .online { color: #22c55e; }
.stats-bar .fault { color: #f87171; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.error-alert { margin-bottom: 1rem; }
.import-result-block { display: flex; align-items: flex-start; gap: 0.5rem; margin-bottom: 1rem; }
.import-result-block .import-result-alert { flex: 1; margin-bottom: 0; }
.import-result-alert { margin-bottom: 1rem; }
.import-errors { margin: 0.5rem 0 0; padding-left: 1.25rem; font-size: 0.85rem; max-height: 120px; overflow-y: auto; }
.table-wrap { margin-bottom: 1rem; }
.telemetry-section { margin-bottom: 1rem; }
.telemetry-section h4 { margin: 0 0 0.5rem; font-size: 0.9rem; color: #94a3b8; }
.form-row { display: flex; gap: 0.75rem; align-items: flex-end; margin-bottom: 1rem; }
.form-row-wrap { flex-wrap: wrap; }
.telemetry-quick { display: flex; gap: 0.5rem; align-items: center; }
.telemetry-section .el-form-item { margin-bottom: 0; }
.telemetry-list, .alerts-list { max-height: 300px; overflow-y: auto; margin-bottom: 1rem; }
.telemetry-item, .alert-item { padding: 0.5rem; border-bottom: 1px solid #334155; font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem; }
.alert-item.pending { background: rgba(248, 113, 113, 0.1); }
.alert-level { font-weight: 600; color: #f87171; min-width: 2rem; }
.alert-time { margin-left: auto; font-size: 0.8rem; color: #64748b; }
.preview-desc { color: #94a3b8; font-size: 0.875rem; margin: 0 0 1rem; }
.preview-errors ul { margin: 0.5rem 0 0; padding-left: 1.25rem; }
.no-preview-data { color: #94a3b8; margin: 1rem 0; }
.mb-1 { margin-bottom: 0.5rem; }
</style>
