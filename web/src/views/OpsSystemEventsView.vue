<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="系统级事件，支持按类型与处理状态筛选" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-input v-model="filterEventType" placeholder="事件类型" class="filter-input" style="width: 140px" />
        <el-select v-model="filterProcessed" placeholder="处理状态" clearable class="filter-input" style="width: 120px">
          <el-option :value="undefined" label="全部" />
          <el-option :value="0" label="未处理" />
          <el-option :value="1" label="已处理" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无系统事件" />
      <el-table v-else :data="pageData?.records" class="table-wrap">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="eventType" label="事件类型">
          <template #default="{ row }">{{ row.eventType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="eventLevel" label="级别" />
        <el-table-column prop="eventContent" label="内容" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.eventContent || '-' }}</template>
        </el-table-column>
        <el-table-column prop="relatedService" label="关联服务">
          <template #default="{ row }">{{ row.relatedService || '-' }}</template>
        </el-table-column>
        <el-table-column prop="relatedId" label="关联ID">
          <template #default="{ row }">{{ row.relatedId || '-' }}</template>
        </el-table-column>
        <el-table-column label="已处理">
          <template #default="{ row }">{{ row.processed ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
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
import { getSystemEventPage } from '@/api/systemEvents';

const pageData = ref<Awaited<ReturnType<typeof getSystemEventPage>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 20;
const filterEventType = ref('');
const filterProcessed = ref<number | undefined>(undefined);

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
    pageData.value = await getSystemEventPage(
      currentPage.value,
      pageSize,
      filterEventType.value || undefined,
      filterProcessed.value
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
