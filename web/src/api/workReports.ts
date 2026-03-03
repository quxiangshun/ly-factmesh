/**
 * 报工 API（MES）
 */
import { request, requestJson } from './client';

const BASE = '/api/work-reports';

export interface WorkReportDTO {
  id: number;
  orderId: number;
  processId: number;
  deviceId: number;
  reportQuantity: number;
  scrapQuantity: number;
  reportTime?: string;
  operator?: string;
  orderCode?: string;
  processName?: string;
}

export interface WorkReportCreateRequest {
  orderId: number;
  processId: number;
  deviceId: number;
  reportQuantity: number;
  scrapQuantity?: number;
  operator?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getWorkReportPage(page = 1, size = 10, orderId?: number): Promise<PageResult<WorkReportDTO>> {
  const q = orderId != null ? `&orderId=${orderId}` : '';
  return requestJson(`${BASE}?page=${page}&size=${size}${q}`);
}

export async function getWorkReportById(id: number): Promise<WorkReportDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createWorkReport(body: WorkReportCreateRequest): Promise<WorkReportDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteWorkReport(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
