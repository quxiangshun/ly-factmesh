/**
 * 认证：Token 存储、请求头注入、登录/登出
 */
const TOKEN_KEY = 'ly-factmesh-token';

export function getToken(): string | null {
  return typeof localStorage !== 'undefined' ? localStorage.getItem(TOKEN_KEY) : null;
}

export function setToken(token: string): void {
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(TOKEN_KEY, token);
  }
}

export function clearAuth(): void {
  if (typeof localStorage !== 'undefined') {
    localStorage.removeItem(TOKEN_KEY);
  }
}

/**
 * 带 Authorization 头的 fetch
 */
export async function fetchWithAuth(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  const token = getToken();
  const headers = new Headers(init?.headers);
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  return fetch(input, { ...init, headers });
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  nickname?: string;
}

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const json = (await res.json()) as ApiResult<LoginResponse>;
  if (json.code !== 0) {
    throw new Error(json.message || '登录失败');
  }
  if (!json.data?.token) {
    throw new Error('登录响应异常');
  }
  setToken(json.data.token);
  return json.data;
}

export function logout(): void {
  clearAuth();
}
