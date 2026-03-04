/**
 * 设备 API（IoT）
 */
import { request, requestJson } from './client';

const BASE = '/api/devices';

export interface DeviceDTO {
  id: string;
  deviceCode: string;
  deviceName: string;
  deviceType?: string;
  model?: string;
  manufacturer?: string;
  installLocation?: string;
  onlineStatus: number;
  runningStatus: number;
  temperature?: number;
  humidity?: number;
  voltage?: number;
  current?: number;
  statusLastUpdateTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface DeviceRegisterRequest {
  deviceCode: string;
  deviceName: string;
  deviceType?: string;
  model?: string;
  manufacturer?: string;
  installLocation?: string;
}

export interface DeviceUpdateRequest {
  deviceName?: string;
  deviceType?: string;
  model?: string;
  manufacturer?: string;
  installLocation?: string;
}

export async function getDeviceList(): Promise<DeviceDTO[]> {
  return requestJson(BASE);
}

export async function getDeviceById(id: string | number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}`);
}

export interface DeviceStatsDTO {
  total: number;
  online: number;
  fault: number;
}

export async function getDeviceStats(): Promise<DeviceStatsDTO> {
  return requestJson(`${BASE}/stats`);
}

export async function registerDevice(body: DeviceRegisterRequest): Promise<DeviceDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

// 批量导入
export interface DeviceBatchImportResult {
  successCount: number;
  failCount: number;
  errors: Array<{ row: number; deviceCode: string; message: string }>;
}

export interface DeviceImportRow {
  row: number;
  deviceCode: string;
  deviceName: string;
  deviceType?: string;
  model?: string;
  manufacturer?: string;
  installLocation?: string;
}

export interface DeviceBatchPreviewResult {
  rows: DeviceImportRow[];
  errors: Array<{ row: number; deviceCode: string; message: string }>;
}

export async function batchPreviewDevices(file: File): Promise<DeviceBatchPreviewResult> {
  const form = new FormData();
  form.append('file', file);
  const res = await request(`${BASE}/batch/preview`, {
    method: 'POST',
    body: form
  });
  return res.json();
}

export async function batchImportDevices(rows: DeviceImportRow[]): Promise<DeviceBatchImportResult> {
  const res = await request(`${BASE}/batch/import`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(rows)
  });
  return res.json();
}

export async function downloadDeviceImportTemplate(): Promise<void> {
  const res = await request(`${BASE}/batch/template`);
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'device_import_template.xlsx';
  a.click();
  URL.revokeObjectURL(url);
}

export async function deviceOnline(id: string | number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/online`, { method: 'POST' });
}

export async function deviceOffline(id: string | number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/offline`, { method: 'POST' });
}

export async function deviceStart(id: string | number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/start`, { method: 'POST' });
}

export async function deviceStop(id: string | number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/stop`, { method: 'POST' });
}

export async function deviceFault(id: string | number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/fault`, { method: 'POST' });
}

