/**
 * 角色 API（Admin）
 */
import { request, requestJson } from './client';

const BASE = '/api/roles';

export interface RoleDTO {
  id: number;
  roleName: string;
  roleCode: string;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

export interface RoleCreateRequest {
  roleName: string;
  roleCode: string;
  description?: string;
}

export interface RoleUpdateRequest {
  roleName?: string;
  roleCode?: string;
  description?: string;
}

export interface PermissionDTO {
  id: number;
  permName: string;
  permCode: string;
  url?: string;
  method?: string;
  parentId?: number;
  createTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getRoles(page = 1, size = 10): Promise<PageResult<RoleDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getRoleById(id: number): Promise<RoleDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createRole(body: RoleCreateRequest): Promise<RoleDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateRole(id: number, body: RoleUpdateRequest): Promise<RoleDTO> {
  const res = await request(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteRole(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}

export async function getRolePermissions(id: number): Promise<PermissionDTO[]> {
  return requestJson(`${BASE}/${id}/permissions`);
}

export async function assignRolePermissions(id: number, permIds: number[]): Promise<void> {
  await request(`${BASE}/${id}/permissions`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(permIds)
  });
}
