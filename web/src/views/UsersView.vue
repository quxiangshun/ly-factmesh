<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="title-with-tip">
          <span class="tip-trigger" title="功能说明" @click.stop="showTip = !showTip">
            <Icon icon="mdi:information-outline" class="tip-icon" />
          </span>
          <div v-if="showTip" class="tip-popover" @click.stop>
            <div class="tip-content">Admin 用户列表，支持创建、编辑、禁用</div>
          </div>
        </div>
        <button type="button" class="btn primary" @click="showCreate = true">新建用户</button>
      </div>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>真实姓名</th>
              <th>邮箱</th>
              <th>手机</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in list" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.username }}</td>
              <td>{{ row.realName || '-' }}</td>
              <td>{{ row.email || '-' }}</td>
              <td>{{ row.phone || '-' }}</td>
              <td>{{ row.status === 1 ? '启用' : '禁用' }}</td>
              <td>
                <button type="button" class="btn small" @click="openEdit(row)">编辑</button>
                <button type="button" class="btn small danger" @click="doDelete(row.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span>共 {{ total }} 条</span>
        <button type="button" class="btn small" :disabled="page <= 1" @click="page--; load()">上一页</button>
        <span>{{ page }} / {{ totalPages || 1 }}</span>
        <button type="button" class="btn small" :disabled="page >= (totalPages || 1)" @click="page++; load()">下一页</button>
      </div>
    </template>
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建用户</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>用户名 *</label>
            <input v-model="createForm.username" required placeholder="3-50字符" />
          </div>
          <div class="form-group">
            <label>密码 *</label>
            <input v-model="createForm.password" type="password" required placeholder="6-20字符" />
          </div>
          <div class="form-group">
            <label>真实姓名 *</label>
            <input v-model="createForm.realName" required placeholder="真实姓名" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="createForm.email" type="email" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>手机</label>
            <input v-model="createForm.phone" placeholder="11位手机号" />
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
        <h3>编辑用户</h3>
        <form v-if="editForm" @submit.prevent="submitEdit">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="editForm.username" disabled class="disabled" />
          </div>
          <div class="form-group">
            <label>真实姓名</label>
            <input v-model="editForm.realName" placeholder="真实姓名" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="editForm.email" type="email" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>手机</label>
            <input v-model="editForm.phone" placeholder="11位手机号" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="editForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="form-group">
            <label>新密码（不修改留空）</label>
            <input v-model="editForm.password" type="password" placeholder="6-20字符" />
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
import { ref, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import {
  getUsers,
  createUser,
  updateUser,
  deleteUser,
  type UserDTO,
  type UserCreateRequest,
  type UserUpdateRequest
} from '@/api/users';

const list = ref<UserDTO[]>([]);
const total = ref(0);
const totalPages = ref(0);
const page = ref(1);
const size = 10;
const loading = ref(true);
const error = ref('');
const showCreate = ref(false);
const showEdit = ref(false);
const createForm = ref<UserCreateRequest>({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  status: 1
});
const editForm = ref<(UserUpdateRequest & { id: number; username: string }) | null>(null);
const createError = ref('');
const editError = ref('');
const creating = ref(false);
const editing = ref(false);
const showTip = ref(false);

function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const res = await getUsers(page.value, size);
    list.value = res.records;
    total.value = res.total;
    totalPages.value = Math.max(1, Math.ceil(res.total / size));
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => { load(); document.addEventListener('click', closeTipOnClickOutside); });
onUnmounted(() => document.removeEventListener('click', closeTipOnClickOutside));

function openEdit(row: UserDTO) {
  editForm.value = {
    id: row.id,
    username: row.username,
    realName: row.realName ?? '',
    email: row.email ?? '',
    phone: row.phone ?? '',
    status: row.status ?? 1
  };
  showEdit.value = true;
  editError.value = '';
}

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createUser(createForm.value);
    showCreate.value = false;
    createForm.value = { username: '', password: '', realName: '', email: '', phone: '', status: 1 };
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
    const { id, username, ...body } = editForm.value;
    await updateUser(id, body);
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
  if (!confirm('确定删除该用户？')) return;
  try {
    await deleteUser(id);
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
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
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
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.form-group input.disabled { opacity: 0.7; cursor: not-allowed; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
