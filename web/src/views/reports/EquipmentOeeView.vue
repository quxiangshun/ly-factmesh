<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="title-with-tip">
          <span class="tip-trigger" title="功能说明" @click.stop="showTip = !showTip">
            <Icon icon="mdi:information-outline" class="tip-icon" />
          </span>
          <div v-if="showTip" class="tip-popover" @click.stop>
            <div class="tip-content">设备综合效率（OEE）= 可用率 × 性能率 × 良品率。当前展示产线产能统计，完整 OEE 需聚合 IoT 设备运行时长与 MES 产量。</div>
          </div>
        </div>
        <input v-model="filterDate" type="date" class="filter-input" />
      <button type="button" class="btn primary" @click="load">查询</button>
        <button type="button" class="btn" @click="setToday">今天</button>
      </div>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="summary-bar">
        <span class="summary-label">统计日期：</span>
        <span class="summary-value">{{ filterDate || '当天' }}</span>
      </div>
      <div v-if="!capacityList.length" class="empty-state">暂无产能数据</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>产线编码</th>
              <th>产线名称</th>
              <th>状态</th>
              <th>统计日期</th>
              <th>完成工单数</th>
              <th>完成产量</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in capacityList" :key="row.lineId">
              <td>{{ row.lineCode }}</td>
              <td>{{ row.lineName }}</td>
              <td>{{ lineStatusLabel(row.status) }}</td>
              <td>{{ row.date || '-' }}</td>
              <td>{{ row.completedOrderCount ?? 0 }}</td>
              <td>{{ row.completedQuantity ?? 0 }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import { getCapacitySummary, type ProductionLineCapacityDTO } from '@/api/lines';

const capacityList = ref<ProductionLineCapacityDTO[]>([]);
const loading = ref(true);
const error = ref('');
const filterDate = ref('');

function lineStatusLabel(s?: number) {
  if (s === 0) return '空闲';
  if (s === 1) return '运行';
  if (s === 2) return '检修';
  return '-';
}

function setToday() {
  const d = new Date();
  filterDate.value = d.toISOString().slice(0, 10);
  load();
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    capacityList.value = await getCapacitySummary(filterDate.value || undefined);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
    capacityList.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  filterDate.value = new Date().toISOString().slice(0, 10);
  load();
});
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.summary-bar { font-size: 0.9rem; color: #94a3b8; margin-bottom: 1rem; }
.summary-bar .summary-value { color: #38bdf8; font-weight: 500; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
</style>
