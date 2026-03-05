<template>
  <section ref="screenRef" class="big-screen" :class="{ fullscreen: isFullscreen }">
    <header class="screen-header">
      <h1 class="screen-title">LY-FactMesh 生产运营大屏</h1>
      <div class="screen-meta">
        <span class="now-time">{{ nowTime }}</span>
        <span class="refresh-tip">每 {{ refreshSeconds }} 秒自动刷新</span>
        <button
          type="button"
          class="fullscreen-btn"
          :title="isFullscreen ? '退出全屏' : '全屏'"
          @click="toggleFullscreen"
        >
          <Icon :icon="isFullscreen ? 'mdi:fullscreen-exit' : 'mdi:fullscreen'" />
        </button>
      </div>
    </header>

    <!-- 核心 KPI 大卡片 -->
    <div class="kpi-row">
      <div class="kpi-card primary">
        <span class="kpi-value">{{ todaySummary.completedQuantity ?? 0 }}</span>
        <span class="kpi-label">今日产量</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-value">{{ todaySummary.completedCount ?? 0 }}</span>
        <span class="kpi-label">今日完成工单</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-value">{{ deviceOnlineRate === '-' ? '无数据' : deviceOnlineRate + '%' }}</span>
        <span class="kpi-label">设备在线率</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-value">{{ qualityRate === '-' ? '无数据' : qualityRate + '%' }}</span>
        <span class="kpi-label">质检完成率</span>
      </div>
    </div>

    <!-- 待办预警 -->
    <div class="alert-row">
      <div class="alert-item" :class="{ warn: pendingAlerts > 0 }">
        <Icon icon="mdi:alert-circle" class="alert-icon" />
        <span>待处理告警</span>
        <strong>{{ pendingAlerts }}</strong>
      </div>
      <div class="alert-item" :class="{ warn: inspectionPending > 0 }">
        <Icon icon="mdi:clipboard-check" class="alert-icon" />
        <span>待检质检</span>
        <strong>{{ inspectionPending }}</strong>
      </div>
      <div class="alert-item" :class="{ warn: ncrPending > 0 }">
        <Icon icon="mdi:alert-octagon" class="alert-icon" />
        <span>待处理不合格品</span>
        <strong>{{ ncrPending }}</strong>
      </div>
      <div class="alert-item" :class="{ warn: belowSafeStockCount > 0 }">
        <Icon icon="mdi:package-variant" class="alert-icon" />
        <span>库存预警</span>
        <strong>{{ belowSafeStockCount }}</strong>
      </div>
    </div>

    <div class="screen-grid">
      <!-- 设备概览 -->
      <div class="panel device-panel">
        <h3 class="panel-title">设备状态</h3>
        <div class="stat-cards">
          <div class="stat-card total">
            <span class="stat-value">{{ deviceStats.total ?? '-' }}</span>
            <span class="stat-label">设备总数</span>
          </div>
          <div class="stat-card online">
            <span class="stat-value">{{ deviceStats.online ?? '-' }}</span>
            <span class="stat-label">在线</span>
          </div>
          <div class="stat-card fault">
            <span class="stat-value">{{ deviceStats.fault ?? '-' }}</span>
            <span class="stat-label">故障</span>
          </div>
        </div>
      </div>

      <!-- 工单概览 -->
      <div class="panel workorder-panel">
        <h3 class="panel-title">工单状态</h3>
        <div class="stat-cards">
          <div class="stat-card">
            <span class="stat-value">{{ workOrderStats.draftCount ?? '-' }}</span>
            <span class="stat-label">草稿</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">{{ workOrderStats.releasedCount ?? '-' }}</span>
            <span class="stat-label">已下发</span>
          </div>
          <div class="stat-card highlight">
            <span class="stat-value">{{ workOrderStats.inProgressCount ?? '-' }}</span>
            <span class="stat-label">进行中</span>
          </div>
          <div class="stat-card done">
            <span class="stat-value">{{ workOrderStats.completedCount ?? '-' }}</span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
      </div>

      <!-- 今日生产 -->
      <div class="panel today-panel">
        <h3 class="panel-title">今日生产</h3>
        <div class="today-stats">
          <div class="today-item">
            <span class="today-value">{{ todaySummary.completedCount ?? 0 }}</span>
            <span class="today-label">完成工单数</span>
          </div>
          <div class="today-item">
            <span class="today-value primary">{{ todaySummary.completedQuantity ?? 0 }}</span>
            <span class="today-label">完成产量</span>
          </div>
          <div class="today-item">
            <span class="today-value">{{ todaySummary.inProgressCount ?? 0 }}</span>
            <span class="today-label">进行中</span>
          </div>
          <div class="today-item">
            <span class="today-value">{{ todaySummary.pausedCount ?? 0 }}</span>
            <span class="today-label">暂停</span>
          </div>
        </div>
      </div>

      <!-- 产线产能图表 -->
      <div class="panel chart-panel wide">
        <h3 class="panel-title">产线产能（今日）</h3>
        <div ref="capacityChartRef" class="chart-container"></div>
      </div>

      <!-- 工单状态分布 -->
      <div class="panel chart-panel">
        <h3 class="panel-title">工单状态分布</h3>
        <div ref="statusChartRef" class="chart-container"></div>
      </div>

      <!-- 设备状态环形图 -->
      <div class="panel chart-panel">
        <h3 class="panel-title">设备状态分布</h3>
        <div ref="deviceChartRef" class="chart-container"></div>
      </div>

      <!-- 近7日产量趋势 -->
      <div class="panel chart-panel wide">
        <h3 class="panel-title">近7日产量趋势</h3>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <!-- 质检进度 -->
      <div class="panel chart-panel">
        <h3 class="panel-title">质检任务进度</h3>
        <div ref="inspectionChartRef" class="chart-container"></div>
      </div>
    </div>

    <div v-if="error" class="error-msg">{{ error }}</div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import * as echarts from 'echarts';
