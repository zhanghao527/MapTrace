package com.timemap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信订阅消息模板 ID 配置
 * 在 application.yml 中配置:
 * subscribe-message:
 *   interaction-template-id: xxx  # 互动提醒模板
 *   report-template-id: xxx       # 举报结果模板
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "subscribe-message")
public class SubscribeMessageConfig {

    /** 互动提醒模板ID（评论、回复、点赞） */
    private String interactionTemplateId = "";

    /** 举报结果通知模板ID */
    private String reportTemplateId = "";
}
