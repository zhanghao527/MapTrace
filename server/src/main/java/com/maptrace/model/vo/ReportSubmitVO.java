package com.maptrace.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ReportSubmitVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;
    private Integer status;
}
