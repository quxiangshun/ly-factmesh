<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="按产品编码、批次号、工单查询质量追溯记录" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-input v-model="filterProductCode" placeholder="产品编码" class="filter-input" style="width: 140px" />
        <el-input v-model="filterBatchNo" placeholder="批次号" class="filter-input" style="width: 140px" />
        <el-input v-model="filterProductionOrder" placeholder="工单编码" class="filter-input" style="width: 140px" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!list.length" description="暂无追溯记录" />
      <el-table v-else :data="list" class="table-wrap">
        <el-table-column prop="productCode" label="产品编码">
          <template #default="{ row }">{{ row.productCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号">
          <template #default="{ row }">{{ row.batchNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="materialBatch" label="物料批次">
          <template #default="{ row }">{{ row.materialBatch || '-' }}</template>
        </el-table-column>
        <el-table-column prop="productionOrder" label="工单">
          <template #default="{ row }">{{ row.productionOrder || '-' }}</template>
        </el-table-column>
        <el-table-column prop="inspectionRecordId" label="质检记录">
          <template #default="{ row }">{{ row.inspectionRecordId ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="nonConformingId" label="不良品">
          <template #default="{ row }">{{ row.nonConformingId ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
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
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
</style>
