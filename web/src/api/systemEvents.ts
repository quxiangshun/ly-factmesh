/**
 * 系统事件 API（OPS）
 */
import { requestJson } from './client';

const BASE = '/api/system-events';

export interface SystemEventDTO {
  id: number;
  eventType: string;
  eventLevel: number;
  eventContent: string;
  relatedService?: string;
  relatedId?: string;
  createTime?: string;
  processed?: number;
  processTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getSystemEventPage(
  page = 1,
  size = 20,
  eventType?: string,
  processed?: number
): Promise<PageResult<SystemEventDTO>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (eventType) params.set('eventType', eventType);
  if (processed != null) params.set('processed', String(processed));
  return requestJson(`${BASE}?${params}`);
}
