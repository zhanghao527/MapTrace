package com.timemap.service;

import com.timemap.model.dto.LoginRequest;
import com.timemap.model.vo.LoginVO;
import com.timemap.model.vo.BindPhoneVO;

public interface AuthService {

    LoginVO login(LoginRequest request);

    BindPhoneVO bindPhone(Long userId, String code);
}
