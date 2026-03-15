package com.maptrace.service;

import com.maptrace.model.vo.AdminLogPageVO;

public interface AdminLogService {
    void log(Long adminUserId, String action, String targetType, Long targetId, String detail);
    AdminLogPageVO getLogs(Long adminUserId, int page, int size);
}
