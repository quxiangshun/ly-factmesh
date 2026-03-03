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

export interface InspectionTaskCompleteRequest {
  operator?: string;
  forceComplete?: boolean;
}

export interface InspectionTaskStatsDTO {
  total: number;
  draftCount: number;
  inProgressCount: number;
  completedCount: number;
}

export interface InspectionTaskNcrContextDTO {
  taskId: number;
  taskCode: string;
  orderId?: number;
  materialId?: number;
  suggestedProductCode?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getInspectionTaskPage(page = 1, size = 10, status?: number): Promise<PageResult<InspectionTaskDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (status !== undefined && status !== null) params.set('status', String(status));
  return requestJson(`${BASE}?${params}`);
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

export async function startInspectionTask(id: number): Promise<InspectionTaskDTO> {
  return requestJson(`${BASE}/${id}/start`, { method: 'POST' });
}

export async function completeInspectionTask(id: number, body?: InspectionTaskCompleteRequest): Promise<InspectionTaskDTO> {
  const res = await request(`${BASE}/${id}/complete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : '{}'
  });
  return res.json();
}

export async function getInspectionTaskStats(): Promise<InspectionTaskStatsDTO> {
  return requestJson(`${BASE}/stats`);
}

export async function getInspectionTaskNcrContext(taskId: number): Promise<InspectionTaskNcrContextDTO> {
  return requestJson(`${BASE}/${taskId}/ncr-context`);
}

export async function deleteInspectionTask(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
