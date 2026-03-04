<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="MES 工单列表，支持下发、开始、完成" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-button type="primary" @click="showCreate = true">新建工单</el-button>
      </div>
      <div v-if="stats" class="stats-bar">
        <span>总数 {{ stats.total }}</span>
        <span>草稿 {{ stats.draftCount }}</span>
        <span>已下发 {{ stats.releasedCount }}</span>
        <span class="progress">进行中 {{ stats.inProgressCount }}</span>
        <span class="done">已完成 {{ stats.completedCount }}</span>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column prop="orderCode" label="编码" />
        <el-table-column label="产品">
          <template #default="{ row }">{{ row.productName }} ({{ row.productCode }})</template>
        </el-table-column>
        <el-table-column label="计划/实际">
          <template #default="{ row }">{{ row.planQuantity }} / {{ row.actualQuantity }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" @click="doRelease(row.id)">下发</el-button>
              <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button size="small" @click="doStart(row.id)">开始</el-button>
            </template>
            <template v-else-if="row.status === 2">
              <el-button size="small" @click="openComplete(row)">完成</el-button>
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
    <el-dialog v-model="showComplete" title="完成工单" width="400px" :close-on-click-modal="false">
      <p v-if="completeRow" class="modal-desc">{{ completeRow.orderCode }} - {{ completeRow.productName }}</p>
      <el-form @submit.prevent="submitComplete">
        <el-form-item label="实际数量">
          <el-input-number v-model="completeActualQty" :min="0" style="width: 100%" />
        </el-form-item>
        <el-alert v-if="completeError" type="error" :title="completeError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showComplete = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="completing">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showCreate" title="新建工单" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="工单编码" required>
          <el-input v-model="createForm.orderCode" placeholder="如 WO-001" />
        </el-form-item>
        <el-form-item label="产品编码" required>
          <el-input v-model="createForm.productCode" placeholder="如 P001" />
        </el-form-item>
        <el-form-item label="产品名称" required>
          <el-input v-model="createForm.productName" placeholder="产品名称" />
        </el-form-item>
        <el-form-item label="计划数量" required>
          <el-input-number v-model="createForm.planQuantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
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
import { ElMessageBox } from 'element-plus';

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
const completeActualQty = ref<number>(0);
const completeError = ref('');
const completing = ref(false);

function openComplete(row: WorkOrderDTO) {
  completeRow.value = row;
  completeActualQty.value = row.actualQuantity ?? row.planQuantity ?? 0;
  completeError.value = '';
  showComplete.value = true;
}

async function submitComplete() {
  if (!completeRow.value) return;
  completeError.value = '';
  completing.value = true;
  try {
    await completeWorkOrder(completeRow.value.id, completeActualQty.value);
    showComplete.value = false;
    completeRow.value = null;
    completeActualQty.value = 0;
    await load();
  } catch (e) {
    completeError.value = e instanceof Error ? e.message : '完成失败';
  } finally {
    completing.value = false;
  }
}

async function doDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该工单？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteWorkOrder(id);
    await load();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { margin-bottom: 1rem; display: flex; align-items: center; justify-content: space-between; gap: 1rem; flex-wrap: wrap; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.stats-bar { display: flex; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
.stats-bar .progress { color: #38bdf8; }
.stats-bar .done { color: #4ade80; }
.error-alert { margin-bottom: 1rem; }
.modal-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
