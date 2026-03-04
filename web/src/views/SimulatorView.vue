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
            <p>工业协议数据模拟器，用于开发环境联调。左侧选择待模拟设备移至右侧，配置更新间隔。生产环境不部署此模块。</p>
            <p><strong>OPC UA 点位格式</strong></p>
            <p><code>ns=2;s=Device_{n}_Temperature</code>、<code>ns=2;s=Device_{n}_Humidity</code>，n 为设备序号。</p>
            <p><strong>Modbus TCP 点位格式</strong></p>
            <p><code>{slaveId}_3_200</code>（压力）、<code>{slaveId}_3_201</code>（电压）。</p>
            <p><strong>实际连接测试</strong></p>
            <p>真实 OPC UA / Modbus TCP 连接测试请使用 Prosys、Modbus Slave 等工具。</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 主布局：左侧穿梭框，右侧状态与点位竖排 -->
    <div class="simulator-layout">
      <div class="transfer-section">
        <!-- 配置栏：放设备选择框顶部 -->
        <div class="config-bar">
          <span class="config-label">运行配置</span>
          <div class="config-field">
            <label>更新间隔 (ms)</label>
            <input v-model.number="form.intervalMs" type="number" min="100" max="300000" placeholder="5000" />
          </div>
          <button type="button" class="btn primary" :disabled="saving" @click="saveConfig">
            {{ saving ? '保存中...' : '保存配置' }}
          </button>
        </div>
        <div class="transfer-wrapper">
          <div class="transfer-panel">
            <div class="transfer-panel-title">可选设备</div>
            <div class="transfer-list" :class="{ empty: !availableDevices.length }">
              <div
                v-for="d in availableDevices"
                :key="d.id"
                class="transfer-item"
                :class="{ selected: leftSelected.has(String(d.id)) }"
                @click="toggleLeftSelect(String(d.id))"
              >
                {{ d.deviceCode }} - {{ d.deviceName }}
              </div>
              <div v-if="!availableDevices.length" class="transfer-empty">暂无可选设备</div>
            </div>
          </div>
          <div class="transfer-actions">
            <button type="button" class="transfer-btn" :disabled="!leftSelected.size" @click="moveToRight">
              <Icon icon="mdi:chevron-right" />
            </button>
            <button type="button" class="transfer-btn" :disabled="!availableDevices.length" @click="moveAllToRight">
              <Icon icon="mdi:chevron-double-right" />
            </button>
            <button type="button" class="transfer-btn" :disabled="!rightSelected.size" @click="moveToLeft">
              <Icon icon="mdi:chevron-left" />
            </button>
            <button type="button" class="transfer-btn" :disabled="!selectedDevices.length" @click="moveAllToLeft">
              <Icon icon="mdi:chevron-double-left" />
            </button>
          </div>
          <div class="transfer-panel">
            <div class="transfer-panel-title">已选设备</div>
            <div class="transfer-list" :class="{ empty: !selectedDevices.length }">
              <div
                v-for="d in selectedDevices"
                :key="d.id"
                class="transfer-item"
                :class="{ selected: rightSelected.has(String(d.id)) }"
                @click="toggleRightSelect(String(d.id))"
              >
                {{ d.deviceCode }} - {{ d.deviceName }}
              </div>
              <div v-if="!selectedDevices.length" class="transfer-empty">请从左侧选择设备</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：状态横排 + 两个模拟点位并排 -->
      <div class="content-sidebar">
        <!-- 模拟器状态：横排类似运行配置 -->
        <div class="status-bar">
          <span class="status-bar-label">模拟器状态</span>
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
          <div v-else-if="error" class="error-text">{{ error }}</div>
          <div v-else class="loading-text">加载中...</div>
        </div>
        <!-- 两个模拟点位并排，高度与设备选择一致 -->
        <div class="values-row">
          <div class="sim-card values-card">
            <h3 class="card-title">OPC UA 模拟点位</h3>
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
          </div>
          <div class="sim-card values-card">
            <h3 class="card-title">Modbus TCP 模拟点位</h3>
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
          </div>
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
import { Icon } from '@iconify/vue';
import {
  getSimulatorConfig,
  getSimulatorStatus,
  getSimulatorValues,
  updateSimulatorConfig,
  type SimulatorStatus,
  type SimulatorValues
} from '@/api/simulator';
import { getDeviceList, type DeviceDTO } from '@/api/devices';

const showTip = ref(false);
const refreshSeconds = 5;
const status = ref<SimulatorStatus | null>(null);
const values = ref<SimulatorValues | Record<string, string> | null>(null);
const error = ref('');
const saving = ref(false);
const deviceList = ref<DeviceDTO[]>([]);
const form = ref({ intervalMs: 5000, deviceIds: [] as string[] });
const leftSelected = ref<Set<string>>(new Set());
const rightSelected = ref<Set<string>>(new Set());

