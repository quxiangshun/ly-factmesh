/**
 * 工单 API（MES）
 */
import { request, requestJson } from './client';

const BASE = '/api/work-orders';

export interface WorkOrderDTO {
  id: number;
  orderCode: string;
  productCode: string;
  productName: string;
  planQuantity: number;
  actualQuantity: number;
  status: number;
  startTime?: string;
  endTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface WorkOrderCreateRequest {
  orderCode: string;
  productCode: string;
  productName: string;
  planQuantity: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getWorkOrderPage(page = 1, size = 10): Promise<PageResult<WorkOrderDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getWorkOrderById(id: number): Promise<WorkOrderDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createWorkOrder(body: WorkOrderCreateRequest): Promise<WorkOrderDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function releaseWorkOrder(id: number): Promise<WorkOrderDTO> {
  return requestJson(`${BASE}/${id}/release`, { method: 'POST' });
}

export async function startWorkOrder(id: number): Promise<WorkOrderDTO> {
  return requestJson(`${BASE}/${id}/start`, { method: 'POST' });
}

export async function completeWorkOrder(id: number, actualQuantity?: number): Promise<WorkOrderDTO> {
  const q = actualQuantity != null ? `?actualQuantity=${actualQuantity}` : '';
  return requestJson(`${BASE}/${id}/complete${q}`, { method: 'POST' });
}

export async function deleteWorkOrder(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}

export async function getWorkOrderStats(): Promise<{
  total: number;
  draftCount: number;
  releasedCount: number;
  inProgressCount: number;
  completedCount: number;
}> {
  return requestJson(`${BASE}/stats`);
}
