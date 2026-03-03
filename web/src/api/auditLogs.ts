/**
 * 审计日志 API（Admin）
 */
import { requestJson } from './client';

const BASE = '/api/audit-logs';

export interface AuditLogDTO {
  id: number;
  tableName?: string;
  recordId?: string;
  operationType?: string;
  oldValue?: string;
  newValue?: string;
  operatorId?: number;
  operatorName?: string;
  createTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getAuditLogs(
  page = 1,
  size = 20,
  params?: { tableName?: string; recordId?: string; operationType?: string }
): Promise<PageResult<AuditLogDTO>> {
  const q = new URLSearchParams();
  q.set('page', String(page));
  q.set('size', String(size));
  if (params?.tableName) q.set('tableName', params.tableName);
  if (params?.recordId) q.set('recordId', params.recordId);
  if (params?.operationType) q.set('operationType', params.operationType);
  return requestJson(`${BASE}?${q}`);
}
