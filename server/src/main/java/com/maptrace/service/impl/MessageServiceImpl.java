package com.maptrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maptrace.mapper.MessageMapper;
import com.maptrace.mapper.UserMapper;
import com.maptrace.model.vo.ConversationVO;
import com.maptrace.model.vo.MessageVO;
import com.maptrace.model.dto.SendMessageRequest;
import com.maptrace.model.entity.Message;
import com.maptrace.model.entity.User;
import com.maptrace.monitor.BusinessMetricsCollector;
import com.maptrace.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final BusinessMetricsCollector metricsCollector;

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        return messageMapper.findConversations(userId);
    }

    @Override
    public List<MessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size) {
        Page<Message> p = new Page<>(page, size);
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<Message>()
                .and(w -> w
                        .and(a -> a.eq(Message::getFromUserId, userId).eq(Message::getToUserId, otherUserId))
                        .or(b -> b.eq(Message::getFromUserId, otherUserId).eq(Message::getToUserId, userId))
                )
                .orderByDesc(Message::getCreateTime);
        messageMapper.selectPage(p, qw);

        return p.getRecords().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public MessageVO sendMessage(SendMessageRequest req, Long userId) {
        Message msg = new Message();
        msg.setFromUserId(userId);
        msg.setToUserId(req.getToUserId());
        msg.setContent(req.getContent());
        msg.setMsgType(req.getMsgType() != null ? req.getMsgType() : "text");
        msg.setReadStatus(0);
        messageMapper.insert(msg);

        // 监控埋点
        metricsCollector.recordMessage(String.valueOf(userId));

        return toResponse(msg);
    }

    @Override
    public void markAsRead(Long fromUserId, Long toUserId) {
        messageMapper.markAsRead(fromUserId, toUserId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return Math.toIntExact(messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getReadStatus, 0)));
    }

    private MessageVO toResponse(Message m) {
        MessageVO r = new MessageVO();
        r.setId(m.getId());
        r.setFromUserId(m.getFromUserId());
        r.setToUserId(m.getToUserId());
        r.setContent(m.getContent());
        r.setMsgType(m.getMsgType());
        r.setReadStatus(m.getReadStatus());
        r.setCreateTime(m.getCreateTime() != null ? m.getCreateTime().toString() : "");

        User from = userMapper.selectById(m.getFromUserId());
        if (from != null) {
            r.setFromNickname(from.getNickname());
            r.setFromAvatarUrl(from.getAvatarUrl());
        }
        return r;
    }
}
