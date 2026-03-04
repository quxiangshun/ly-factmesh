<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="多租户数据隔离与租户配置" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-button type="primary" @click="showCreate = true">新建租户</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="tenantCode" label="租户编码" />
        <el-table-column prop="tenantName" label="租户名称" />
        <el-table-column prop="contact" label="联系人">
          <template #default="{ row }">{{ row.contact || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话">
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
        v-model:current-page="currentPage"
        :total="pageData?.total ?? 0"
        :page-size="pageSize"
        layout="prev, pager, next"
        class="pagination"
      />
    </template>
    <el-dialog v-model="showCreate" title="新建租户" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="租户编码" required>
          <el-input v-model="createForm.tenantCode" placeholder="唯一编码" />
        </el-form-item>
        <el-form-item label="租户名称" required>
          <el-input v-model="createForm.tenantName" placeholder="租户名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="createForm.contact" placeholder="可选" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="createForm.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="createForm.status" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showEdit" title="编辑租户" width="400px" :close-on-click-modal="false">
      <el-form v-if="editForm" :model="editForm" @submit.prevent="submitEdit">
        <el-form-item label="租户编码">
          <el-input :model-value="editForm.tenantCode" disabled />
        </el-form-item>
        <el-form-item label="租户名称">
          <el-input v-model="editForm.tenantName" placeholder="租户名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="editForm.contact" placeholder="可选" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
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
  getTenants,
  createTenant,
  updateTenant,
  deleteTenant,
  type TenantDTO,
  type TenantCreateRequest,
  type TenantUpdateRequest
} from '@/api/tenants';
import { ElMessageBox } from 'element-plus';

const pageData = ref<Awaited<ReturnType<typeof getTenants>> | null>(null);
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
    pageData.value = await getTenants(currentPage.value, pageSize);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);

const showCreate = ref(false);
const createForm = ref<TenantCreateRequest>({
  tenantCode: '',
  tenantName: '',
  contact: '',
  phone: '',
  status: 1
});
const createError = ref('');
const creating = ref(false);

const showEdit = ref(false);
const editForm = ref<(TenantUpdateRequest & TenantDTO) | null>(null);
const editError = ref('');
const editing = ref(false);

function openEdit(row: TenantDTO) {
  editForm.value = {
    ...row,
    tenantName: row.tenantName,
    contact: row.contact ?? '',
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
    await createTenant(createForm.value);
    showCreate.value = false;
    createForm.value = { tenantCode: '', tenantName: '', contact: '', phone: '', status: 1 };
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
    const { id, tenantCode, createTime, updateTime, ...body } = editForm.value;
    await updateTenant(id!, body);
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
    await ElMessageBox.confirm('确定删除该租户？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteTenant(id);
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
