package com.timemap.service;

import com.timemap.model.vo.ConversationVO;
import com.timemap.model.vo.MessageVO;
import com.timemap.model.dto.SendMessageRequest;

import java.util.List;

public interface MessageService {
    List<ConversationVO> getConversations(Long userId);
    List<MessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size);
    MessageVO sendMessage(SendMessageRequest req, Long userId);
    void markAsRead(Long fromUserId, Long toUserId);
    int getUnreadCount(Long userId);
}
