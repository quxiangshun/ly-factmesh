/**
 * 不合格品（NCR）API（QMS）
 */
import { request, requestJson } from './client';

const BASE = '/api/ncr';

export interface NonConformingProductDTO {
  id: number;
  ncrNo?: string;
  productCode: string;
  batchNo?: string;
  quantity: number;
  reason: string;
  disposalMethod?: number;
  disposalResult: number;
  taskId?: number;
  createTime?: string;
  disposeTime?: string;
  remark?: string;
}

export interface NonConformingProductCreateRequest {
  productCode: string;
  batchNo?: string;
  quantity: number;
  reason: string;
  disposalMethod?: number;
  taskId?: number;
  remark?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getNcrPage(page = 1, size = 10, disposalResult?: number, taskId?: number): Promise<PageResult<NonConformingProductDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (disposalResult !== undefined && disposalResult !== null) params.set('disposalResult', String(disposalResult));
  if (taskId !== undefined && taskId !== null) params.set('taskId', String(taskId));
  return requestJson(`${BASE}?${params}`);
}

export async function getNcrByTaskId(taskId: number): Promise<NonConformingProductDTO[]> {
  return requestJson(`${BASE}/by-task/${taskId}`);
}

export async function getNcrById(id: number): Promise<NonConformingProductDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createNcr(body: NonConformingProductCreateRequest): Promise<NonConformingProductDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export interface NcrDisposeRequest {
  disposalMethod: number;
  remark?: string;
}

export async function disposeNcr(id: number, body?: NcrDisposeRequest): Promise<NonConformingProductDTO> {
  const res = await request(`${BASE}/${id}/dispose`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : '{}'
  });
  return res.json();
}

export async function deleteNcr(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
