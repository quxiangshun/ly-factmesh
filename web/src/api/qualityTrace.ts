/**
 * 质量追溯 API（QMS）
 */
import { requestJson } from './client';

const BASE = '/api/quality-trace';

export interface QualityTraceabilityDTO {
  id: number;
  productCode?: string;
  batchNo?: string;
  materialBatch?: string;
  productionOrder?: string;
  inspectionRecordId?: number;
  nonConformingId?: number;
  createTime?: string;
}

export async function traceQuality(
  productCode?: string,
  batchNo?: string,
  productionOrder?: string
): Promise<QualityTraceabilityDTO[]> {
  const params = new URLSearchParams();
  if (productCode) params.set('productCode', productCode);
  if (batchNo) params.set('batchNo', batchNo);
  if (productionOrder) params.set('productionOrder', productionOrder);
  const qs = params.toString();
  return requestJson(`${BASE}${qs ? '?' + qs : ''}`);
}
