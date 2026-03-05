/**
 * 报表 API（Admin）- 预定义模板、自定义报表定义、执行
 */
import { request, requestJson } from './client';

const BASE = '/api/reports';

export interface ReportTemplate {
  code: string;
  name: string;
  params: string[];
}

export interface ReportDefDTO {
  id?: number;
  name: string;
  reportType: string;
  paramsJson?: string;
  createTime?: string;
  updateTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export interface ReportExecuteResult {
  columns: string[];
  data: Record<string, unknown>[];
}

export async function getReportTemplates(): Promise<ReportTemplate[]> {
  return requestJson(`${BASE}/templates`);
}

export async function getReportDefPage(
  page = 1,
  size = 20
): Promise<PageResult<ReportDefDTO>> {
  return requestJson(`${BASE}/definitions?page=${page}&size=${size}`);
}

export async function getReportDefById(id: number): Promise<ReportDefDTO> {
  return requestJson(`${BASE}/definitions/${id}`);
}

export async function createReportDef(
  body: ReportDefDTO
): Promise<ReportDefDTO> {
  const res = await fetch(BASE + '/definitions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${(await import('./auth')).getToken()}`
    },
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    const err = (await res.json()) as { message?: string };
    throw new Error(err.message || `创建失败 ${res.status}`);
  }
  return res.json();
}

export async function updateReportDef(
  id: number,
  body: ReportDefDTO
): Promise<ReportDefDTO> {
  const res = await request(`${BASE}/definitions/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    const err = (await res.json()) as { message?: string };
    throw new Error(err.message || `更新失败 ${res.status}`);
  }
  return res.json();
}

export async function deleteReportDef(id: number): Promise<void> {
  await request(`${BASE}/definitions/${id}`, { method: 'DELETE' });
}

export async function executeReport(body: {
  reportType?: string;
  definitionId?: number;
  params?: Record<string, unknown>;
}): Promise<ReportExecuteResult> {
  const res = await request(`${BASE}/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    const err = (await res.json()) as { message?: string };
    throw new Error(err.message || `执行失败 ${res.status}`);
  }
  return res.json();
}
