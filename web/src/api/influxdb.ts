/**
 * InfluxDB 管理 API（运维）
 */
import { fetchWithAuth, clearAuth } from './auth';
import router from '@/router';

const BASE = '/api/influxdb';

export interface InfluxDbConnection {
  id: number;
  name: string;
  url: string;
  token?: string;
  org?: string;
  createTime?: string;
  updateTime?: string;
}

export async function listConnections(): Promise<InfluxDbConnection[]> {
  return requestJson<InfluxDbConnection[]>(`${BASE}/connections`);
}

export async function createConnection(data: Partial<InfluxDbConnection>): Promise<InfluxDbConnection> {
  return requestJson<InfluxDbConnection>(`${BASE}/connections`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
}

export async function updateConnection(id: number, data: Partial<InfluxDbConnection>): Promise<InfluxDbConnection> {
  return requestJson<InfluxDbConnection>(`${BASE}/connections/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
}

export async function deleteConnection(id: number): Promise<void> {
  await request(`${BASE}/connections/${id}`, { method: 'DELETE' });
}

export interface InfluxDbQueryResult {
  rows?: Record<string, unknown>[];
  error?: string;
}

export async function executeQuery(params: {
  connectionId?: number;
  url?: string;
  token?: string;
  org?: string;
  query: string;
}): Promise<InfluxDbQueryResult> {
  const res = await fetchWithAuth(`${BASE}/query`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  const data = (await res.json()) as InfluxDbQueryResult;
  if (res.status === 401) {
    clearAuth();
    const redirect = encodeURIComponent(window.location.pathname + window.location.search);
    await router.replace({ name: 'Login', query: { redirect } });
    throw new Error('登录已过期');
  }
  return data;
}

export interface InfluxDbPingResult {
  status?: string;
  message?: string;
  error?: string;
}

export async function pingConnection(params: {
  connectionId?: number;
  url?: string;
  token?: string;
}): Promise<InfluxDbPingResult> {
  const res = await fetchWithAuth(`${BASE}/ping`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  const data = (await res.json()) as InfluxDbPingResult;
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
