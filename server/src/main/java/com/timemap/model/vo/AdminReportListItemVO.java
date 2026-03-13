package com.timemap.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class AdminReportListItemVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String targetType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    private String reason;
    private Integer status;
    private String createTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reporterUserId;
    private String reporterNickname;
    private String targetPreview;
    private String targetImageUrl;
}