import { getDeviceStats, getPendingAlertCount } from '@/api/devices';
import { getWorkOrderStats, getWorkOrderSummary } from '@/api/workOrders';
import { getCapacitySummary } from '@/api/lines';
import { getInspectionTaskStats } from '@/api/inspectionTasks';
import { getNcrPage } from '@/api/ncr';
import { getInventoryBelowSafeStock } from '@/api/inventory';

const refreshSeconds = 30;
const nowTime = ref('');
const error = ref('');
const deviceStats = ref<{ total: number; online: number; fault: number }>({ total: 0, online: 0, fault: 0 });
const workOrderStats = ref<{
  total: number;
  draftCount: number;
  releasedCount: number;
  inProgressCount: number;
  completedCount: number;
}>({
  total: 0,
  draftCount: 0,
  releasedCount: 0,
  inProgressCount: 0,
  completedCount: 0
});
const todaySummary = ref<{
  date: string;
  completedCount: number;
  completedQuantity: number;
  inProgressCount: number;
  pausedCount: number;
}>({
  date: '',
  completedCount: 0,
  completedQuantity: 0,
  inProgressCount: 0,
  pausedCount: 0
});

const screenRef = ref<HTMLElement | null>(null);
const isFullscreen = ref(false);
const capacityChartRef = ref<HTMLElement | null>(null);
const statusChartRef = ref<HTMLElement | null>(null);
const deviceChartRef = ref<HTMLElement | null>(null);
const trendChartRef = ref<HTMLElement | null>(null);
const inspectionChartRef = ref<HTMLElement | null>(null);

const pendingAlerts = ref(0);
const inspectionStats = ref<{ total: number; draftCount: number; inProgressCount: number; completedCount: number }>({ total: 0, draftCount: 0, inProgressCount: 0, completedCount: 0 });
const ncrPending = ref(0);
const belowSafeStockCount = ref(0);
const trendData = ref<Array<{ date: string; quantity: number }>>([]);

