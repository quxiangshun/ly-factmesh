<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="QMS 质检任务列表，支持新建、开始、完成、删除" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 120px">
          <el-option label="待检" :value="0" />
          <el-option label="检验中" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已关闭" :value="3" />
        </el-select>
        <el-button type="primary" @click="showCreate = true">新建质检任务</el-button>
      </div>
      <div v-if="stats" class="stats-bar">
        <span>总数 {{ stats.total }}</span>
        <span>待检 {{ stats.draftCount }}</span>
        <span class="progress">检验中 {{ stats.inProgressCount }}</span>
        <span class="done">已完成 {{ stats.completedCount }}</span>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column prop="taskCode" label="任务编码" />
        <el-table-column prop="orderId" label="工单ID">
          <template #default="{ row }">{{ row.orderId ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="materialId" label="物料ID">
          <template #default="{ row }">{{ row.materialId ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="检验类型">
          <template #default="{ row }">{{ inspectionTypeText(row.inspectionType) }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" @click="doStart(row.id)">开始</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button size="small" @click="openResults(row)">检验结果</el-button>
              <el-button size="small" @click="openCompleteModal(row)">完成</el-button>
            </template>
            <template v-else-if="row.status === 2">
              <el-button size="small" @click="openResults(row)">查看结果</el-button>
            </template>
            <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
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
    <el-dialog v-model="showCreate" title="新建质检任务" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="任务编码" required>
          <el-input v-model="createForm.taskCode" placeholder="如 Q-001" />
        </el-form-item>
        <el-form-item label="工单ID">
          <el-input-number v-model="createForm.orderId" placeholder="可选" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="物料ID">
          <el-input-number v-model="createForm.materialId" placeholder="可选" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="检验类型">
          <el-select v-model="createForm.inspectionType" placeholder="请选择" clearable style="width: 100%">
            <el-option label="来料检验" :value="0" />
            <el-option label="过程检验" :value="1" />
            <el-option label="成品检验" :value="2" />
            <el-option label="出货检验" :value="3" />
          </el-select>
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showResults" :title="`检验结果 - ${resultsTask?.taskCode}`" width="600px" :close-on-click-modal="false">
      <div v-if="resultsTask?.status === 1" class="toolbar-inline">
        <el-button size="small" type="primary" @click="showAddResult = true">添加检验项</el-button>
      </div>
      <el-skeleton v-if="resultsLoading" :rows="4" animated />
      <template v-else>
        <el-table v-if="resultsList.length" :data="resultsList" size="small" class="results-table-wrap">
          <el-table-column prop="inspectionItem" label="检验项" />
          <el-table-column prop="standardValue" label="标准值">
            <template #default="{ row }">{{ row.standardValue ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="actualValue" label="实际值">
            <template #default="{ row }">{{ row.actualValue ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="判定">
            <template #default="{ row }">{{ row.judgment === 0 ? '合格' : '不合格' }}</template>
          </el-table-column>
          <el-table-column prop="inspector" label="检验员">
            <template #default="{ row }">{{ row.inspector ?? '-' }}</template>
          </el-table-column>
          <el-table-column v-if="resultsTask?.status === 1" label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="doDeleteResult(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <p v-else class="empty-hint">暂无检验结果</p>
      </template>
      <div v-if="showAddResult" class="form-section">
        <h4>添加检验项</h4>
        <el-form :model="resultForm" @submit.prevent="submitAddResult">
          <el-form-item label="检验项" required>
            <el-input v-model="resultForm.inspectionItem" placeholder="如：尺寸" />
          </el-form-item>
          <el-form-item label="标准值">
            <el-input v-model="resultForm.standardValue" placeholder="可选" />
          </el-form-item>
          <el-form-item label="实际值">
            <el-input v-model="resultForm.actualValue" placeholder="可选" />
          </el-form-item>
          <el-form-item label="判定" required>
            <el-select v-model="resultForm.judgment" style="width: 100%">
              <el-option :value="0" label="合格" />
              <el-option :value="1" label="不合格" />
            </el-select>
          </el-form-item>
          <el-form-item label="检验员">
            <el-input v-model="resultForm.inspector" placeholder="可选" />
          </el-form-item>
          <el-alert v-if="resultError" type="error" :title="resultError" show-icon class="error-alert" />
          <div class="dialog-footer">
            <el-button @click="showAddResult = false">取消</el-button>
            <el-button type="primary" native-type="submit" :loading="addingResult">确定</el-button>
          </div>
        </el-form>
      </div>
      <div v-if="resultsTask?.status === 1 && resultsList.some(r => r.judgment === 1)" class="toolbar-inline" style="margin-top: 0.5rem;">
        <el-button size="small" @click="openCreateNcrFromTask">创建不合格品</el-button>
      </div>
      <div class="dialog-footer" style="margin-top: 1rem;">
        <el-button @click="closeResults">关闭</el-button>
      </div>
    </el-dialog>
    <el-dialog v-model="showComplete" :title="`完成质检任务 - ${completeTask?.taskCode}`" width="400px" :close-on-click-modal="false">
      <el-alert v-if="completeFailCount > 0" type="warning" :title="`存在 ${completeFailCount} 项不合格，请先创建不合格品或勾选强制完成`" show-icon class="error-alert" />
      <el-form :model="completeForm" @submit.prevent="submitComplete">
        <el-form-item label="检验员">
          <el-input v-model="completeForm.operator" placeholder="可选" />
        </el-form-item>
        <el-form-item v-if="completeFailCount > 0">
          <el-checkbox v-model="completeForm.forceComplete">强制完成（存在不合格项）</el-checkbox>
        </el-form-item>
        <el-alert v-if="completeError" type="error" :title="completeError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showComplete = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="completing">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import {
  getInspectionTaskPage,
  getInspectionTaskStats,
  createInspectionTask,
  startInspectionTask,
  completeInspectionTask,
  deleteInspectionTask,
  type InspectionTaskCreateRequest,
  type InspectionTaskDTO
} from '@/api/inspectionTasks';
import {
  getInspectionResultsByTaskId,
  createInspectionResult,
  deleteInspectionResult,
  type InspectionResultCreateRequest
} from '@/api/inspectionResults';
import { ElMessageBox } from 'element-plus';

