import request from '../utils/request';
import type { PageVO, UserListItem, UserDetailVO, PunishUserParams, UnpunishUserParams } from './typings';

/** 用户列表 */
export const getUsers = (params: Record<string, unknown>) =>
  request.get<unknown, PageVO<UserListItem>>('/api/admin/user/list', { params });

/** 用户详情 */
export const getUserDetail = (id: string) =>
  request.get<unknown, UserDetailVO>(`/api/admin/user/${id}`);

/** 用户照片 */
export const getUserPhotos = (id: string, params: Record<string, unknown>) =>
  request.get<unknown, PageVO<unknown>>(`/api/admin/user/${id}/photos`, { params });

/** 用户评论 */
export const getUserComments = (id: string, params: Record<string, unknown>) =>
  request.get<unknown, PageVO<unknown>>(`/api/admin/user/${id}/comments`, { params });

/** 处罚用户 */
export const punishUser = (data: PunishUserParams) =>
  request.post<unknown, void>('/api/admin/user/punish', data);

/** 解除处罚 */
export const unpunishUser = (data: UnpunishUserParams) =>
  request.post<unknown, void>('/api/admin/user/unpunish', data);
