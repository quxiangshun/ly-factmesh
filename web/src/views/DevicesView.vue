<template>
  <section class="page">
    <h1 class="page-title">设备管理</h1>
    <p class="page-desc">IoT 设备列表，支持上下线、启停、故障、遥测、告警</p>
    <div class="stats-bar" v-if="stats">
      <span>总数 {{ stats.total }}</span>
      <span class="online">在线 {{ stats.online }}</span>
      <span class="fault">故障 {{ stats.fault }}</span>
    </div>
    <div class="toolbar">
      <button type="button" class="btn primary" @click="showCreate = true">注册设备</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>编码</th>
              <th>名称</th>
              <th>类型</th>
              <th>制造商</th>
              <th>在线</th>
              <th>运行</th>
              <th>温度/湿度</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in list" :key="row.id">
              <td>{{ row.deviceCode }}</td>
              <td>{{ row.deviceName }}</td>
              <td>{{ row.deviceType || '-' }}</td>
              <td>{{ row.manufacturer || '-' }}</td>
              <td>{{ onlineText(row.onlineStatus) }}</td>
              <td>{{ runningText(row.runningStatus) }}</td>
              <td>{{ row.temperature != null ? row.temperature + '°C' : '-' }} / {{ row.humidity != null ? row.humidity + '%' : '-' }}</td>
              <td>
                <button v-if="row.onlineStatus !== 1" type="button" class="btn small" @click="doOnline(row.id)">上线</button>
                <template v-else>
                  <button type="button" class="btn small" @click="doOffline(row.id)">离线</button>
                  <button v-if="row.runningStatus !== 1" type="button" class="btn small" @click="doStart(row.id)">启动</button>
                  <button v-else type="button" class="btn small" @click="doStop(row.id)">停止</button>
                  <button type="button" class="btn small danger" @click="doFault(row.id)">故障</button>
                </template>
                <button type="button" class="btn small" @click="openEdit(row)">编辑</button>
                <button type="button" class="btn small" @click="openTelemetry(row)">遥测</button>
                <button type="button" class="btn small" @click="openAlerts(row)">告警</button>
                <button type="button" class="btn small danger" @click="doDelete(row.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
    <div v-if="showEdit" class="modal-mask" @click.self="showEdit = false">
      <div class="modal">
        <h3>编辑设备</h3>
        <form v-if="editForm" @submit.prevent="submitEdit">
          <div class="form-group">
            <label>设备编码</label>
            <input :value="editForm.deviceCode" disabled class="readonly" />
          </div>
          <div class="form-group">
            <label>设备名称 *</label>
            <input v-model="editForm.deviceName" required />
          </div>
          <div class="form-group">
            <label>设备类型</label>
            <input v-model="editForm.deviceType" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>型号</label>
            <input v-model="editForm.model" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>制造商</label>
            <input v-model="editForm.manufacturer" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>安装位置</label>
            <input v-model="editForm.installLocation" placeholder="可选" />
          </div>
          <p v-if="editError" class="error-msg">{{ editError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showEdit = false">取消</button>
            <button type="submit" class="btn primary" :disabled="editing">保存</button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>注册设备</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>设备编码</label>
            <input v-model="createForm.deviceCode" required placeholder="如 DEV-001" />
          </div>
          <div class="form-group">
            <label>设备名称</label>
            <input v-model="createForm.deviceName" required placeholder="设备名称" />
          </div>
          <div class="form-group">
            <label>设备类型</label>
            <input v-model="createForm.deviceType" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>型号</label>
            <input v-model="createForm.model" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>制造商</label>
            <input v-model="createForm.manufacturer" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>安装位置</label>
            <input v-model="createForm.installLocation" placeholder="可选" />
          </div>
          <p v-if="createError" class="error-msg">{{ createError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showCreate = false">取消</button>
            <button type="submit" class="btn primary" :disabled="creating">确定</button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showTelemetry" class="modal-mask" @click.self="showTelemetry = false">
      <div class="modal modal-lg">
        <h3>遥测数据 - {{ selectedDevice?.deviceName }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label>测点</label>
            <input v-model="telemetryField" placeholder="如 temperature" />
          </div>
          <button type="button" class="btn primary" @click="loadTelemetry">查询</button>
        </div>
        <div v-if="telemetryLoading" class="loading">加载中…</div>
        <div v-else class="telemetry-list">
          <div v-for="p in telemetryData" :key="p.time + p.field" class="telemetry-item">
            {{ p.field }}: {{ p.value }} @ {{ formatTime(p.time) }}
          </div>
          <p v-if="telemetryData.length === 0 && !telemetryLoading">暂无数据</p>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn" @click="showTelemetry = false">关闭</button>
        </div>
      </div>
    </div>
    <div v-if="showAlerts" class="modal-mask" @click.self="showAlerts = false">
      <div class="modal modal-lg">
        <h3>告警列表 - {{ selectedDevice?.deviceName }}</h3>
        <button type="button" class="btn primary" @click="openCreateAlert">新建告警</button>
        <div v-if="alertsLoading" class="loading">加载中…</div>
        <div v-else class="alerts-list">
          <div v-for="a in deviceAlerts" :key="a.id" class="alert-item" :class="{ pending: a.alertStatus === 0 }">
            <span class="alert-level">L{{ a.alertLevel }}</span>
            <span>{{ a.alertType }}: {{ a.alertContent }}</span>
            <span class="alert-time">{{ a.createTime }}</span>
            <button v-if="a.alertStatus === 0" type="button" class="btn small" @click="resolveAlert(a.id)">处理</button>
          </div>
          <p v-if="deviceAlerts.length === 0 && !alertsLoading">暂无告警</p>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn" @click="showAlerts = false">关闭</button>
        </div>
      </div>
    </div>
    <div v-if="showCreateAlert" class="modal-mask" @click.self="showCreateAlert = false">
      <div class="modal">
        <h3>新建告警</h3>
        <form v-if="selectedDevice" @submit.prevent="submitCreateAlert">
          <div class="form-group">
            <label>告警类型</label>
            <input v-model="alertForm.alertType" required placeholder="如 TEMPERATURE_HIGH" />
          </div>
          <div class="form-group">
            <label>告警级别 (1-4)</label>
            <input v-model.number="alertForm.alertLevel" type="number" min="1" max="4" required />
          </div>
          <div class="form-group">
            <label>告警内容</label>
            <textarea v-model="alertForm.alertContent" required rows="3"></textarea>
          </div>
          <p v-if="alertError" class="error-msg">{{ alertError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showCreateAlert = false">取消</button>
            <button type="submit" class="btn primary" :disabled="alertCreating">确定</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
  getDeviceList,
  getDeviceStats,
  registerDevice,
  updateDevice,
  deviceOnline,
  deviceOffline,
  deviceStart,
  deviceStop,
  deviceFault,
  deleteDevice,
  queryTelemetry,
  getDeviceAlerts,
  createDeviceAlert,
  resolveDeviceAlert,
  type DeviceRegisterRequest,
  type DeviceUpdateRequest,
  type DeviceDTO,
  type DeviceStatsDTO,
  type DeviceTelemetryPoint,
  type DeviceAlertDTO
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
const telemetryLoading = ref(false);
const deviceAlerts = ref<DeviceAlertDTO[]>([]);
const alertsLoading = ref(false);
const alertForm = ref({ alertType: '', alertLevel: 2, alertContent: '' });
const alertError = ref('');
const alertCreating = ref(false);

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
  loadTelemetry();
}

async function loadTelemetry() {
  if (!selectedDevice.value) return;
  telemetryLoading.value = true;
  try {
    telemetryData.value = await queryTelemetry(selectedDevice.value.id, {
      field: telemetryField.value || undefined,
      limit: 100
    });
  } catch {
    telemetryData.value = [];
  } finally {
    telemetryLoading.value = false;
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

onMounted(load);

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

async function doOnline(id: number) {
  try {
    await deviceOnline(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doOffline(id: number) {
  try {
    await deviceOffline(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doStart(id: number) {
  try {
    await deviceStart(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doStop(id: number) {
  try {
    await deviceStop(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doFault(id: number) {
  try {
    await deviceFault(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function doDelete(id: number) {
  if (!confirm('确定删除该设备？')) return;
  try {
    await deleteDevice(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; margin-right: 0.25rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.form-group input.readonly { opacity: 0.7; cursor: not-allowed; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
.stats-bar { display: flex; gap: 1rem; margin-bottom: 1rem; font-size: 0.9rem; color: #94a3b8; }
.stats-bar .online { color: #22c55e; }
.stats-bar .fault { color: #f87171; }
.modal-lg { min-width: 480px; }
.form-row { display: flex; gap: 0.5rem; align-items: flex-end; margin-bottom: 1rem; }
.form-row .form-group { margin-bottom: 0; flex: 1; }
.telemetry-list, .alerts-list { max-height: 300px; overflow-y: auto; margin-bottom: 1rem; }
.telemetry-item, .alert-item { padding: 0.5rem; border-bottom: 1px solid #334155; font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem; }
.alert-item.pending { background: rgba(248, 113, 113, 0.1); }
.alert-level { font-weight: 600; color: #f87171; min-width: 2rem; }
.alert-time { margin-left: auto; font-size: 0.8rem; color: #64748b; }
.form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; resize: vertical; }
</style>
