<template>
  <section class="page">
    <p class="page-desc">用户操作记录，谁在何时执行了哪些操作</p>
    <div class="toolbar">
      <input v-model="filterModule" placeholder="模块筛选" class="filter-input" />
      <input v-model="filterUsername" placeholder="用户名筛选" class="filter-input" />
      <button type="button" class="btn" @click="load">查询</button>
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
              <th>模块</th>
              <th>操作</th>
              <th>方法</th>
              <th>URL</th>
              <th>状态</th>
              <th>耗时(ms)</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.username || '-' }}</td>
              <td>{{ row.module || '-' }}</td>
              <td>{{ row.operation || '-' }}</td>
              <td>{{ row.method || '-' }}</td>
              <td class="url-cell">{{ row.url || '-' }}</td>
              <td>{{ row.status === 1 ? '成功' : '失败' }}</td>
              <td>{{ row.duration ?? '-' }}</td>
              <td>{{ formatTime(row.createTime) }}</td>
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
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { getOperationLogs } from '@/api/operationLogs';

const filterModule = ref('');
const filterUsername = ref('');
const pageData = ref<Awaited<ReturnType<typeof getOperationLogs>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 20;

const totalPages = computed(() => {
  const t = pageData.value?.total ?? 0;
  return Math.max(1, Math.ceil(t / pageSize));
});

function formatTime(t?: string) {
  if (!t) return '-';
  try {
    const d = new Date(t);
    return d.toLocaleString('zh-CN');
  } catch {
    return t;
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    pageData.value = await getOperationLogs(currentPage.value, pageSize, {
      module: filterModule.value || undefined,
      username: filterUsername.value || undefined
    });
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch(currentPage, load);
onMounted(load);
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; gap: 0.5rem; align-items: center; }
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; width: 140px; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.url-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pagination { margin-top: 1rem; display: flex; align-items: center; gap: 0.75rem; font-size: 0.9rem; color: #94a3b8; }
</style>
