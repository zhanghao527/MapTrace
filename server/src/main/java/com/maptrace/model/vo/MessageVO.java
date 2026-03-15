package com.maptrace.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class MessageVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromUserId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toUserId;
    private String content;
    private String msgType;
    private Integer readStatus;
    private String createTime;
    private String fromNickname;
    private String fromAvatarUrl;
}
