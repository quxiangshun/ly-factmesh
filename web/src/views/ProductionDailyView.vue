<template>
  <section class="page">
    <h1 class="page-title">生产日报</h1>
    <p class="page-desc">按日统计完成工单数、产量及进行中/暂停数量</p>
    <div class="toolbar">
      <input v-model="filterDate" type="date" class="filter-input" />
      <button type="button" class="btn primary" @click="load">查询</button>
      <button type="button" class="btn" @click="setToday">今天</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else-if="summary">
      <div class="summary-cards">
        <div class="card">
          <span class="card-label">统计日期</span>
          <span class="card-value">{{ summary.date || '-' }}</span>
        </div>
        <div class="card">
          <span class="card-label">已完成工单数</span>
          <span class="card-value highlight">{{ summary.completedCount ?? 0 }}</span>
        </div>
        <div class="card">
          <span class="card-label">已完成产量</span>
          <span class="card-value highlight">{{ summary.completedQuantity ?? 0 }}</span>
        </div>
        <div class="card">
          <span class="card-label">进行中工单数</span>
          <span class="card-value">{{ summary.inProgressCount ?? 0 }}</span>
        </div>
        <div class="card">
          <span class="card-label">暂停工单数</span>
          <span class="card-value">{{ summary.pausedCount ?? 0 }}</span>
        </div>
      </div>
      <div class="detail-section">
        <h3 class="section-title">当日完成工单明细</h3>
        <div v-if="!detailList.length" class="empty-state">当日无完成工单</div>
        <div v-else class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>工单编码</th>
                <th>产品</th>
                <th>计划数量</th>
                <th>实际产量</th>
                <th>完成时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detailList" :key="row.id">
                <td>{{ row.orderCode }}</td>
                <td>{{ row.productName }} ({{ row.productCode }})</td>
                <td>{{ row.planQuantity }}</td>
                <td>{{ row.actualQuantity }}</td>
                <td>{{ row.endTime ? formatTime(row.endTime) : '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getWorkOrderSummary, getWorkOrderSummaryDetail, type WorkOrderSummaryDTO, type WorkOrderDTO } from '@/api/workOrders';

const summary = ref<WorkOrderSummaryDTO | null>(null);
const detailList = ref<WorkOrderDTO[]>([]);
const loading = ref(true);
const error = ref('');
const filterDate = ref('');

function formatTime(s?: string) {
  if (!s) return '-';
  const d = new Date(s);
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
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
    const date = filterDate.value || undefined;
    const [s, d] = await Promise.all([
      getWorkOrderSummary(date),
      getWorkOrderSummaryDetail(date, 1, 50)
    ]);
    summary.value = s;
    detailList.value = d || [];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
    summary.value = null;
    detailList.value = [];
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
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1.5rem; display: flex; gap: 0.5rem; align-items: center; }
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.summary-cards { display: flex; flex-wrap: wrap; gap: 1rem; }
.card { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 1rem 1.25rem; min-width: 140px; }
.card-label { display: block; font-size: 0.8rem; color: #94a3b8; margin-bottom: 0.25rem; }
.card-value { font-size: 1.25rem; font-weight: 600; color: #e5e7eb; }
.card-value.highlight { color: #38bdf8; }
.detail-section { margin-top: 1.5rem; }
.section-title { font-size: 1rem; color: #e5e7eb; margin: 0 0 0.75rem; }
.empty-state { color: #94a3b8; padding: 1.5rem; text-align: center; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
</style>
