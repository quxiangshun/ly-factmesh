/**
 * 工序 API（MES）
 */
import { request, requestJson } from './client';

const BASE = '/api/processes';

export interface ProcessDTO {
  id: number;
  processCode: string;
  processName: string;
  sequence: number;
  workCenter?: string;
  createTime?: string;
}

export interface ProcessCreateRequest {
  processCode: string;
  processName: string;
  sequence?: number;
  workCenter?: string;
}

export interface ProcessUpdateRequest {
  processName: string;
  sequence?: number;
  workCenter?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getProcessPage(page = 1, size = 10): Promise<PageResult<ProcessDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getProcessList(): Promise<ProcessDTO[]> {
  return requestJson(`${BASE}/list`);
}

export async function getProcessById(id: number): Promise<ProcessDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createProcess(body: ProcessCreateRequest): Promise<ProcessDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateProcess(id: number, body: ProcessUpdateRequest): Promise<ProcessDTO> {
  return requestJson(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
}

export async function deleteProcess(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
