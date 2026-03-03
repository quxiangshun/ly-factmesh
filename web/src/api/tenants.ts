/**
 * 租户 API（Admin）
 */
import { request, requestJson } from './client';

const BASE = '/api/tenants';

export interface TenantDTO {
  id: number;
  tenantCode: string;
  tenantName: string;
  contact?: string;
  phone?: string;
  status?: number;
  config?: string;
  createTime?: string;
  updateTime?: string;
}

export interface TenantCreateRequest {
  tenantCode: string;
  tenantName: string;
  contact?: string;
  phone?: string;
  status?: number;
  config?: string;
}

export interface TenantUpdateRequest {
  tenantName?: string;
  contact?: string;
  phone?: string;
  status?: number;
  config?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getTenants(page = 1, size = 10): Promise<PageResult<TenantDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getTenantById(id: number): Promise<TenantDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createTenant(body: TenantCreateRequest): Promise<TenantDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateTenant(id: number, body: TenantUpdateRequest): Promise<TenantDTO> {
  return requestJson(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
}

export async function deleteTenant(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