const deviceOnlineRate = computed(() => {
  const t = deviceStats.value.total;
  const o = deviceStats.value.online;
  if (!t || t === 0) return '-';
  return Math.round((o / t) * 100);
});

const inspectionPending = computed(() => {
  return (inspectionStats.value.draftCount ?? 0) + (inspectionStats.value.inProgressCount ?? 0);
});

const qualityRate = computed(() => {
  const total = inspectionStats.value.total;
  const completed = inspectionStats.value.completedCount ?? 0;
  if (!total || total === 0) return '-';
  return Math.round((completed / total) * 100);
});

let capacityChart: echarts.ECharts | null = null;
let statusChart: echarts.ECharts | null = null;
let deviceChart: echarts.ECharts | null = null;
let trendChart: echarts.ECharts | null = null;
let inspectionChart: echarts.ECharts | null = null;
let refreshTimer: ReturnType<typeof setInterval> | null = null;

function updateNowTime() {
  const d = new Date();
  nowTime.value = d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
}

async function loadData() {
  error.value = '';
  try {
    const today = new Date().toISOString().slice(0, 10);
    const [dev, wo, summary, capacity, alertsRes, inspectionRes, ncrRes, invRes] = await Promise.all([
      getDeviceStats(),
      getWorkOrderStats(),
      getWorkOrderSummary(today),
      getCapacitySummary(today),
      getPendingAlertCount(),
      getInspectionTaskStats(),
      getNcrPage(1, 1, 0),
      getInventoryBelowSafeStock(1, 1)
    ]);
    deviceStats.value = dev;
    workOrderStats.value = wo;
    todaySummary.value = summary;
    pendingAlerts.value = alertsRes?.count ?? 0;
    inspectionStats.value = inspectionRes ?? { total: 0, draftCount: 0, inProgressCount: 0, completedCount: 0 };
    ncrPending.value = ncrRes?.total ?? 0;
    belowSafeStockCount.value = invRes?.total ?? 0;
    renderCapacityChart(capacity);
    renderStatusChart(wo);
    renderDeviceChart(dev);
    await loadTrendData();
    renderTrendChart(trendData.value);
    renderInspectionChart(inspectionStats.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '数据加载失败';
    deviceStats.value = { total: 0, online: 0, fault: 0 };
    workOrderStats.value = { total: 0, draftCount: 0, releasedCount: 0, inProgressCount: 0, completedCount: 0 };
    todaySummary.value = { date: '', completedCount: 0, completedQuantity: 0, inProgressCount: 0, pausedCount: 0 };
    pendingAlerts.value = 0;
    inspectionStats.value = { total: 0, draftCount: 0, inProgressCount: 0, completedCount: 0 };
    ncrPending.value = 0;
    belowSafeStockCount.value = 0;
    trendData.value = [];
  }
}

async function loadTrendData() {
  const list: Array<{ date: string; quantity: number }> = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    const dateStr = d.toISOString().slice(0, 10);
    try {
      const s = await getWorkOrderSummary(dateStr);
      list.push({ date: dateStr.slice(5), quantity: s.completedQuantity ?? 0 });
    } catch {
      list.push({ date: dateStr.slice(5), quantity: 0 });
    }
  }
  trendData.value = list;
}

function renderCapacityChart(data: Array<{ lineName: string; completedQuantity?: number }>) {
  if (!capacityChartRef.value) return;
  const list = data?.length ? data : [{ lineName: '暂无数据', completedQuantity: 0 }];
  if (!capacityChart) {
    capacityChart = echarts.init(capacityChartRef.value);
  }
  capacityChart.setOption({
    backgroundColor: 'transparent',
    grid: { left: 60, right: 40, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: list.map((d) => d.lineName),
      axisLine: { lineStyle: { color: '#475569' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#1e293b' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [
      {
        type: 'bar',
        data: list.map((d) => d.completedQuantity ?? 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#38bdf8' },
            { offset: 1, color: '#0ea5e9' }
          ])
        },
        barWidth: '50%'
      }
    ]
  });
}