const pageData = ref<Awaited<ReturnType<typeof getInspectionTaskPage>> | null>(null);
const stats = ref<Awaited<ReturnType<typeof getInspectionTaskStats>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;
const filterStatus = ref<string | number>('');

const totalPages = computed(() => {
  const t = pageData.value?.total ?? 0;
  return Math.max(1, Math.ceil(t / pageSize));
});

function statusText(s: number) {
  const map: Record<number, string> = { 0: '待检', 1: '检验中', 2: '已完成', 3: '已关闭' };
  return map[s] ?? String(s);
}

function inspectionTypeText(t?: number) {
  const map: Record<number, string> = { 0: '来料检验', 1: '过程检验', 2: '成品检验', 3: '出货检验' };
  return t !== undefined && t !== null ? (map[t] ?? String(t)) : '-';
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const statusParam = filterStatus.value === '' ? undefined : Number(filterStatus.value);
    const [pageRes, statsRes] = await Promise.all([
      getInspectionTaskPage(currentPage.value, pageSize, statusParam),
      getInspectionTaskStats()
    ]);
    pageData.value = pageRes;
    stats.value = statsRes;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch([currentPage, filterStatus], load);
const showTip = ref(false);
function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}
onMounted(() => { load(); document.addEventListener('click', closeTipOnClickOutside); });
onUnmounted(() => document.removeEventListener('click', closeTipOnClickOutside));

const showCreate = ref(false);
const createForm = ref<InspectionTaskCreateRequest>({
  taskCode: '',
  orderId: undefined,
  materialId: undefined,
  deviceId: undefined,
  inspectionType: undefined
});
const createError = ref('');
const creating = ref(false);

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createInspectionTask(createForm.value);
    showCreate.value = false;
    createForm.value = { taskCode: '', orderId: undefined, materialId: undefined, deviceId: undefined, inspectionType: undefined };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

