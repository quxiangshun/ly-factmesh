<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="跨服务审计记录，谁在何时执行了哪些操作" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-input v-model="filterService" placeholder="服务名筛选" class="filter-input" style="width: 140px" />
        <el-input v-model.number="filterUserId" type="number" placeholder="用户ID" class="filter-input" style="width: 120px" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无审计记录" />
      <el-table v-else :data="pageData?.records" class="table-wrap">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="serviceName" label="服务">
          <template #default="{ row }">{{ row.serviceName || '-' }}</template>
        </el-table-column>
        <el-table-column label="用户名">
          <template #default="{ row }">{{ row.username || row.userId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型">
          <template #default="{ row }">{{ row.operationType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operationContent || '-' }}</template>
        </el-table-column>
        <el-table-column label="结果">
          <template #default="{ row }">{{ row.operationResult === 1 ? '成功' : '失败' }}</template>
        </el-table-column>
        <el-table-column prop="clientIp" label="IP">
          <template #default="{ row }">{{ row.clientIp || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.operationTime) }}</template>
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
import { getOpsAuditLogPage } from '@/api/opsAuditLogs';

const pageData = ref<Awaited<ReturnType<typeof getOpsAuditLogPage>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 20;
const filterService = ref('');
const filterUserId = ref<number | undefined>(undefined);

const totalPages = computed(() => Math.max(1, Math.ceil((pageData.value?.total ?? 0) / pageSize)));

function formatTime(t?: string) {
  if (!t) return '-';
  try {
    return new Date(t).toLocaleString('zh-CN');
  } catch {
    return t;
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    pageData.value = await getOpsAuditLogPage(
      currentPage.value,
      pageSize,
      filterService.value || undefined,
      filterUserId.value
    );
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
