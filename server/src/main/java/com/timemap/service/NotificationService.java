package com.timemap.service;

import com.timemap.model.vo.NotificationVO;
import java.util.List;

public interface NotificationService {
    void createNotification(Long userId, Long fromUserId, String type, Long photoId, Long commentId, String content);
    List<NotificationVO> getNotifications(Long userId, int page, int size);
    int getUnreadCount(Long userId);
    void markAllRead(Long userId);
}
