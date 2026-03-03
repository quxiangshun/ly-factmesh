<template>
  <section class="page">
    <h1 class="page-title">质量追溯</h1>
    <p class="page-desc">按产品编码、批次号、工单查询质量追溯记录</p>
    <div class="toolbar">
      <input v-model="filterProductCode" placeholder="产品编码" class="filter-input" />
      <input v-model="filterBatchNo" placeholder="批次号" class="filter-input" />
      <input v-model="filterProductionOrder" placeholder="工单编码" class="filter-input" />
      <button type="button" class="btn primary" @click="load">查询</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div v-if="!list.length" class="empty-state">暂无追溯记录</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>产品编码</th>
              <th>批次号</th>
              <th>物料批次</th>
              <th>工单</th>
              <th>质检记录</th>
              <th>不良品</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in list" :key="row.id">
              <td>{{ row.productCode || '-' }}</td>
              <td>{{ row.batchNo || '-' }}</td>
              <td>{{ row.materialBatch || '-' }}</td>
              <td>{{ row.productionOrder || '-' }}</td>
              <td>{{ row.inspectionRecordId ?? '-' }}</td>
              <td>{{ row.nonConformingId ?? '-' }}</td>
              <td>{{ formatTime(row.createTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { traceQuality } from '@/api/qualityTrace';

const list = ref<Awaited<ReturnType<typeof traceQuality>>>([]);
const loading = ref(true);
const error = ref('');
const filterProductCode = ref('');
const filterBatchNo = ref('');
const filterProductionOrder = ref('');

function formatTime(t?: string) {
  if (!t) return '-';
  try {
    return new Date(t).toLocaleString('zh-CN');
  } catch {
    return t;
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    list.value = await traceQuality(
      filterProductCode.value || undefined,
      filterBatchNo.value || undefined,
      filterProductionOrder.value || undefined
    );
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
    list.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; width: 140px; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
</style>
