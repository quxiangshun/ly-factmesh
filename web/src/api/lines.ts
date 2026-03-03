/**
 * 产线 API（MES）
 */
import { request, requestJson } from './client';

const BASE = '/api/lines';

export interface ProductionLineDTO {
  id: number;
  lineCode: string;
  lineName: string;
  description?: string;
  sequence: number;
  createTime?: string;
  updateTime?: string;
}

export interface ProductionLineCreateRequest {
  lineCode: string;
  lineName: string;
  description?: string;
  sequence?: number;
}

export interface ProductionLineUpdateRequest {
  lineName: string;
  description?: string;
  sequence?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getLinePage(page = 1, size = 10): Promise<PageResult<ProductionLineDTO>> {
  return requestJson(`${BASE}?page=${page}&size=${size}`);
}

export async function getLineList(): Promise<ProductionLineDTO[]> {
  return requestJson(`${BASE}/list`);
}

export async function getLineById(id: number): Promise<ProductionLineDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createLine(body: ProductionLineCreateRequest): Promise<ProductionLineDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateLine(id: number, body: ProductionLineUpdateRequest): Promise<ProductionLineDTO> {
  return requestJson(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
}

export async function deleteLine(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}

export interface ProductionLineCapacityDTO {
  lineId: number;
  lineCode: string;
  lineName: string;
  status?: number;
  date?: string;
  completedOrderCount?: number;
  completedQuantity?: number;
}

export async function getCapacitySummary(date?: string): Promise<ProductionLineCapacityDTO[]> {
  const qs = date ? `?date=${date}` : '';
  return requestJson(`${BASE}/capacity-summary${qs}`);
}
