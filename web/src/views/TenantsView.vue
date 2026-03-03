<template>
  <section class="page">
    <p class="page-desc">多租户数据隔离与租户配置</p>
    <div class="toolbar">
      <button type="button" class="btn primary" @click="showCreate = true">新建租户</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>租户编码</th>
              <th>租户名称</th>
              <th>联系人</th>
              <th>联系电话</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.tenantCode }}</td>
              <td>{{ row.tenantName }}</td>
              <td>{{ row.contact || '-' }}</td>
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
        <button type="button" class="btn small" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
        <span class="page-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页，{{ pageData?.total ?? 0 }} 条</span>
        <button type="button" class="btn small" :disabled="currentPage >= totalPages" @click="currentPage++">下一页</button>
      </div>
    </template>
    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建租户</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>租户编码 *</label>
            <input v-model="createForm.tenantCode" required placeholder="唯一编码" />
          </div>
          <div class="form-group">
            <label>租户名称 *</label>
            <input v-model="createForm.tenantName" required placeholder="租户名称" />
          </div>
          <div class="form-group">
            <label>联系人</label>
            <input v-model="createForm.contact" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="createForm.phone" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="createForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
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
        <h3>编辑租户</h3>
        <form v-if="editForm" @submit.prevent="submitEdit">
          <div class="form-group">
            <label>租户编码</label>
            <input v-model="editForm.tenantCode" disabled class="readonly" />
          </div>
          <div class="form-group">
            <label>租户名称</label>
            <input v-model="editForm.tenantName" placeholder="租户名称" />
          </div>
          <div class="form-group">
            <label>联系人</label>
            <input v-model="editForm.contact" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="editForm.phone" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model.number="editForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
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
import { ref, computed, watch, onMounted } from 'vue';
import {
  getTenants,
  createTenant,
  updateTenant,
  deleteTenant,
  type TenantDTO,
  type TenantCreateRequest,
  type TenantUpdateRequest
} from '@/api/tenants';

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
onMounted(load);

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
  if (!confirm('确定删除该租户？')) return;
  try {
    await deleteTenant(id);
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
.toolbar { margin-bottom: 1rem; display: flex; gap: 0.5rem; align-items: center; }
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
.readonly { opacity: 0.7; cursor: not-allowed; }
.pagination { margin-top: 1rem; display: flex; align-items: center; gap: 0.75rem; font-size: 0.9rem; color: #94a3b8; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
