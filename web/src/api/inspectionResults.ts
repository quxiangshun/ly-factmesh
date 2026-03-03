/**
 * 质检结果 API（QMS）
 */
import { request, requestJson } from './client';

const BASE = '/api/inspection-results';

export interface InspectionResultDTO {
  id: number;
  taskId: number;
  inspectionItem: string;
  standardValue?: string;
  actualValue?: string;
  judgment: number;
  inspector?: string;
  inspectionTime?: string;
  createTime?: string;
}

export interface InspectionResultCreateRequest {
  taskId: number;
  inspectionItem: string;
  standardValue?: string;
  actualValue?: string;
  judgment: number;
  inspector?: string;
}

export async function getInspectionResultsByTaskId(taskId: number): Promise<InspectionResultDTO[]> {
  return requestJson(`${BASE}?taskId=${taskId}`);
}

export async function createInspectionResult(body: InspectionResultCreateRequest): Promise<InspectionResultDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteInspectionResult(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
