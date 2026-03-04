/**
 * SQL 执行 API（运维 PG 管理）
 */
import { fetchWithAuth, clearAuth } from './auth';
import router from '@/router';

const BASE = '/api/sql';

export interface SqlExecuteResult {
  columns: string[];
  rows: Record<string, unknown>[];
}

export interface SqlErrorResult {
  error: string;
}

export interface SqlExecuteRequest {
  sql: string;
  host?: string;
  port?: string;
  username?: string;
  password?: string;
  database?: string;
}

export async function executeSql(sql: string, conn?: Partial<SqlExecuteRequest>): Promise<SqlExecuteResult | SqlErrorResult> {
  const body: Record<string, string> = { sql };
  if (conn?.host) body.host = conn.host;
  if (conn?.port) body.port = conn.port;
  if (conn?.username) body.username = conn.username;
  if (conn?.password) body.password = conn.password;
  if (conn?.database) body.database = conn.database;
  const res = await fetchWithAuth(`${BASE}/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = (await res.json()) as SqlExecuteResult | SqlErrorResult;
  if (res.status === 401) {
    clearAuth();
    const redirect = encodeURIComponent(window.location.pathname + window.location.search);
    await router.replace({ name: 'Login', query: { redirect } });
    throw new Error('登录已过期');
  }
  if (!res.ok) return data as SqlErrorResult;
  return data;
}
