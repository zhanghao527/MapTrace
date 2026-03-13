import request from '../utils/request';
import type { AdminLogPageVO } from './typings';

/** 操作日志列表 */
export const getLogs = (params: Record<string, unknown>) =>
  request.get<unknown, AdminLogPageVO>('/api/admin/report/logs', { params });
