<template>
  <section class="page simulator-page">
    <div class="page-header">
      <h2 class="page-title">工业协议模拟</h2>
      <el-tooltip placement="bottom">
        <template #content>
          <div class="tip-content">
            <p><strong>功能说明</strong></p>
            <p>工业协议数据模拟器，用于开发环境联调。左侧选择待模拟设备移至右侧，配置更新间隔。生产环境不部署此模块。</p>
            <p><strong>OPC UA 点位格式</strong></p>
            <p><code>ns=2;s=Device_{n}_Temperature</code>、<code>ns=2;s=Device_{n}_Humidity</code>，n 为设备序号。</p>
            <p><strong>Modbus TCP 点位格式</strong></p>
            <p><code>{slaveId}_3_200</code>（压力）、<code>{slaveId}_3_201</code>（电压）。</p>
            <p><strong>实际连接测试</strong></p>
            <p>真实 OPC UA / Modbus TCP 连接测试请使用 Prosys、Modbus Slave 等工具。</p>
          </div>
        </template>
        <el-icon class="tip-icon"><InfoFilled /></el-icon>
      </el-tooltip>
    </div>

    <div class="bars-row">
      <div class="config-bar">
        <div class="bar-title">运行配置</div>
        <div class="bar-body">
          <el-form-item label="更新间隔 (ms)" label-position="left" class="config-form-item">
            <el-input-number v-model="form.intervalMs" :min="100" :max="300000" placeholder="5000" />
          </el-form-item>
          <el-button type="primary" :disabled="saving" @click="saveConfig">
            {{ saving ? '保存中...' : '保存配置' }}
          </el-button>
        </div>
      </div>
      <div class="status-bar">
        <div class="bar-title">模拟器状态</div>
        <div class="bar-body">
          <div v-if="status" class="status-bar-items">
            <div class="status-bar-item">
              <span class="status-label">OPC UA</span>
              <span class="status-value" :class="{ running: status.opcua?.running }">{{ status.opcua?.running ? '运行中' : '未运行' }}</span>
              <span class="status-note">{{ status.opcua?.endpoint }} · {{ status.opcua?.intervalMs ?? 5000 }} ms · {{ status.opcua?.deviceCount ?? 1 }} 台</span>
            </div>
            <div class="status-bar-item">
              <span class="status-label">Modbus TCP</span>
              <span class="status-value" :class="{ running: status.modbus?.running }">{{ status.modbus?.running ? '运行中' : '未运行' }}</span>
              <span class="status-note">{{ status.modbus?.host }}:{{ status.modbus?.port }} · {{ status.modbus?.intervalMs ?? 5000 }} ms · {{ status.modbus?.deviceCount ?? 1 }} 台</span>
            </div>
          </div>
          <el-alert v-else-if="error" type="error" :title="error" show-icon />
          <div v-else class="loading-text">加载中...</div>
        </div>
      </div>
    </div>
    <div class="simulator-layout">
      <div class="transfer-section">
        <el-transfer
          v-model="form.deviceIds"
          :data="transferData"
          :titles="['可选设备', '已选设备']"
          filterable
          filter-placeholder="搜索设备"
          class="simulator-transfer"
        />
      </div>

      <div class="content-sidebar">
        <div class="values-row">
          <el-card class="values-card" shadow="never">
            <template #header>OPC UA 模拟点位</template>
            <div v-if="values?.error" class="values-list">
              <div class="value-empty error-text">{{ values.error }}</div>
            </div>
            <div v-else-if="!values" class="values-list">
              <div class="value-empty loading-text">加载中...</div>
            </div>
            <div v-else-if="values?.opcua && Object.keys(values.opcua).length" class="values-list">
              <div v-for="(v, k) in values.opcua" :key="k" class="value-row">
                <span class="point-id">{{ k }}</span>
                <span class="point-value">{{ typeof v === 'number' ? v.toFixed(2) : v }}</span>
              </div>
            </div>
            <div v-else class="values-list">
              <div class="value-empty">请先选择设备</div>
            </div>
          </el-card>
          <el-card class="values-card" shadow="never">
            <template #header>Modbus TCP 模拟点位</template>
            <div v-if="values?.error" class="values-list">
              <div class="value-empty error-text">{{ values.error }}</div>
            </div>
            <div v-else-if="!values" class="values-list">
              <div class="value-empty loading-text">加载中...</div>
            </div>
            <div v-else-if="values?.modbus && Object.keys(values.modbus).length" class="values-list">
              <div v-for="(v, k) in values.modbus" :key="k" class="value-row">
                <span class="point-id">{{ k }}</span>
                <span class="point-value">{{ v }}</span>
              </div>
            </div>
            <div v-else class="values-list">
              <div class="value-empty">请先选择设备</div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <div class="sim-footer">
      <span class="refresh-tip">每 {{ refreshSeconds }} 秒自动刷新</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import {
  getSimulatorConfig,
  getSimulatorStatus,
  getSimulatorValues,
  updateSimulatorConfig,
  type SimulatorStatus,
  type SimulatorValues
} from '@/api/simulator';
import { getDeviceList, type DeviceDTO } from '@/api/devices';

