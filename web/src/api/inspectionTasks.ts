/**
 * 质检任务 API（QMS）
 */
import { request, requestJson } from './client';

const BASE = '/api/inspection-tasks';

export interface InspectionTaskDTO {
  id: number;
  taskCode: string;
  orderId?: number;
  materialId?: number;
  deviceId?: number;
  inspectionType?: number;
  inspectionTime?: string;
  status: number;
  operator?: string;
  createTime?: string;
  updateTime?: string;
}

export interface InspectionTaskCreateRequest {
  taskCode: string;
  orderId?: number;
  materialId?: number;
  deviceId?: number;
  inspectionType?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getInspectionTaskPage(page = 1, size = 10): Promise<PageResult<InspectionTaskDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getInspectionTaskById(id: number): Promise<InspectionTaskDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createInspectionTask(body: InspectionTaskCreateRequest): Promise<InspectionTaskDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function completeInspectionTask(id: number): Promise<InspectionTaskDTO> {
  return requestJson(`${BASE}/${id}/complete`, { method: 'POST' });
}

export async function deleteInspectionTask(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
