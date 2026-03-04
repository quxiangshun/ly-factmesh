<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="设备综合效率（OEE）= 可用率 × 性能率 × 良品率。当前展示产线产能统计，完整 OEE 需聚合 IoT 设备运行时长与 MES 产量。" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-date-picker v-model="filterDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" clearable class="filter-input" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="setToday">今天</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <div class="summary-bar">
        <span class="summary-label">统计日期：</span>
        <span class="summary-value">{{ filterDate || '当天' }}</span>
      </div>
      <el-empty v-if="!capacityList.length" description="暂无产能数据" />
      <el-table v-else :data="capacityList" class="table-wrap">
        <el-table-column prop="lineCode" label="产线编码" />
        <el-table-column prop="lineName" label="产线名称" />
        <el-table-column label="状态">
          <template #default="{ row }">{{ lineStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="date" label="统计日期">
          <template #default="{ row }">{{ row.date || '-' }}</template>
        </el-table-column>
        <el-table-column label="完成工单数">
          <template #default="{ row }">{{ row.completedOrderCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="完成产量">
          <template #default="{ row }">{{ row.completedQuantity ?? 0 }}</template>
        </el-table-column>
      </el-table>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
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
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.filter-input { width: 180px; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.error-alert { margin-bottom: 1rem; }
.summary-bar { font-size: 0.9rem; color: #94a3b8; margin-bottom: 1rem; }
.summary-bar .summary-value { color: #38bdf8; font-weight: 500; }
.table-wrap { margin-bottom: 1rem; }
</style>
