<template>
  <section class="page">
    <p class="page-desc">QMS 质检任务列表，支持新建、开始、完成、删除</p>
    <div class="stats-bar" v-if="stats">
      <span>总数 {{ stats.total }}</span>
      <span>待检 {{ stats.draftCount }}</span>
      <span class="progress">检验中 {{ stats.inProgressCount }}</span>
      <span class="done">已完成 {{ stats.completedCount }}</span>
    </div>
    <div class="toolbar">
      <select v-model="filterStatus" class="filter-select">
        <option value="">全部状态</option>
        <option :value="0">待检</option>
        <option :value="1">检验中</option>
        <option :value="2">已完成</option>
        <option :value="3">已关闭</option>
      </select>
      <button type="button" class="btn primary" @click="showCreate = true">新建质检任务</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>任务编码</th>
              <th>工单ID</th>
              <th>物料ID</th>
              <th>检验类型</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.taskCode }}</td>
              <td>{{ row.orderId ?? '-' }}</td>
              <td>{{ row.materialId ?? '-' }}</td>
              <td>{{ inspectionTypeText(row.inspectionType) }}</td>
              <td>{{ statusText(row.status) }}</td>
              <td>
                <template v-if="row.status === 0">
                  <button type="button" class="btn small" @click="doStart(row.id)">开始</button>
                </template>
                <template v-else-if="row.status === 1">
                  <button type="button" class="btn small" @click="openResults(row)">检验结果</button>
                  <button type="button" class="btn small" @click="openCompleteModal(row)">完成</button>
                </template>
                <template v-else-if="row.status === 2">
                  <button type="button" class="btn small" @click="openResults(row)">查看结果</button>
                </template>
                <button type="button" class="btn small danger" @click="doDelete(row.id)">删除</button>
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
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建质检任务</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>任务编码</label>
            <input v-model="createForm.taskCode" required placeholder="如 Q-001" />
          </div>
          <div class="form-group">
            <label>工单ID</label>
            <input v-model.number="createForm.orderId" type="number" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>物料ID</label>
            <input v-model.number="createForm.materialId" type="number" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>检验类型</label>
            <select v-model.number="createForm.inspectionType">
              <option :value="undefined">请选择</option>
              <option :value="0">来料检验</option>
              <option :value="1">过程检验</option>
              <option :value="2">成品检验</option>
              <option :value="3">出货检验</option>
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
    <div v-if="showResults" class="modal-mask" @click.self="showResults = false">
      <div class="modal modal-lg">
        <h3>检验结果 - {{ resultsTask?.taskCode }}</h3>
        <div v-if="resultsTask?.status === 1" class="toolbar-inline">
          <button type="button" class="btn small primary" @click="showAddResult = true">添加检验项</button>
        </div>
        <div v-if="resultsLoading" class="loading">加载中…</div>
        <div v-else class="results-table-wrap">
          <table class="data-table" v-if="resultsList.length">
            <thead>
              <tr>
                <th>检验项</th>
                <th>标准值</th>
                <th>实际值</th>
                <th>判定</th>
                <th>检验员</th>
                <th v-if="resultsTask?.status === 1">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in resultsList" :key="r.id">
                <td>{{ r.inspectionItem }}</td>
                <td>{{ r.standardValue ?? '-' }}</td>
                <td>{{ r.actualValue ?? '-' }}</td>
                <td>{{ r.judgment === 0 ? '合格' : '不合格' }}</td>
                <td>{{ r.inspector ?? '-' }}</td>
                <td v-if="resultsTask?.status === 1">
                  <button type="button" class="btn small danger" @click="doDeleteResult(r.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-else class="empty-hint">暂无检验结果</p>
        </div>
        <div v-if="showAddResult" class="form-section">
          <h4>添加检验项</h4>
          <form @submit.prevent="submitAddResult">
            <div class="form-group">
              <label>检验项</label>
              <input v-model="resultForm.inspectionItem" required placeholder="如：尺寸" />
            </div>
            <div class="form-group">
              <label>标准值</label>
              <input v-model="resultForm.standardValue" placeholder="可选" />
            </div>
            <div class="form-group">
              <label>实际值</label>
              <input v-model="resultForm.actualValue" placeholder="可选" />
            </div>
            <div class="form-group">
              <label>判定</label>
              <select v-model.number="resultForm.judgment" required>
                <option :value="0">合格</option>
                <option :value="1">不合格</option>
              </select>
            </div>
            <div class="form-group">
              <label>检验员</label>
              <input v-model="resultForm.inspector" placeholder="可选" />
            </div>
            <p v-if="resultError" class="error-msg">{{ resultError }}</p>
            <div class="modal-actions">
              <button type="button" class="btn" @click="showAddResult = false">取消</button>
              <button type="submit" class="btn primary" :disabled="addingResult">确定</button>
            </div>
          </form>
        </div>
        <div v-if="resultsTask?.status === 1 && resultsList.some(r => r.judgment === 1)" class="toolbar-inline" style="margin-top: 0.5rem;">
          <button type="button" class="btn small" @click="openCreateNcrFromTask">创建不合格品</button>
        </div>
        <div class="modal-actions" style="margin-top: 1rem;">
          <button type="button" class="btn" @click="closeResults">关闭</button>
        </div>
      </div>
    </div>
    <div v-if="showComplete" class="modal-mask" @click.self="showComplete = false">
      <div class="modal">
        <h3>完成质检任务 - {{ completeTask?.taskCode }}</h3>
        <p v-if="completeFailCount > 0" class="warn-msg">存在 {{ completeFailCount }} 项不合格，请先创建不合格品或勾选强制完成</p>
        <form @submit.prevent="submitComplete">
          <div class="form-group">
            <label>检验员</label>
            <input v-model="completeForm.operator" placeholder="可选" />
          </div>
          <div class="form-group" v-if="completeFailCount > 0">
            <label>
              <input type="checkbox" v-model="completeForm.forceComplete" />
              强制完成（存在不合格项）
            </label>
          </div>
          <p v-if="completeError" class="error-msg">{{ completeError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showComplete = false">取消</button>
            <button type="submit" class="btn primary" :disabled="completing">确定</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
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
    pageData.value = await getInspectionTaskPage(currentPage.value, pageSize, statusParam);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch([currentPage, filterStatus], load);
onMounted(load);

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
  if (!confirm('确定删除该质检任务？')) return;
  try {
    await deleteInspectionTask(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
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
  if (!confirm('确定删除该检验项？')) return;
  try {
    await deleteInspectionResult(id);
    await loadResults();
  } catch (e) {
    resultError.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; align-items: center; gap: 0.75rem; }
.filter-select { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; margin-right: 0.25rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.warn-msg { color: #fbbf24; margin-bottom: 0.75rem; font-size: 0.9rem; }
.stats-bar { display: flex; gap: 1.5rem; margin-bottom: 1rem; padding: 0.75rem 1rem; background: #1e293b; border-radius: 8px; font-size: 0.9rem; color: #94a3b8; }
.stats-bar .progress { color: #38bdf8; }
.stats-bar .done { color: #34d399; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { display: flex; align-items: center; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
.pagination .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; }
.modal.modal-lg { min-width: 560px; max-width: 90vw; }
.toolbar-inline { margin-bottom: 1rem; }
.form-section { margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #334155; }
.form-section h4 { margin: 0 0 0.75rem; font-size: 1rem; color: #94a3b8; }
.results-table-wrap { margin: 0.5rem 0; }
.empty-hint { color: #64748b; font-size: 0.9rem; margin: 0.5rem 0; }
.form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
