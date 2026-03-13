package com.timemap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.timemap.model.dto.UpdateProfileRequest;
import com.timemap.model.vo.UserInfoVO;
import com.timemap.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    UserInfoVO updateProfile(Long userId, UpdateProfileRequest request);

    UserInfoVO getUserInfo(Long userId);

    UserInfoVO uploadAvatar(Long userId, MultipartFile file);

    /**
     * 检查用户是否被封禁（isBanned=1），若是则抛出异常
     */
    void checkBanned(Long userId);

    /**
     * 检查用户是否被禁言（muteUntil 在当前时间之后），若是则抛出异常
     */
    void checkMuted(Long userId);

    /**
     * 检查用户是否被禁止上传（banUploadUntil 在当前时间之后），若是则抛出异常
     */
    void checkBanUpload(Long userId);

}
