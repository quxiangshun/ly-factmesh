<template>
  <section class="page">
    <h1 class="page-title">角色管理</h1>
    <p class="page-desc">Admin 角色列表，支持创建、编辑、分配权限、删除</p>
    <div class="toolbar">
      <button type="button" class="btn primary" @click="showCreate = true">新建角色</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>角色名称</th>
              <th>角色代码</th>
              <th>描述</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.roleName }}</td>
              <td>{{ row.roleCode }}</td>
              <td>{{ row.description || '-' }}</td>
              <td>
                <button type="button" class="btn small" @click="openEdit(row)">编辑</button>
                <button type="button" class="btn small" @click="openPerms(row)">分配权限</button>
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
        <h3>新建角色</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>角色名称 *</label>
            <input v-model="createForm.roleName" required placeholder="如 管理员" />
          </div>
          <div class="form-group">
            <label>角色代码 *</label>
            <input v-model="createForm.roleCode" required placeholder="如 ADMIN" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <input v-model="createForm.description" placeholder="可选" />
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
        <h3>编辑角色</h3>
        <form v-if="editForm" @submit.prevent="submitEdit">
          <div class="form-group">
            <label>角色名称 *</label>
            <input v-model="editForm.roleName" required />
          </div>
          <div class="form-group">
            <label>角色代码 *</label>
            <input v-model="editForm.roleCode" required />
          </div>
          <div class="form-group">
            <label>描述</label>
            <input v-model="editForm.description" placeholder="可选" />
          </div>
          <p v-if="editError" class="error-msg">{{ editError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showEdit = false">取消</button>
            <button type="submit" class="btn primary" :disabled="editing">保存</button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showPerms" class="modal-mask" @click.self="showPerms = false">
      <div class="modal modal-wide">
        <h3>分配权限 - {{ permRole?.roleName }}</h3>
        <div v-if="permLoading" class="loading">加载中…</div>
        <div v-else class="perm-tree">
          <label v-for="p in allPerms" :key="p.id" class="perm-item">
            <input type="checkbox" :value="p.id" v-model="selectedPermIds" />
            {{ p.permName }} ({{ p.permCode }}) {{ p.url ? `- ${p.url}` : '' }}
          </label>
        </div>
        <p v-if="permError" class="error-msg">{{ permError }}</p>
        <div class="modal-actions">
          <button type="button" class="btn" @click="showPerms = false">取消</button>
          <button type="button" class="btn primary" :disabled="permSaving" @click="submitPerms">保存</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import {
  getRoles,
  createRole,
  updateRole,
  deleteRole,
  getRolePermissions,
  assignRolePermissions,
  getPermissionTree,
  type RoleDTO,
  type RoleCreateRequest,
  type RoleUpdateRequest
} from '@/api/roles';
import { getPermissionTree, type PermissionTreeDTO } from '@/api/permissions';

const pageData = ref<Awaited<ReturnType<typeof getRoles>> | null>(null);
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
    pageData.value = await getRoles(currentPage.value, pageSize);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);
onMounted(load);

const showCreate = ref(false);
const createForm = ref<RoleCreateRequest>({ roleName: '', roleCode: '', description: '' });
const createError = ref('');
const creating = ref(false);

const showEdit = ref(false);
const editForm = ref<(RoleUpdateRequest & { id: number }) | null>(null);
const editError = ref('');
const editing = ref(false);

const showPerms = ref(false);
const permRole = ref<RoleDTO | null>(null);
const allPerms = ref<{ id: number; permName: string; permCode: string; url?: string }[]>([]);
const selectedPermIds = ref<number[]>([]);
const permLoading = ref(false);
const permError = ref('');
const permSaving = ref(false);

function flattenPerms(nodes: PermissionTreeDTO[]): { id: number; permName: string; permCode: string; url?: string }[] {
  const out: { id: number; permName: string; permCode: string; url?: string }[] = [];
  function walk(n: PermissionTreeDTO) {
    out.push({ id: n.id, permName: n.permName, permCode: n.permCode, url: n.url });
    n.children?.forEach(walk);
  }
  nodes.forEach(walk);
  return out;
}

async function openPerms(row: RoleDTO) {
  permRole.value = row;
  permLoading.value = true;
  permError.value = '';
  try {
    const [rolePerms, tree] = await Promise.all([getRolePermissions(row.id), getPermissionTree()]);
    allPerms.value = flattenPerms(tree);
    selectedPermIds.value = rolePerms.map((p) => p.id);
  } catch (e) {
    permError.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    permLoading.value = false;
  }
  showPerms.value = true;
}

async function submitPerms() {
  if (!permRole.value) return;
  permSaving.value = true;
  permError.value = '';
  try {
    await assignRolePermissions(permRole.value.id, selectedPermIds.value);
    showPerms.value = false;
    permRole.value = null;
  } catch (e) {
    permError.value = e instanceof Error ? e.message : '保存失败';
  } finally {
    permSaving.value = false;
  }
}

function openEdit(row: RoleDTO) {
  editForm.value = {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    description: row.description ?? ''
  };
  showEdit.value = true;
  editError.value = '';
}

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createRole(createForm.value);
    showCreate.value = false;
    createForm.value = { roleName: '', roleCode: '', description: '' };
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
    await updateRole(id, body);
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
  if (!confirm('确定删除该角色？')) return;
  try {
    await deleteRole(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; }
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
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; max-height: 90vh; overflow-y: auto; }
.modal.modal-wide { min-width: 480px; max-width: 90vw; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
.perm-tree { max-height: 300px; overflow-y: auto; margin: 1rem 0; }
.perm-item { display: block; padding: 0.35rem 0; font-size: 0.9rem; color: #e5e7eb; cursor: pointer; }
.perm-item input { margin-right: 0.5rem; }
</style>
