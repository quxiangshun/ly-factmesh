/**
 * Redis 管理 API（运维）
 */
import { fetchWithAuth, clearAuth } from './auth';
import router from '@/router';

const BASE = '/api/redis';

export interface RedisConnection {
  id: number;
  name: string;
  host: string;
  port: number;
  password?: string;
  database: number;
  createTime?: string;
  updateTime?: string;
}

export async function listConnections(): Promise<RedisConnection[]> {
  return requestJson<RedisConnection[]>(`${BASE}/connections`);
}

export async function createConnection(data: Partial<RedisConnection>): Promise<RedisConnection> {
  return requestJson<RedisConnection>(`${BASE}/connections`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
}

export async function updateConnection(id: number, data: Partial<RedisConnection>): Promise<RedisConnection> {
  return requestJson<RedisConnection>(`${BASE}/connections/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
}

export async function deleteConnection(id: number): Promise<void> {
  await request(`${BASE}/connections/${id}`, { method: 'DELETE' });
}

export interface RedisExecuteResult {
  result?: unknown;
  error?: string;
}

export async function executeCommand(params: {
  connectionId?: number;
  host?: string;
  port?: string;
  password?: string;
  database?: string;
  cmd: string;
}): Promise<RedisExecuteResult> {
  const res = await fetchWithAuth(`${BASE}/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  const data = (await res.json()) as RedisExecuteResult;
  if (res.status === 401) {
    clearAuth();
    const redirect = encodeURIComponent(window.location.pathname + window.location.search);
    await router.replace({ name: 'Login', query: { redirect } });
    throw new Error('登录已过期');
  }
  return data;
}

async function request(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  const res = await fetchWithAuth(input, init);
  if (res.status === 401) {
    clearAuth();
    const redirect = encodeURIComponent(window.location.pathname + window.location.search);
    await router.replace({ name: 'Login', query: { redirect } });
    throw new Error('登录已过期');
  }
  return res;
}

async function requestJson<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const res = await request(input, init);
  if (!res.ok) {
    const ct = res.headers.get('content-type');
    if (ct?.includes('application/json')) {
      const err = (await res.json()) as { error?: string; message?: string };
      throw new Error(err.error || err.message || `请求失败 ${res.status}`);
    }
    throw new Error(await res.text() || `请求失败 ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  const ct = res.headers.get('content-type');
  if (ct?.includes('application/json')) return res.json() as Promise<T>;
  return undefined as T;
}
