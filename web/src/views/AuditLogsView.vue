<template>
  <section class="page">
    <p class="page-desc">数据变更记录</p>
    <div class="toolbar">
      <input v-model="filterTable" placeholder="表名筛选" class="filter-input" />
      <input v-model="filterRecordId" placeholder="记录ID筛选" class="filter-input" />
      <input v-model="filterOpType" placeholder="操作类型" class="filter-input" />
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
              <th>表名</th>
              <th>记录ID</th>
              <th>操作类型</th>
              <th>操作人</th>
              <th>时间</th>
              <th>详情</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.tableName || '-' }}</td>
              <td>{{ row.recordId || '-' }}</td>
              <td>{{ row.operationType || '-' }}</td>
              <td>{{ row.operatorName || '-' }}</td>
              <td>{{ formatTime(row.createTime) }}</td>
              <td>
                <button type="button" class="btn small" @click="openDetail(row)">查看</button>
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
    <div v-if="showDetail" class="modal-mask" @click.self="showDetail = false">
      <div class="modal modal-wide">
        <h3>审计详情</h3>
        <div v-if="detailRow" class="detail-content">
          <p><strong>表名</strong>: {{ detailRow.tableName }}</p>
          <p><strong>记录ID</strong>: {{ detailRow.recordId }}</p>
          <p><strong>操作类型</strong>: {{ detailRow.operationType }}</p>
          <p><strong>操作人</strong>: {{ detailRow.operatorName }}</p>
          <p><strong>时间</strong>: {{ formatTime(detailRow.createTime) }}</p>
          <p v-if="detailRow.oldValue"><strong>变更前</strong>: <pre class="json-pre">{{ detailRow.oldValue }}</pre></p>
          <p v-if="detailRow.newValue"><strong>变更后</strong>: <pre class="json-pre">{{ detailRow.newValue }}</pre></p>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
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
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; gap: 0.5rem; align-items: center; }
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; width: 120px; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.pagination { margin-top: 1rem; display: flex; align-items: center; gap: 0.75rem; font-size: 0.9rem; color: #94a3b8; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 320px; max-height: 90vh; overflow-y: auto; }
.modal-wide { min-width: 480px; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.detail-content p { margin: 0.5rem 0; font-size: 0.9rem; color: #e5e7eb; }
.json-pre { margin: 0.25rem 0; padding: 0.5rem; background: #0f172a; border-radius: 6px; font-size: 0.8rem; overflow-x: auto; max-height: 200px; overflow-y: auto; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
