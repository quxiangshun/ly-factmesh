<template>
  <section class="page">
    <p class="page-desc">按物料/批次/工单/领料单追溯出入库记录</p>
    <div class="toolbar">
      <select v-model="filterMaterialId" class="filter-select">
        <option :value="undefined">全部物料</option>
        <option v-for="m in materials" :key="m.id" :value="m.id">{{ m.materialCode }} - {{ m.materialName }}</option>
      </select>
      <input v-model="filterBatchNo" placeholder="批次号" class="filter-input" />
      <input v-model.number="filterOrderId" type="number" placeholder="工单ID" class="filter-input" />
      <input v-model.number="filterReqId" type="number" placeholder="领料单ID" class="filter-input" />
      <button type="button" class="btn primary" @click="load">查询</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div v-if="!pageData?.records?.length" class="empty-state">暂无追溯记录</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>物料</th>
              <th>批次</th>
              <th>类型</th>
              <th>数量</th>
              <th>仓库</th>
              <th>工单</th>
              <th>领料单</th>
              <th>操作人</th>
              <th>参考单号</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.materialCode }} {{ row.materialName }}</td>
              <td>{{ row.batchNo || '-' }}</td>
              <td>{{ txTypeLabel(row.transactionType) }}</td>
              <td :class="row.transactionType === 1 ? 'qty-in' : 'qty-out'">
                {{ row.transactionType === 1 ? '+' : '-' }}{{ row.quantity }}
              </td>
              <td>{{ row.warehouse || '-' }}</td>
              <td>{{ row.orderId || '-' }}</td>
              <td>{{ row.reqId || '-' }}</td>
              <td>{{ row.operator || '-' }}</td>
              <td>{{ row.referenceNo || '-' }}</td>
              <td>{{ formatTime(row.transactionTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <button type="button" class="btn small" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
        <span class="page-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页，{{ pageData?.total ?? 0 }} 条</span>
        <button type="button" class="btn small" :disabled="currentPage >= totalPages" @click="currentPage++">下一页</button>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
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
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.filter-select { padding: 0.4rem 0.75rem; border-radius: 6px; background: #1e293b; color: #e5e7eb; border: 1px solid #475569; min-width: 180px; }
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; width: 120px; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.qty-in { color: #34d399; }
.qty-out { color: #f87171; }
.pagination { display: flex; align-items: center; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
</style>
