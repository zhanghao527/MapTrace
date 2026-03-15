package com.maptrace.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String phoneMasked;
    private Boolean phoneBound;
    private Boolean needPhone;
    private Boolean needProfile;
    private Integer gender;
    private String country;
    private String province;
    private String city;
    private Boolean profileCompleted;
    private Boolean isAdmin;
    private LocalDateTime createTime;
    private LocalDateTime muteUntil;
    private LocalDateTime banUploadUntil;
    private Boolean isBanned;
    private Integer violationCount;

    public static UserInfoVO from(com.maptrace.model.entity.User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhoneMasked(maskPhone(user.getPhone()));
        vo.setPhoneBound(user.getPhone() != null && !user.getPhone().isBlank());
        vo.setNeedPhone(user.getPhone() == null || user.getPhone().isBlank());
        vo.setNeedProfile(user.getNickname() == null || user.getNickname().isBlank()
                || user.getAvatarUrl() == null || user.getAvatarUrl().isBlank());
        vo.setGender(user.getGender());
        vo.setCountry(user.getCountry());
        vo.setProvince(user.getProvince());
        vo.setCity(user.getCity());
        vo.setProfileCompleted(user.getProfileCompleted() != null && user.getProfileCompleted() == 1);
        vo.setCreateTime(user.getCreateTime());
        vo.setMuteUntil(user.getMuteUntil());
        vo.setBanUploadUntil(user.getBanUploadUntil());
        vo.setIsBanned(user.getIsBanned() != null && user.getIsBanned() == 1);
        vo.setViolationCount(user.getViolationCount() != null ? user.getViolationCount() : 0);
        return vo;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
