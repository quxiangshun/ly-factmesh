<template>
  <section class="page">
    <header class="page-header">
      <h1>自定义报表</h1>
      <p class="page-desc">选择预定义模板（生产日报、库存统计、设备统计等）或已保存的报表定义，执行后查看结果；可将当前配置保存为定义以便重复使用。</p>
    </header>
    <div class="toolbar">
      <div class="toolbar-actions">
        <span class="section-label">报表类型</span>
        <select v-model="reportSource" class="filter-input">
          <option value="template">按模板</option>
          <option value="definition">按已保存定义</option>
        </select>
        <template v-if="reportSource === 'template'">
          <select v-model="selectedTemplate" class="filter-input">
            <option value="">请选择</option>
            <option
              v-for="t in templates"
              :key="t.code"
              :value="t.code"
            >{{ t.name }}</option>
          </select>
        </template>
        <template v-else>
          <select v-model="selectedDefId" class="filter-input">
            <option :value="0">请选择</option>
            <option
              v-for="d in definitions"
              :key="d.id"
              :value="d.id!"
            >{{ d.name }}</option>
          </select>
        </template>
        <template v-if="reportSource === 'template' && selectedTemplate">
          <template v-if="paramKeys.includes('date')">
            <input v-model="params.date" type="date" class="filter-input" placeholder="日期" />
          </template>
          <template v-if="paramKeys.includes('page')">
            <input v-model.number="params.page" type="number" min="1" class="filter-input num" placeholder="页码" />
          </template>
          <template v-if="paramKeys.includes('size')">
            <input v-model.number="params.size" type="number" min="1" max="200" class="filter-input num" placeholder="每页" />
          </template>
        </template>
        <button type="button" class="btn primary" :disabled="!canExecute" @click="run">执行</button>
        <button v-if="reportSource === 'template' && selectedTemplate" type="button" class="btn" @click="openSaveDialog">保存为定义</button>
      </div>
    </div>

    <div class="definitions-section">
      <h3 class="section-title">已保存报表定义</h3>
      <button type="button" class="btn small" @click="loadDefinitions">刷新</button>
      <div v-if="definitions.length" class="def-list">
        <div
          v-for="d in definitions"
          :key="d.id"
          class="def-item"
        >
          <span>{{ d.name }}</span>
          <span class="def-type">{{ d.reportType }}</span>
          <button type="button" class="btn-link" @click="editDefinition(d)">编辑</button>
          <button type="button" class="btn-link danger" @click="confirmDelete(d)">删除</button>
        </div>
      </div>
      <div v-else class="empty-state">暂无已保存定义</div>
    </div>

    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else-if="result">
      <div class="result-section">
        <h3 class="section-title">查询结果</h3>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th v-for="col in result.columns" :key="col">{{ col }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in result.data" :key="i">
                <td v-for="col in result.columns" :key="col">{{ formatCell(row[col]) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="!result.data.length" class="empty-state">无数据</div>
        </div>
      </div>
    </template>

    <dialog v-if="showSaveDialog" class="modal" open @click.self="showSaveDialog = false">
      <div class="modal-content" @click.stop>
        <h3>保存报表定义</h3>
        <div class="form-row">
          <label>名称</label>
          <input v-model="saveForm.name" type="text" placeholder="报表名称" />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn" @click="showSaveDialog = false">取消</button>
          <button type="button" class="btn primary" @click="saveDefinition">保存</button>
        </div>
      </div>
    </dialog>

    <dialog v-if="showEditDialog" class="modal" open @click.self="showEditDialog = false">
      <div class="modal-content" @click.stop>
        <h3>编辑报表定义</h3>
        <div class="form-row">
          <label>名称</label>
          <input v-model="editForm.name" type="text" placeholder="报表名称" />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn" @click="showEditDialog = false">取消</button>
          <button type="button" class="btn primary" @click="submitEdit">更新</button>
        </div>
      </div>
    </dialog>

    <dialog v-if="deleteTarget" class="modal" open @click.self="deleteTarget = null">
      <div class="modal-content" @click.stop>
        <p>确定删除「{{ deleteTarget.name }}」？</p>
        <div class="modal-actions">
          <button type="button" class="btn" @click="deleteTarget = null">取消</button>
          <button type="button" class="btn danger" @click="doDelete">删除</button>
        </div>
      </div>
    </dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import {
  getReportTemplates,
  getReportDefPage,
  createReportDef,
  updateReportDef,
  deleteReportDef,
  executeReport,
  type ReportDefDTO,
  type ReportExecuteResult
} from '@/api/reports';

const reportSource = ref<'template' | 'definition'>('template');
const templates = ref<{ code: string; name: string; params: string[] }[]>([]);
const definitions = ref<ReportDefDTO[]>([]);
const selectedTemplate = ref('');
const selectedDefId = ref(0);
const params = ref<Record<string, unknown>>({ date: '', page: 1, size: 50 });
const loading = ref(false);
const error = ref('');
const result = ref<ReportExecuteResult | null>(null);
const showSaveDialog = ref(false);
const showEditDialog = ref(false);
const saveForm = ref({ name: '' });
const editForm = ref({ id: 0, name: '' });
const deleteTarget = ref<ReportDefDTO | null>(null);

const paramKeys = computed(() => {
  if (!selectedTemplate.value) return [];
  const t = templates.value.find((x) => x.code === selectedTemplate.value);
  return t?.params ?? [];
});

const canExecute = computed(() => {
  if (reportSource.value === 'template') return !!selectedTemplate.value;
  return !!selectedDefId.value;
});

function formatCell(val: unknown): string {
  if (val == null) return '-';
  if (typeof val === 'object') return JSON.stringify(val);
  return String(val);
}

async function loadTemplates() {
  try {
    const list = await getReportTemplates();
    templates.value = list as { code: string; name: string; params: string[] }[];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载模板失败';
  }
}

async function loadDefinitions() {
  try {
    const r = await getReportDefPage(1, 100);
    definitions.value = r.records;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载定义失败';
  }
}

async function run() {
  loading.value = true;
  error.value = '';
  result.value = null;
  try {
    const body: { reportType?: string; definitionId?: number; params?: Record<string, unknown> } = {};
    if (reportSource.value === 'template') {
      body.reportType = selectedTemplate.value;
      const p: Record<string, unknown> = {};
      if (params.value.date) p.date = params.value.date;
      if (params.value.page) p.page = params.value.page;
      if (params.value.size) p.size = params.value.size;
      body.params = Object.keys(p).length ? p : undefined;
    } else {
      body.definitionId = selectedDefId.value;
    }
    result.value = await executeReport(body);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '执行失败';
  } finally {
    loading.value = false;
  }
}

function openSaveDialog() {
  const t = templates.value.find((x) => x.code === selectedTemplate.value);
  saveForm.value = { name: t?.name ?? '' };
  showSaveDialog.value = true;
}

async function saveDefinition() {
  const name = saveForm.value.name?.trim();
  if (!name) {
    error.value = '请输入名称';
    return;
  }
  try {
    const p: Record<string, unknown> = {};
    if (params.value.date) p.date = params.value.date;
    if (params.value.page) p.page = params.value.page;
    if (params.value.size) p.size = params.value.size;
    await createReportDef({
      name,
      reportType: selectedTemplate.value,
      paramsJson: Object.keys(p).length ? JSON.stringify(p) : undefined
    });
    showSaveDialog.value = false;
    loadDefinitions();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败';
  }
}

function editDefinition(d: ReportDefDTO) {
  editForm.value = { id: d.id!, name: d.name };
  showEditDialog.value = true;
}

async function submitEdit() {
  if (!editForm.value.id) return;
  const name = editForm.value.name?.trim();
  if (!name) {
    error.value = '请输入名称';
    return;
  }
  try {
    const def = definitions.value.find((x) => x.id === editForm.value.id);
    if (!def) return;
    await updateReportDef(editForm.value.id, { ...def, name });
    showEditDialog.value = false;
    loadDefinitions();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '更新失败';
  }
}

function confirmDelete(d: ReportDefDTO) {
  deleteTarget.value = d;
}

async function doDelete() {
  const target = deleteTarget.value;
  if (!target?.id) return;
  try {
    await deleteReportDef(target.id);
    if (selectedDefId.value === target.id) selectedDefId.value = 0;
    deleteTarget.value = null;
    loadDefinitions();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}

onMounted(() => {
  const today = new Date().toISOString().slice(0, 10);
  params.value = { ...params.value, date: today };
  loadTemplates();
  loadDefinitions();
});
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-header { margin-bottom: 1.5rem; }
.page-header h1 { font-size: 1.5rem; color: #e5e7eb; margin: 0 0 0.5rem; }
.page-desc { font-size: 0.9rem; color: #94a3b8; margin: 0; line-height: 1.5; max-width: 640px; }
.toolbar { margin-bottom: 1.5rem; }
.toolbar-actions { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.section-label { color: #94a3b8; font-size: 0.875rem; }
.filter-input { padding: 0.4rem 0.75rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; min-width: 120px; }
.filter-input.num { min-width: 70px; }
.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.danger { background: #dc2626; color: #fff; border-color: #dc2626; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn-link { background: none; border: none; color: #38bdf8; cursor: pointer; font-size: 0.875rem; padding: 0 0.25rem; }
.btn-link.danger { color: #f87171; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.error-msg { color: #f87171; margin-bottom: 1rem; font-size: 0.9rem; }
.loading { color: #94a3b8; margin: 1rem 0; }
.definitions-section { margin-bottom: 1.5rem; }
.section-title { font-size: 1rem; color: #e5e7eb; margin: 0 0 0.75rem; display: inline-block; margin-right: 0.5rem; }
.def-list { display: flex; flex-direction: column; gap: 0.5rem; }
.def-item { display: flex; gap: 0.5rem; align-items: center; padding: 0.5rem; background: #1e293b; border-radius: 6px; }
.def-type { color: #94a3b8; font-size: 0.8rem; }
.empty-state { color: #94a3b8; padding: 1rem; }
.result-section { margin-top: 1rem; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; color: #e5e7eb; }
.data-table th, .data-table td { padding: 0.5rem 0.75rem; text-align: left; border-bottom: 1px solid #334155; }
.data-table th { color: #38bdf8; font-weight: 600; }
.modal { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; }
.modal-content { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 1.5rem; min-width: 320px; }
.modal-content h3 { margin: 0 0 1rem; color: #e5e7eb; }
.form-row { margin-bottom: 1rem; }
.form-row label { display: block; margin-bottom: 0.25rem; color: #94a3b8; font-size: 0.875rem; }
.form-row input { width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px; background: #0f172a; color: #e5e7eb; }
.modal-actions { display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; }
</style>