function renderStatusChart(stats: typeof workOrderStats.value) {
  if (!statusChartRef.value) return;
  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value);
  }
  const items = [
    { value: stats.draftCount, name: '草稿' },
    { value: stats.releasedCount, name: '已下发' },
    { value: stats.inProgressCount, name: '进行中' },
    { value: stats.completedCount, name: '已完成' }
  ].filter((i) => i.value > 0);
  if (items.length === 0) items.push({ value: 1, name: '无数据' });
  statusChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    legend: { bottom: 5, textStyle: { color: '#94a3b8' } },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderColor: '#0f172a', borderWidth: 2 },
        label: { show: true, color: '#e5e7eb' },
        data: items,
        color: ['#64748b', '#3b82f6', '#38bdf8', '#22c55e']
      }
    ]
  });
}

function renderDeviceChart(stats: { total: number; online: number; fault: number }) {
  if (!deviceChartRef.value) return;
  if (!deviceChart) {
    deviceChart = echarts.init(deviceChartRef.value);
  }
  const items = [
    { value: stats.online, name: '在线' },
    { value: stats.fault, name: '故障' },
    { value: Math.max(0, stats.total - stats.online - stats.fault), name: '离线' }
  ].filter((i) => i.value > 0);
  if (items.length === 0) items.push({ value: 1, name: '无设备' });
  deviceChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    legend: { bottom: 5, textStyle: { color: '#94a3b8' } },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderColor: '#0f172a', borderWidth: 2 },
        label: { show: true, color: '#e5e7eb' },
        data: items,
        color: ['#22c55e', '#ef4444', '#64748b']
      }
    ]
  });
}

function renderTrendChart(data: Array<{ date: string; quantity: number }>) {
  if (!trendChartRef.value) return;
  if (!trendChart) trendChart = echarts.init(trendChartRef.value);
  trendChart.setOption({
    backgroundColor: 'transparent',
    grid: { left: 50, right: 30, top: 20, bottom: 35 },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.date),
      axisLine: { lineStyle: { color: '#475569' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#1e293b' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [{
      type: 'line',
      data: data.map((d) => d.quantity),
      smooth: true,
      areaStyle: { color: 'rgba(56, 189, 248, 0.2)' },
      lineStyle: { color: '#38bdf8' },
      itemStyle: { color: '#38bdf8' }
    }]
  });
}

function renderInspectionChart(stats: { draftCount: number; inProgressCount: number; completedCount: number }) {
  if (!inspectionChartRef.value) return;
  if (!inspectionChart) inspectionChart = echarts.init(inspectionChartRef.value);
  const items = [
    { value: stats.draftCount, name: '待检' },
    { value: stats.inProgressCount, name: '检验中' },
    { value: stats.completedCount, name: '已完成' }
  ].filter((i) => i.value > 0);
  if (items.length === 0) items.push({ value: 1, name: '无数据' });
  inspectionChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    legend: { bottom: 5, textStyle: { color: '#94a3b8' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderColor: '#0f172a', borderWidth: 2 },
      label: { show: true, color: '#e5e7eb' },
      data: items,
      color: ['#f59e0b', '#38bdf8', '#22c55e']
    }]
  });
}

function handleResize() {
  capacityChart?.resize();
  statusChart?.resize();
  deviceChart?.resize();
  trendChart?.resize();
  inspectionChart?.resize();
}

function toggleFullscreen() {
  if (!screenRef.value) return;
  if (!document.fullscreenElement) {
    screenRef.value.requestFullscreen?.().then(() => {
      isFullscreen.value = true;
    }).catch(() => {});
  } else {
    document.exitFullscreen?.().then(() => {
      isFullscreen.value = false;
    }).catch(() => {});
  }
}

function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement;
}

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange);
  updateNowTime();
  setInterval(updateNowTime, 1000);
  loadData();
  refreshTimer = setInterval(loadData, refreshSeconds * 1000);
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
  if (document.fullscreenElement) {
    document.exitFullscreen?.().catch(() => {});
  }
  if (refreshTimer) clearInterval(refreshTimer);
  window.removeEventListener('resize', handleResize);
  capacityChart?.dispose();
  statusChart?.dispose();
  deviceChart?.dispose();
  trendChart?.dispose();
  inspectionChart?.dispose();
});
</script>

