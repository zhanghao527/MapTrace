package com.maptrace.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class AdminLoginVO {
    private String token;
    private String role;
    private String nickname;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adminId;
    private Boolean mustChangePassword;
}
