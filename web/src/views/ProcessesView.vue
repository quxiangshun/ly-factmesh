<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="title-with-tip">
          <span class="tip-trigger" title="功能说明" @click.stop="showTip = !showTip">
            <Icon icon="mdi:information-outline" class="tip-icon" />
          </span>
          <div v-if="showTip" class="tip-popover" @click.stop>
            <div class="tip-content">MES 工序定义，用于报工与工艺路线</div>
          </div>
        </div>
        <button type="button" class="btn primary" @click="showCreate = true">新建工序</button>
      </div>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div v-if="!pageData?.records?.length" class="empty-state">暂无工序数据，请点击「新建工序」添加</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>编码</th>
              <th>名称</th>
              <th>排序</th>
              <th>工作中心</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.processCode }}</td>
              <td>{{ row.processName }}</td>
              <td>{{ row.sequence ?? 0 }}</td>
              <td>{{ row.workCenter || '-' }}</td>
              <td>{{ formatTime(row.createTime) }}</td>
              <td>
                <button type="button" class="btn small" @click="openEdit(row)">编辑</button>
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
        <h3>新建工序</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>工序编码</label>
            <input v-model="createForm.processCode" required placeholder="如 P001" />
          </div>
          <div class="form-group">
            <label>工序名称</label>
            <input v-model="createForm.processName" required placeholder="工序名称" />
          </div>
          <div class="form-group">
            <label>排序号</label>
            <input v-model.number="createForm.sequence" type="number" min="0" />
          </div>
          <div class="form-group">
            <label>工作中心</label>
            <input v-model="createForm.workCenter" placeholder="可选" />
          </div>
          <p v-if="createError" class="error-msg">{{ createError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showCreate = false">取消</button>
            <button type="submit" class="btn primary" :disabled="creating">确定</button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showEdit" class="modal-mask" @click.self="showEdit = false">
      <div class="modal">
        <h3>编辑工序</h3>
        <form v-if="editForm" @submit.prevent="submitEdit">
          <div class="form-group">
            <label>工序编码</label>
            <input :value="editForm.processCode" disabled class="readonly" />
          </div>
          <div class="form-group">
            <label>工序名称 *</label>
            <input v-model="editForm.processName" required />
          </div>
          <div class="form-group">
            <label>排序号</label>
            <input v-model.number="editForm.sequence" type="number" min="0" />
          </div>
          <div class="form-group">
            <label>工作中心</label>
            <input v-model="editForm.workCenter" placeholder="可选" />
          </div>
          <p v-if="editError" class="error-msg">{{ editError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showEdit = false">取消</button>
            <button type="submit" class="btn primary" :disabled="editing">保存</button>
          </div>
        </form>
      </div>
    </div>
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
onMounted(load);

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
  if (!confirm('确定删除该工序？')) return;
  try {
    await deleteProcess(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.form-group input.readonly { opacity: 0.7; cursor: not-allowed; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { display: flex; align-items: center; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
.pagination .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
