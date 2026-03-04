<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="用户操作记录，谁在何时执行了哪些操作" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-input v-model="filterModule" placeholder="模块筛选" class="filter-input" style="width: 140px" />
        <el-input v-model="filterUsername" placeholder="用户名筛选" class="filter-input" style="width: 140px" />
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名">
          <template #default="{ row }">{{ row.username || '-' }}</template>
        </el-table-column>
        <el-table-column prop="module" label="模块">
          <template #default="{ row }">{{ row.module || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operation" label="操作">
          <template #default="{ row }">{{ row.operation || '-' }}</template>
        </el-table-column>
        <el-table-column prop="method" label="方法">
          <template #default="{ row }">{{ row.method || '-' }}</template>
        </el-table-column>
        <el-table-column prop="url" label="URL" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.url || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">{{ row.status === 1 ? '成功' : '失败' }}</template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)">
          <template #default="{ row }">{{ row.duration ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="currentPage"
        :total="pageData?.total ?? 0"
        :page-size="pageSize"
        layout="prev, pager, next"
        class="pagination"
      />
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
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
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
</style>
