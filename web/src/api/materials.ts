/**
 * 物料 API（WMS）
 */
import { request, requestJson } from './client';

const BASE = '/api/materials';

export interface MaterialDTO {
  id: number;
  materialCode: string;
  materialName: string;
  materialType?: string;
  specification?: string;
  unit?: string;
  createTime?: string;
  updateTime?: string;
}

export interface MaterialCreateRequest {
  materialCode: string;
  materialName: string;
  materialType?: string;
  specification?: string;
  unit?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getMaterialPage(page = 1, size = 10): Promise<PageResult<MaterialDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getMaterialById(id: number): Promise<MaterialDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createMaterial(body: MaterialCreateRequest): Promise<MaterialDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteMaterial(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