<style scoped>
.big-screen {
  height: 100%;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #0f172a 0%, #020617 100%);
  padding: 0.5rem 0.75rem;
  color: #e5e7eb;
  box-sizing: border-box;
}
.screen-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #334155;
}
.screen-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(90deg, #38bdf8, #22c55e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.screen-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.8rem;
  color: #94a3b8;
}
.now-time { font-family: ui-monospace, monospace; }
.refresh-tip { opacity: 0.8; }
.fullscreen-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem 0.5rem;
  background: rgba(56, 189, 248, 0.15);
  border: 1px solid #38bdf8;
  border-radius: 6px;
  color: #38bdf8;
  cursor: pointer;
  font-size: 1rem;
}
.fullscreen-btn:hover {
  background: rgba(56, 189, 248, 0.25);
}

.kpi-row {
  flex-shrink: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}
.kpi-card {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  text-align: center;
}
.kpi-card.primary {
  border-color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
}
.kpi-value {
  display: block;
  font-size: 1.35rem;
  font-weight: 700;
  color: #e5e7eb;
}
.kpi-card.primary .kpi-value { color: #38bdf8; }
.kpi-label { font-size: 0.7rem; color: #94a3b8; }

.alert-row {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}
.alert-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.6rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #94a3b8;
}
.alert-item strong { color: #e5e7eb; margin-left: 0.2rem; }
.alert-item.warn {
  border-color: #f59e0b;
  background: rgba(245, 158, 11, 0.15);
  color: #fcd34d;
}
.alert-item.warn strong { color: #fbbf24; }
.alert-icon { font-size: 0.95rem; color: #64748b; }
.alert-item.warn .alert-icon { color: #f59e0b; }

.big-screen.fullscreen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.screen-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(4, minmax(0, 1fr));
  gap: 0.5rem;
}
.panel {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.panel.wide { grid-column: span 2; }
.panel-title {
  font-size: 0.75rem;
  font-weight: 600;
  margin: 0 0 0.35rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  flex-shrink: 0;
}

.stat-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.stat-card {
  flex: 1;
  min-width: 55px;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 4px;
  padding: 0.4rem 0.5rem;
  text-align: center;
  border: 1px solid #334155;
}
.stat-card.total { border-color: #475569; }
.stat-card.online { border-color: #22c55e; }
.stat-card.fault { border-color: #ef4444; }
.stat-card.highlight { border-color: #38bdf8; }
.stat-card.done { border-color: #22c55e; }
.stat-value {
  display: block;
  font-size: 1.15rem;
  font-weight: 700;
  color: #e5e7eb;
}
.stat-card.online .stat-value { color: #22c55e; }
.stat-card.fault .stat-value { color: #ef4444; }
.stat-card.highlight .stat-value { color: #38bdf8; }
.stat-card.done .stat-value { color: #22c55e; }
.stat-label { font-size: 0.65rem; color: #64748b; }

.today-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.4rem;
}
.today-item {
  text-align: center;
  padding: 0.35rem;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 4px;
  border: 1px solid #334155;
}
.today-value {
  display: block;
  font-size: 1rem;
  font-weight: 700;
  color: #e5e7eb;
}
.today-value.primary { color: #38bdf8; }
.today-label { font-size: 0.65rem; color: #64748b; }

.chart-container {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.error-msg {
  position: fixed;
  bottom: 0.5rem;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.4rem 0.8rem;
  background: #7f1d1d;
  color: #fecaca;
  border-radius: 4px;
  font-size: 0.8rem;
  z-index: 10;
}

@media (max-width: 1024px) {
  .screen-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  .panel.wide { grid-column: span 1; }
  .today-stats { grid-template-columns: repeat(2, 1fr); }
}
</style>
