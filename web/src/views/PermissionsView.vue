<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="title-with-tip">
          <span class="tip-trigger" title="功能说明" @click.stop="showTip = !showTip">
            <Icon icon="mdi:information-outline" class="tip-icon" />
          </span>
          <div v-if="showTip" class="tip-popover" @click.stop>
            <div class="tip-content">Admin 权限列表与树形结构，支持创建、编辑、删除</div>
          </div>
        </div>
        <div class="tabs">
          <button :class="['tab', { active: tab === 'list' }]" @click="tab = 'list'">列表</button>
          <button :class="['tab', { active: tab === 'tree' }]" @click="tab = 'tree'">树形</button>
        </div>
        <button type="button" class="btn primary" @click="showCreate = true">新建权限</button>
      </div>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div v-if="tab === 'list'" class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>权限名称</th>
              <th>权限代码</th>
              <th>URL</th>
              <th>方法</th>
              <th>父ID</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.permName }}</td>
              <td>{{ row.permCode }}</td>
              <td>{{ row.url || '-' }}</td>
              <td>{{ row.method || '-' }}</td>
              <td>{{ row.parentId ?? '-' }}</td>
              <td>
                <button type="button" class="btn small" @click="openEdit(row)">编辑</button>
                <button type="button" class="btn small danger" @click="doDelete(row.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="pagination">
          <button type="button" class="btn small" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
          <span class="page-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页，{{ pageData?.total ?? 0 }} 条</span>
          <button type="button" class="btn small" :disabled="currentPage >= totalPages" @click="currentPage++">下一页</button>
        </div>
      </div>
      <div v-else class="tree-wrap">
        <div v-for="node in treeData" :key="node.id" class="tree-node">
          <span class="tree-label">{{ node.permName }} ({{ node.permCode }}) {{ node.url ? `- ${node.url}` : '' }}</span>
          <div v-if="node.children?.length" class="tree-children">
            <div v-for="c in node.children" :key="c.id" class="tree-node tree-child">
              <span class="tree-label">{{ c.permName }} ({{ c.permCode }}) {{ c.url ? `- ${c.url}` : '' }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建权限</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>权限名称 *</label>
            <input v-model="createForm.permName" required placeholder="如 用户管理" />
          </div>
          <div class="form-group">
            <label>权限代码 *</label>
            <input v-model="createForm.permCode" required placeholder="如 user:list" />
          </div>
          <div class="form-group">
            <label>URL</label>
            <input v-model="createForm.url" placeholder="如 /api/users" />
          </div>
          <div class="form-group">
            <label>HTTP方法</label>
            <input v-model="createForm.method" placeholder="如 GET" />
          </div>
          <div class="form-group">
            <label>父权限ID</label>
            <input v-model.number="createForm.parentId" type="number" placeholder="可选，留空为根" />
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
        <h3>编辑权限</h3>
        <form v-if="editForm" @submit.prevent="submitEdit">
          <div class="form-group">
            <label>权限名称 *</label>
            <input v-model="editForm.permName" required />
          </div>
          <div class="form-group">
            <label>权限代码 *</label>
            <input v-model="editForm.permCode" required />
          </div>
          <div class="form-group">
            <label>URL</label>
            <input v-model="editForm.url" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>HTTP方法</label>
            <input v-model="editForm.method" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>父权限ID</label>
            <input v-model.number="editForm.parentId" type="number" placeholder="可选" />
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
  getPermissions,
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission,
  type PermissionDTO,
  type PermissionCreateRequest,
  type PermissionUpdateRequest,
  type PermissionTreeDTO
} from '@/api/permissions';

const tab = ref<'list' | 'tree'>('list');
const pageData = ref<Awaited<ReturnType<typeof getPermissions>> | null>(null);
const treeData = ref<PermissionTreeDTO[]>([]);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;

const totalPages = computed(() => {
  const t = pageData.value?.total ?? 0;
  return Math.max(1, Math.ceil(t / pageSize));
});

async function loadList() {
  pageData.value = await getPermissions(currentPage.value, pageSize);
}

async function loadTree() {
  treeData.value = await getPermissionTree();
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    if (tab.value === 'list') await loadList();
    else await loadTree();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

const showTip = ref(false);
function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}
watch([currentPage, tab], load);
onMounted(() => { load(); document.addEventListener('click', closeTipOnClickOutside); });
onUnmounted(() => document.removeEventListener('click', closeTipOnClickOutside));

const showCreate = ref(false);
const createForm = ref<PermissionCreateRequest>({ permName: '', permCode: '', url: '', method: '', parentId: undefined });
const createError = ref('');
const creating = ref(false);

const showEdit = ref(false);
const editForm = ref<(PermissionUpdateRequest & { id: number }) | null>(null);
const editError = ref('');
const editing = ref(false);

function openEdit(row: PermissionDTO) {
  editForm.value = {
    id: row.id,
    permName: row.permName,
    permCode: row.permCode,
    url: row.url ?? '',
    method: row.method ?? '',
    parentId: row.parentId ?? undefined
  };
  showEdit.value = true;
  editError.value = '';
}

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    const body = { ...createForm.value };
    if (!body.url) delete body.url;
    if (!body.method) delete body.method;
    if (body.parentId == null || body.parentId === 0) delete body.parentId;
    await createPermission(body);
    showCreate.value = false;
    createForm.value = { permName: '', permCode: '', url: '', method: '', parentId: undefined };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

async function submitEdit() {
  if (!editForm.value) return;
  editError.value = '';
  editing.value = true;
  try {
    const { id, ...body } = editForm.value;
    if (!body.url) delete body.url;
    if (!body.method) delete body.method;
    await updatePermission(id, body);
    showEdit.value = false;
    editForm.value = null;
    await load();
  } catch (e) {
    editError.value = e instanceof Error ? e.message : '保存失败';
  } finally {
    editing.value = false;
  }
}

async function doDelete(id: number) {
  if (!confirm('确定删除该权限？')) return;
  try {
    await deletePermission(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.tabs { display: flex; gap: 0.35rem; }
.tab { padding: 0.2rem 0.45rem; font-size: 0.8rem; border-radius: 4px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.tab.active { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; margin-right: 0.25rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { margin-top: 1rem; display: flex; align-items: center; gap: 0.75rem; font-size: 0.9rem; color: #94a3b8; }
.tree-wrap { padding: 0.5rem 0; }
.tree-node { padding: 0.35rem 0; color: #e5e7eb; }
.tree-child { padding-left: 1.5rem; color: #94a3b8; font-size: 0.9rem; }
.tree-label { font-size: 0.9rem; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
