<template>
  <section class="page">
    <h1 class="page-title">物料管理</h1>
    <p class="page-desc">WMS 物料列表，支持新建与删除</p>
    <div class="toolbar">
      <button type="button" class="btn primary" @click="showCreate = true">新建物料</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>编码</th>
              <th>名称</th>
              <th>类型</th>
              <th>规格</th>
              <th>单位</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.materialCode }}</td>
              <td>{{ row.materialName }}</td>
              <td>{{ row.materialType || '-' }}</td>
              <td>{{ row.specification || '-' }}</td>
              <td>{{ row.unit || '-' }}</td>
              <td>
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
        <h3>新建物料</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>物料编码</label>
            <input v-model="createForm.materialCode" required placeholder="如 MAT-001" />
          </div>
          <div class="form-group">
            <label>物料名称</label>
            <input v-model="createForm.materialName" required placeholder="物料名称" />
          </div>
          <div class="form-group">
            <label>物料类型</label>
            <input v-model="createForm.materialType" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>规格</label>
            <input v-model="createForm.specification" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>单位</label>
            <input v-model="createForm.unit" placeholder="如 个、kg" />
          </div>
          <p v-if="createError" class="error-msg">{{ createError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showCreate = false">取消</button>
            <button type="submit" class="btn primary" :disabled="creating">确定</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { getMaterialPage, createMaterial, deleteMaterial, type MaterialCreateRequest } from '@/api/materials';

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
  if (!confirm('确定删除该物料？')) return;
  try {
    await deleteMaterial(id);
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
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn.danger { color: #f87171; border-color: #f87171; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; margin-bottom: 1rem; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { display: flex; align-items: center; gap: 1rem; font-size: 0.9rem; color: #94a3b8; }
.pagination .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
