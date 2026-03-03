<template>
  <section class="page">
    <h1 class="page-title">领料单</h1>
    <p class="page-desc">生产领料/退料，MES 工单联动</p>
    <div class="toolbar">
      <select v-model="filterOrderId" class="filter-select">
        <option :value="undefined">全部工单</option>
        <option v-for="wo in workOrders" :key="wo.id" :value="wo.id">{{ wo.orderCode }} - {{ wo.productName }}</option>
      </select>
      <select v-model="filterStatus" class="filter-select">
        <option :value="undefined">全部状态</option>
        <option :value="0">草稿</option>
        <option :value="1">已提交</option>
        <option :value="2">已完成</option>
      </select>
      <button type="button" class="btn primary" @click="showCreate = true">新建领料单</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div v-if="!pageData?.records?.length" class="empty-state">暂无领料单</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>领料单号</th>
              <th>工单</th>
              <th>类型</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.reqNo }}</td>
              <td>{{ orderLabel(row.orderId) }}</td>
              <td>{{ row.reqType === 2 ? '退料' : '领料' }}</td>
              <td>{{ statusLabel(row.status) }}</td>
              <td>{{ formatTime(row.createTime) }}</td>
              <td>
                <button type="button" class="btn small" @click="openDetail(row)">详情</button>
                <template v-if="row.status === 0">
                  <button type="button" class="btn small primary" @click="doSubmit(row.id)">提交</button>
                  <button type="button" class="btn small danger" @click="doCancel(row.id)">取消</button>
                </template>
                <template v-else-if="row.status === 1">
                  <button type="button" class="btn small primary" @click="openComplete(row)">完成</button>
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
    <div v-if="detailRow" class="modal-mask" @click.self="detailRow = null">
      <div class="modal">
        <h3>领料单详情 - {{ detailRow.reqNo }}</h3>
        <div class="detail-content">
          <p>类型：{{ detailRow.reqType === 2 ? '退料' : '领料' }} | 状态：{{ statusLabel(detailRow.status) }}</p>
          <table class="data-table">
            <thead>
              <tr>
                <th>物料</th>
                <th>批次</th>
                <th>申请数量</th>
                <th>实发数量</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in detailRow.details || []" :key="d.id">
                <td>{{ d.materialCode }} {{ d.materialName }}</td>
                <td>{{ d.batchNo || '-' }}</td>
                <td>{{ d.quantity }}</td>
                <td>{{ d.actualQuantity ?? '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn" @click="detailRow = null">关闭</button>
        </div>
      </div>
    </div>
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建领料单（草稿）</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>物料 *</label>
            <select v-model="createForm.materialId" required>
              <option value="">请选择物料</option>
              <option v-for="m in materials" :key="m.id" :value="m.id">{{ m.materialCode }} - {{ m.materialName }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>数量 *</label>
            <input v-model.number="createForm.quantity" type="number" min="1" required />
          </div>
          <div class="form-group">
            <label>工单（可选）</label>
            <select v-model="createForm.orderId">
              <option :value="undefined">无</option>
              <option v-for="wo in workOrders" :key="wo.id" :value="wo.id">{{ wo.orderCode }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>类型</label>
            <select v-model="createForm.reqType">
              <option :value="1">领料</option>
              <option :value="2">退料</option>
            </select>
          </div>
          <p v-if="createError" class="error-msg">{{ createError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showCreate = false">取消</button>
            <button type="submit" class="btn primary" :disabled="creating">确定</button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="completeRow" class="modal-mask" @click.self="completeRow = null">
      <div class="modal">
        <h3>完成领料 - {{ completeRow.reqNo }}</h3>
        <p class="hint">确认实发数量后完成，无修改则使用申请数量</p>
        <table class="data-table">
          <thead>
            <tr>
              <th>物料</th>
              <th>申请</th>
              <th>实发</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in completeDetails" :key="d.id">
              <td>{{ d.materialName }}</td>
              <td>{{ d.quantity }}</td>
              <td>
                <input v-model.number="d.actualQuantity" type="number" min="0" class="inline-input" />
              </td>
            </tr>
          </tbody>
        </table>
        <p v-if="completeError" class="error-msg">{{ completeError }}</p>
        <div class="modal-actions">
          <button type="button" class="btn" @click="completeRow = null">取消</button>
          <button type="button" class="btn primary" :disabled="completing" @click="doComplete">确定完成</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import {
  getRequisitionPage,
  getRequisitionById,
  createRequisitionDraft,
  submitRequisition,
  completeRequisition,
  cancelRequisition,
  type MaterialRequisitionDTO,
  type MaterialRequisitionDetailDTO,
  type RequisitionManualCreateRequest
} from '@/api/requisitions';
import { getWorkOrderPage, type WorkOrderDTO } from '@/api/workOrders';
import { getMaterialPage, type MaterialDTO } from '@/api/materials';

const pageData = ref<Awaited<ReturnType<typeof getRequisitionPage>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;
const filterOrderId = ref<number | undefined>(undefined);
const filterStatus = ref<number | undefined>(undefined);

const workOrders = ref<WorkOrderDTO[]>([]);
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

function statusLabel(s?: number) {
  if (s === 0) return '草稿';
  if (s === 1) return '已提交';
  if (s === 2) return '已完成';
  return '-';
}

function orderLabel(orderId?: number) {
  if (!orderId) return '-';
  const wo = workOrders.value.find((w) => w.id === orderId);
  return wo ? wo.orderCode : String(orderId);
}

async function loadOptions() {
  try {
    const [woRes, matRes] = await Promise.all([
      getWorkOrderPage(1, 500),
      getMaterialPage(1, 500)
    ]);
    workOrders.value = woRes?.records ?? [];
    materials.value = matRes?.records ?? [];
  } catch {
    workOrders.value = [];
    materials.value = [];
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    pageData.value = await getRequisitionPage(currentPage.value, pageSize, filterOrderId.value, filterStatus.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch([currentPage, filterOrderId, filterStatus], load);
onMounted(() => {
  loadOptions();
  load();
});

const detailRow = ref<MaterialRequisitionDTO | null>(null);

async function openDetail(row: MaterialRequisitionDTO) {
  try {
    const full = await getRequisitionById(row.id);
    detailRow.value = full;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载详情失败';
  }
}

const showCreate = ref(false);
const createForm = ref<RequisitionManualCreateRequest>({ materialId: 0, quantity: 1, reqType: 1 });
const createError = ref('');
const creating = ref(false);

async function submitCreate() {
  createError.value = '';
  const materialId = Number(createForm.value.materialId);
  const quantity = Number(createForm.value.quantity);
  if (!materialId || quantity < 1) {
    createError.value = '请选择物料并输入数量';
    return;
  }
  creating.value = true;
  try {
    await createRequisitionDraft({
      ...createForm.value,
      materialId,
      quantity,
      orderId: createForm.value.orderId ? Number(createForm.value.orderId) : undefined,
      reqType: createForm.value.reqType ?? 1
    });
    showCreate.value = false;
    createForm.value = { materialId: 0, quantity: 1, reqType: 1 };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

async function doSubmit(id: number) {
  try {
    await submitRequisition(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '提交失败';
  }
}

async function doCancel(id: number) {
  if (!confirm('确定取消该领料单？')) return;
  try {
    await cancelRequisition(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '取消失败';
  }
}

const completeRow = ref<MaterialRequisitionDTO | null>(null);
const completeDetails = ref<(MaterialRequisitionDetailDTO & { actualQuantity: number })[]>([]);
const completeError = ref('');
const completing = ref(false);

async function openComplete(row: MaterialRequisitionDTO) {
  try {
    const full = await getRequisitionById(row.id);
    completeRow.value = full;
    completeDetails.value = (full.details ?? []).map((d) => ({
      ...d,
      actualQuantity: d.actualQuantity ?? d.quantity
    }));
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  }
}

async function doComplete() {
  if (!completeRow.value) return;
  completeError.value = '';
  completing.value = true;
  try {
    await completeRequisition(completeRow.value.id, {
      details: completeDetails.value.map((d) => ({ detailId: d.id, actualQuantity: d.actualQuantity }))
    });
    completeRow.value = null;
    await load();
  } catch (e) {
    completeError.value = e instanceof Error ? e.message : '完成失败';
  } finally {
    completing.value = false;
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; gap: 0.75rem; align-items: center; }
.filter-select { padding: 0.4rem 0.75rem; border-radius: 6px; background: #1e293b; color: #e5e7eb; border: 1px solid #475569; min-width: 160px; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { display: flex; align-items: center; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
.pagination .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 360px; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.detail-content { margin-bottom: 1rem; }
.hint { font-size: 0.875rem; color: #94a3b8; margin-bottom: 1rem; }
.inline-input { width: 80px; padding: 0.25rem; border: 1px solid #475569; border-radius: 4px; background: #0f172a; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
