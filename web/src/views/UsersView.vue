<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="Admin 用户列表，支持创建、编辑、禁用" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-button type="primary" @click="showCreate = true">新建用户</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="list" class="table-wrap">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名">
          <template #default="{ row }">{{ row.realName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
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
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        class="pagination"
      />
    </template>
    <el-dialog v-model="showCreate" title="新建用户" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="用户名" required>
          <el-input v-model="createForm.username" placeholder="3-50字符" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="createForm.password" type="password" placeholder="6-20字符" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="createForm.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" type="email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="createForm.phone" placeholder="11位手机号" />
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showEdit" title="编辑用户" width="400px" :close-on-click-modal="false">
      <el-form v-if="editForm" :model="editForm" @submit.prevent="submitEdit">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="editForm.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" type="email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="editForm.phone" placeholder="11位手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item label="新密码（不修改留空）">
          <el-input v-model="editForm.password" type="password" placeholder="6-20字符" show-password />
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
import { ref, computed, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import {
  getUsers,
  createUser,
  updateUser,
  deleteUser,
  type UserDTO,
  type UserCreateRequest,
  type UserUpdateRequest
} from '@/api/users';
import { ElMessageBox } from 'element-plus';

const list = ref<UserDTO[]>([]);
const total = ref(0);
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

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)));

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const res = await getUsers(page.value, size);
    list.value = res.records;
    total.value = res.total;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(page, load);

onMounted(load);

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
  try {
    await ElMessageBox.confirm('确定删除该用户？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteUser(id);
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
</style>
