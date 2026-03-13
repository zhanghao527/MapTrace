import request from '../utils/request';
import type { AdminLoginVO, AdminAccountVO, LoginParams, ChangePasswordParams } from './typings';

/** 管理员登录 */
export const login = (data: LoginParams) =>
  request.post<unknown, AdminLoginVO>('/api/admin/auth/login', data);

/** 获取当前管理员信息 */
export const getAdminInfo = () =>
  request.get<unknown, AdminAccountVO>('/api/admin/auth/info');

/** 修改密码 */
export const changePassword = (data: ChangePasswordParams) =>
  request.post<unknown, void>('/api/admin/auth/change-password', data);
