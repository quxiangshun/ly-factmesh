<template>
  <section class="page">
    <h1 class="page-title">库存管理</h1>
    <p class="page-desc">库存查询、预警、盘点</p>
    <div class="toolbar">
      <select v-model="filterMaterialId" class="filter-select">
        <option :value="undefined">全部物料</option>
        <option v-for="m in materials" :key="m.id" :value="m.id">{{ m.materialCode }} - {{ m.materialName }}</option>
      </select>
      <input v-model="filterWarehouse" placeholder="仓库" class="filter-input" />
      <input v-model="filterBatchNo" placeholder="批次号" class="filter-input" />
      <button type="button" class="btn" @click="load">查询</button>
      <button v-if="!showBelowSafe" type="button" class="btn primary" @click="showBelowSafe = true">低于安全库存</button>
      <button v-else type="button" class="btn" @click="showBelowSafe = false">全部库存</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div v-if="!pageData?.records?.length" class="empty-state">暂无库存记录</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>物料</th>
              <th>批次</th>
              <th>仓库</th>
              <th>数量</th>
              <th>安全库存</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id" :class="{ 'row-warning': row.belowSafeStock }">
              <td>{{ row.materialCode }} {{ row.materialName }}</td>
              <td>{{ row.batchNo || '-' }}</td>
              <td>{{ row.warehouse || '-' }}</td>
              <td>{{ row.quantity }}</td>
              <td>{{ row.safeStock ?? '-' }}</td>
              <td>
                <span v-if="row.belowSafeStock" class="badge danger">低于安全库存</span>
                <span v-else class="badge ok">正常</span>
              </td>
              <td>{{ formatTime(row.lastUpdateTime) }}</td>
              <td>
                <button type="button" class="btn small" @click="openAdjust(row)">调整</button>
                <button type="button" class="btn small" @click="openCount(row)">盘点</button>
                <button type="button" class="btn small" @click="openSafeStock(row)">安全库存</button>
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
    <div v-if="adjustRow" class="modal-mask" @click.self="adjustRow = null">
      <div class="modal">
        <h3>调整库存 - {{ adjustRow.materialName }}</h3>
        <p class="hint">正数入库、负数出库</p>
        <div class="form-group">
          <label>数量</label>
          <input v-model.number="adjustForm.quantity" type="number" placeholder="正数入库、负数出库" />
        </div>
        <div class="form-group">
          <label>参考单号</label>
          <input v-model="adjustForm.referenceNo" placeholder="可选" />
        </div>
        <div class="form-group">
          <label>操作人</label>
          <input v-model="adjustForm.operator" placeholder="可选" />
        </div>
        <p v-if="adjustError" class="error-msg">{{ adjustError }}</p>
        <div class="modal-actions">
          <button type="button" class="btn" @click="adjustRow = null">取消</button>
          <button type="button" class="btn primary" :disabled="adjusting" @click="doAdjust">确定</button>
        </div>
      </div>
    </div>
    <div v-if="countRow" class="modal-mask" @click.self="countRow = null">
      <div class="modal">
        <h3>盘点确认 - {{ countRow.materialName }}</h3>
        <p class="hint">当前账面：{{ countRow.quantity }}，请输入实盘数量</p>
        <div class="form-group">
          <label>实盘数量</label>
          <input v-model.number="countForm.actualQuantity" type="number" min="0" />
        </div>
        <div class="form-group">
          <label>操作人</label>
          <input v-model="countForm.operator" placeholder="可选" />
        </div>
        <p v-if="countError" class="error-msg">{{ countError }}</p>
        <div class="modal-actions">
          <button type="button" class="btn" @click="countRow = null">取消</button>
          <button type="button" class="btn primary" :disabled="counting" @click="doCount">确定</button>
        </div>
      </div>
    </div>
    <div v-if="safeStockRow" class="modal-mask" @click.self="safeStockRow = null">
      <div class="modal">
        <h3>更新安全库存 - {{ safeStockRow.materialName }}</h3>
        <div class="form-group">
          <label>安全库存数量</label>
          <input v-model.number="safeStockForm.safeStock" type="number" min="0" />
        </div>
        <p v-if="safeStockError" class="error-msg">{{ safeStockError }}</p>
        <div class="modal-actions">
          <button type="button" class="btn" @click="safeStockRow = null">取消</button>
          <button type="button" class="btn primary" :disabled="safeStockSaving" @click="doUpdateSafeStock">确定</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import {
  getInventoryPage,
  getInventoryBelowSafeStock,
  adjustInventory,
  countInventory,
  updateSafeStock,
  type InventoryDTO,
  type InventoryAdjustRequest
} from '@/api/inventory';
import { getMaterialPage, type MaterialDTO } from '@/api/materials';

