/**
 * 权限 API（Admin）
 */
import { request, requestJson } from './client';

const BASE = '/api/permissions';

export interface PermissionDTO {
  id: number;
  permName: string;
  permCode: string;
  url?: string;
  method?: string;
  parentId?: number;
  createTime?: string;
}

export interface PermissionTreeDTO extends PermissionDTO {
  children?: PermissionTreeDTO[];
}

export interface PermissionCreateRequest {
  permName: string;
  permCode: string;
  url?: string;
  method?: string;
  parentId?: number;
}

export interface PermissionUpdateRequest {
  permName?: string;
  permCode?: string;
  url?: string;
  method?: string;
  parentId?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getPermissions(page = 1, size = 10): Promise<PageResult<PermissionDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getPermissionTree(): Promise<PermissionTreeDTO[]> {
  return requestJson(`${BASE}/tree`);
}

export async function getPermissionById(id: number): Promise<PermissionDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createPermission(body: PermissionCreateRequest): Promise<PermissionDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updatePermission(id: number, body: PermissionUpdateRequest): Promise<PermissionDTO> {
  const res = await request(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deletePermission(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