const selectedIdSet = computed(() => new Set(form.value.deviceIds));
const availableDevices = computed(() =>
  deviceList.value.filter((d) => !selectedIdSet.value.has(String(d.id)))
);
const selectedDevices = computed(() =>
  deviceList.value.filter((d) => selectedIdSet.value.has(String(d.id)))
);

function toggleLeftSelect(id: string) {
  const s = new Set(leftSelected.value);
  if (s.has(id)) s.delete(id);
  else s.add(id);
  leftSelected.value = s;
}
function toggleRightSelect(id: string) {
  const s = new Set(rightSelected.value);
  if (s.has(id)) s.delete(id);
  else s.add(id);
  rightSelected.value = s;
}
function moveToRight() {
  const ids = Array.from(leftSelected.value);
  form.value.deviceIds = [...form.value.deviceIds, ...ids];
  leftSelected.value = new Set();
}
function moveAllToRight() {
  form.value.deviceIds = deviceList.value.map((d) => String(d.id));
  leftSelected.value = new Set();
}
function moveToLeft() {
  const toRemove = new Set(rightSelected.value);
  form.value.deviceIds = form.value.deviceIds.filter((id) => !toRemove.has(id));
  rightSelected.value = new Set();
}
function moveAllToLeft() {
  form.value.deviceIds = [];
  rightSelected.value = new Set();
}

let timer: ReturnType<typeof setInterval> | null = null;

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

function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}

onMounted(() => {
  loadDevices();
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
.simulator-layout {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 2fr 3fr;
  grid-template-rows: 1fr;
  gap: 1rem;
  overflow: hidden;
  align-items: stretch;
}
.config-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  padding: 0.5rem 0.75rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 8px;
}
.config-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #94a3b8;
}
.config-field {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.config-field label {
  font-size: 0.8rem;
  color: #64748b;
  white-space: nowrap;
}
.config-field input {
  width: 100px;
  padding: 0.4rem 0.6rem;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid #334155;
  border-radius: 4px;
  color: #e5e7eb;
  font-size: 0.9rem;
}
.config-field input:focus {
  outline: none;
  border-color: #38bdf8;
}
.transfer-section {
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.transfer-wrapper {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: stretch;
  gap: 0.75rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 0.75rem;
}
.transfer-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.transfer-panel-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: #94a3b8;
  text-align: center;
}
.transfer-list {
  flex: 1;
  min-height: 120px;
  overflow-y: auto;
  border: 1px solid #334155;
  border-radius: 6px;
  background: rgba(15, 23, 42, 0.5);
}
.transfer-list.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}
.transfer-item {
  padding: 0.4rem 0;
  font-size: 0.6rem;
  color: #94a3b8;
  cursor: pointer;
  border-bottom: 1px solid #334155;
  transition: background 0.15s;
}
.transfer-item:last-child {
  border-bottom: none;
}
.transfer-item:hover {
  background: rgba(56, 189, 248, 0.1);
}
.transfer-item.selected {
  background: rgba(56, 189, 248, 0.2);
  color: #38bdf8;
}
.transfer-empty {
  color: #64748b;
  font-size: 0.6rem;
  padding: 1rem;
}
.transfer-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.5rem;
  padding: 0 0.25rem;
}
.transfer-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid #475569;
  border-radius: 6px;
  background: #1e293b;
  color: #94a3b8;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.transfer-btn:hover:not(:disabled) {
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
  border-color: #38bdf8;
}
.transfer-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.transfer-btn svg {
  width: 1.25rem;
  height: 1.25rem;
}
.content-sidebar {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  overflow: hidden;
}
.status-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.4rem 0.6rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 8px;
}
.status-bar-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #94a3b8;
  white-space: nowrap;
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
.values-row .values-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.values-row .values-card .values-list {
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
.sim-card {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 0.75rem 1rem;
}
.values-row .sim-card .card-title {
  margin: 0 0 0.5rem;
  font-size: 0.85rem;
}
.card-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 1rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
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
.value-empty {
  padding: 1rem;
  font-size: 0.9rem;
  color: #64748b;
}
.error-text,
.loading-text {
  font-size: 0.9rem;
  color: #94a3b8;
}
.error-text {
  color: #f87171;
}
.values-card .values-list {
  overflow-y: auto;
}
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
  .simulator-layout {
    grid-template-columns: 1fr;
  }
  .values-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .transfer-wrapper {
    flex-direction: column;
  }
  .transfer-actions {
    flex-direction: row;
    justify-content: center;
  }
  .config-bar {
    flex-wrap: wrap;
  }
  .status-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
