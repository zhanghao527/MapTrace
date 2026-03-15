package com.maptrace.service;

import com.maptrace.model.dto.LoginRequest;
import com.maptrace.model.vo.LoginVO;
import com.maptrace.model.vo.BindPhoneVO;

public interface AuthService {

    LoginVO login(LoginRequest request);

    BindPhoneVO bindPhone(Long userId, String code);
}
