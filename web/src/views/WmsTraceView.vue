<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="按物料/批次/工单/领料单追溯出入库记录" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-select v-model="filterMaterialId" placeholder="全部物料" clearable class="filter-input" style="width: 180px">
          <el-option v-for="m in materials" :key="m.id" :value="m.id" :label="`${m.materialCode} - ${m.materialName}`" />
        </el-select>
        <el-input v-model="filterBatchNo" placeholder="批次号" class="filter-input" style="width: 120px" />
        <el-input v-model.number="filterOrderId" type="number" placeholder="工单ID" class="filter-input" style="width: 120px" />
        <el-input v-model.number="filterReqId" type="number" placeholder="领料单ID" class="filter-input" style="width: 120px" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无追溯记录" />
      <el-table v-else :data="pageData?.records" class="table-wrap">
        <el-table-column label="物料">
          <template #default="{ row }">{{ row.materialCode }} {{ row.materialName }}</template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次">
          <template #default="{ row }">{{ row.batchNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="类型">
          <template #default="{ row }">{{ txTypeLabel(row.transactionType) }}</template>
        </el-table-column>
        <el-table-column label="数量">
          <template #default="{ row }">
            <span :class="row.transactionType === 1 ? 'qty-in' : 'qty-out'">
              {{ row.transactionType === 1 ? '+' : '-' }}{{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="warehouse" label="仓库">
          <template #default="{ row }">{{ row.warehouse || '-' }}</template>
        </el-table-column>
        <el-table-column prop="orderId" label="工单">
          <template #default="{ row }">{{ row.orderId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reqId" label="领料单">
          <template #default="{ row }">{{ row.reqId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人">
          <template #default="{ row }">{{ row.operator || '-' }}</template>
        </el-table-column>
        <el-table-column prop="referenceNo" label="参考单号">
          <template #default="{ row }">{{ row.referenceNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.transactionTime) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="currentPage"
        :total="pageData?.total ?? 0"
        :page-size="pageSize"
        layout="prev, pager, next"
        class="pagination"
      />
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { traceInventory } from '@/api/inventory';
import { getMaterialPage, type MaterialDTO } from '@/api/materials';

const pageData = ref<Awaited<ReturnType<typeof traceInventory>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 20;
const filterMaterialId = ref<number | undefined>(undefined);
const filterBatchNo = ref('');
const filterOrderId = ref<number | undefined>(undefined);
const filterReqId = ref<number | undefined>(undefined);
const materials = ref<MaterialDTO[]>([]);

const totalPages = computed(() => Math.max(1, Math.ceil((pageData.value?.total ?? 0) / pageSize)));

function formatTime(t?: string) {
  if (!t) return '-';
  try {
    return new Date(t).toLocaleString('zh-CN');
  } catch {
    return t;
  }
}

function txTypeLabel(t?: number) {
  if (t === 1) return '入库';
  if (t === 2) return '出库';
  if (t === 3) return '调整';
  return '-';
}

async function loadOptions() {
  try {
    const res = await getMaterialPage(1, 500);
    materials.value = res?.records ?? [];
  } catch {
    materials.value = [];
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    pageData.value = await traceInventory(
      currentPage.value,
      pageSize,
      filterMaterialId.value,
      filterBatchNo.value || undefined,
      filterOrderId.value,
      filterReqId.value
    );
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);
onMounted(() => {
  loadOptions();
  load();
});
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.qty-in { color: #34d399; }
.qty-out { color: #f87171; }
.pagination { margin-top: 1rem; }
</style>
