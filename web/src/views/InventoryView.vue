<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="库存查询、预警、盘点" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-select v-model="filterMaterialId" placeholder="全部物料" clearable style="width: 200px">
          <el-option v-for="m in materials" :key="m.id" :value="m.id" :label="`${m.materialCode} - ${m.materialName}`" />
        </el-select>
        <el-input v-model="filterWarehouse" placeholder="仓库" style="width: 120px" clearable />
        <el-input v-model="filterBatchNo" placeholder="批次号" style="width: 120px" clearable />
        <el-button @click="load">查询</el-button>
        <el-button v-if="!showBelowSafe" type="primary" @click="showBelowSafe = true">低于安全库存</el-button>
        <el-button v-else @click="showBelowSafe = false">全部库存</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无库存记录" />
      <el-table
        v-else
        :data="pageData?.records"
        :row-class-name="({ row }: { row: { belowSafeStock?: boolean } }) => (row.belowSafeStock ? 'row-warning' : '')"
        class="table-wrap"
      >
        <el-table-column label="物料">
          <template #default="{ row }">{{ row.materialCode }} {{ row.materialName }}</template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次">
          <template #default="{ row }">{{ row.batchNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="warehouse" label="仓库">
          <template #default="{ row }">{{ row.warehouse || '-' }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" />
        <el-table-column prop="safeStock" label="安全库存">
          <template #default="{ row }">{{ row.safeStock ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.belowSafeStock" type="danger" size="small">低于安全库存</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间">
          <template #default="{ row }">{{ formatTime(row.lastUpdateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openAdjust(row)">调整</el-button>
            <el-button size="small" @click="openCount(row)">盘点</el-button>
            <el-button size="small" @click="openSafeStock(row)">安全库存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="totalPages > 1"
        v-model:current-page="currentPage"
        :total="pageData?.total ?? 0"
        :page-size="pageSize"
        layout="prev, pager, next"
        class="pagination"
      />
    </template>
    <el-dialog v-if="adjustRow" v-model="adjustVisible" :title="`调整库存 - ${adjustRow.materialName}`" width="400px" :close-on-click-modal="false">
      <p class="hint">正数入库、负数出库</p>
      <el-form @submit.prevent="doAdjust">
        <el-form-item label="数量">
          <el-input-number v-model="adjustForm.quantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="参考单号">
          <el-input v-model="adjustForm.referenceNo" placeholder="可选" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="adjustForm.operator" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="adjustError" type="error" :title="adjustError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="adjustRow = null">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="adjusting">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-if="countRow" v-model="countVisible" :title="`盘点确认 - ${countRow.materialName}`" width="400px" :close-on-click-modal="false">
      <p class="hint">当前账面：{{ countRow.quantity }}，请输入实盘数量</p>
      <el-form @submit.prevent="doCount">
        <el-form-item label="实盘数量">
          <el-input-number v-model="countForm.actualQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="countForm.operator" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="countError" type="error" :title="countError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="countRow = null">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="counting">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-if="safeStockRow" v-model="safeStockVisible" :title="`更新安全库存 - ${safeStockRow.materialName}`" width="400px" :close-on-click-modal="false">
      <el-form @submit.prevent="doUpdateSafeStock">
        <el-form-item label="安全库存数量">
          <el-input-number v-model="safeStockForm.safeStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-alert v-if="safeStockError" type="error" :title="safeStockError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="safeStockRow = null">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="safeStockSaving">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
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

const adjustRow = ref<InventoryDTO | null>(null);
const adjustVisible = computed({
  get: () => !!adjustRow.value,
  set: (v) => { if (!v) adjustRow.value = null; }
});
const countRow = ref<InventoryDTO | null>(null);
const countVisible = computed({
  get: () => !!countRow.value,
  set: (v) => { if (!v) countRow.value = null; }
});
const safeStockRow = ref<InventoryDTO | null>(null);
const safeStockVisible = computed({
  get: () => !!safeStockRow.value,
  set: (v) => { if (!v) safeStockRow.value = null; }
});

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
const showTip = ref(false);
function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}
onMounted(() => {
  document.addEventListener('click', closeTipOnClickOutside);
  loadOptions();
  load();
});
onUnmounted(() => document.removeEventListener('click', closeTipOnClickOutside));

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
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
.hint { font-size: 0.875rem; color: #94a3b8; margin-bottom: 1rem; }
:deep(.row-warning) { background: rgba(248, 113, 113, 0.08); }
</style>
