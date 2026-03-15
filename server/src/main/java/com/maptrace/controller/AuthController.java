package com.maptrace.controller;

import com.maptrace.common.Result;
import com.maptrace.model.dto.BindPhoneRequest;
import com.maptrace.model.vo.BindPhoneVO;
import com.maptrace.model.dto.LoginRequest;
import com.maptrace.model.vo.LoginVO;
import com.maptrace.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/bind-phone")
    public Result<BindPhoneVO> bindPhone(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BindPhoneRequest request) {
        return Result.success(authService.bindPhone(userId, request.getCode()));
    }
}
