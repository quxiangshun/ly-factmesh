<template>
  <section class="page">
    <p class="page-desc">PostgreSQL 数据库管理，使用页面输入连接，仅支持 SELECT 查询</p>

    <div class="pg-grid">
      <!-- 连接 + 常用命令 -->
      <div class="conn-row-section">
        <div class="pg-section conn-section">
          <div class="section-header">
            <h3 class="section-title">连接</h3>
            <button type="button" class="btn primary small" :disabled="loading" @click="fillDefaults">填充默认</button>
          </div>
          <div class="conn-form">
            <div class="conn-row">
              <label>主机</label>
              <input v-model="connForm.host" type="text" class="form-input" placeholder="localhost" />
            </div>
            <div class="conn-row">
              <label>端口</label>
              <input v-model="connForm.port" type="text" class="form-input" placeholder="5432" />
            </div>
            <div class="conn-row">
              <label>数据库</label>
              <input v-model="connForm.database" type="text" class="form-input" placeholder="可选，默认 postgres" />
            </div>
            <div class="conn-row">
              <label>用户名</label>
              <input v-model="connForm.username" type="text" class="form-input" placeholder="postgres" />
            </div>
            <div class="conn-row">
              <label>密码</label>
              <input v-model="connForm.password" type="password" class="form-input" placeholder="postgres" />
            </div>
          </div>
        </div>
        <div class="pg-section commands-section">
          <h3 class="section-title">常用命令</h3>
          <div class="command-list">
            <div
              v-for="cmd in commonCommands"
              :key="cmd.sql"
              class="command-item"
              title="点击使用"
              @click="useCommand(cmd.sql)"
            >
              <span class="command-icon">👆</span>
              <div class="command-content">
                <code class="command-sql">{{ cmd.sql }}</code>
                <span class="command-desc">{{ cmd.desc }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 终端 + 结果 左右 -->
      <div class="terminal-result-row">
      <div class="pg-section terminal-section">
        <div class="section-header">
          <h3 class="section-title">SQL 终端</h3>
          <button type="button" class="btn primary small" :disabled="loading || !sql.trim()" @click="execute">
            执行
          </button>
        </div>
        <textarea
          v-model="sql"
          class="sql-input"
          placeholder="SELECT * FROM sys_user LIMIT 10"
          spellcheck="false"
        ></textarea>
      </div>
      <div class="pg-section result-section">
        <div class="result-header">
          <h3 class="section-title">结果</h3>
          <button type="button" class="btn small" :disabled="!hasResult" @click="clearResult">清除</button>
        </div>
        <div class="result-area result-console" ref="resultRef">
          <div v-if="!hasResult" class="result-empty">
            {{ error ? `错误: ${error}` : '执行 SELECT 查询后在此查看结果' }}
          </div>
          <div v-else class="result-table-wrap">
            <table class="result-table">
              <thead>
                <tr>
                  <th v-for="col in resultColumns" :key="col" class="result-th">{{ col }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, i) in resultRows" :key="i">
                  <td v-for="col in resultColumns" :key="col" class="result-td">
                    {{ formatCell(row[col]) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { executeSql } from '@/api/sql';

const connForm = ref({
  host: 'localhost',
  port: '5432',
  database: '',
  username: 'postgres',
  password: 'postgres',
});

const commonCommands = [
  { sql: 'SELECT version();', desc: '查看 PostgreSQL 版本' },
  { sql: 'SELECT current_database();', desc: '查看当前数据库' },
  { sql: 'SELECT datname FROM pg_database WHERE datistemplate = false;', desc: '列出所有数据库' },
  { sql: "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;", desc: '列出 public  schema 下的表' },
  { sql: "SELECT current_database() AS 当前库, schemaname AS schema, tablename AS 表名 FROM pg_tables WHERE schemaname NOT IN ('pg_catalog', 'information_schema') ORDER BY schemaname, tablename;", desc: '查询当前连接库的所有表' },
];

const loading = ref(false);
const sql = ref('SELECT 1 as test');
const resultColumns = ref<string[]>([]);
const resultRows = ref<Record<string, unknown>[]>([]);
const error = ref('');
const resultRef = ref<HTMLElement | null>(null);
const hasResult = ref(false);

function fillDefaults() {
  connForm.value = {
    host: 'localhost',
    port: '5432',
    database: '',
    username: 'postgres',
    password: 'postgres',
  };
}

function useCommand(cmdSql: string) {
  sql.value = cmdSql;
}

async function execute() {
  const s = sql.value?.trim();
  if (!s) return;
  loading.value = true;
  error.value = '';
  hasResult.value = false;
  try {
    const res = await executeSql(s, {
      host: connForm.value.host,
      port: connForm.value.port,
      database: connForm.value.database || undefined,
      username: connForm.value.username,
      password: connForm.value.password,
    });
    if ('error' in res) {
      error.value = res.error;
    } else {
      resultColumns.value = res.columns;
      resultRows.value = res.rows;
      hasResult.value = true;
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
  resultColumns.value = [];
  resultRows.value = [];
  error.value = '';
  hasResult.value = false;
}

function formatCell(val: unknown): string {
  if (val == null) return 'NULL';
  if (typeof val === 'object') return JSON.stringify(val);
  return String(val);
}

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

.pg-grid {
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: auto 1fr;
  gap: 0.75rem;
  flex: 1;
  min-height: 0;
  min-width: 0;
}
.conn-row-section {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 0.75rem;
}
.terminal-result-row {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 0.75rem;
  min-height: 0;
}
.commands-section {
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.command-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #475569 transparent;
}
.command-list::-webkit-scrollbar { width: 4px; }
.command-list::-webkit-scrollbar-track { background: transparent; }
.command-list::-webkit-scrollbar-thumb { background: #475569; border-radius: 2px; }
.command-list::-webkit-scrollbar-thumb:hover { background: #64748b; }
.command-item {
  display: flex;
  align-items: flex-start;
  gap: 0.4rem;
  padding: 0.35rem 0.5rem;
  border-radius: 4px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid #334155;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.command-item:hover {
  background: rgba(56, 189, 248, 0.1);
  border-color: #38bdf8;
}
.command-icon {
  font-size: 1rem;
  flex-shrink: 0;
}
.command-content {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
  flex: 1;
}
.command-sql {
  font-size: 0.75rem;
  color: #38bdf8;
  word-break: break-all;
  flex-shrink: 1;
}
.command-desc {
  font-size: 0.7rem;
  color: #94a3b8;
  flex-shrink: 0;
}
.pg-section {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 0.6rem 0.75rem;
  min-width: 0;
}
.terminal-section, .result-section {
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.section-header, .result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  gap: 0.5rem;
}
.section-header .section-title, .result-header .section-title { margin: 0; }
.section-title { margin: 0; font-size: 0.9rem; color: #38bdf8; }

.conn-form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.4rem 0.75rem; }
.conn-row { display: flex; flex-direction: column; gap: 0.2rem; min-width: 0; }
.conn-row label { font-size: 0.75rem; color: #94a3b8; }
.conn-row .form-input {
  padding: 0.3rem 0.5rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  font-size: 0.8rem;
  width: 100%;
  box-sizing: border-box;
}

.sql-input {
  width: 100%;
  min-height: 100px;
  padding: 0.5rem 0.75rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  font-family: ui-monospace, monospace;
  font-size: 0.85rem;
  resize: vertical;
  box-sizing: border-box;
  scrollbar-width: thin;
  scrollbar-color: #475569 transparent;
}
.sql-input::-webkit-scrollbar { width: 4px; height: 4px; }
.sql-input::-webkit-scrollbar-track { background: transparent; }
.sql-input::-webkit-scrollbar-thumb { background: #475569; border-radius: 2px; }
.sql-input::-webkit-scrollbar-thumb:hover { background: #64748b; }
.sql-input::placeholder { color: #64748b; }

.result-area {
  flex: 1;
  min-height: 0;
  overflow: auto;
  border-radius: 4px;
  scrollbar-width: thin;
  scrollbar-color: #475569 transparent;
}
.result-area::-webkit-scrollbar { width: 4px; height: 4px; }
.result-area::-webkit-scrollbar-track { background: transparent; }
.result-area::-webkit-scrollbar-thumb { background: #475569; border-radius: 2px; }
.result-area::-webkit-scrollbar-thumb:hover { background: #64748b; }
.result-console {
  background: #0f172a;
  border: 1px solid #475569;
}
.result-empty { color: #94a3b8; padding: 1rem; font-size: 0.85rem; }
.result-table-wrap {
  overflow: auto;
  min-width: 0;
  scrollbar-width: thin;
  scrollbar-color: #475569 transparent;
}
.result-table-wrap::-webkit-scrollbar { width: 4px; height: 4px; }
.result-table-wrap::-webkit-scrollbar-track { background: transparent; }
.result-table-wrap::-webkit-scrollbar-thumb { background: #475569; border-radius: 2px; }
.result-table-wrap::-webkit-scrollbar-thumb:hover { background: #64748b; }
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

.btn {
  padding: 0.35rem 0.6rem;
  font-size: 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #475569;
  background: #1e293b;
  color: #e5e7eb;
}
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.75rem; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
