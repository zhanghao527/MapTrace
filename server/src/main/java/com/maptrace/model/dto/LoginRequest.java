package com.maptrace.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "code不能为空")
    private String code;

    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private String country;
    private String province;
    private String city;
}
