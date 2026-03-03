/**
 * 领料单 API（WMS）
 */
import { request, requestJson } from './client';

const BASE = '/api/requisitions';

export interface MaterialRequisitionDetailDTO {
  id: number;
  reqId: number;
  materialId: number;
  materialCode: string;
  materialName: string;
  batchNo?: string;
  quantity: number;
  actualQuantity?: number;
}

export interface MaterialRequisitionDTO {
  id: number;
  reqNo: string;
  orderId?: number;
  reqType: number;
  status: number;
  createTime?: string;
  updateTime?: string;
  details?: MaterialRequisitionDetailDTO[];
}

export interface RequisitionManualCreateRequest {
  materialId: number;
  quantity: number;
  orderId?: number;
  reqType?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getRequisitionPage(
  page = 1,
  size = 10,
  orderId?: number,
  status?: number
): Promise<PageResult<MaterialRequisitionDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (orderId != null) params.set('orderId', String(orderId));
  if (status != null) params.set('status', String(status));
  return requestJson(`${BASE}?${params}`);
}

export async function getRequisitionById(id: number): Promise<MaterialRequisitionDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createRequisitionDraft(body: RequisitionManualCreateRequest): Promise<number> {
  const res = await request(BASE + '/draft', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function submitRequisition(id: number): Promise<void> {
  await request(`${BASE}/${id}/submit`, { method: 'POST' });
}

export async function completeRequisition(id: number, requestBody?: { details?: { detailId: number; actualQuantity: number }[] }): Promise<void> {
  await request(`${BASE}/${id}/complete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestBody ?? {})
  });
}

export async function cancelRequisition(id: number): Promise<void> {
  await request(`${BASE}/${id}/cancel`, { method: 'POST' });
}
