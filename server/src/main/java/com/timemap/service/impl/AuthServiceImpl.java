package com.timemap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timemap.mapper.UserMapper;
import com.timemap.model.dto.LoginResponse;
import com.timemap.model.dto.WxSessionResponse;
import com.timemap.model.entity.User;
import com.timemap.service.AuthService;
import com.timemap.util.JwtUtil;
import com.timemap.util.WxApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final WxApiUtil wxApiUtil;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(String code) {
        // 1. 调用微信接口获取 openid
        WxSessionResponse wxResp = wxApiUtil.code2Session(code);
        String openid = wxResp.getOpenid();

        // 2. 查询或创建用户
        boolean isNew = false;
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid)
        );

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("");
            user.setAvatarUrl("");
            userMapper.insert(user);
            isNew = true;
        }

        // 3. 签发 JWT Token
        String token = jwtUtil.generateToken(user.getId(), openid);

        return LoginResponse.of(token, user.getId(), isNew);
    }
}
