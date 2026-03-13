package com.timemap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timemap.common.BusinessException;
import com.timemap.common.ErrorCode;
import com.timemap.mapper.UserMapper;
import com.timemap.model.dto.UpdateProfileRequest;
import com.timemap.model.vo.UserInfoVO;
import com.timemap.model.entity.User;
import com.timemap.service.AdminAuthService;
import com.timemap.service.CosService;
import com.timemap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final AdminAuthService adminAuthService;
    private final CosService cosService;

    @Override
    public UserInfoVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getProvince() != null) {
            user.setProvince(request.getProvince());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        user.setProfileCompleted(isProfileComplete(user) ? 1 : 0);

        this.updateById(user);

        return buildResponse(user);
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return buildResponse(user);
    }

    @Override
    public UserInfoVO uploadAvatar(Long userId, MultipartFile file) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String avatarUrl = cosService.upload(file);
        user.setAvatarUrl(avatarUrl);
        user.setProfileCompleted(isProfileComplete(user) ? 1 : 0);
        this.updateById(user);
        return buildResponse(user);
    }

    @Override
    public void checkBanned(Long userId) {
        User user = this.getById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (user.getIsBanned() != null && user.getIsBanned() == 1) {
            throw new BusinessException(ErrorCode.USER_BANNED, "账号已被封禁，无法执行此操作");
        }
    }

    @Override
    public void checkMuted(Long userId) {
        User user = this.getById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (user.getIsBanned() != null && user.getIsBanned() == 1) {
            throw new BusinessException(ErrorCode.USER_BANNED, "账号已被封禁，无法执行此操作");
        }
        if (user.getMuteUntil() != null && user.getMuteUntil().isAfter(LocalDateTime.now())) {
            long days = Duration.between(LocalDateTime.now(), user.getMuteUntil()).toDays() + 1;
            throw new BusinessException(ErrorCode.USER_MUTED, "你已被禁言，剩余 " + days + " 天，期间无法评论和发送私信");
        }
    }

    @Override
    public void checkBanUpload(Long userId) {
        User user = this.getById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (user.getIsBanned() != null && user.getIsBanned() == 1) {
            throw new BusinessException(ErrorCode.USER_BANNED, "账号已被封禁，无法执行此操作");
        }
        if (user.getBanUploadUntil() != null && user.getBanUploadUntil().isAfter(LocalDateTime.now())) {
            long days = Duration.between(LocalDateTime.now(), user.getBanUploadUntil()).toDays() + 1;
            throw new BusinessException(ErrorCode.USER_UPLOAD_BANNED, "你已被禁止上传照片，剩余 " + days + " 天");
        }
    }

    private UserInfoVO buildResponse(User user) {
        UserInfoVO response = UserInfoVO.from(user);
        response.setIsAdmin(adminAuthService.isAdmin(user.getId()));
        return response;
    }

    private boolean isProfileComplete(User user) {
        return user.getNickname() != null && !user.getNickname().isBlank()
                && user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank();
    }
}
