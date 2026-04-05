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
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String token;
        if (UserRole.ADMIN.equals(authCheck.roleType())) {
            token = request.getHeader("satoken");
        } else if (UserRole.COMPANY.equals(authCheck.roleType())) {
            token = request.getHeader("companytoken");
        } else {
            token = request.getHeader("managertoken");
        }
        ThrowUtils.throwIf(StringUtils.isBlank(token), ErrorCode.HEADER_PARAMS_ERROR);
        Object userIdByToken = StpUtil.getLoginIdByToken(token);
        ThrowUtils.throwIf(Objects.isNull(userIdByToken), ErrorCode.NOT_LOGIN_ERROR);
        String role = StpUtil.getLoginDeviceByToken(token);
        ThrowUtils.throwIf(!authCheck.roleType().equals(role), ErrorCode.NO_AUTH_ERROR);
        // 通过权限校验，放行
        return joinPoint.proceed();
    }


//    /**
//     * 执行拦截
//     *
//     * @param joinPoint
//     * @param authCheck
//     * @return
//     */
//    @Around("@annotation(authCheck)")
//    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
//        String mustRole = authCheck.mustRole();
//        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
//        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
//        // 当前登录用户
//        OldUser loginUser = userService.getLoginUser(request);
//        RoleTypeEnum mustRoleEnum = RoleTypeEnum.getEnumByValue(mustRole);
//        // 不需要权限，放行
//        if (mustRoleEnum == null) {
//            return joinPoint.proceed();
//        }
//        // 必须有该权限才通过
//        RoleTypeEnum userRoleEnum = RoleTypeEnum.getEnumByValue(loginUser.getUserRole());
//        if (userRoleEnum == null) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        // 如果被封号，直接拒绝
//        if (RoleTypeEnum.BAN.equals(userRoleEnum)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        // 必须有管理员权限
//        if (RoleTypeEnum.ADMIN.equals(mustRoleEnum)) {
//            // 用户没有管理员权限，拒绝
//            if (!RoleTypeEnum.ADMIN.equals(userRoleEnum)) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
//        }
//        // 通过权限校验，放行
//        return joinPoint.proceed();
//    }
}

