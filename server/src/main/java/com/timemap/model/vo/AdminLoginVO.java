package com.timemap.model.vo;

import lombok.Data;

@Data
public class AdminLoginVO {
    private String token;
    private String role;
    private String nickname;
    private Long adminId;
    private Boolean mustChangePassword;
}
