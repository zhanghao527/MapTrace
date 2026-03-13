package com.timemap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timemap.config.SubscribeMessageConfig;
import com.timemap.mapper.NotificationMapper;
import com.timemap.mapper.PhotoMapper;
import com.timemap.mapper.UserMapper;
import com.timemap.model.vo.NotificationVO;
import com.timemap.model.entity.Notification;
import com.timemap.model.entity.Photo;
import com.timemap.model.entity.User;
import com.timemap.service.NotificationService;
import com.timemap.service.WxSubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final PhotoMapper photoMapper;
    private final WxSubscribeMessageService wxSubscribeMessageService;
    private final SubscribeMessageConfig subscribeMessageConfig;

    /** 互动类通知类型 */
    private static final Set<String> INTERACTION_TYPES = Set.of(
            "comment", "reply", "photo_like", "comment_like");

    /** 举报结果类通知类型 */
    private static final Set<String> REPORT_TYPES = Set.of(
            "report_result", "content_removed");

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void createNotification(Long userId, Long fromUserId, String type,
                                   Long photoId, Long commentId, String content) {
        // 不给自己发通知
        if (userId.equals(fromUserId)) return;

        Notification n = new Notification();
        n.setUserId(userId);
        n.setFromUserId(fromUserId);
        n.setType(type);
        n.setPhotoId(photoId);
        n.setCommentId(commentId);
        n.setContent(content != null && content.length() > 200 ? content.substring(0, 200) : content);
        n.setIsRead(0);
        notificationMapper.insert(n);

        // 异步尝试发送微信订阅消息（无额度时静默跳过，不影响主流程）
        try {
            trySendSubscribeMessage(userId, fromUserId, type, photoId, content);
        } catch (Exception e) {
            log.warn("发送订阅消息异常，不影响站内通知: userId={}, type={}", userId, type, e);
        }
    }

    /**
     * 根据通知类型尝试发送对应的微信订阅消息
     */
    private void trySendSubscribeMessage(Long userId, Long fromUserId, String type,
                                         Long photoId, String content) {
        if (INTERACTION_TYPES.contains(type)) {
            String templateId = subscribeMessageConfig.getInteractionTemplateId();
            if (templateId == null || templateId.isEmpty()) return;

            User fromUser = userMapper.selectById(fromUserId);
            String nickname = (fromUser != null && fromUser.getNickname() != null)
                    ? fromUser.getNickname() : "有人";

            String actionDesc = switch (type) {
                case "comment" -> "评论了你的照片";
                case "reply" -> "回复了你的评论";
                case "photo_like" -> "赞了你的照片";
                case "comment_like" -> "赞了你的评论";
                default -> "与你互动";
            };

            // 跳转页面：有 photoId 就跳详情页，否则跳通知列表
            String page = photoId != null
                    ? "pages/detail/detail?id=" + photoId
                    : "pages/profile/profile";

            // 模板数据（具体 key 需要与你在微信后台申请的模板字段对应）
            // 常见字段: thing1(用户昵称), thing2(互动内容), time3(互动时间)
            Map<String, String> data = new HashMap<>();
            data.put("thing1", limitStr(nickname, 20));
            data.put("thing2", limitStr(actionDesc, 20));
            data.put("time3", LocalDateTime.now().format(TIME_FMT));

            wxSubscribeMessageService.trySend(userId, templateId, page, data);

        } else if (REPORT_TYPES.contains(type)) {
            String templateId = subscribeMessageConfig.getReportTemplateId();
            if (templateId == null || templateId.isEmpty()) return;

            String page = "pages/my-reports/my-reports";

            Map<String, String> data = new HashMap<>();
            data.put("thing1", limitStr(content != null ? content : "你的举报已处理", 20));
            data.put("time2", LocalDateTime.now().format(TIME_FMT));

            wxSubscribeMessageService.trySend(userId, templateId, page, data);
        }
    }

    private String limitStr(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) : text;
    }

    @Override
    public List<NotificationVO> getNotifications(Long userId, int page, int size) {
        Page<Notification> p = new Page<>(page, size);
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        notificationMapper.selectPage(p, qw);

        return p.getRecords().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public int getUnreadCount(Long userId) {
        return Math.toIntExact(notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)));
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    private NotificationVO toResponse(Notification n) {
        NotificationVO r = new NotificationVO();
        r.setId(n.getId());
        r.setType(n.getType());
        r.setFromUserId(n.getFromUserId());
        r.setPhotoId(n.getPhotoId());
        r.setCommentId(n.getCommentId());
        r.setContent(n.getContent());
        r.setIsRead(n.getIsRead());
        r.setCreateTime(n.getCreateTime() != null ? n.getCreateTime().toString() : "");

        User from = userMapper.selectById(n.getFromUserId());
        if (from != null) {
            r.setFromNickname(from.getNickname());
            r.setFromAvatarUrl(from.getAvatarUrl());
        }

        if (n.getPhotoId() != null) {
            Photo photo = photoMapper.selectById(n.getPhotoId());
            if (photo != null) {
                r.setPhotoThumbnail(photo.getThumbnailUrl());
            }
        }
        return r;
    }
}
