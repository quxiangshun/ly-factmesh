<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="按日统计完成工单数、产量及进行中/暂停数量" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-date-picker v-model="filterDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" clearable class="filter-input" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="setToday">今天</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else-if="summary">
      <div class="summary-cards">
        <el-card shadow="never" class="summary-card">
          <span class="card-label">统计日期</span>
          <span class="card-value">{{ summary.date || '-' }}</span>
        </el-card>
        <el-card shadow="never" class="summary-card">
          <span class="card-label">已完成工单数</span>
          <span class="card-value highlight">{{ summary.completedCount ?? 0 }}</span>
        </el-card>
        <el-card shadow="never" class="summary-card">
          <span class="card-label">已完成产量</span>
          <span class="card-value highlight">{{ summary.completedQuantity ?? 0 }}</span>
        </el-card>
        <el-card shadow="never" class="summary-card">
          <span class="card-label">进行中工单数</span>
          <span class="card-value">{{ summary.inProgressCount ?? 0 }}</span>
        </el-card>
        <el-card shadow="never" class="summary-card">
          <span class="card-label">暂停工单数</span>
          <span class="card-value">{{ summary.pausedCount ?? 0 }}</span>
        </el-card>
      </div>
      <div class="detail-section">
        <h3 class="section-title">当日完成工单明细</h3>
        <el-empty v-if="!detailList.length" description="当日无完成工单" />
        <el-table v-else :data="detailList" class="table-wrap">
          <el-table-column prop="orderCode" label="工单编码" />
          <el-table-column label="产品">
            <template #default="{ row }">{{ row.productName }} ({{ row.productCode }})</template>
          </el-table-column>
          <el-table-column prop="planQuantity" label="计划数量" />
          <el-table-column prop="actualQuantity" label="实际产量" />
          <el-table-column label="完成时间">
            <template #default="{ row }">{{ row.endTime ? formatTime(row.endTime) : '-' }}</template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
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
.toolbar { margin-bottom: 1.5rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.filter-input { width: 180px; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.error-alert { margin-bottom: 1rem; }
.summary-cards { display: flex; flex-wrap: wrap; gap: 1rem; }
.summary-card {
  min-width: 140px;
  background: #1e293b;
  border: 1px solid #334155;
}
.summary-card :deep(.el-card__body) { padding: 1rem 1.25rem; }
.card-label { display: block; font-size: 0.8rem; color: #94a3b8; margin-bottom: 0.25rem; }
.card-value { font-size: 1.25rem; font-weight: 600; color: #e5e7eb; }
.card-value.highlight { color: #38bdf8; }
.detail-section { margin-top: 1.5rem; }
.section-title { font-size: 1rem; color: #e5e7eb; margin: 0 0 0.75rem; }
.table-wrap { margin-bottom: 1rem; }
</style>
