package com.timemap.service;

import com.timemap.model.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(String code);
}
