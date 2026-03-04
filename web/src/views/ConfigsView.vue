<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="Admin 系统参数配置，支持创建、编辑、删除" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-input v-model="filterKey" placeholder="配置键筛选" style="width: 180px" clearable />
        <el-button @click="load">查询</el-button>
        <el-button type="primary" @click="showCreate = true">新建配置</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="configKey" label="配置键" />
        <el-table-column prop="configValue" label="配置值" min-width="120">
          <template #default="{ row }">
            <span class="config-value-cell">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="configDesc" label="描述">
          <template #default="{ row }">{{ row.configDesc || '-' }}</template>
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
    <el-dialog v-model="showCreate" title="新建配置" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="配置键" required>
          <el-input v-model="createForm.configKey" placeholder="如 app.name" />
        </el-form-item>
        <el-form-item label="配置值" required>
          <el-input v-model="createForm.configValue" type="textarea" :rows="3" placeholder="配置值" />
        </el-form-item>
        <el-form-item label="配置描述">
          <el-input v-model="createForm.configDesc" placeholder="可选" />
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
    <el-dialog v-model="showEdit" title="编辑配置" width="400px" :close-on-click-modal="false">
      <el-form v-if="editForm" :model="editForm" @submit.prevent="submitEdit">
        <el-form-item label="配置键">
          <el-input v-model="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值" required>
          <el-input v-model="editForm.configValue" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="配置描述">
          <el-input v-model="editForm.configDesc" placeholder="可选" />
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
  getConfigs,
  createConfig,
  updateConfig,
  deleteConfig,
  type ConfigDTO,
  type ConfigCreateRequest,
  type ConfigUpdateRequest
} from '@/api/configs';
import { ElMessageBox } from 'element-plus';

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
  try {
    await ElMessageBox.confirm('确定删除该配置？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteConfig(id);
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
.config-value-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
