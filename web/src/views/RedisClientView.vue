<template>
  <section class="page">
    <p class="page-desc">Redis 客户端，可连接任意 Redis 实例，连接信息可保存到数据库管理</p>

    <div class="redis-grid">
      <!-- 连接管理 -->
      <div class="redis-section conn-section">
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
            <span class="conn-addr">{{ c.host }}:{{ c.port }}</span>
            <div class="conn-actions">
              <button type="button" class="btn-icon" title="编辑" @click.stop="openEdit(c)">✎</button>
              <button type="button" class="btn-icon danger" title="删除" @click.stop="doDelete(c.id)">×</button>
            </div>
          </div>
        </div>
        <div v-else class="conn-empty">暂无保存连接，点击新增或使用下方快速连接</div>

        <div class="quick-conn">
          <h4 class="sub-title">快速连接</h4>
          <div class="quick-form">
            <div class="form-row">
              <label>主机</label>
              <input v-model="quickForm.host" type="text" class="form-input" placeholder="localhost" />
            </div>
            <div class="form-row">
              <label>端口</label>
              <input v-model="quickForm.port" type="text" class="form-input" placeholder="6379" />
            </div>
            <div class="form-row">
              <label>密码</label>
              <input v-model="quickForm.password" type="password" class="form-input" placeholder="可选" />
            </div>
            <div class="form-row">
              <label>DB</label>
              <input v-model.number="quickForm.database" type="number" class="form-input" placeholder="0" min="0" />
            </div>
          </div>
        </div>
      </div>

      <!-- 命令 + 结果 -->
      <div class="redis-section cmd-section">
        <div class="section-header">
          <h3 class="section-title">命令</h3>
          <button type="button" class="btn primary small" :disabled="loading || !cmd.trim()" @click="execute">执行</button>
        </div>
        <input
          v-model="cmd"
          type="text"
          class="cmd-input"
          placeholder="PING / GET key / KEYS * / INFO"
          @keydown.enter="execute"
        />
        <div class="common-cmds">
          <span
            v-for="c in commonCommands"
            :key="c.cmd"
            class="cmd-chip"
            @click="cmd = c.cmd"
          >{{ c.cmd }}</span>
        </div>
      </div>

      <div class="redis-section result-section">
        <div class="result-header">
          <h3 class="section-title">结果</h3>
          <button type="button" class="btn small" :disabled="!result" @click="clearResult">清除</button>
        </div>
        <div class="result-area" ref="resultRef">
          <div v-if="!result && !error" class="result-empty">选择连接后输入命令执行</div>
          <div v-else-if="error" class="result-error">{{ error }}</div>
          <pre v-else class="result-pre">{{ formatResult(result) }}</pre>
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
            <input v-model="modalForm.name" required placeholder="如：本地Redis" />
          </div>
          <div class="form-group">
            <label>主机 *</label>
            <input v-model="modalForm.host" required placeholder="localhost" />
          </div>
          <div class="form-group">
            <label>端口</label>
            <input v-model.number="modalForm.port" type="number" placeholder="6379" />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="modalForm.password" type="password" placeholder="可选，编辑时留空保留原密码" />
          </div>
          <div class="form-group">
            <label>DB</label>
            <input v-model.number="modalForm.database" type="number" placeholder="0" min="0" />
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
import { ref, onMounted } from 'vue';
import {
  listConnections,
  createConnection,
  updateConnection,
  deleteConnection,
  executeCommand,
  type RedisConnection,
} from '@/api/redis';

const connections = ref<RedisConnection[]>([]);
const selectedId = ref<number | null>(null);
const quickForm = ref({ host: 'localhost', port: '6379', password: '', database: 0 });
const cmd = ref('PING');
const loading = ref(false);
const result = ref<unknown>(null);
const error = ref('');
const resultRef = ref<HTMLElement | null>(null);

const commonCommands = [
  { cmd: 'PING' },
  { cmd: 'INFO' },
  { cmd: 'KEYS *' },
  { cmd: 'DBSIZE' },
  { cmd: 'GET key' },
];

const showModal = ref(false);
const editingId = ref<number | null>(null);
const modalForm = ref({ name: '', host: 'localhost', port: 6379, password: '', database: 0 });
const modalError = ref('');
const saving = ref(false);

async function load() {
  try {
    connections.value = await listConnections();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  }
}