async function doStart(id: number) {
  try {
    await startInspectionTask(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '开始失败';
  }
}

const showComplete = ref(false);
const completeTask = ref<InspectionTaskDTO | null>(null);
const completeFailCount = ref(0);
const completeForm = ref({ operator: '', forceComplete: false });
const completeError = ref('');
const completing = ref(false);

async function openCompleteModal(row: InspectionTaskDTO) {
  completeTask.value = row;
  completeForm.value = { operator: '', forceComplete: false };
  completeError.value = '';
  const list = await getInspectionResultsByTaskId(row.id);
  completeFailCount.value = list.filter(r => r.judgment === 1).length;
  showComplete.value = true;
}

async function submitComplete() {
  if (!completeTask.value) return;
  if (completeFailCount.value > 0 && !completeForm.value.forceComplete) {
    completeError.value = '存在不合格项，请先创建不合格品或勾选强制完成';
    return;
  }
  completing.value = true;
  completeError.value = '';
  try {
    await completeInspectionTask(completeTask.value.id, {
      operator: completeForm.value.operator || undefined,
      forceComplete: completeForm.value.forceComplete
    });
    showComplete.value = false;
    await load();
  } catch (e) {
    completeError.value = e instanceof Error ? e.message : '完成失败';
  } finally {
    completing.value = false;
  }
}

const router = useRouter();
function openCreateNcrFromTask() {
  if (!resultsTask.value) return;
  closeResults();
  router.push({ path: '/qms/ncr', query: { fromTask: String(resultsTask.value.id) } });
}

async function doDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该质检任务？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteInspectionTask(id);
    await load();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}

const showResults = ref(false);
const resultsTask = ref<InspectionTaskDTO | null>(null);
const resultsList = ref<Awaited<ReturnType<typeof getInspectionResultsByTaskId>>>([]);
const resultsLoading = ref(false);
const showAddResult = ref(false);
const resultForm = ref<InspectionResultCreateRequest>({
  taskId: 0,
  inspectionItem: '',
  standardValue: '',
  actualValue: '',
  judgment: 0,
  inspector: ''
});
const resultError = ref('');
const addingResult = ref(false);

async function openResults(row: InspectionTaskDTO) {
  resultsTask.value = row;
  showResults.value = true;
  showAddResult.value = false;
  await loadResults();
}

function closeResults() {
  showResults.value = false;
  resultsTask.value = null;
  resultsList.value = [];
}

async function loadResults() {
  if (!resultsTask.value) return;
  resultsLoading.value = true;
  try {
    resultsList.value = await getInspectionResultsByTaskId(resultsTask.value.id);
  } finally {
    resultsLoading.value = false;
  }
}

async function submitAddResult() {
  if (!resultsTask.value) return;
  resultError.value = '';
  addingResult.value = true;
  try {
    await createInspectionResult({
      ...resultForm.value,
      taskId: resultsTask.value.id
    });
    resultForm.value = { taskId: resultsTask.value.id, inspectionItem: '', standardValue: '', actualValue: '', judgment: 0, inspector: '' };
    showAddResult.value = false;
    await loadResults();
  } catch (e) {
    resultError.value = e instanceof Error ? e.message : '添加失败';
  } finally {
    addingResult.value = false;
  }
}

async function doDeleteResult(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该检验项？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteInspectionResult(id);
    await loadResults();
  } catch (e) {
    if (e !== 'cancel') resultError.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { margin-bottom: 1rem; display: flex; align-items: center; justify-content: space-between; gap: 1rem; flex-wrap: wrap; }
.toolbar-actions { display: flex; align-items: center; gap: 0.75rem; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.stats-bar { display: flex; gap: 1.5rem; font-size: 0.9rem; color: #94a3b8; }
.stats-bar .progress { color: #38bdf8; }
.stats-bar .done { color: #34d399; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
.toolbar-inline { margin-bottom: 1rem; }
.form-section { margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #334155; }
.form-section h4 { margin: 0 0 0.75rem; font-size: 1rem; color: #94a3b8; }
.results-table-wrap { margin: 0.5rem 0; }
.empty-hint { color: #64748b; font-size: 0.9rem; margin: 0.5rem 0; }
</style>
