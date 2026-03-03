<template>
  <section class="page">
    <p class="page-desc">MES 工单列表，支持下发、开始、完成</p>
    <div class="stats-bar" v-if="stats">
      <span>总数 {{ stats.total }}</span>
      <span>草稿 {{ stats.draftCount }}</span>
      <span>已下发 {{ stats.releasedCount }}</span>
      <span class="progress">进行中 {{ stats.inProgressCount }}</span>
      <span class="done">已完成 {{ stats.completedCount }}</span>
    </div>
    <div class="toolbar">
      <button type="button" class="btn primary" @click="showCreate = true">新建工单</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>编码</th>
              <th>产品</th>
              <th>计划/实际</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.orderCode }}</td>
              <td>{{ row.productName }} ({{ row.productCode }})</td>
              <td>{{ row.planQuantity }} / {{ row.actualQuantity }}</td>
              <td>{{ statusText(row.status) }}</td>
              <td>
                <template v-if="row.status === 0">
                  <button type="button" class="btn small" @click="doRelease(row.id)">下发</button>
                  <button type="button" class="btn small danger" @click="doDelete(row.id)">删除</button>
                </template>
                <template v-else-if="row.status === 1">
                  <button type="button" class="btn small" @click="doStart(row.id)">开始</button>
                </template>
                <template v-else-if="row.status === 2">
                  <button type="button" class="btn small" @click="openComplete(row)">完成</button>
                </template>
              </td>
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
    <div v-if="showComplete" class="modal-mask" @click.self="showComplete = false">
      <div class="modal">
        <h3>完成工单</h3>
        <p v-if="completeRow" class="modal-desc">{{ completeRow.orderCode }} - {{ completeRow.productName }}</p>
        <form @submit.prevent="submitComplete">
          <div class="form-group">
            <label>实际数量</label>
            <input v-model.number="completeActualQty" type="number" min="0" :placeholder="'计划: ' + (completeRow?.planQuantity ?? 0)" />
          </div>
          <p v-if="completeError" class="error-msg">{{ completeError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showComplete = false">取消</button>
            <button type="submit" class="btn primary" :disabled="completing">确定</button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建工单</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>工单编码</label>
            <input v-model="createForm.orderCode" required placeholder="如 WO-001" />
          </div>
          <div class="form-group">
            <label>产品编码</label>
            <input v-model="createForm.productCode" required placeholder="如 P001" />
          </div>
          <div class="form-group">
            <label>产品名称</label>
            <input v-model="createForm.productName" required placeholder="产品名称" />
          </div>
          <div class="form-group">
            <label>计划数量</label>
            <input v-model.number="createForm.planQuantity" type="number" min="1" required />
          </div>
          <p v-if="createError" class="error-msg">{{ createError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showCreate = false">取消</button>
            <button type="submit" class="btn primary" :disabled="creating">确定</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import {
  getWorkOrderPage,
  getWorkOrderStats,
  createWorkOrder,
  releaseWorkOrder,
  startWorkOrder,
  completeWorkOrder,
  deleteWorkOrder,
  type WorkOrderDTO,
  type WorkOrderCreateRequest
} from '@/api/workOrders';

const pageData = ref<Awaited<ReturnType<typeof getWorkOrderPage>> | null>(null);
const stats = ref<Awaited<ReturnType<typeof getWorkOrderStats>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;

const totalPages = computed(() => {
  const t = pageData.value?.total ?? 0;
  return Math.max(1, Math.ceil(t / pageSize));
});

function statusText(s: number) {
  const map: Record<number, string> = { 0: '草稿', 1: '已下发', 2: '进行中', 3: '已完成', 4: '已关闭' };
  return map[s] ?? String(s);
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const [pageRes, statsRes] = await Promise.all([
      getWorkOrderPage(currentPage.value, pageSize),
      getWorkOrderStats()
    ]);
    pageData.value = pageRes;
    stats.value = statsRes;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);
onMounted(load);

const showCreate = ref(false);
const createForm = ref<WorkOrderCreateRequest>({
  orderCode: '',
  productCode: '',
  productName: '',
  planQuantity: 100
});
const createError = ref('');
const creating = ref(false);

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createWorkOrder(createForm.value);
    showCreate.value = false;
    createForm.value = { orderCode: '', productCode: '', productName: '', planQuantity: 100 };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

async function doRelease(id: number) {
  try {
    await releaseWorkOrder(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '下发失败';
  }
}

async function doStart(id: number) {
  try {
    await startWorkOrder(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '开始失败';
  }
}

const showComplete = ref(false);
const completeRow = ref<WorkOrderDTO | null>(null);
const completeActualQty = ref<number | ''>('');
const completeError = ref('');
const completing = ref(false);

function openComplete(row: WorkOrderDTO) {
  completeRow.value = row;
  completeActualQty.value = row.actualQuantity ?? row.planQuantity ?? '';
  completeError.value = '';
  showComplete.value = true;
}

async function submitComplete() {
  if (!completeRow.value) return;
  completeError.value = '';
  completing.value = true;
  try {
    const qty = completeActualQty.value === '' ? undefined : Number(completeActualQty.value);
    await completeWorkOrder(completeRow.value.id, qty);
    showComplete.value = false;
    completeRow.value = null;
    completeActualQty.value = '';
    await load();
  } catch (e) {
    completeError.value = e instanceof Error ? e.message : '完成失败';
  } finally {
    completing.value = false;
  }
}

async function doDelete(id: number) {
  if (!confirm('确定删除该工单？')) return;
  try {
    await deleteWorkOrder(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.modal .modal-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.stats-bar { display: flex; gap: 1rem; margin-bottom: 1rem; font-size: 0.9rem; color: #94a3b8; }
.stats-bar .progress { color: #38bdf8; }
.stats-bar .done { color: #4ade80; }
.toolbar { margin-bottom: 1rem; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { display: flex; align-items: center; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
.pagination .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
