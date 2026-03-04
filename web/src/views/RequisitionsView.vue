<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="生产领料/退料，MES 工单联动" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-select v-model="filterOrderId" placeholder="全部工单" clearable style="width: 180px">
          <el-option v-for="wo in workOrders" :key="wo.id" :value="wo.id" :label="`${wo.orderCode} - ${wo.productName}`" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 120px">
          <el-option label="草稿" :value="0" />
          <el-option label="已提交" :value="1" />
          <el-option label="已完成" :value="2" />
        </el-select>
        <el-button type="primary" @click="showCreate = true">新建领料单</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无领料单" />
      <el-table v-else :data="pageData?.records" class="table-wrap">
        <el-table-column prop="reqNo" label="领料单号" />
        <el-table-column label="工单">
          <template #default="{ row }">{{ orderLabel(row.orderId) }}</template>
        </el-table-column>
        <el-table-column label="类型">
          <template #default="{ row }">{{ row.reqType === 2 ? '退料' : '领料' }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">{{ statusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column label="创建时间">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 0">
              <el-button size="small" type="primary" @click="doSubmit(row.id)">提交</el-button>
              <el-button size="small" type="danger" @click="doCancel(row.id)">取消</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button size="small" type="primary" @click="openComplete(row)">完成</el-button>
            </template>
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
    <el-dialog v-if="detailRow" v-model="detailVisible" :title="`领料单详情 - ${detailRow.reqNo}`" width="560px" :close-on-click-modal="false">
      <div v-if="detailRow" class="detail-content">
        <p>类型：{{ detailRow.reqType === 2 ? '退料' : '领料' }} | 状态：{{ statusLabel(detailRow.status) }}</p>
        <el-table :data="detailRow.details || []" size="small">
          <el-table-column label="物料">
            <template #default="{ row }">{{ row.materialCode }} {{ row.materialName }}</template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次">
            <template #default="{ row }">{{ row.batchNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="申请数量" />
          <el-table-column prop="actualQuantity" label="实发数量">
            <template #default="{ row }">{{ row.actualQuantity ?? '-' }}</template>
          </el-table-column>
        </el-table>
      </div>
      <div class="dialog-footer">
        <el-button @click="detailRow = null">关闭</el-button>
      </div>
    </el-dialog>
    <el-dialog v-model="showCreate" title="新建领料单（草稿）" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="物料" required>
          <el-select v-model="createForm.materialId" placeholder="请选择物料" style="width: 100%" required>
            <el-option v-for="m in materials" :key="m.id" :value="m.id" :label="`${m.materialCode} - ${m.materialName}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="createForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工单（可选）">
          <el-select v-model="createForm.orderId" placeholder="无" clearable style="width: 100%">
            <el-option v-for="wo in workOrders" :key="wo.id" :value="wo.id" :label="wo.orderCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="createForm.reqType" style="width: 100%">
            <el-option label="领料" :value="1" />
            <el-option label="退料" :value="2" />
          </el-select>
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-if="completeRow" v-model="completeVisible" :title="`完成领料 - ${completeRow.reqNo}`" width="480px" :close-on-click-modal="false">
      <p class="hint">确认实发数量后完成，无修改则使用申请数量</p>
      <el-table :data="completeDetails" size="small">
        <el-table-column prop="materialName" label="物料" />
        <el-table-column prop="quantity" label="申请" width="80" />
        <el-table-column label="实发" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.actualQuantity" :min="0" size="small" controls-position="right" />
          </template>
        </el-table-column>
      </el-table>
      <el-alert v-if="completeError" type="error" :title="completeError" show-icon class="error-alert" />
      <div class="dialog-footer">
        <el-button @click="completeRow = null">取消</el-button>
        <el-button type="primary" :disabled="completing" :loading="completing" @click="doComplete">确定完成</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
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
import { ElMessageBox } from 'element-plus';

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

const detailRow = ref<MaterialRequisitionDTO | null>(null);
const detailVisible = computed({
  get: () => !!detailRow.value,
  set: (v) => { if (!v) detailRow.value = null; }
});
const completeRow = ref<MaterialRequisitionDTO | null>(null);
const completeVisible = computed({
  get: () => !!completeRow.value,
  set: (v) => { if (!v) completeRow.value = null; }
});

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
  try {
    await ElMessageBox.confirm('确定取消该领料单？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await cancelRequisition(id);
    await load();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '取消失败';
  }
}

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
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
.detail-content { margin-bottom: 1rem; }
.hint { font-size: 0.875rem; color: #94a3b8; margin-bottom: 1rem; }
</style>
