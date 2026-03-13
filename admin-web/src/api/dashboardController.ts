import request from '../utils/request';
import type { DashboardStatsVO } from './typings';

/** 获取仪表盘统计数据 */
export const getDashboardStats = () =>
  request.get<unknown, DashboardStatsVO>('/api/admin/dashboard/stats');
