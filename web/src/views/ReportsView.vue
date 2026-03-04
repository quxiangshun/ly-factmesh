<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="MES 报工录入，关联工单、工序、设备" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-select v-model="filterOrderId" placeholder="全部工单" clearable style="width: 220px">
          <el-option v-for="wo in workOrders" :key="wo.id" :value="wo.id" :label="`${wo.orderCode} - ${wo.productName}`" />
        </el-select>
        <el-button type="primary" @click="showCreate = true">新建报工</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-empty v-if="!pageData?.records?.length" description="暂无报工记录，请点击「新建报工」添加" />
      <el-table v-else :data="pageData?.records" class="table-wrap">
        <el-table-column label="工单">
          <template #default="{ row }">{{ row.orderCode || row.orderId }}</template>
        </el-table-column>
        <el-table-column label="工序">
          <template #default="{ row }">{{ row.processName || row.processId }}</template>
        </el-table-column>
        <el-table-column label="设备">
          <template #default="{ row }">{{ deviceLabel(row.deviceId) }}</template>
        </el-table-column>
        <el-table-column label="报工/报废">
          <template #default="{ row }">{{ row.reportQuantity }} / {{ row.scrapQuantity ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="操作员">
          <template #default="{ row }">{{ row.operator || '-' }}</template>
        </el-table-column>
        <el-table-column label="报工时间">
          <template #default="{ row }">{{ formatTime(row.reportTime) }}</template>
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
    <el-dialog v-model="showCreate" title="新建报工" width="440px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="工单" required>
          <el-select v-model="createForm.orderId" placeholder="请选择工单" style="width: 100%" required>
            <el-option v-for="wo in workOrders" :key="wo.id" :value="wo.id" :label="`${wo.orderCode} - ${wo.productName} (${wo.statusText})`" />
          </el-select>
        </el-form-item>
        <el-form-item label="工序" required>
          <el-select v-model="createForm.processId" placeholder="请选择工序" style="width: 100%" required>
            <el-option v-for="p in processes" :key="p.id" :value="p.id" :label="`${p.processCode} - ${p.processName}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" required>
          <el-select v-model="createForm.deviceId" placeholder="请选择设备" style="width: 100%" required>
            <el-option v-for="d in devices" :key="d.id" :value="d.id" :label="`${d.deviceCode} - ${d.deviceName}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="报工数量" required>
          <el-input-number v-model="createForm.reportQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="报废数量">
          <el-input-number v-model="createForm.scrapQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作员">
          <el-input v-model="createForm.operator" placeholder="可选" />
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
import {
  getWorkReportPage,
  createWorkReport,
  deleteWorkReport,
  type WorkReportCreateRequest
} from '@/api/workReports';
import { getWorkOrderPage, type WorkOrderDTO } from '@/api/workOrders';
import { getProcessList, type ProcessDTO } from '@/api/processes';
import { getDeviceList, type DeviceDTO } from '@/api/devices';
import { ElMessageBox } from 'element-plus';

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

function deviceLabel(id?: number) {
  if (!id) return '-';
  const d = devices.value.find((x) => x.id === id);
  return d ? `${d.deviceCode} - ${d.deviceName}` : String(id);
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
  try {
    await ElMessageBox.confirm('确定删除该报工记录？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteWorkReport(id);
    await load();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
