package com.timemap.controller;

import com.timemap.common.ErrorCode;
import com.timemap.common.Result;
import com.timemap.common.ThrowUtils;
import com.timemap.model.dto.UpdateProfileRequest;
import com.timemap.model.vo.UserInfoVO;
import com.timemap.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/profile")
    public Result<UserInfoVO> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserInfoVO response = userService.updateProfile(userId, request);
        return Result.success(response);
    }

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(@RequestAttribute("userId") Long userId) {
        UserInfoVO response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    @PostMapping("/avatar")
    public Result<UserInfoVO> uploadAvatar(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "请选择头像");
        return Result.success(userService.uploadAvatar(userId, file));
    }
}
