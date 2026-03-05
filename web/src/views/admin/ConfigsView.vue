<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="title-with-tip">
          <span class="tip-trigger" title="功能说明" @click.stop="showTip = !showTip">
            <Icon icon="mdi:information-outline" class="tip-icon" />
          </span>
          <div v-if="showTip" class="tip-popover" @click.stop>
            <div class="tip-content">Admin 系统参数配置，支持创建、编辑、删除</div>
          </div>
        </div>
        <input v-model="filterKey" placeholder="配置键筛选" class="filter-input" />
        <button type="button" class="btn" @click="load">查询</button>
        <button type="button" class="btn primary" @click="showCreate = true">新建配置</button>
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
              <th>配置键</th>
              <th>配置值</th>
              <th>描述</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.configKey }}</td>
              <td class="config-value">{{ row.configValue }}</td>
              <td>{{ row.configDesc || '-' }}</td>
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
        <h3>新建配置</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>配置键 *</label>
            <input v-model="createForm.configKey" required placeholder="如 app.name" />
          </div>
          <div class="form-group">
            <label>配置值 *</label>
            <textarea v-model="createForm.configValue" required placeholder="配置值" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>配置描述</label>
            <input v-model="createForm.configDesc" placeholder="可选" />
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
        <h3>编辑配置</h3>
        <form v-if="editForm" @submit.prevent="submitEdit">
          <div class="form-group">
            <label>配置键</label>
            <input v-model="editForm.configKey" disabled class="disabled" />
          </div>
          <div class="form-group">
            <label>配置值 *</label>
            <textarea v-model="editForm.configValue" required rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>配置描述</label>
            <input v-model="editForm.configDesc" placeholder="可选" />
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import {
  getConfigs,
  createConfig,
  updateConfig,
  deleteConfig,
  type ConfigDTO,
  type ConfigCreateRequest,
  type ConfigUpdateRequest
} from '@/api/configs';

const filterKey = ref('');
const pageData = ref<Awaited<ReturnType<typeof getConfigs>> | null>(null);
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
    pageData.value = await getConfigs(currentPage.value, pageSize, filterKey.value || undefined);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);
onMounted(load);

const showCreate = ref(false);
const createForm = ref<ConfigCreateRequest>({
  configKey: '',
  configValue: '',
  configDesc: '',
  status: 1
});
const createError = ref('');
const creating = ref(false);

const showEdit = ref(false);
const editForm = ref<(ConfigUpdateRequest & { id: number; configKey: string }) | null>(null);
const editError = ref('');
const editing = ref(false);

function openEdit(row: ConfigDTO) {
  editForm.value = {
    id: row.id,
    configKey: row.configKey,
    configValue: row.configValue,
    configDesc: row.configDesc ?? '',
    status: row.status ?? 1
  };
  showEdit.value = true;
  editError.value = '';
}

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createConfig(createForm.value);
    showCreate.value = false;
    createForm.value = { configKey: '', configValue: '', configDesc: '', status: 1 };
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
    const { id, configKey, ...body } = editForm.value;
    await updateConfig(id, body);
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
  if (!confirm('确定删除该配置？')) return;
  try {
    await deleteConfig(id);
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
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; width: 180px; }
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
.config-value { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pagination { margin-top: 1rem; display: flex; align-items: center; gap: 0.75rem; font-size: 0.9rem; color: #94a3b8; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.form-group input.disabled { opacity: 0.7; cursor: not-allowed; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
