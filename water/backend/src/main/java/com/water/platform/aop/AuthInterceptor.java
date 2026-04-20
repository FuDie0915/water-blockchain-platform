package com.water.platform.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.constant.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
public class AuthInterceptor {
    /**
     * 执行拦截
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 统一从 satoken header 读取 token
        String token = request.getHeader("satoken");
        ThrowUtils.throwIf(StringUtils.isBlank(token), ErrorCode.HEADER_PARAMS_ERROR);
        Object userIdByToken = StpUtil.getLoginIdByToken(token);
        ThrowUtils.throwIf(Objects.isNull(userIdByToken), ErrorCode.NOT_LOGIN_ERROR);
        String role = StpUtil.getLoginDeviceByToken(token);
        ThrowUtils.throwIf(!authCheck.roleType().equals(role), ErrorCode.NO_AUTH_ERROR);
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
