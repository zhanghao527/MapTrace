package com.maptrace.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class AdminLogVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adminUserId;
    private String adminNickname;
    private String action;
    private String targetType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    private String detail;
    private String createTime;
}
