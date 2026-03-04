<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="WMS 物料列表，支持新建与删除" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-button type="primary" @click="showCreate = true">新建物料</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无物料数据，请点击「新建物料」添加" />
      <el-table v-else :data="pageData?.records" class="table-wrap">
        <el-table-column prop="materialCode" label="编码" />
        <el-table-column prop="materialName" label="名称" />
        <el-table-column prop="materialType" label="类型">
          <template #default="{ row }">{{ row.materialType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="specification" label="规格">
          <template #default="{ row }">{{ row.specification || '-' }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位">
          <template #default="{ row }">{{ row.unit || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
    <el-dialog v-model="showCreate" title="新建物料" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="物料编码" required>
          <el-input v-model="createForm.materialCode" placeholder="如 MAT-001" />
        </el-form-item>
        <el-form-item label="物料名称" required>
          <el-input v-model="createForm.materialName" placeholder="物料名称" />
        </el-form-item>
        <el-form-item label="物料类型">
          <el-input v-model="createForm.materialType" placeholder="可选" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="createForm.specification" placeholder="可选" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="createForm.unit" placeholder="如 个、kg" />
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { getMaterialPage, createMaterial, deleteMaterial, type MaterialCreateRequest } from '@/api/materials';
import { ElMessageBox } from 'element-plus';

const pageData = ref<Awaited<ReturnType<typeof getMaterialPage>> | null>(null);
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
    pageData.value = await getMaterialPage(currentPage.value, pageSize);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);
onMounted(load);

const showCreate = ref(false);
const createForm = ref<MaterialCreateRequest>({
  materialCode: '',
  materialName: '',
  materialType: '',
  specification: '',
  unit: ''
});
const createError = ref('');
const creating = ref(false);

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createMaterial(createForm.value);
    showCreate.value = false;
    createForm.value = { materialCode: '', materialName: '', materialType: '', specification: '', unit: '' };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

async function doDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该物料？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteMaterial(id);
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
