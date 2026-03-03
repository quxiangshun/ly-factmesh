/**
 * 运维审计日志 API（OPS）
 */
import { requestJson } from './client';

const BASE = '/api/ops-audit-logs';

export interface OpsAuditLogDTO {
  id: number;
  serviceName: string;
  userId?: number;
  username?: string;
  operationType: string;
  operationContent: string;
  operationResult: number;
  operationTime?: string;
  clientIp?: string;
  requestParams?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getOpsAuditLogPage(
  page = 1,
  size = 20,
  serviceName?: string,
  userId?: number
): Promise<PageResult<OpsAuditLogDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (serviceName) params.set('serviceName', serviceName);
  if (userId != null) params.set('userId', String(userId));
  return requestJson(`${BASE}?${params}`);
}
