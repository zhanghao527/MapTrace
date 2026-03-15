package com.maptrace.service;

import com.maptrace.model.vo.ConversationVO;
import com.maptrace.model.vo.MessageVO;
import com.maptrace.model.dto.SendMessageRequest;

import java.util.List;

public interface MessageService {
    List<ConversationVO> getConversations(Long userId);
    List<MessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size);
    MessageVO sendMessage(SendMessageRequest req, Long userId);
    void markAsRead(Long fromUserId, Long toUserId);
    int getUnreadCount(Long userId);
}
