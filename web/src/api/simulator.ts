/**
 * 数据模拟器 API（仅开发环境）
 */
import { requestJson } from './client';

const BASE = '/api/simulator';

export interface SimulatorConfig {
  intervalMs: number;
  deviceCount: number;
}

export interface SimulatorStatus {
  opcua: {
    enabled: boolean;
    running: boolean;
    endpoint: string;
    intervalMs: number;
    deviceCount: number;
    note: string;
  };
  modbus: {
    enabled: boolean;
    running: boolean;
    host: string;
    port: number;
    intervalMs: number;
    deviceCount: number;
    note: string;
  };
}

export interface SimulatorValues {
  opcua?: Record<string, number>;
  modbus?: Record<string, number>;
}

export async function getSimulatorConfig(): Promise<SimulatorConfig> {
  return requestJson(`${BASE}/config`);
}

export async function updateSimulatorConfig(config: Partial<SimulatorConfig>): Promise<SimulatorConfig> {
  return requestJson<SimulatorConfig>(`${BASE}/config`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config)
  });
}

export async function getSimulatorStatus(): Promise<SimulatorStatus> {
  return requestJson(`${BASE}/status`);
}

export async function getSimulatorValues(): Promise<SimulatorValues> {
  return requestJson(`${BASE}/values`);
}
