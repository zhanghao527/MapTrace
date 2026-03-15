package com.maptrace.util;

import com.maptrace.mapper.UserMapper;
import com.maptrace.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 批量查询辅助类
 * 用于解决 N+1 查询问题
 */
@Component
@RequiredArgsConstructor
public class BatchQueryHelper {

    private final UserMapper userMapper;

    /**
     * 批量查询用户信息，返回 Map<userId, User>
     */
    public Map<Long, User> batchQueryUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }
}
