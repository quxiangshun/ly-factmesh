<template>
  <section class="page simulator-page">
    <div class="page-header">
      <h2 class="page-title">工业协议模拟</h2>
      <div class="title-with-tip">
        <span class="tip-trigger" title="使用说明" @click.stop="showTip = !showTip">
          <Icon icon="mdi:information-outline" class="tip-icon" />
        </span>
        <div v-if="showTip" class="tip-popover" @click.stop>
          <div class="tip-content">
            <p><strong>功能说明</strong></p>
            <p>工业协议数据模拟器，用于开发环境联调。可在下方配置更新间隔与设备数量，未设置时默认 5000ms、1 台设备。生产环境不部署此模块。</p>
            <p><strong>OPC UA 点位格式</strong></p>
            <p><code>ns=2;s=Device_{n}_Temperature</code>、<code>ns=2;s=Device_{n}_Humidity</code>，n 为设备序号（1～N）。</p>
            <p><strong>Modbus TCP 点位格式</strong></p>
            <p><code>{slaveId}_3_200</code>（压力）、<code>{slaveId}_3_201</code>（电压），slaveId=1～N 对应设备序号。</p>
            <p><strong>实际连接测试</strong></p>
            <p>真实 OPC UA / Modbus TCP 连接测试请使用 Prosys OPC UA Simulation Server、Modbus Slave 等工具。</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 配置区 -->
    <div class="sim-card config-card">
      <h3 class="card-title">运行时配置</h3>
      <div class="config-form">
        <div class="form-row">
          <label>更新间隔 (ms)</label>
          <input v-model.number="form.intervalMs" type="number" min="100" max="300000" placeholder="默认 5000" />
        </div>
        <div class="form-row">
          <label>设备数量</label>
          <input v-model.number="form.deviceCount" type="number" min="1" max="1000" placeholder="默认 1" />
        </div>
        <button type="button" class="btn primary small" :disabled="saving" @click="saveConfig">
          {{ saving ? '保存中...' : '保存配置' }}
        </button>
      </div>
    </div>

    <div class="simulator-grid">
      <!-- 状态 -->
      <div class="sim-card status-card">
        <h3 class="card-title">模拟器状态</h3>
        <div v-if="status" class="status-grid">
          <div class="status-item">
            <span class="status-label">OPC UA</span>
            <span class="status-value" :class="{ running: status.opcua?.running }">{{ status.opcua?.running ? '运行中' : '未运行' }}</span>
            <span class="status-note">{{ status.opcua?.endpoint }} · 间隔 {{ status.opcua?.intervalMs ?? 5000 }} ms · {{ status.opcua?.deviceCount ?? 1 }} 台</span>
          </div>
          <div class="status-item">
            <span class="status-label">Modbus TCP</span>
            <span class="status-value" :class="{ running: status.modbus?.running }">{{ status.modbus?.running ? '运行中' : '未运行' }}</span>
            <span class="status-note">{{ status.modbus?.host }}:{{ status.modbus?.port }} · 间隔 {{ status.modbus?.intervalMs ?? 5000 }} ms · {{ status.modbus?.deviceCount ?? 1 }} 台</span>
          </div>
        </div>
        <div v-else-if="error" class="error-text">{{ error }}</div>
        <div v-else class="loading-text">加载中...</div>
      </div>

      <!-- OPC UA 模拟值 -->
      <div class="sim-card values-card">
        <h3 class="card-title">OPC UA 模拟点位</h3>
        <div v-if="values?.opcua" class="values-list">
          <div v-for="(v, k) in values.opcua" :key="k" class="value-row">
            <span class="point-id">{{ k }}</span>
            <span class="point-value">{{ typeof v === 'number' ? v.toFixed(2) : v }}</span>
          </div>
        </div>
        <div v-else-if="values?.error" class="error-text">{{ values.error }}</div>
        <div v-else class="loading-text">加载中...</div>
      </div>

      <!-- Modbus TCP 模拟值 -->
      <div class="sim-card values-card">
        <h3 class="card-title">Modbus TCP 模拟点位</h3>
        <div v-if="values?.modbus" class="values-list">
          <div v-for="(v, k) in values.modbus" :key="k" class="value-row">
            <span class="point-id">{{ k }}</span>
            <span class="point-value">{{ v }}</span>
          </div>
        </div>
        <div v-else-if="values?.error" class="error-text">{{ values.error }}</div>
        <div v-else class="loading-text">加载中...</div>
      </div>
    </div>

    <div class="sim-footer">
      <span class="refresh-tip">每 {{ refreshSeconds }} 秒自动刷新</span>
      <button type="button" class="btn small" @click="loadData">手动刷新</button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import {
  getSimulatorConfig,
  getSimulatorStatus,
  getSimulatorValues,
  updateSimulatorConfig,
  type SimulatorStatus,
  type SimulatorValues
} from '@/api/simulator';

