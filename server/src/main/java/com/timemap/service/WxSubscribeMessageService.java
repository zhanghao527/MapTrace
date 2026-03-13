package com.timemap.service;

import java.util.Map;

/**
 * 微信订阅消息服务
 */
public interface WxSubscribeMessageService {

    /**
     * 记录用户授权订阅（前端调用 wx.requestSubscribeMessage 成功后上报）
     */
    void recordSubscription(Long userId, String templateId);

    /**
     * 尝试发送订阅消息（有额度才发，无额度静默跳过）
     *
     * @param userId     接收用户ID
     * @param templateId 模板ID
     * @param page       点击消息跳转的小程序页面
     * @param data       模板数据 key -> value
     * @return true=发送成功, false=无额度或发送失败
     */
    boolean trySend(Long userId, String templateId, String page, Map<String, String> data);
}
