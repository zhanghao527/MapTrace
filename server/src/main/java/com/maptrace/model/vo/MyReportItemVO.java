package com.maptrace.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class MyReportItemVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String targetType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    private String reason;
    private Integer status;
    private String handleResult;
    private String createTime;
    private String handledTime;
    private String targetPreview;
    private String targetImageUrl;
}
