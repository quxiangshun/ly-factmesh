/**
 * 统一 API 客户端：带鉴权、401 时清空登录态并跳转登录页
 */
import { fetchWithAuth, clearAuth } from './auth';
import router from '@/router';

export async function request(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  const res = await fetchWithAuth(input, init);
  if (res.status === 401) {
    clearAuth();
    const redirect = typeof window !== 'undefined' ? encodeURIComponent(window.location.pathname + window.location.search) : '';
    await router.replace({ name: 'Login', query: redirect ? { redirect } : {} });
    throw new Error('登录已过期，请重新登录');
  }
  return res;
}

export async function requestJson<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const res = await request(input, init);
  if (!res.ok) {
    const ct = res.headers.get('content-type');
    if (ct?.includes('application/json')) {
      const err = (await res.json()) as { message?: string };
      throw new Error(err.message || `请求失败 ${res.status}`);
    }
    const text = await res.text();
    throw new Error(text || `请求失败 ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  const ct = res.headers.get('content-type');
  if (ct?.includes('application/json')) return res.json() as Promise<T>;
  return undefined as T;
}
