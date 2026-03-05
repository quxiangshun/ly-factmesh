/**
 * mom-ai 模块接口（经网关 /api/ai/** 转发至 mom-ai）
 */
import { requestJson } from './client';

const BASE = '/api/ai';

export interface Capability {
  code: string;
  name: string;
  status: string;
  description: string;
}

export interface CapabilitiesResponse {
  module: string;
  version: string;
  lang: string;
  capabilities: Capability[];
}

export function getCapabilities(): Promise<CapabilitiesResponse> {
  return requestJson(`${BASE}/capabilities`);
}

export interface TrainRequest {
  epochs?: number;
  lr?: number;
  train_x?: number[][];
  train_y?: number[];
}

export interface TrainResponse {
  status: string;
  epochs: number;
  lr: number;
  final_loss: number;
  model_path: string;
}

export function train(data: TrainRequest): Promise<TrainResponse> {
  return requestJson(`${BASE}/train`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
}

export interface PredictRequest {
  data: number[][];
}

export interface PredictResponse {
  input_data: number[][];
  predictions: number[];
}

export function predict(data: PredictRequest): Promise<PredictResponse> {
  return requestJson(`${BASE}/predict`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
}