const pageData = ref<Awaited<ReturnType<typeof getInventoryPage>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;
const filterMaterialId = ref<number | undefined>(undefined);
const filterWarehouse = ref('');
const filterBatchNo = ref('');
const showBelowSafe = ref(false);

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
    if (showBelowSafe.value) {
      pageData.value = await getInventoryBelowSafeStock(currentPage.value, pageSize);
    } else {
      pageData.value = await getInventoryPage(
        currentPage.value,
        pageSize,
        filterMaterialId.value,
        filterWarehouse.value || undefined,
        filterBatchNo.value || undefined
      );
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch([currentPage, filterMaterialId, showBelowSafe], load);
onMounted(() => {
  loadOptions();
  load();
});

const adjustRow = ref<InventoryDTO | null>(null);
const adjustForm = ref<Pick<InventoryAdjustRequest, 'quantity' | 'referenceNo' | 'operator'>>({
  quantity: 0,
  referenceNo: '',
  operator: ''
});
const adjustError = ref('');
const adjusting = ref(false);

function openAdjust(row: InventoryDTO) {
  adjustRow.value = row;
  adjustForm.value = { quantity: 0, referenceNo: '', operator: '' };
}

async function doAdjust() {
  if (!adjustRow.value) return;
  if (adjustForm.value.quantity === 0) {
    adjustError.value = '请输入非零数量';
    return;
  }
  adjusting.value = true;
  adjustError.value = '';
  try {
    await adjustInventory({
      materialId: adjustRow.value.materialId,
      batchNo: adjustRow.value.batchNo,
      quantity: adjustForm.value.quantity,
      warehouse: adjustRow.value.warehouse,
      referenceNo: adjustForm.value.referenceNo || undefined,
      operator: adjustForm.value.operator || undefined
    });
    adjustRow.value = null;
    await load();
  } catch (e) {
    adjustError.value = e instanceof Error ? e.message : '调整失败';
  } finally {
    adjusting.value = false;
  }
}

const countRow = ref<InventoryDTO | null>(null);
const countForm = ref({ actualQuantity: 0, operator: '' });
const countError = ref('');
const counting = ref(false);

function openCount(row: InventoryDTO) {
  countRow.value = row;
  countForm.value = { actualQuantity: row.quantity, operator: '' };
}

async function doCount() {
  if (!countRow.value) return;
  const q = Number(countForm.value.actualQuantity);
  if (isNaN(q) || q < 0) {
    countError.value = '请输入有效实盘数量';
    return;
  }
  counting.value = true;
  countError.value = '';
  try {
    await countInventory({
      inventoryId: countRow.value.id,
      actualQuantity: q,
      operator: countForm.value.operator || undefined
    });
    countRow.value = null;
    await load();
  } catch (e) {
    countError.value = e instanceof Error ? e.message : '盘点失败';
  } finally {
    counting.value = false;
  }
}

const safeStockRow = ref<InventoryDTO | null>(null);
const safeStockForm = ref({ safeStock: 0 });
const safeStockError = ref('');
const safeStockSaving = ref(false);

function openSafeStock(row: InventoryDTO) {
  safeStockRow.value = row;
  safeStockForm.value = { safeStock: row.safeStock ?? 0 };
}

async function doUpdateSafeStock() {
  if (!safeStockRow.value) return;
  const v = Number(safeStockForm.value.safeStock);
  if (isNaN(v) || v < 0) {
    safeStockError.value = '请输入有效安全库存';
    return;
  }
  safeStockSaving.value = true;
  safeStockError.value = '';
  try {
    await updateSafeStock(safeStockRow.value.id, v);
    safeStockRow.value = null;
    await load();
  } catch (e) {
    safeStockError.value = e instanceof Error ? e.message : '更新失败';
  } finally {
    safeStockSaving.value = false;
  }
}
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
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.row-warning { background: rgba(248, 113, 113, 0.08); }
.badge { font-size: 0.75rem; padding: 0.15rem 0.4rem; border-radius: 4px; }
.badge.danger { background: rgba(248, 113, 113, 0.3); color: #f87171; }
.badge.ok { background: rgba(56, 189, 248, 0.2); color: #38bdf8; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { display: flex; align-items: center; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
.pagination .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.hint { font-size: 0.875rem; color: #94a3b8; margin-bottom: 1rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
