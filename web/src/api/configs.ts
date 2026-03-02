/**
 * 系统配置 API（Admin）
 */
import { request, requestJson } from './client';

const BASE = '/api/configs';

export interface ConfigDTO {
  id: number;
  configKey: string;
  configValue: string;
  configDesc?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface ConfigCreateRequest {
  configKey: string;
  configValue: string;
  configDesc?: string;
  status?: number;
}

export interface ConfigUpdateRequest {
  configValue?: string;
  configDesc?: string;
  status?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getConfigs(page = 1, size = 10, configKey?: string): Promise<PageResult<ConfigDTO>> {
  const q = configKey ? `&configKey=${encodeURIComponent(configKey)}` : '';
  return requestJson(`${BASE}?page=${page}&size=${size}${q}`);
}

export async function getConfigByKey(configKey: string): Promise<ConfigDTO> {
  return requestJson(`${BASE}/key/${encodeURIComponent(configKey)}`);
}

export async function getConfigById(id: number): Promise<ConfigDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createConfig(body: ConfigCreateRequest): Promise<ConfigDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateConfig(id: number, body: ConfigUpdateRequest): Promise<ConfigDTO> {
  const res = await request(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteConfig(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