const showTip = ref(false);
const refreshSeconds = 5;
const status = ref<SimulatorStatus | null>(null);
const values = ref<SimulatorValues | Record<string, string> | null>(null);
const error = ref('');
const saving = ref(false);
const form = ref({ intervalMs: 5000, deviceCount: 1 });

let timer: ReturnType<typeof setInterval> | null = null;

async function loadConfig() {
  try {
    const c = await getSimulatorConfig();
    form.value = { intervalMs: c.intervalMs, deviceCount: c.deviceCount };
  } catch {
    form.value = { intervalMs: 5000, deviceCount: 1 };
  }
}

async function saveConfig() {
  saving.value = true;
  try {
    await updateSimulatorConfig(form.value);
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

function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}

onMounted(() => {
  loadConfig();
  loadData();
  timer = setInterval(loadData, refreshSeconds * 1000);
  document.addEventListener('click', closeTipOnClickOutside);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  document.removeEventListener('click', closeTipOnClickOutside);
});
</script>

<style scoped>
.simulator-page {
  padding: 1rem 0;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.25rem;
}
.page-title {
  font-size: 1.15rem;
  font-weight: 600;
  margin: 0;
  color: #e5e7eb;
}
.title-with-tip {
  position: relative;
  display: flex;
  align-items: center;
}
.tip-trigger {
  display: flex;
  align-items: center;
  padding: 0.25rem;
  cursor: pointer;
  color: #94a3b8;
  border-radius: 4px;
  transition: color 0.15s;
}
.tip-trigger:hover {
  color: #38bdf8;
}
.tip-icon {
  font-size: 1.2rem;
}
.tip-popover {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 0.35rem;
  min-width: 360px;
  max-width: 480px;
  padding: 1rem;
  background: #1e293b;
  border: 1px solid #475569;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.35);
  z-index: 100;
}
.tip-popover .tip-content p {
  margin: 0 0 0.6rem;
  font-size: 0.85rem;
  line-height: 1.5;
  color: #94a3b8;
}
.tip-popover .tip-content p:last-child {
  margin-bottom: 0;
}
.tip-popover .tip-content strong {
  color: #e5e7eb;
}
.tip-popover .tip-content code {
  font-size: 0.8rem;
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
  padding: 0.1rem 0.3rem;
  border-radius: 3px;
}
.simulator-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}
.sim-card {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 1rem 1.25rem;
}
.card-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 1rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.status-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.status-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.status-label {
  font-weight: 600;
  color: #e5e7eb;
}
.status-value {
  font-size: 0.9rem;
  color: #64748b;
}
.status-value.running {
  color: #22c55e;
}
.status-note {
  font-size: 0.75rem;
  color: #64748b;
}
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
.value-row:last-child {
  border-bottom: none;
}
.point-id {
  font-family: ui-monospace, monospace;
  color: #94a3b8;
  font-size: 0.8rem;
}
.point-value {
  font-weight: 600;
  color: #38bdf8;
}
.error-text,
.loading-text {
  font-size: 0.9rem;
  color: #94a3b8;
}
.error-text {
  color: #f87171;
}
.config-card {
  margin-bottom: 1rem;
}
.config-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 1rem;
}
.config-form .form-row {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.config-form .form-row label {
  font-size: 0.8rem;
  color: #94a3b8;
}
.config-form .form-row input {
  width: 140px;
  padding: 0.4rem 0.6rem;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid #334155;
  border-radius: 4px;
  color: #e5e7eb;
  font-size: 0.9rem;
}
.config-form .form-row input:focus {
  outline: none;
  border-color: #38bdf8;
}
.values-card .values-list {
  max-height: 280px;
  overflow-y: auto;
}
.sim-footer {
  margin-top: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}
.refresh-tip {
  font-size: 0.85rem;
  color: #64748b;
}

@media (max-width: 1024px) {
  .simulator-grid {
    grid-template-columns: 1fr;
  }
}
</style>
