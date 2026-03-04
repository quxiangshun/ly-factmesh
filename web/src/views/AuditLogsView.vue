<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="数据变更记录" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-input v-model="filterTable" placeholder="表名筛选" class="filter-input" style="width: 120px" />
        <el-input v-model="filterRecordId" placeholder="记录ID筛选" class="filter-input" style="width: 120px" />
        <el-input v-model="filterOpType" placeholder="操作类型" class="filter-input" style="width: 120px" />
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tableName" label="表名">
          <template #default="{ row }">{{ row.tableName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="recordId" label="记录ID">
          <template #default="{ row }">{{ row.recordId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型">
          <template #default="{ row }">{{ row.operationType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人">
          <template #default="{ row }">{{ row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="详情" width="80">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">查看</el-button>
          </template>
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

    <el-dialog v-model="showDetail" title="审计详情" width="520px">
      <div v-if="detailRow" class="detail-content">
        <p><strong>表名</strong>: {{ detailRow.tableName }}</p>
        <p><strong>记录ID</strong>: {{ detailRow.recordId }}</p>
        <p><strong>操作类型</strong>: {{ detailRow.operationType }}</p>
        <p><strong>操作人</strong>: {{ detailRow.operatorName }}</p>
        <p><strong>时间</strong>: {{ formatTime(detailRow.createTime) }}</p>
        <p v-if="detailRow.oldValue"><strong>变更前</strong>: <pre class="json-pre">{{ detailRow.oldValue }}</pre></p>
        <p v-if="detailRow.newValue"><strong>变更后</strong>: <pre class="json-pre">{{ detailRow.newValue }}</pre></p>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { getAuditLogs, type AuditLogDTO } from '@/api/auditLogs';

const filterTable = ref('');
const filterRecordId = ref('');
const filterOpType = ref('');
const pageData = ref<Awaited<ReturnType<typeof getAuditLogs>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 20;
const showDetail = ref(false);
const detailRow = ref<AuditLogDTO | null>(null);

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

function openDetail(row: AuditLogDTO) {
  detailRow.value = row;
  showDetail.value = true;
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    pageData.value = await getAuditLogs(currentPage.value, pageSize, {
      tableName: filterTable.value || undefined,
      recordId: filterRecordId.value || undefined,
      operationType: filterOpType.value || undefined
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
.detail-content p { margin: 0.5rem 0; font-size: 0.9rem; color: #e5e7eb; }
.json-pre { margin: 0.25rem 0; padding: 0.5rem; background: #0f172a; border-radius: 6px; font-size: 0.8rem; overflow-x: auto; max-height: 200px; overflow-y: auto; }
</style>
