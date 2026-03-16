package com.maptrace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maptrace.common.Result;
import com.maptrace.model.entity.User;
import com.maptrace.mapper.UserMapper;
import com.maptrace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (Boolean.TRUE.equals(request.getAttribute("isWebAdmin"))) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未提供Token");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            writeUnauthorized(response, "Token无效或已过期");
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        // 校验用户是否存在，不存在则返回40100让前端清除token重新登录
        User user = userMapper.selectById(userId);
        if (user == null) {
            writeUnauthorized(response, "用户不存在，请重新登录");
            return false;
        }

        request.setAttribute("userId", userId);
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(com.maptrace.common.ErrorCode.NOT_LOGIN_ERROR, message)));
    }
}
