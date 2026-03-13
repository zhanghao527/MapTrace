package com.timemap.service;

import com.timemap.model.vo.AdminLogPageVO;

public interface AdminLogService {
    void log(Long adminUserId, String action, String targetType, Long targetId, String detail);
    AdminLogPageVO getLogs(Long adminUserId, int page, int size);
}
