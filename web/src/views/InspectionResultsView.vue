<template>
  <section class="page">
    <h1 class="page-title">质检结果</h1>
    <p class="page-desc">检验项录入与质量判定</p>
    <div class="toolbar">
      <select v-model="selectedTaskId" class="filter-select" @change="loadResults">
        <option :value="0">选择质检任务</option>
        <option v-for="t in tasks" :key="t.id" :value="t.id">
          {{ t.taskCode }} - {{ t.orderCode || '工单' + (t.orderId ?? '-') }} ({{ t.status === 2 ? '已完成' : t.status === 1 ? '进行中' : '草稿' }})
        </option>
      </select>
      <button type="button" class="btn primary" :disabled="!selectedTaskId" @click="showCreate = true">新建结果</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="tasksLoading" class="loading">加载任务列表…</div>
    <template v-else-if="selectedTaskId">
      <div v-if="resultsLoading" class="loading">加载结果…</div>
      <template v-else>
        <div v-if="!results.length" class="empty-state">该任务暂无质检结果</div>
        <div v-else class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>检验项</th>
                <th>标准值</th>
                <th>实测值</th>
                <th>判定</th>
                <th>检验员</th>
                <th>检验时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in results" :key="row.id">
                <td>{{ row.inspectionItem }}</td>
                <td>{{ row.standardValue || '-' }}</td>
                <td>{{ row.actualValue || '-' }}</td>
                <td>{{ judgmentLabel(row.judgment) }}</td>
                <td>{{ row.inspector || '-' }}</td>
                <td>{{ formatTime(row.inspectionTime || row.createTime) }}</td>
                <td>
                  <button type="button" class="btn small danger" @click="doDelete(row.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </template>
    <div v-else class="empty-state">请选择质检任务</div>
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建质检结果</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>检验项 *</label>
            <input v-model="createForm.inspectionItem" required placeholder="检验项名称" />
          </div>
          <div class="form-group">
            <label>标准值</label>
            <input v-model="createForm.standardValue" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>实测值</label>
            <input v-model="createForm.actualValue" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>判定 *</label>
            <select v-model="createForm.judgment">
              <option :value="1">合格</option>
              <option :value="0">不合格</option>
            </select>
          </div>
          <div class="form-group">
            <label>检验员</label>
            <input v-model="createForm.inspector" placeholder="可选" />
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
import { ref, onMounted } from 'vue';
import {
  getInspectionResultsByTaskId,
  createInspectionResult,
  deleteInspectionResult,
  type InspectionResultDTO,
  type InspectionResultCreateRequest
} from '@/api/inspectionResults';
import { getInspectionTaskPage, type InspectionTaskDTO } from '@/api/inspectionTasks';

const tasks = ref<InspectionTaskDTO[]>([]);
const tasksLoading = ref(true);
const selectedTaskId = ref(0);
const results = ref<InspectionResultDTO[]>([]);
const resultsLoading = ref(false);
const error = ref('');
const showCreate = ref(false);
const createForm = ref<Omit<InspectionResultCreateRequest, 'taskId'>>({
  inspectionItem: '',
  standardValue: '',
  actualValue: '',
  judgment: 1,
  inspector: ''
});
const createError = ref('');
const creating = ref(false);

function formatTime(t?: string) {
  if (!t) return '-';
  try {
    return new Date(t).toLocaleString('zh-CN');
  } catch {
    return t;
  }
}

function judgmentLabel(j?: number) {
  return j === 1 ? '合格' : j === 0 ? '不合格' : '-';
}

async function loadTasks() {
  tasksLoading.value = true;
  try {
    const res = await getInspectionTaskPage(1, 200);
    tasks.value = res?.records ?? [];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载任务失败';
    tasks.value = [];
  } finally {
    tasksLoading.value = false;
  }
}

async function loadResults() {
  if (!selectedTaskId.value) {
    results.value = [];
    return;
  }
  resultsLoading.value = true;
  error.value = '';
  try {
    results.value = await getInspectionResultsByTaskId(selectedTaskId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载结果失败';
    results.value = [];
  } finally {
    resultsLoading.value = false;
  }
}

onMounted(loadTasks);

const submitCreate = async () => {
  createError.value = '';
  if (!selectedTaskId.value) {
    createError.value = '请先选择质检任务';
    return;
  }
  if (!createForm.value.inspectionItem?.trim()) {
    createError.value = '请输入检验项';
    return;
  }
  creating.value = true;
  try {
    await createInspectionResult({
      taskId: selectedTaskId.value,
      ...createForm.value
    });
    showCreate.value = false;
    createForm.value = { inspectionItem: '', standardValue: '', actualValue: '', judgment: 1, inspector: '' };
    await loadResults();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
};

async function doDelete(id: number) {
  if (!confirm('确定删除该质检结果？')) return;
  try {
    await deleteInspectionResult(id);
    await loadResults();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; gap: 0.75rem; align-items: center; }
.filter-select { padding: 0.4rem 0.75rem; border-radius: 6px; background: #1e293b; color: #e5e7eb; border: 1px solid #475569; min-width: 280px; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
