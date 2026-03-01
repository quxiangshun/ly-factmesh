/**
 * 设备 API（IoT）
 */
import { request, requestJson } from './client';

const BASE = '/api/devices';

export interface DeviceDTO {
  id: number;
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

export async function getDeviceList(): Promise<DeviceDTO[]> {
  return requestJson(BASE);
}

export async function getDeviceById(id: number): Promise<DeviceDTO> {
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

export async function deviceOnline(id: number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/online`, { method: 'POST' });
}

export async function deviceOffline(id: number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/offline`, { method: 'POST' });
}

export async function deviceStart(id: number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/start`, { method: 'POST' });
}

export async function deviceStop(id: number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/stop`, { method: 'POST' });
}

export async function deviceFault(id: number): Promise<DeviceDTO> {
  return requestJson(`${BASE}/${id}/fault`, { method: 'POST' });
}

export async function deleteDevice(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}

// 遥测数据
export interface DeviceTelemetryRequest {
  deviceId: number;
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
  deviceId: number,
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

export async function getDeviceAlerts(deviceId: number, page = 1, size = 20): Promise<DeviceAlertDTO[]> {
  return requestJson(`${BASE}/alerts/device/${deviceId}?page=${page}&size=${size}`);
}

export async function getPendingAlerts(page = 1, size = 20): Promise<DeviceAlertDTO[]> {
  return requestJson(`${BASE}/alerts/pending?page=${page}&size=${size}`);
}

export async function getPendingAlertCount(): Promise<{ count: number }> {
  return requestJson(`${BASE}/alerts/pending/count`);
}
