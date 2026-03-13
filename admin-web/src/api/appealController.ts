import request from '../utils/request';
import type { AppealPageVO, AppealVO, HandleAppealParams } from './typings';

/** 申诉列表 */
export const getAppeals = (params: Record<string, unknown>) =>
  request.get<unknown, AppealPageVO>('/api/admin/report/appeals', { params });

/** 申诉详情 */
export const getAppealDetail = (id: number) =>
  request.get<unknown, AppealVO>(`/api/admin/report/appeal/${id}`);

/** 采纳申诉 */
export const resolveAppeal = (data: HandleAppealParams) =>
  request.post<unknown, void>('/api/admin/report/appeal/resolve', data);

/** 驳回申诉 */
export const rejectAppeal = (data: HandleAppealParams) =>
  request.post<unknown, void>('/api/admin/report/appeal/reject', data);
