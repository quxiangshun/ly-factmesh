/**
 * 全局日志 API（OPS）
 */
import { requestJson } from './client';

const BASE = '/api/global-logs';

export interface GlobalLogDTO {
  id: number;
  serviceName: string;
  logType: number;
  logLevel: string;
  logContent: string;
  requestId?: string;
  clientIp?: string;
  createTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getGlobalLogPage(
  page = 1,
  size = 20,
  serviceName?: string
): Promise<PageResult<GlobalLogDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (serviceName) params.set('serviceName', serviceName);
  return requestJson(`${BASE}?${params}`);
}
