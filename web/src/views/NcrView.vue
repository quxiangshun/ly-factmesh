<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="QMS 不合格品登记与处置管理" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
        <el-select v-model="filterDisposal" placeholder="全部" clearable style="width: 120px">
          <el-option label="待处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-button type="primary" @click="showCreate = true">新建不合格品</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <el-table :data="pageData?.records" class="table-wrap">
        <el-table-column label="NCR编号">
          <template #default="{ row }">{{ row.ncrNo ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="productCode" label="产品编码" />
        <el-table-column prop="batchNo" label="批次号">
          <template #default="{ row }">{{ row.batchNo ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" />
        <el-table-column prop="reason" label="不合格原因" />
        <el-table-column label="处置方式">
          <template #default="{ row }">{{ disposalMethodText(row.disposalMethod) }}</template>
        </el-table-column>
        <el-table-column label="处置状态">
          <template #default="{ row }">{{ disposalResultText(row.disposalResult) }}</template>
        </el-table-column>
        <el-table-column label="创建时间">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.disposalResult === 0">
              <el-button size="small" @click="openDisposeModal(row)">处置完成</el-button>
            </template>
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
    <el-dialog v-model="showCreate" title="新建不合格品" width="420px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreate">
        <el-form-item label="产品编码" required>
          <el-input v-model="createForm.productCode" placeholder="如 P001" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="createForm.batchNo" placeholder="可选" />
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="createForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="不合格原因" required>
          <el-input v-model="createForm.reason" type="textarea" :rows="3" placeholder="请描述不合格原因" />
        </el-form-item>
        <el-form-item label="处置方式">
          <el-select v-model="createForm.disposalMethod" placeholder="请选择" clearable style="width: 100%">
            <el-option label="待处置" :value="0" />
            <el-option label="返工" :value="1" />
            <el-option label="报废" :value="2" />
            <el-option label="让步接收" :value="3" />
            <el-option label="退货" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联质检任务ID">
          <el-input-number v-model="createForm.taskId" placeholder="可选" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="createError" type="error" :title="createError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="creating">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog v-model="showDispose" :title="`处置完成 - ${disposeRow?.ncrNo ?? disposeRow?.productCode}`" width="400px" :close-on-click-modal="false">
      <el-form @submit.prevent="submitDispose">
        <el-form-item label="处置方式" required>
          <el-select v-model="disposeForm.disposalMethod" placeholder="请选择" style="width: 100%" clearable>
            <el-option label="返工" :value="1" />
            <el-option label="报废" :value="2" />
            <el-option label="让步接收" :value="3" />
            <el-option label="退货" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置说明">
          <el-input v-model="disposeForm.remark" placeholder="可选" />
        </el-form-item>
        <el-alert v-if="disposeError" type="error" :title="disposeError" show-icon class="error-alert" />
        <div class="dialog-footer">
          <el-button @click="showDispose = false">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="disposing">确定</el-button>
        </div>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import {
  getNcrPage,
  createNcr,
  disposeNcr,
  deleteNcr,
  type NonConformingProductCreateRequest,
  type NonConformingProductDTO
} from '@/api/ncr';
import { getInspectionTaskNcrContext } from '@/api/inspectionTasks';
import { ElMessageBox } from 'element-plus';

const pageData = ref<Awaited<ReturnType<typeof getNcrPage>> | null>(null);
const loading = ref(true);
const error = ref('');
const currentPage = ref(1);
const pageSize = 10;
const filterDisposal = ref<string | number>('');

const totalPages = computed(() => {
  const t = pageData.value?.total ?? 0;
  return Math.max(1, Math.ceil(t / pageSize));
});

function disposalMethodText(m?: number) {
  const map: Record<number, string> = { 0: '待处置', 1: '返工', 2: '报废', 3: '让步接收', 4: '退货' };
  return m !== undefined && m !== null ? (map[m] ?? String(m)) : '-';
}

function disposalResultText(r: number) {
  return r === 1 ? '已处理' : '待处理';
}

function formatTime(s?: string) {
  if (!s) return '-';
  try {
    return new Date(s).toLocaleString('zh-CN');
  } catch {
    return s;
  }
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const disposalParam = filterDisposal.value === '' ? undefined : Number(filterDisposal.value);
    pageData.value = await getNcrPage(currentPage.value, pageSize, disposalParam);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

const route = useRoute();
const router = useRouter();
watch([currentPage, filterDisposal], load);
onMounted(async () => {
  await load();
  const fromTask = route.query.fromTask;
  if (fromTask) {
    const taskId = Number(fromTask);
    if (!isNaN(taskId)) {
      try {
        const ctx = await getInspectionTaskNcrContext(taskId);
        createForm.value = {
          productCode: ctx.suggestedProductCode ?? `P-${taskId}`,
          batchNo: '',
          quantity: 1,
          reason: `质检任务 ${ctx.taskCode} 不合格`,
          disposalMethod: undefined,
          taskId: taskId,
          remark: ''
        };
        showCreate.value = true;
        router.replace({ path: '/qms/ncr' });
      } catch {
        router.replace({ path: '/qms/ncr' });
      }
    }
  }
});

const showCreate = ref(false);
const createForm = ref<NonConformingProductCreateRequest>({
  productCode: '',
  batchNo: '',
  quantity: 1,
  reason: '',
  disposalMethod: undefined,
  taskId: undefined,
  remark: ''
});
const createError = ref('');
const creating = ref(false);

async function submitCreate() {
  createError.value = '';
  creating.value = true;
  try {
    await createNcr(createForm.value);
    showCreate.value = false;
    createForm.value = { productCode: '', batchNo: '', quantity: 1, reason: '', disposalMethod: undefined, taskId: undefined, remark: '' };
    await load();
  } catch (e) {
    createError.value = e instanceof Error ? e.message : '创建失败';
  } finally {
    creating.value = false;
  }
}

const showDispose = ref(false);
const disposeRow = ref<NonConformingProductDTO | null>(null);
const disposeForm = ref({ disposalMethod: undefined as number | undefined, remark: '' });
const disposeError = ref('');
const disposing = ref(false);

function openDisposeModal(row: NonConformingProductDTO) {
  disposeRow.value = row;
  disposeForm.value = { disposalMethod: undefined, remark: '' };
  disposeError.value = '';
  showDispose.value = true;
}

async function submitDispose() {
  if (!disposeRow.value || disposeForm.value.disposalMethod === undefined) {
    disposeError.value = '请选择处置方式';
    return;
  }
  disposing.value = true;
  disposeError.value = '';
  try {
    await disposeNcr(disposeRow.value.id, {
      disposalMethod: disposeForm.value.disposalMethod,
      remark: disposeForm.value.remark || undefined
    });
    showDispose.value = false;
    await load();
  } catch (e) {
    disposeError.value = e instanceof Error ? e.message : '处置失败';
  } finally {
    disposing.value = false;
  }
}

async function doDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该不合格品记录？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteNcr(id);
    await load();
  } catch (e) {
    if (e !== 'cancel') error.value = e instanceof Error ? e.message : '删除失败';
  }
}
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; align-items: center; gap: 0.75rem; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; margin-right: 0.25rem; }
.error-alert { margin-bottom: 1rem; }
.table-wrap { margin-bottom: 1rem; }
.pagination { margin-top: 1rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
