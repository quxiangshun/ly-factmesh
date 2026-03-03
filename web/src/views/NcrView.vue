<template>
  <section class="page">
    <p class="page-desc">QMS 不合格品登记与处置管理</p>
    <div class="toolbar">
      <select v-model="filterDisposal" class="filter-select">
        <option value="">全部</option>
        <option :value="0">待处理</option>
        <option :value="1">已处理</option>
      </select>
      <button type="button" class="btn primary" @click="showCreate = true">新建不合格品</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>NCR编号</th>
              <th>产品编码</th>
              <th>批次号</th>
              <th>数量</th>
              <th>不合格原因</th>
              <th>处置方式</th>
              <th>处置状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageData?.records" :key="row.id">
              <td>{{ row.ncrNo ?? '-' }}</td>
              <td>{{ row.productCode }}</td>
              <td>{{ row.batchNo ?? '-' }}</td>
              <td>{{ row.quantity }}</td>
              <td>{{ row.reason }}</td>
              <td>{{ disposalMethodText(row.disposalMethod) }}</td>
              <td>{{ disposalResultText(row.disposalResult) }}</td>
              <td>{{ formatTime(row.createTime) }}</td>
              <td>
                <template v-if="row.disposalResult === 0">
                  <button type="button" class="btn small" @click="openDisposeModal(row)">处置完成</button>
                </template>
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
        <h3>新建不合格品</h3>
        <form @submit.prevent="submitCreate">
          <div class="form-group">
            <label>产品编码</label>
            <input v-model="createForm.productCode" required placeholder="如 P001" />
          </div>
          <div class="form-group">
            <label>批次号</label>
            <input v-model="createForm.batchNo" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>数量</label>
            <input v-model.number="createForm.quantity" type="number" min="1" required />
          </div>
          <div class="form-group">
            <label>不合格原因</label>
            <textarea v-model="createForm.reason" required placeholder="请描述不合格原因" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>处置方式</label>
            <select v-model.number="createForm.disposalMethod">
              <option :value="undefined">请选择</option>
              <option :value="0">待处置</option>
              <option :value="1">返工</option>
              <option :value="2">报废</option>
              <option :value="3">让步接收</option>
              <option :value="4">退货</option>
            </select>
          </div>
          <div class="form-group">
            <label>关联质检任务ID</label>
            <input v-model.number="createForm.taskId" type="number" placeholder="可选" />
          </div>
          <div class="form-group">
            <label>备注</label>
            <input v-model="createForm.remark" placeholder="可选" />
          </div>
          <p v-if="createError" class="error-msg">{{ createError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showCreate = false">取消</button>
            <button type="submit" class="btn primary" :disabled="creating">确定</button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showDispose" class="modal-mask" @click.self="showDispose = false">
      <div class="modal">
        <h3>处置完成 - {{ disposeRow?.ncrNo ?? disposeRow?.productCode }}</h3>
        <form @submit.prevent="submitDispose">
          <div class="form-group">
            <label>处置方式</label>
            <select v-model.number="disposeForm.disposalMethod" required>
              <option :value="undefined">请选择</option>
              <option :value="1">返工</option>
              <option :value="2">报废</option>
              <option :value="3">让步接收</option>
              <option :value="4">退货</option>
            </select>
          </div>
          <div class="form-group">
            <label>处置说明</label>
            <input v-model="disposeForm.remark" placeholder="可选" />
          </div>
          <p v-if="disposeError" class="error-msg">{{ disposeError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="showDispose = false">取消</button>
            <button type="submit" class="btn primary" :disabled="disposing">确定</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
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
  if (!confirm('确定删除该不合格品记录？')) return;
  try {
    await deleteNcr(id);
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
.toolbar { margin-bottom: 1rem; display: flex; align-items: center; gap: 0.75rem; }
.filter-select { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; margin-right: 0.25rem; }
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
.form-group input, .form-group textarea, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; box-sizing: border-box; }
.form-group textarea { resize: vertical; min-height: 60px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
