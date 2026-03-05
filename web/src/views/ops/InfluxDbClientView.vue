<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="title-with-tip">
          <span class="tip-trigger" title="功能说明" @click.stop="showPageTip = !showPageTip">
            <Icon icon="mdi:information-outline" class="tip-icon" />
          </span>
          <div v-if="showPageTip" class="tip-popover" @click.stop>
            <div class="tip-content">InfluxDB 管理，可连接任意 InfluxDB 2.x 实例，执行 Flux 查询，连接信息可保存到数据库</div>
          </div>
        </div>
      </div>
    </div>
    <div class="influx-grid">
      <!-- 连接管理 -->
      <div class="influx-section conn-section">
        <div class="section-header">
          <h3 class="section-title">连接管理</h3>
          <button type="button" class="btn primary small" @click="openCreate">新增</button>
        </div>
        <div class="conn-list" v-if="connections.length">
          <div
            v-for="c in connections"
            :key="c.id"
            class="conn-item"
            :class="{ selected: selectedId === c.id }"
            @click="selectConn(c)"
          >
            <span class="conn-name">{{ c.name }}</span>
            <span class="conn-addr">{{ c.url }}</span>
            <div class="conn-actions">
              <button type="button" class="btn-icon" title="编辑" @click.stop="openEdit(c)">✎</button>
              <button type="button" class="btn-icon danger" title="删除" @click.stop="doDelete(c.id)">×</button>
            </div>
          </div>
        </div>
        <div v-else class="conn-empty">暂无保存连接，点击新增或使用下方快速连接</div>

        <div class="quick-conn">
          <div class="quick-header">
            <h4 class="sub-title">快速连接</h4>
            <button type="button" class="btn small" @click="fillDefaults">填充默认</button>
          </div>
          <div class="quick-form">
            <div class="form-row full">
              <label>URL *</label>
              <input v-model="quickForm.url" type="text" class="form-input" placeholder="http://localhost:8086" />
            </div>
            <div class="form-row">
              <label>Token</label>
              <input v-model="quickForm.token" type="password" class="form-input" placeholder="可选" />
            </div>
            <div class="form-row">
              <label>Org</label>
              <input v-model="quickForm.org" type="text" class="form-input" placeholder="可选" />
            </div>
          </div>
        </div>
      </div>

      <!-- 查询 + 结果 -->
      <div class="influx-section cmd-section">
        <div class="section-header">
          <div class="title-with-tip">
            <h3 class="section-title">Flux 查询</h3>
            <span class="tip-trigger" title="常用命令" @click.stop="showTip = !showTip">💡</span>
            <div v-if="showTip" class="tip-popover" @click.stop>
              <div class="tip-title">常用命令</div>
              <div v-for="q in commonQueries" :key="q.query" class="tip-item" @click="applyQuery(q.query); showTip = false">
                <span class="tip-hand" title="点击应用">👆</span>
                <div class="tip-content">
                  <code class="tip-cmd">{{ q.query }}</code>
                  <span class="tip-desc">{{ q.desc }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="section-actions">
            <button type="button" class="btn small" :disabled="loading" @click="doPing">测试连接</button>
            <button type="button" class="btn primary small" :disabled="loading || !query.trim()" @click="execute">执行</button>
          </div>
        </div>
        <textarea
          v-model="query"
          class="query-input"
          placeholder='from(bucket: "my-bucket") |> range(start: -1h)'
          rows="4"
          spellcheck="false"
        ></textarea>
      </div>

      <div class="influx-section result-section">
        <div class="result-header">
          <h3 class="section-title">结果</h3>
          <button type="button" class="btn small" :disabled="!rows.length" @click="clearResult">清除</button>
        </div>
        <div class="result-area" ref="resultRef">
          <div v-if="!rows.length && !error" class="result-empty">
            {{ hasExecuted ? '查询成功，0 条结果' : (selectedId || quickForm.url ? '选择连接并输入 Flux 查询后点击执行' : '选择连接或填写快速连接后输入 Flux 查询执行') }}
          </div>
          <div v-else-if="error" class="result-error">{{ error }}</div>
          <div v-else class="result-table-wrap">
            <table class="result-table">
              <thead>
                <tr>
                  <th v-for="col in columns" :key="col" class="result-th">{{ col }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, i) in rows" :key="i">
                  <td v-for="col in columns" :key="col" class="result-td">{{ formatCell(row[col]) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <h3>{{ editingId ? '编辑连接' : '新增连接' }}</h3>
        <form @submit.prevent="submitConn">
          <div class="form-group">
            <label>名称 *</label>
            <input v-model="modalForm.name" required placeholder="如：本地 InfluxDB" />
          </div>
          <div class="form-group">
            <label>URL *</label>
            <input v-model="modalForm.url" required placeholder="http://localhost:8086" />
          </div>
          <div class="form-group">
            <label>Token</label>
            <input v-model="modalForm.token" type="password" placeholder="可选，编辑时留空保留原 Token" />
          </div>
          <div class="form-group">
            <label>Org</label>
            <input v-model="modalForm.org" placeholder="可选" />
          </div>
          <p v-if="modalError" class="error-msg">{{ modalError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn" @click="closeModal">取消</button>
            <button type="submit" class="btn primary" :disabled="saving">确定</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { Icon } from '@iconify/vue';
import {
  listConnections,
  createConnection,
  updateConnection,
  deleteConnection,
  executeQuery,
  pingConnection,
  type InfluxDbConnection,
} from '@/api/influxdb';

const connections = ref<InfluxDbConnection[]>([]);
const selectedId = ref<number | null>(null);
const quickForm = ref({ url: 'http://localhost:8086', token: '', org: '' });
const query = ref('buckets()');
const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);
const error = ref('');
const hasExecuted = ref(false);
const resultRef = ref<HTMLElement | null>(null);

const showTip = ref(false);
const showPageTip = ref(false);
function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) {
    showTip.value = false;
    showPageTip.value = false;
  }
}
const commonQueries = [
  { query: 'buckets()', desc: '列出所有 Bucket' },
  { query: 'from(bucket: "iot-telemetry") |> range(start: -1h)', desc: 'iot-telemetry 最近 1 小时数据' },
  { query: 'from(bucket: "iot-telemetry") |> range(start: -24h) |> limit(n: 10)', desc: 'iot-telemetry 最近 24 小时 10 条' },
  { query: 'import "influxdata/influxdb/schema" schema.tagValues(bucket: "iot-telemetry", tag: "_measurement")', desc: '列出 measurement' },
];

function applyQuery(cmd: string) {
  query.value = cmd;
}

const columns = computed(() => {
  if (rows.value.length === 0) return [];
  const keys = new Set<string>();
  rows.value.forEach(r => Object.keys(r).forEach(k => keys.add(k)));
  return Array.from(keys);
});

const showModal = ref(false);
const editingId = ref<number | null>(null);
const modalForm = ref({ name: '', url: 'http://localhost:8086', token: '', org: '' });
const modalError = ref('');
const saving = ref(false);

async function load() {
  try {
    connections.value = await listConnections();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  }
}

function selectConn(c: InfluxDbConnection) {
  selectedId.value = c.id;
}

function fillDefaults() {
  quickForm.value = {
    url: 'http://localhost:8086',
    token: 'ly-factmesh-iot-token',
    org: 'ly-factmesh',
  };
}

function openCreate() {
  editingId.value = null;
  modalForm.value = { name: '', url: 'http://localhost:8086', token: '', org: '' };
  modalError.value = '';
  showModal.value = true;
}

function openEdit(c: InfluxDbConnection) {
  editingId.value = c.id;
  modalForm.value = {
    name: c.name,
    url: c.url,
    token: '',
    org: c.org ?? '',
  };
  modalError.value = '';
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

async function submitConn() {
  saving.value = true;
  modalError.value = '';
  try {
    const data = {
      name: modalForm.value.name,
      url: modalForm.value.url,
      org: modalForm.value.org || undefined,
    } as Record<string, unknown>;
    if (editingId.value) {
      if (modalForm.value.token) data.token = modalForm.value.token;
    } else {
      data.token = modalForm.value.token || '';
    }
    if (editingId.value) {
      await updateConnection(editingId.value, data);
    } else {
      const created = await createConnection(data);
      selectedId.value = created.id;
    }
    await load();
    closeModal();
  } catch (e) {
    modalError.value = e instanceof Error ? e.message : '保存失败';
  } finally {
    saving.value = false;
  }
}

async function doDelete(id: number) {
  if (!confirm('确定删除该连接？')) return;
  try {
    await deleteConnection(id);
    if (selectedId.value === id) selectedId.value = null;
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败';
  }
}

function getParams() {
  if (selectedId.value) {
    return { connectionId: selectedId.value, query: query.value };
  }
  return {
    url: quickForm.value.url,
    token: quickForm.value.token || undefined,
    org: quickForm.value.org || undefined,
    query: query.value,
  };
}

async function doPing() {
  loading.value = true;
  error.value = '';
  try {
    const params = selectedId.value
      ? { connectionId: selectedId.value }
      : { url: quickForm.value.url, token: quickForm.value.token || undefined };
    const res = await pingConnection(params);
    if (res.error) {
      error.value = res.error;
    } else {
      rows.value = [{ status: res.status, message: res.message }];
      error.value = '';
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '连接失败';
  } finally {
    loading.value = false;
  }
}

async function execute() {
  const q = query.value?.trim();
  if (!q) return;
  loading.value = true;
  error.value = '';
  rows.value = [];
  try {
    const res = await executeQuery({ ...getParams(), query: q });
    if (res.error) {
      error.value = res.error;
    } else {
      rows.value = res.rows ?? [];
      error.value = '';
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '执行失败';
  } finally {
    loading.value = false;
    setTimeout(() => resultRef.value?.scrollTo({ top: 0 }), 0);
  }
}

function clearResult() {
  rows.value = [];
  error.value = '';
  hasExecuted.value = false;
}

function formatCell(val: unknown): string {
  if (val == null) return 'NULL';
  if (typeof val === 'object') return JSON.stringify(val);
  return String(val);
}

onMounted(() => {
  load();
  document.addEventListener('click', closeTipOnClickOutside);
});
onBeforeUnmount(() => {
  document.removeEventListener('click', closeTipOnClickOutside);
});
</script>

<style scoped>
.page {
  padding: 0;
  min-width: 0;
  overflow-x: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.toolbar { margin-bottom: 0.5rem; flex-shrink: 0; }
.toolbar-actions { display: flex; align-items: center; }

.influx-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  grid-template-rows: auto 1fr;
  gap: 0.75rem;
  flex: 1;
  min-height: 0;
  min-width: 0;
}
.influx-section {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 0.6rem 0.75rem;
  min-width: 0;
}
.conn-section { grid-row: 1 / -1; display: flex; flex-direction: column; min-height: 0; overflow: hidden; }
.cmd-section { grid-column: 2; }
.result-section { grid-column: 2; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }

.section-header, .result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  gap: 0.5rem;
}
.section-actions { display: flex; gap: 0.4rem; }
.section-title, .sub-title { margin: 0; font-size: 0.9rem; color: #38bdf8; }
.quick-header { display: flex; align-items: center; justify-content: space-between; margin-top: 0.75rem; margin-bottom: 0.4rem; }
.sub-title { margin: 0; font-size: 0.8rem; }

.conn-list {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  flex: 0 1 auto;
  overflow-y: auto;
}
.conn-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.3rem;
  padding: 0.4rem 0.5rem;
  border-radius: 4px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid #334155;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.conn-item:hover { background: rgba(56, 189, 248, 0.08); border-color: #475569; }
.conn-item.selected { background: rgba(56, 189, 248, 0.15); border-color: #38bdf8; }
.conn-name { font-weight: 600; color: #e5e7eb; flex: 1; min-width: 0; }
.conn-addr { font-size: 0.75rem; color: #94a3b8; word-break: break-all; }
.conn-actions { display: flex; gap: 0.2rem; }
.btn-icon {
  padding: 0.15rem 0.35rem;
  font-size: 0.8rem;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  border-radius: 2px;
}
.btn-icon:hover { color: #38bdf8; background: rgba(56, 189, 248, 0.1); }
.btn-icon.danger:hover { color: #f87171; background: rgba(248, 113, 113, 0.1); }
.conn-empty { font-size: 0.8rem; color: #64748b; padding: 0.5rem 0; }

.quick-form { display: grid; grid-template-columns: 1fr 1fr; gap: 0.4rem; }
.form-row { display: flex; flex-direction: column; gap: 0.2rem; min-width: 0; }
.form-row.full { grid-column: 1 / -1; }
.form-row label { font-size: 0.75rem; color: #94a3b8; }
.form-input {
  padding: 0.3rem 0.5rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  font-size: 0.8rem;
}

.title-with-tip { position: relative; display: flex; align-items: center; gap: 0.2rem; }
.tip-trigger {
  font-size: 1rem;
  cursor: pointer;
  opacity: 0.8;
}
.tip-trigger:hover { opacity: 1; }
.tip-popover {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 0.3rem;
  min-width: 320px;
  max-width: 420px;
  padding: 0.6rem;
  background: #1e293b;
  border: 1px solid #475569;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 100;
}
.tip-title { font-size: 0.8rem; color: #94a3b8; margin-bottom: 0.5rem; }
.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 0.4rem;
  padding: 0.35rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.15s;
}
.tip-item:hover { background: rgba(56, 189, 248, 0.1); }
.tip-hand { font-size: 0.9rem; flex-shrink: 0; }
.tip-content { display: flex; flex-direction: column; gap: 0.15rem; min-width: 0; }
.tip-cmd { font-size: 0.75rem; color: #38bdf8; word-break: break-all; }
.tip-desc { font-size: 0.7rem; color: #94a3b8; }

.query-input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  font-family: ui-monospace, monospace;
  font-size: 0.85rem;
  resize: vertical;
  box-sizing: border-box;
}

.result-area {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: #0f172a;
  border: 1px solid #475569;
  border-radius: 4px;
}
.result-empty, .result-error { padding: 1rem; font-size: 0.85rem; }
.result-empty { color: #64748b; }
.result-error { color: #f87171; }
.result-table-wrap { overflow: auto; min-width: 0; }
.result-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.8rem;
  font-family: ui-monospace, monospace;
}
.result-th, .result-td {
  padding: 0.35rem 0.6rem;
  text-align: left;
  border-bottom: 1px solid #334155;
  color: #e5e7eb;
}
.result-th {
  color: #38bdf8;
  font-weight: 600;
  background: rgba(56, 189, 248, 0.08);
  position: sticky;
  top: 0;
}

.btn { padding: 0.35rem 0.6rem; font-size: 0.8rem; border-radius: 4px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.75rem; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn.danger:hover { border-color: #f87171; color: #f87171; }

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 1.25rem;
  min-width: 340px;
}
.modal h3 { margin: 0 0 1rem; font-size: 1rem; color: #38bdf8; }
.form-group { margin-bottom: 0.75rem; }
.form-group label { display: block; font-size: 0.8rem; color: #94a3b8; margin-bottom: 0.25rem; }
.form-group input {
  width: 100%;
  padding: 0.4rem 0.5rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  box-sizing: border-box;
}
.error-msg { color: #f87171; font-size: 0.8rem; margin: 0.5rem 0; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>
