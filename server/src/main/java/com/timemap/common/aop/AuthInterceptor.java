package com.timemap.common.aop;

import com.timemap.common.BusinessException;
import com.timemap.common.ErrorCode;
import com.timemap.common.annotation.AuthCheck;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AuthCheck 注解 AOP 拦截器
 * 适配项目 JWT 认证机制：通过 request attribute 获取用户/管理员信息
 */
@Aspect
@Component
public class AuthInterceptor {

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前登录用户 ID（由 JwtInterceptor 或 AdminWebAuthInterceptor 设置）
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 如果不需要特定角色，仅校验登录即可
        if (mustRole == null || mustRole.isEmpty()) {
            return joinPoint.proceed();
        }

        // 角色校验：admin 角色需要 isWebAdmin 标记 + adminRole 匹配
        Boolean isWebAdmin = (Boolean) request.getAttribute("isWebAdmin");
        String adminRole = (String) request.getAttribute("adminRole");

        switch (mustRole) {
            case "admin":
                // 任意管理员角色
                if (!Boolean.TRUE.equals(isWebAdmin)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
                break;
            case "super_admin":
                // 超级管理员
                if (!Boolean.TRUE.equals(isWebAdmin) || !"super_admin".equals(adminRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
                break;
            case "moderator":
                // 审核员或超级管理员
                if (!Boolean.TRUE.equals(isWebAdmin)
                        || (!"moderator".equals(adminRole) && !"super_admin".equals(adminRole))) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
                break;
            default:
                // 自定义角色：精确匹配
                if (!Boolean.TRUE.equals(isWebAdmin) || !mustRole.equals(adminRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
                break;
        }

        return joinPoint.proceed();
    }
}
