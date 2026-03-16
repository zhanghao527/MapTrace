package com.maptrace.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理所有在线用户的 WebSocket 连接
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /** userId -> WebSocketSession */
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        WebSocketSession old = sessions.put(userId, session);
        if (old != null && old.isOpen()) {
            try {
                old.close();
            } catch (IOException e) {
                log.warn("关闭旧 WebSocket 连接失败: userId={}", userId);
            }
        }
        log.info("WebSocket 连接建立: userId={}, 当前在线: {}", userId, sessions.size());
    }

    public void removeSession(Long userId) {
        sessions.remove(userId);
        log.info("WebSocket 连接断开: userId={}, 当前在线: {}", userId, sessions.size());
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 向指定用户推送消息
     */
    public boolean sendToUser(Long userId, String jsonMessage) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            return false;
        }
        try {
            session.sendMessage(new TextMessage(jsonMessage));
            return true;
        } catch (IOException e) {
            log.warn("WebSocket 推送失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }
}
