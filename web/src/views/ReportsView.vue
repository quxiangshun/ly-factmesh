<template>
  <section class="page">
    <p class="page-desc">MES 报工录入，关联工单、工序、设备</p>
    <div class="toolbar">
      <select v-model="filterOrderId" class="filter-select">
        <option :value="undefined">全部工单</option>
        <option v-for="wo in workOrders" :key="wo.id" :value="wo.id">
          {{ wo.orderCode }} - {{ wo.productName }}
        </option>
      </select>
      <button type="button" class="btn primary" @click="showCreate = true">新建报工</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div v-if="!pageData?.records?.length" class="empty-state">暂无报工记录，请点击「新建报工」添加</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>工单</th>
              <th>工序</th>
              <th>设备</th>
              <th>报工/报废</th>
              <th>操作员</th>
              <th>报工时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.orderCode || row.orderId }}</td>
              <td>{{ row.processName || row.processId }}</td>
              <td>{{ deviceLabel(row.deviceId) }}</td>
              <td>{{ row.reportQuantity }} / {{ row.scrapQuantity ?? 0 }}</td>
              <td>{{ row.operator || '-' }}</td>
              <td>{{ formatTime(row.reportTime) }}</td>
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
        <h3>新建报工</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>工单 *</label>
            <select v-model="createForm.orderId" required>
              <option value="">请选择工单</option>
              <option v-for="wo in workOrders" :key="wo.id" :value="wo.id">
                {{ wo.orderCode }} - {{ wo.productName }} ({{ wo.statusText }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>工序 *</label>
            <select v-model="createForm.processId" required>
              <option value="">请选择工序</option>
              <option v-for="p in processes" :key="p.id" :value="p.id">
                {{ p.processCode }} - {{ p.processName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>设备 *</label>
            <select v-model="createForm.deviceId" required>
              <option value="">请选择设备</option>
              <option v-for="d in devices" :key="d.id" :value="d.id">
                {{ d.deviceCode }} - {{ d.deviceName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>报工数量 *</label>
            <input v-model.number="createForm.reportQuantity" type="number" min="0" required />
          </div>
          <div class="form-group">
            <label>报废数量</label>
            <input v-model.number="createForm.scrapQuantity" type="number" min="0" />
          </div>
          <div class="form-group">
            <label>操作员</label>
            <input v-model="createForm.operator" placeholder="可选" />
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
import {
  getWorkReportPage,
  createWorkReport,
  deleteWorkReport,
  type WorkReportCreateRequest
} from '@/api/workReports';
import { getWorkOrderPage, type WorkOrderDTO } from '@/api/workOrders';
import { getProcessList, type ProcessDTO } from '@/api/processes';
import { getDeviceList, type DeviceDTO } from '@/api/devices';

const pageData = ref<Awaited<ReturnType<typeof getWorkReportPage>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;
const filterOrderId = ref<number | undefined>(undefined);

const workOrders = ref<WorkOrderDTO[]>([]);
const processes = ref<ProcessDTO[]>([]);
const devices = ref<DeviceDTO[]>([]);

const totalPages = computed(() => {
  const t = pageData.value?.total ?? 0;
  return Math.max(1, Math.ceil(t / pageSize));
});

function formatTime(s?: string) {
  if (!s) return '-';
  try {
    const d = new Date(s);
    return d.toLocaleString('zh-CN');
  } catch {
    return s;
  }
}

async function loadOptions() {
  try {
    const [woRes, procRes, devRes] = await Promise.all([
      getWorkOrderPage(1, 200),
      getProcessList(),
      getDeviceList()
    ]);
    workOrders.value = (woRes?.records ?? []).filter(
      (wo: WorkOrderDTO) => wo.status === 1 || wo.status === 2
    ).map((wo: WorkOrderDTO) => ({
      ...wo,
      statusText: wo.status === 1 ? '已下发' : '进行中'
    }));
    processes.value = procRes ?? [];
    devices.value = devRes ?? [];
  } catch {
    workOrders.value = [];
    processes.value = [];
    devices.value = [];
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    pageData.value = await getWorkReportPage(currentPage.value, pageSize, filterOrderId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

watch([currentPage, filterOrderId], load);
onMounted(() => {
  loadOptions();
  load();
});

const showCreate = ref(false);
const createForm = ref<WorkReportCreateRequest>({
  orderId: 0,
  processId: 0,
  deviceId: 0,
  reportQuantity: 0,
  scrapQuantity: 0,
  operator: ''
});
const createError = ref('');
const creating = ref(false);

async function submitCreate() {
  createError.value = '';
  const orderId = Number(createForm.value.orderId);
  const processId = Number(createForm.value.processId);
  const deviceId = Number(createForm.value.deviceId);
  if (!orderId || !processId || !deviceId) {
    createError.value = '请选择工单、工序、设备';
    return;
  }
  creating.value = true;
  try {
    await createWorkReport({
      ...createForm.value,
      orderId,
      processId,
      deviceId
    });
    showCreate.value = false;
    createForm.value = { orderId: 0, processId: 0, deviceId: 0, reportQuantity: 0, scrapQuantity: 0, operator: '' };
    await loadOptions();
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

async function doDelete(id: number) {
  if (!confirm('确定删除该报工记录？')) return;
  try {
    await deleteWorkReport(id);
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.empty-state { color: #94a3b8; padding: 2rem; text-align: center; }
.page-title { margin: 0 0 0.25rem; font-size: 1.5rem; color: #e5e7eb; }
.page-desc { margin: 0 0 1rem; font-size: 0.9rem; color: #94a3b8; }
.toolbar { margin-bottom: 1rem; display: flex; align-items: center; gap: 0.75rem; }
.filter-select { padding: 0.4rem 0.75rem; border-radius: 6px; background: #1e293b; color: #e5e7eb; border: 1px solid #475569; min-width: 180px; }
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
.modal { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; min-width: 360px; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #94a3b8; }
.form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