const refreshSeconds = 5;
const status = ref<SimulatorStatus | null>(null);
const values = ref<SimulatorValues | Record<string, string> | null>(null);
const error = ref('');
const saving = ref(false);
const deviceList = ref<DeviceDTO[]>([]);
const form = ref({ intervalMs: 5000, deviceIds: [] as string[] });

const transferData = computed(() =>
  deviceList.value.map((d) => ({
    key: String(d.id),
    label: `${d.deviceCode} - ${d.deviceName}`
  }))
);

async function loadDevices() {
  try {
    deviceList.value = await getDeviceList();
  } catch {
    deviceList.value = [];
  }
}

async function loadConfig() {
  try {
    const c = await getSimulatorConfig();
    form.value = {
      intervalMs: c.intervalMs,
      deviceIds: Array.isArray(c.deviceIds) ? c.deviceIds.map(String) : []
    };
  } catch {
    form.value = { intervalMs: 5000, deviceIds: [] };
  }
}

async function saveConfig() {
  saving.value = true;
  try {
    await updateSimulatorConfig({
      intervalMs: form.value.intervalMs,
      deviceIds: form.value.deviceIds
    });
    await loadData();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败';
  } finally {
    saving.value = false;
  }
}

async function loadData() {
  error.value = '';
  try {
    const [s, v] = await Promise.all([getSimulatorStatus(), getSimulatorValues()]);
    status.value = s;
    values.value = v;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
    values.value = { error: 'mom-simulator 未启动或网络异常' };
  }
}

let timer: ReturnType<typeof setInterval> | null = null;

onMounted(() => {
  loadDevices();
  loadConfig();
  loadData();
  timer = setInterval(loadData, refreshSeconds * 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
.simulator-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  max-width: 1400px;
  margin: 0 auto;
}
.page-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}
.bars-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  flex-shrink: 0;
}
.config-bar,
.status-bar {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 8px;
  min-width: 0;
}
.bar-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #94a3b8;
  flex-shrink: 0;
}
.bar-body {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
  min-width: 0;
}
.config-bar .bar-body :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0.5rem;
  display: inline-flex;
  align-items: center;
}
.config-bar .bar-body :deep(.el-form-item__label) {
  padding-bottom: 0;
}
.config-bar .bar-body :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
}
.simulator-layout {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr;
  gap: 0.75rem;
  overflow: hidden;
  align-items: stretch;
}
.transfer-section {
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.simulator-transfer {
  flex: 1;
  min-height: 200px;
}
.simulator-transfer :deep(.el-transfer-panel) {
  background: rgba(30, 41, 59, 0.6);
  border-color: #334155;
}
.content-sidebar {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  overflow: hidden;
}
.status-bar-items {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}
.status-bar-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.8rem;
}
.status-bar-item .status-label {
  font-weight: 600;
  color: #e5e7eb;
}
.status-bar-item .status-value {
  font-size: 0.8rem;
  color: #64748b;
}
.status-bar-item .status-value.running {
  color: #22c55e;
}
.status-bar-item .status-note {
  font-size: 0.7rem;
  color: #64748b;
}
.values-row {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  overflow: hidden;
}
.values-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.values-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}
.page-title {
  font-size: 1.15rem;
  font-weight: 600;
  margin: 0;
  color: #e5e7eb;
}
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.tip-content p { margin: 0 0 0.6rem; font-size: 0.85rem; line-height: 1.5; }
.tip-content code { font-size: 0.8rem; color: #38bdf8; }
.values-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.value-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4rem 0;
  border-bottom: 1px solid #334155;
  font-size: 0.9rem;
}
.value-row:last-child { border-bottom: none; }
.point-id {
  font-family: ui-monospace, monospace;
  color: #94a3b8;
  font-size: 0.8rem;
}
.point-value {
  font-weight: 600;
  color: #38bdf8;
}
.value-empty {
  padding: 1rem;
  font-size: 0.9rem;
  color: #64748b;
}
.error-text { color: #f87171; }
.loading-text { color: #94a3b8; }
.sim-footer {
  flex-shrink: 0;
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
}
.refresh-tip {
  font-size: 0.85rem;
  color: #64748b;
}

@media (max-width: 1200px) {
  .bars-row {
    grid-template-columns: 1fr;
  }
  .simulator-layout {
    grid-template-columns: 1fr;
  }
  .values-row {
    grid-template-columns: 1fr;
  }
}
</style>