function selectConn(c: RedisConnection) {
  selectedId.value = c.id;
}

function openCreate() {
  editingId.value = null;
  modalForm.value = { name: '', host: 'localhost', port: 6379, password: '', database: 0 };
  modalError.value = '';
  showModal.value = true;
}

function openEdit(c: RedisConnection) {
  editingId.value = c.id;
  modalForm.value = {
    name: c.name,
    host: c.host,
    port: c.port,
    password: '',
    database: c.database ?? 0,
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
      host: modalForm.value.host,
      port: modalForm.value.port,
      database: modalForm.value.database,
    } as Record<string, unknown>;
    if (editingId.value) {
      if (modalForm.value.password) data.password = modalForm.value.password;
    } else {
      data.password = modalForm.value.password || '';
    }
    if (editingId.value) {
      await updateConnection(editingId.value, data);
    } else {
      await createConnection(data);
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

async function execute() {
  const c = cmd.value?.trim();
  if (!c) return;
  loading.value = true;
  error.value = '';
  result.value = null;
  try {
    const params: { connectionId?: number; host?: string; port?: string; password?: string; database?: string; cmd: string } = { cmd: c };
    if (selectedId.value) {
      params.connectionId = selectedId.value;
    } else {
      params.host = quickForm.value.host;
      params.port = String(quickForm.value.port || 6379);
      if (quickForm.value.password) params.password = quickForm.value.password;
      params.database = String(quickForm.value.database ?? 0);
    }
    const res = await executeCommand(params);
    if (res.error) {
      error.value = res.error;
    } else {
      result.value = res.result;
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '执行失败';
  } finally {
    loading.value = false;
    setTimeout(() => resultRef.value?.scrollTo({ top: 0 }), 0);
  }
}

function clearResult() {
  result.value = null;
  error.value = '';
}

function formatResult(val: unknown): string {
  if (val == null) return 'null';
  if (typeof val === 'string') return val;
  if (Array.isArray(val)) return val.map((v, i) => `${i + 1}) ${formatResult(v)}`).join('\n');
  return JSON.stringify(val, null, 2);
}

onMounted(load);
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
.page-desc { margin: 0 0 0.5rem; font-size: 0.85rem; color: #94a3b8; flex-shrink: 0; }

.redis-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  grid-template-rows: auto auto 1fr;
  gap: 0.75rem;
  flex: 1;
  min-height: 0;
  min-width: 0;
}
.redis-section {
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
.section-title, .sub-title { margin: 0; font-size: 0.9rem; color: #38bdf8; }
.sub-title { margin-top: 0.75rem; margin-bottom: 0.4rem; font-size: 0.8rem; }

.conn-list {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  flex: 0 1 auto;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #475569 transparent;
}
.conn-list::-webkit-scrollbar { width: 4px; }
.conn-list::-webkit-scrollbar-thumb { background: #475569; border-radius: 2px; }
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
.conn-addr { font-size: 0.75rem; color: #94a3b8; }
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
.form-row label { font-size: 0.75rem; color: #94a3b8; }
.form-input {
  padding: 0.3rem 0.5rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  font-size: 0.8rem;
}

.cmd-input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  font-family: ui-monospace, monospace;
  font-size: 0.9rem;
  box-sizing: border-box;
}
.common-cmds { display: flex; flex-wrap: wrap; gap: 0.3rem; margin-top: 0.5rem; }
.cmd-chip {
  padding: 0.2rem 0.5rem;
  font-size: 0.75rem;
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
  border-radius: 4px;
  cursor: pointer;
}
.cmd-chip:hover { background: rgba(56, 189, 248, 0.25); }

.result-area {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: #0f172a;
  border: 1px solid #475569;
  border-radius: 4px;
  scrollbar-width: thin;
  scrollbar-color: #475569 transparent;
}
.result-area::-webkit-scrollbar { width: 4px; height: 4px; }
.result-area::-webkit-scrollbar-thumb { background: #475569; border-radius: 2px; }
.result-empty, .result-error { padding: 1rem; font-size: 0.85rem; }
.result-empty { color: #64748b; }
.result-error { color: #f87171; }
.result-pre {
  margin: 0;
  padding: 1rem;
  font-size: 0.8rem;
  font-family: ui-monospace, monospace;
  color: #e5e7eb;
  white-space: pre-wrap;
  word-break: break-all;
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
  min-width: 320px;
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
