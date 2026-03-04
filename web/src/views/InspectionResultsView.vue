<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="检验项录入与质量判定" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-select v-model="selectedTaskId" placeholder="选择质检任务" style="width: 320px" @change="loadResults">
          <el-option :value="0" label="选择质检任务" />
          <el-option v-for="t in tasks" :key="t.id" :value="t.id" :label="`${t.taskCode} - ${t.orderCode || '工单' + (t.orderId ?? '-')} (${t.status === 2 ? '已完成' : t.status === 1 ? '进行中' : '草稿'})`" />
        </el-select>
        <el-button type="primary" :disabled="!selectedTaskId" @click="showCreate = true">新建结果</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="tasksLoading" :rows="3" animated />
    <template v-else-if="selectedTaskId">
      <el-skeleton v-if="resultsLoading" :rows="5" animated />
      <template v-else>
        <el-empty v-if="!results.length" description="该任务暂无质检结果" />
        <el-table v-else :data="results" class="table-wrap">
          <el-table-column prop="inspectionItem" label="检验项" />
          <el-table-column prop="standardValue" label="标准值">
            <template #default="{ row }">{{ row.standardValue || '-' }}</template>
          </el-table-column>
          <el-table-column prop="actualValue" label="实测值">
            <template #default="{ row }">{{ row.actualValue || '-' }}</template>
          </el-table-column>
          <el-table-column label="判定">
            <template #default="{ row }">{{ judgmentLabel(row.judgment) }}</template>
          </el-table-column>
          <el-table-column prop="inspector" label="检验员">
            <template #default="{ row }">{{ row.inspector || '-' }}</template>
          </el-table-column>
          <el-table-column label="检验时间">
            <template #default="{ row }">{{ formatTime(row.inspectionTime || row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </template>
    <el-empty v-else description="请选择质检任务" />
    <el-dialog v-model="showCreate" title="新建质检结果" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="检验项" required>
          <el-input v-model="createForm.inspectionItem" placeholder="检验项名称" />
        </el-form-item>
        <el-form-item label="标准值">
          <el-input v-model="createForm.standardValue" placeholder="可选" />
        </el-form-item>
        <el-form-item label="实测值">
          <el-input v-model="createForm.actualValue" placeholder="可选" />
        </el-form-item>
        <el-form-item label="判定" required>
          <el-select v-model="createForm.judgment" style="width: 100%">
            <el-option :value="1" label="合格" />
            <el-option :value="0" label="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item label="检验员">
          <el-input v-model="createForm.inspector" placeholder="可选" />
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
import { ref, onMounted, onUnmounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import {
  getInspectionResultsByTaskId,
  createInspectionResult,
  deleteInspectionResult,
  type InspectionResultDTO,
  type InspectionResultCreateRequest
} from '@/api/inspectionResults';
import { getInspectionTaskPage, type InspectionTaskDTO } from '@/api/inspectionTasks';
import { ElMessageBox } from 'element-plus';

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

const showTip = ref(false);
function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}
onMounted(() => { loadTasks(); document.addEventListener('click', closeTipOnClickOutside); });
onUnmounted(() => document.removeEventListener('click', closeTipOnClickOutside));

async function submitCreate() {
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
}

async function doDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该质检结果？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteInspectionResult(id);
    await loadResults();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.75rem; align-items: center; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
