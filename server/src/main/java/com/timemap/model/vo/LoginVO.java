package com.timemap.model.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private Boolean isNew;
    private Boolean needPhone;
    private Boolean needProfile;

    public static LoginVO of(String token, Long userId, Boolean isNew,
                             Boolean needPhone, Boolean needProfile) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(userId);
        vo.setIsNew(isNew);
        vo.setNeedPhone(needPhone);
        vo.setNeedProfile(needProfile);
        return vo;
    }
}
