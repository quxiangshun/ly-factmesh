<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="Admin 角色列表，支持创建、编辑、分配权限、删除" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-button type="primary" @click="showCreate = true">新建角色</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色代码" />
        <el-table-column prop="description" label="描述">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="openPerms(row)">分配权限</el-button>
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
    <el-dialog v-model="showCreate" title="新建角色" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="角色名称" required>
          <el-input v-model="createForm.roleName" placeholder="如 管理员" />
        </el-form-item>
        <el-form-item label="角色代码" required>
          <el-input v-model="createForm.roleCode" placeholder="如 ADMIN" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showEdit" title="编辑角色" width="400px" :close-on-click-modal="false">
      <el-form v-if="editForm" :model="editForm" @submit.prevent="submitEdit">
        <el-form-item label="角色名称" required>
          <el-input v-model="editForm.roleName" />
        </el-form-item>
        <el-form-item label="角色代码" required>
          <el-input v-model="editForm.roleCode" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="editError" type="error" :title="editError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showEdit = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="editing">保存</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showPerms" :title="`分配权限 - ${permRole?.roleName}`" width="520px" :close-on-click-modal="false">
      <el-skeleton v-if="permLoading" :rows="5" animated />
      <el-checkbox-group v-else v-model="selectedPermIds" class="perm-tree">
        <div v-for="p in allPerms" :key="p.id" class="perm-item">
          <el-checkbox :label="p.id">
            {{ p.permName }} ({{ p.permCode }}) {{ p.url ? `- ${p.url}` : '' }}
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <el-alert v-if="permError" type="error" :title="permError" show-icon class="error-alert" />
      <div class="dialog-footer">
        <el-button @click="showPerms = false">取消</el-button>
        <el-button type="primary" :disabled="permSaving" :loading="permSaving" @click="submitPerms">保存</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import {
  getRoles,
  createRole,
  updateRole,
  deleteRole,
  getRolePermissions,
  assignRolePermissions,
  type RoleDTO,
  type RoleCreateRequest,
  type RoleUpdateRequest
} from '@/api/roles';
import { getPermissionTree, type PermissionTreeDTO } from '@/api/permissions';
import { ElMessageBox } from 'element-plus';

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

const showTip = ref(false);
function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}
watch(currentPage, load);
onMounted(() => { load(); document.addEventListener('click', closeTipOnClickOutside); });
onUnmounted(() => document.removeEventListener('click', closeTipOnClickOutside));

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
  try {
    await ElMessageBox.confirm('确定删除该角色？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteRole(id);
    await load();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}
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
.perm-tree { max-height: 300px; overflow-y: auto; margin: 1rem 0; }
.perm-item { padding: 0.35rem 0; font-size: 0.9rem; }
</style>
