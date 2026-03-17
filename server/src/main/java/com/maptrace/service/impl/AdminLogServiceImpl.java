package com.maptrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maptrace.mapper.AdminAccountMapper;
import com.maptrace.mapper.AdminLogMapper;
import com.maptrace.model.vo.AdminLogPageVO;
import com.maptrace.model.vo.AdminLogVO;
import com.maptrace.model.entity.AdminAccount;
import com.maptrace.model.entity.AdminLog;
import com.maptrace.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl implements AdminLogService {

    private final AdminLogMapper adminLogMapper;
    private final AdminAccountMapper adminAccountMapper;

    @Override
    public void log(Long adminUserId, String action, String targetType, Long targetId, String detail) {
        AdminLog log = new AdminLog();
        log.setAdminUserId(adminUserId);
        log.setAction(action);
        log.setTargetType(targetType != null ? targetType : "");
        log.setTargetId(targetId);
        log.setDetail(detail != null && detail.length() > 2000 ? detail.substring(0, 2000) : detail);
        adminLogMapper.insert(log);
    }

    @Override
    public AdminLogPageVO getLogs(Long adminUserId, int page, int size) {
        Page<AdminLog> p = new Page<>(page, size);
        LambdaQueryWrapper<AdminLog> qw = new LambdaQueryWrapper<AdminLog>()
                .orderByDesc(AdminLog::getCreateTime);
        adminLogMapper.selectPage(p, qw);

        AdminLogPageVO response = new AdminLogPageVO();
        response.setList(p.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setTotal(p.getTotal());
        response.setHasMore((long) page * size < p.getTotal());
        return response;
    }

    private AdminLogVO toResponse(AdminLog log) {
        AdminLogVO r = new AdminLogVO();
        r.setId(log.getId());
        r.setAdminUserId(log.getAdminUserId());
        r.setAction(log.getAction());
        r.setTargetType(log.getTargetType());
        r.setTargetId(log.getTargetId());
        r.setDetail(log.getDetail());
        r.setCreateTime(log.getCreateTime() != null ? log.getCreateTime().toString() : "");
        AdminAccount admin = adminAccountMapper.selectById(log.getAdminUserId());
        if (admin != null) {
            r.setAdminNickname(admin.getNickname());
        }
        return r;
    }
}
