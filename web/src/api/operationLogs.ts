/**
 * 操作日志 API（Admin）
 */
import { requestJson } from './client';

const BASE = '/api/operation-logs';

export interface OperationLogDTO {
  id: number;
  userId?: number;
  username?: string;
  module?: string;
  operation?: string;
  method?: string;
  url?: string;
  params?: string;
  ip?: string;
  status?: number;
  errorMsg?: string;
  duration?: number;
  createTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getOperationLogs(
  page = 1,
  size = 20,
  params?: { userId?: number; module?: string; username?: string }
): Promise<PageResult<OperationLogDTO>> {
  const q = new URLSearchParams();
  q.set('page', String(page));
  q.set('size', String(size));
  if (params?.userId != null) q.set('userId', String(params.userId));
  if (params?.module) q.set('module', params.module);
  if (params?.username) q.set('username', params.username);
  return requestJson(`${BASE}?${q}`);
}
