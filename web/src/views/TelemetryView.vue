<template>
  <section class="page">
    <h1 class="page-title">设备遥测</h1>
    <p class="page-desc">查询设备采集数据、历史曲线</p>
    <div class="toolbar">
      <div class="filter-row">
        <div class="form-group">
          <label>设备</label>
          <select v-model="selectedDeviceId" class="select">
            <option :value="null">请选择设备</option>
            <option v-for="d in devices" :key="d.id" :value="d.id">{{ d.deviceName }} ({{ d.deviceCode }})</option>
          </select>
        </div>
        <div class="form-group">
          <label>测点</label>
          <input v-model="field" placeholder="如 temperature、humidity、voltage" class="input-sm" />
        </div>
        <div class="form-group">
          <label>时间范围</label>
          <select v-model="timeRange" class="select">
            <option value="1h">最近1小时</option>
            <option value="24h">最近24小时</option>
            <option value="7d">最近7天</option>
            <option value="30d">最近30天</option>
          </select>
        </div>
        <button type="button" class="btn primary" @click="loadData" :disabled="!selectedDeviceId">查询</button>
      </div>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="data-section">
        <h3 v-if="points.length > 0">共 {{ points.length }} 条数据</h3>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>测点</th>
                <th>数值</th>
                <th>采集时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(p, i) in points" :key="i">
                <td>{{ p.field }}</td>
                <td>{{ p.value }}</td>
                <td>{{ formatTime(p.time) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-if="points.length === 0 && !loading" class="empty-msg">暂无数据，请选择设备并查询</p>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getDeviceList, queryTelemetry, type DeviceDTO, type DeviceTelemetryPoint } from '@/api/devices';

const devices = ref<DeviceDTO[]>([]);
const selectedDeviceId = ref<number | null>(null);
const field = ref('');
const timeRange = ref('24h');
const points = ref<DeviceTelemetryPoint[]>([]);
const loading = ref(false);
const error = ref('');

function getStartEnd(): { start: string; end: string } {
  const end = new Date();
  let start: Date;
  switch (timeRange.value) {
    case '1h':
      start = new Date(end.getTime() - 60 * 60 * 1000);
      break;
    case '24h':
      start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
      break;
    case '7d':
      start = new Date(end.getTime() - 7 * 24 * 60 * 60 * 1000);
      break;
    case '30d':
      start = new Date(end.getTime() - 30 * 24 * 60 * 60 * 1000);
      break;
    default:
      start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
  }
  return {
    start: start.toISOString().slice(0, 19),
    end: end.toISOString().slice(0, 19)
  };
}

function formatTime(t?: string | { toString: () => string }) {
  if (!t) return '-';
  const s = typeof t === 'string' ? t : t.toString();
  try {
    return new Date(s).toLocaleString();
  } catch {
    return s;
  }
}

async function loadDevices() {
  try {
    devices.value = await getDeviceList();
    if (devices.value.length > 0 && !selectedDeviceId.value) {
      selectedDeviceId.value = devices.value[0].id;
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载设备列表失败';
  }
}

async function loadData() {
  if (!selectedDeviceId.value) return;
  loading.value = true;
  error.value = '';
  try {
    const { start, end } = getStartEnd();
    points.value = await queryTelemetry(selectedDeviceId.value, {
      field: field.value || undefined,
      start,
      end,
      limit: 1000
    });
  } catch (e) {
    error.value = e instanceof Error ? e.message : '查询失败';
    points.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadDevices();
  if (selectedDeviceId.value) loadData();
});
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; }
.filter-row { display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end; }
.form-group { display: flex; flex-direction: column; gap: 0.25rem; }
.form-group label { font-size: 0.875rem; color: #94a3b8; }
.select { width: 200px; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; }
.input-sm { width: 180px; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.data-section { margin-top: 1rem; }
.data-section h3 { margin: 0 0 0.5rem; font-size: 0.95rem; color: #94a3b8; }
.table-wrap { overflow-x: auto; max-height: 400px; overflow-y: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; position: sticky; top: 0; background: #1e293b; }
.empty-msg { color: #64748b; font-size: 0.9rem; margin-top: 1rem; }
</style>