export async function updateDevice(id: string | number, body: DeviceUpdateRequest): Promise<DeviceDTO> {
  const res = await request(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteDevice(id: string | number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}

export async function updateDeviceStatus(
  id: string | number,
  params?: { temperature?: number; humidity?: number; voltage?: number; current?: number }
): Promise<DeviceDTO> {
  const sp = new URLSearchParams();
  if (params?.temperature != null) sp.set('temperature', String(params.temperature));
  if (params?.humidity != null) sp.set('humidity', String(params.humidity));
  if (params?.voltage != null) sp.set('voltage', String(params.voltage));
  if (params?.current != null) sp.set('current', String(params.current));
  const qs = sp.toString();
  return requestJson(`${BASE}/${id}/status${qs ? '?' + qs : ''}`, { method: 'PATCH' });
}

// 遥测数据
export interface DeviceTelemetryRequest {
  deviceId: string | number;
  deviceCode?: string;
  timestamp?: number;
  data: Record<string, number>;
}

export interface DeviceTelemetryPoint {
  deviceId: number;
  deviceCode?: string;
  field: string;
  value: number;
  time: string;
}

export async function reportTelemetry(body: DeviceTelemetryRequest): Promise<void> {
  await request(`${BASE}/telemetry/report`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
}

export async function queryTelemetry(
  deviceId: string | number,
  params?: { field?: string; start?: string; end?: string; limit?: number }
): Promise<DeviceTelemetryPoint[]> {
  const sp = new URLSearchParams();
  if (params?.field) sp.set('field', params.field);
  if (params?.start) sp.set('start', params.start);
  if (params?.end) sp.set('end', params.end);
  if (params?.limit) sp.set('limit', String(params.limit));
  const qs = sp.toString();
  return requestJson(`${BASE}/telemetry/${deviceId}${qs ? '?' + qs : ''}`);
}

// 设备告警
export interface DeviceAlertDTO {
  id: number;
  deviceId: number;
  deviceCode?: string;
  ruleId?: number;
  alertType: string;
  alertLevel: number;
  alertContent: string;
  alertStatus: number;
  createTime?: string;
  resolveTime?: string;
  resolvedBy?: string;
  remark?: string;
}

export interface DeviceAlertCreateRequest {
  deviceId: number;
  deviceCode?: string;
  alertType: string;
  alertLevel: number;
  alertContent: string;
  remark?: string;
}

export async function createDeviceAlert(body: DeviceAlertCreateRequest): Promise<DeviceAlertDTO> {
  const res = await request(`${BASE}/alerts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function resolveDeviceAlert(id: number, resolvedBy?: string, remark?: string): Promise<DeviceAlertDTO> {
  const sp = new URLSearchParams();
  if (resolvedBy) sp.set('resolvedBy', resolvedBy);
  if (remark) sp.set('remark', remark);
  const qs = sp.toString();
  return requestJson(`${BASE}/alerts/${id}/resolve${qs ? '?' + qs : ''}`, { method: 'POST' });
}

export async function getDeviceAlerts(deviceId: string | number, page = 1, size = 20): Promise<DeviceAlertDTO[]> {
  return requestJson(`${BASE}/alerts/device/${deviceId}?page=${page}&size=${size}`);
}

export async function getAlertsAll(page = 1, size = 20): Promise<DeviceAlertDTO[]> {
  return requestJson(`${BASE}/alerts?page=${page}&size=${size}`);
}

export async function getPendingAlerts(page = 1, size = 20): Promise<DeviceAlertDTO[]> {
  return requestJson(`${BASE}/alerts/pending?page=${page}&size=${size}`);
}

export async function getPendingAlertCount(): Promise<{ count: number }> {
  return requestJson(`${BASE}/alerts/pending/count`);
}

// 告警规则
export interface DeviceAlertRuleDTO {
  id: number;
  ruleName?: string;
  deviceId?: number;
  deviceType?: string;
  fieldName: string;
  operator: string;
  thresholdValue: number;
  alertType: string;
  alertLevel: number;
  alertContentTemplate?: string;
  enabled: number;
  cooldownSeconds: number;
  createTime?: string;
  updateTime?: string;
}

export interface DeviceAlertRuleCreateRequest {
  ruleName?: string;
  deviceId?: number;
  deviceType?: string;
  fieldName: string;
  operator: string;
  thresholdValue: number;
  alertType: string;
  alertLevel: number;
  alertContentTemplate?: string;
  enabled?: number;
  cooldownSeconds?: number;
}

export interface DeviceAlertRuleUpdateRequest {
  ruleName?: string;
  deviceId?: number;
  deviceType?: string;
  fieldName?: string;
  operator?: string;
  thresholdValue?: number;
  alertType?: string;
  alertLevel?: number;
  alertContentTemplate?: string;
  enabled?: number;
  cooldownSeconds?: number;
}

export async function getAlertRules(page = 1, size = 20): Promise<DeviceAlertRuleDTO[]> {
  return requestJson(`${BASE}/alert-rules?page=${page}&size=${size}`);
}

export async function createAlertRule(body: DeviceAlertRuleCreateRequest): Promise<DeviceAlertRuleDTO> {
  const res = await request(`${BASE}/alert-rules`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateAlertRule(id: number, body: DeviceAlertRuleUpdateRequest): Promise<DeviceAlertRuleDTO> {
  const res = await request(`${BASE}/alert-rules/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteAlertRule(id: number): Promise<void> {
  await request(`${BASE}/alert-rules/${id}`, { method: 'DELETE' });
}
