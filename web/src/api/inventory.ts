/**
 * 库存 API（WMS）
 */
import { request, requestJson } from './client';

const BASE = '/api/inventory';

export interface InventoryDTO {
  id: number;
  materialId: number;
  materialCode: string;
  materialName: string;
  batchNo?: string;
  warehouse?: string;
  location?: string;
  quantity: number;
  safeStock?: number;
  belowSafeStock?: boolean;
  lastUpdateTime?: string;
}

export interface InventoryTransactionDTO {
  id: number;
  materialId: number;
  materialCode: string;
  materialName: string;
  batchNo?: string;
  transactionType: number;
  quantity: number;
  warehouse?: string;
  location?: string;
  orderId?: number;
  reqId?: number;
  transactionTime?: string;
  operator?: string;
  referenceNo?: string;
}

export interface InventoryAdjustRequest {
  materialId: number;
  batchNo?: string;
  quantity: number;
  warehouse?: string;
  location?: string;
  referenceNo?: string;
  operator?: string;
  orderId?: number;
  reqId?: number;
}

export interface InventoryCountRequest {
  inventoryId: number;
  actualQuantity: number;
  operator?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getInventoryPage(
  page = 1,
  size = 10,
  materialId?: number,
  warehouse?: string,
  batchNo?: string
): Promise<PageResult<InventoryDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (materialId != null) params.set('materialId', String(materialId));
  if (warehouse) params.set('warehouse', warehouse);
  if (batchNo) params.set('batchNo', batchNo);
  return requestJson(`${BASE}?${params}`);
}

export async function getInventoryBelowSafeStock(page = 1, size = 10): Promise<PageResult<InventoryDTO>> {
  return requestJson(`${BASE}/below-safe-stock?page=${page}&size=${size}`);
}

export async function adjustInventory(body: InventoryAdjustRequest): Promise<void> {
  await request(`${BASE}/adjust`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
}

export async function countInventory(body: InventoryCountRequest): Promise<void> {
  await request(`${BASE}/count`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
}

export async function updateSafeStock(id: number, safeStock: number): Promise<void> {
  await request(`${BASE}/${id}/safe-stock?safeStock=${safeStock}`, { method: 'PUT' });
}

export async function getInventoryTransactions(
  materialId: number,
  page = 1,
  size = 20
): Promise<PageResult<InventoryTransactionDTO>> {
  return requestJson(`${BASE}/transactions?materialId=${materialId}&page=${page}&size=${size}`);
}

export async function traceInventory(
  page = 1,
  size = 20,
  materialId?: number,
  batchNo?: string,
  orderId?: number,
  reqId?: number
): Promise<PageResult<InventoryTransactionDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (materialId != null) params.set('materialId', String(materialId));
  if (batchNo) params.set('batchNo', batchNo);
  if (orderId != null) params.set('orderId', String(orderId));
  if (reqId != null) params.set('reqId', String(reqId));
  return requestJson(`${BASE}/trace?${params}`);
}
