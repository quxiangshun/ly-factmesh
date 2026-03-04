<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="toolbar-tip-wrap">
          <el-tooltip content="查询设备采集数据、历史曲线" placement="bottom">
            <el-icon class="tip-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <el-form :inline="true" class="filter-row" label-position="left" :label-width="60">
          <el-form-item label="设备">
            <el-select v-model="selectedDeviceId" placeholder="请选择设备" class="filter-select">
              <el-option v-for="d in devices" :key="d.id" :value="d.id" :label="`${d.deviceName} (${d.deviceCode})`" />
            </el-select>
          </el-form-item>
          <el-form-item label="测点">
            <el-input v-model="field" placeholder="如 temperature、humidity、voltage" class="input-sm" style="width: 180px" />
          </el-form-item>
          <el-form-item label="时间范围">
            <el-select v-model="timeRange" placeholder="选择" style="width: 140px">
              <el-option value="1h" label="最近1小时" />
              <el-option value="24h" label="最近24小时" />
              <el-option value="7d" label="最近7天" />
              <el-option value="30d" label="最近30天" />
            </el-select>
          </el-form-item>
          <el-form-item label-width="0">
            <el-button type="primary" :disabled="!selectedDeviceId" @click="loadData">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <div class="data-section">
        <h3 v-if="points.length > 0">共 {{ points.length }} 条数据</h3>
        <el-table :data="points" class="table-wrap" max-height="400">
          <el-table-column prop="field" label="测点" />
          <el-table-column prop="value" label="数值" />
          <el-table-column label="采集时间">
            <template #default="{ row }">{{ formatTime(row.time) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="points.length === 0" description="暂无数据，请选择设备并查询" />
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { getDeviceList, queryTelemetry, type DeviceDTO, type DeviceTelemetryPoint } from '@/api/devices';

const devices = ref<DeviceDTO[]>([]);
const selectedDeviceId = ref<string | number | null>(null);
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
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; flex-wrap: wrap; gap: 1rem; align-items: center; }
.toolbar-tip-wrap { display: flex; align-items: center; }
.filter-row { display: flex; flex-wrap: wrap; align-items: center; }
.filter-row :deep(.el-form-item) { margin-bottom: 0; margin-right: 0.5rem; }
.filter-row :deep(.el-form-item__label) { padding-right: 2px; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.filter-select { width: 200px; }
.error-alert { margin-bottom: 1rem; }
.data-section { margin-top: 1rem; }
.data-section h3 { margin: 0 0 0.5rem; font-size: 0.95rem; color: #94a3b8; }
.table-wrap { margin-bottom: 1rem; }
</style>
