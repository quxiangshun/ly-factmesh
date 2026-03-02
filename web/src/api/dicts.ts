/**
 * 字典 API（Admin）
 */
import { request, requestJson } from './client';

const BASE = '/api/dicts';

export interface DictDTO {
  id: number;
  dictType: string;
  dictCode: string;
  dictName: string;
  dictValue: string;
  sortOrder?: number;
  status?: number;
  createTime?: string;
}

export interface DictCreateRequest {
  dictType: string;
  dictCode: string;
  dictName: string;
  dictValue: string;
  sortOrder?: number;
  status?: number;
}

export interface DictUpdateRequest {
  dictType?: string;
  dictCode?: string;
  dictName?: string;
  dictValue?: string;
  sortOrder?: number;
  status?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getDicts(page = 1, size = 10, dictType?: string): Promise<PageResult<DictDTO>> {
  const q = dictType ? `&dictType=${encodeURIComponent(dictType)}` : '';
  return requestJson(`${BASE}?page=${page}&size=${size}${q}`);
}

export async function getDictsByType(dictType: string): Promise<DictDTO[]> {
  return requestJson(`${BASE}/type/${encodeURIComponent(dictType)}`);
}

export async function getDictById(id: number): Promise<DictDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createDict(body: DictCreateRequest): Promise<DictDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateDict(id: number, body: DictUpdateRequest): Promise<DictDTO> {
  const res = await request(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteDict(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
