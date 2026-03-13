import request from '../utils/request';
import type { AdminAccountVO, CreateAdminParams, UpdateAdminParams } from './typings';

/** 管理员列表 */
export const getAdminAccounts = () =>
  request.get<unknown, AdminAccountVO[]>('/api/admin/account/list');

/** 创建管理员 */
export const createAdminAccount = (data: CreateAdminParams) =>
  request.post<unknown, AdminAccountVO>('/api/admin/account/create', data);

/** 更新管理员 */
export const updateAdminAccount = (id: number, data: UpdateAdminParams) =>
  request.put<unknown, void>(`/api/admin/account/${id}`, data);

/** 重置密码 */
export const resetAdminPassword = (id: number) =>
  request.post<unknown, void>(`/api/admin/account/${id}/reset-password`);

/** 切换启用/禁用 */
export const toggleAdminAccount = (id: number) =>
  request.post<unknown, void>(`/api/admin/account/${id}/toggle`);
