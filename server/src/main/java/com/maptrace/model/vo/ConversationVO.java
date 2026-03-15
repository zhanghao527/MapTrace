package com.maptrace.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ConversationVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String lastMessage;
    private String lastTime;
    private Integer unreadCount;
}
