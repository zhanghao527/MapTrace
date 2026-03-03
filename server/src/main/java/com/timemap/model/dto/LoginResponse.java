package com.timemap.model.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private Long userId;
    private Boolean isNew;

    public static LoginResponse of(String token, Long userId, Boolean isNew) {
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(userId);
        resp.setIsNew(isNew);
        return resp;
    }
}
