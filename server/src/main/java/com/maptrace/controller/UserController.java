package com.maptrace.controller;

import com.maptrace.common.ErrorCode;
import com.maptrace.common.Result;
import com.maptrace.common.ThrowUtils;
import com.maptrace.model.dto.UpdateProfileRequest;
import com.maptrace.model.vo.UserInfoVO;
import com.maptrace.service.UserService;
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
