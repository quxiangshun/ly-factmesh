<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="MES 工序定义，用于报工与工艺路线" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-button type="primary" @click="showCreate = true">新建工序</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无工序数据，请点击「新建工序」添加" />
      <el-table v-else :data="pageData?.records" class="table-wrap">
        <el-table-column prop="processCode" label="编码" />
        <el-table-column prop="processName" label="名称" />
        <el-table-column prop="sequence" label="排序" width="80">
          <template #default="{ row }">{{ row.sequence ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="workCenter" label="工作中心">
          <template #default="{ row }">{{ row.workCenter || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
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
    <el-dialog v-model="showCreate" title="新建工序" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="工序编码" required>
          <el-input v-model="createForm.processCode" placeholder="如 P001" />
        </el-form-item>
        <el-form-item label="工序名称" required>
          <el-input v-model="createForm.processName" placeholder="工序名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="createForm.sequence" :min="0" />
        </el-form-item>
        <el-form-item label="工作中心">
          <el-input v-model="createForm.workCenter" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showEdit" title="编辑工序" width="400px" :close-on-click-modal="false">
      <el-form v-if="editForm" :model="editForm" @submit.prevent="submitEdit">
        <el-form-item label="工序编码">
          <el-input :model-value="editForm.processCode" disabled />
        </el-form-item>
        <el-form-item label="工序名称" required>
          <el-input v-model="editForm.processName" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="editForm.sequence" :min="0" />
        </el-form-item>
        <el-form-item label="工作中心">
          <el-input v-model="editForm.workCenter" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="editError" type="error" :title="editError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showEdit = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="editing">保存</el-button>
        </div>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import {
  getProcessPage,
  createProcess,
  updateProcess,
  deleteProcess,
  type ProcessCreateRequest,
  type ProcessDTO,
  type ProcessUpdateRequest
} from '@/api/processes';
import { ElMessageBox } from 'element-plus';

const pageData = ref<Awaited<ReturnType<typeof getProcessPage>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;

const totalPages = computed(() => {
  const t = pageData.value?.total ?? 0;
  return Math.max(1, Math.ceil(t / pageSize));
});

async function load() {
  loading.value = true;
  error.value = '';
  try {
    pageData.value = await getProcessPage(currentPage.value, pageSize);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);

const showCreate = ref(false);
const createForm = ref<ProcessCreateRequest>({
  processCode: '',
  processName: '',
  sequence: 0,
  workCenter: ''
});
const createError = ref('');
const creating = ref(false);

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createProcess(createForm.value);
    showCreate.value = false;
    createForm.value = { processCode: '', processName: '', sequence: 0, workCenter: '' };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

const showEdit = ref(false);
const editId = ref<number | null>(null);
const editForm = ref<ProcessUpdateRequest & { processCode?: string } | null>(null);
const editError = ref('');
const editing = ref(false);

function formatTime(s?: string) {
  if (!s) return '-';
  try {
    return new Date(s).toLocaleString('zh-CN');
  } catch {
    return s;
  }
}

function openEdit(row: ProcessDTO) {
  editId.value = row.id;
  editForm.value = {
    processCode: row.processCode,
    processName: row.processName,
    sequence: row.sequence ?? 0,
    workCenter: row.workCenter ?? ''
  };
  editError.value = '';
  showEdit.value = true;
}

async function submitEdit() {
  if (!editForm.value || editId.value == null) return;
  editError.value = '';
  editing.value = true;
  try {
    await updateProcess(editId.value, {
      processName: editForm.value.processName,
      sequence: editForm.value.sequence ?? 0,
      workCenter: editForm.value.workCenter ?? ''
    });
    showEdit.value = false;
    editId.value = null;
    editForm.value = null;
    await load();
  } catch (e) {
    editError.value = e instanceof Error ? e.message : '保存失败';
  } finally {
    editing.value = false;
  }
}

async function doDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该工序？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteProcess(id);
    await load();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}

onMounted(load);
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
