/**
 * 用户 API（Admin）
 */
import { request, requestJson } from './client';

const BASE = '/api/users';

export interface UserDTO {
  id: number;
  username: string;
  realName?: string;
  email?: string;
  phone?: string;
  status?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  realName: string;
  email?: string;
  phone?: string;
  status?: number;
}

export interface UserUpdateRequest {
  realName?: string;
  email?: string;
  phone?: string;
  status?: number;
  password?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export async function getUsers(page = 1, size = 10): Promise<PageResult<UserDTO>> {
  return requestJson<PageResult<UserDTO>>(`${BASE}?page=${page}&size=${size}`);
}

export async function getUserById(id: number): Promise<UserDTO> {
  return requestJson(`${BASE}/${id}`);
}

export async function createUser(body: UserCreateRequest): Promise<UserDTO> {
  const res = await request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function updateUser(id: number, body: UserUpdateRequest): Promise<UserDTO> {
  const res = await request(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

export async function deleteUser(id: number): Promise<void> {
  await request(`${BASE}/${id}`, { method: 'DELETE' });
}
