package com.maptrace.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class AdminAccountVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String nickname;
    private String role;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long linkedUserId;
    private Boolean isEnabled;
    private String lastLoginTime;
    private String lastLoginIp;
    private String createTime;
}
