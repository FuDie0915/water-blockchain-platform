package com.water.platform.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author ：devon
 * @date ：2024/10/12 14:39
 * @description：token工具类
 * @version: 1.0
 */
@Component
public class TokenUtil {

    private static UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        TokenUtil.userMapper = userMapper;
    }

    public static String getToken() {
        return StpUtil.getTokenInfo().getTokenValue();
    }

    public static User getLoginUser() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String token = request.getHeader("satoken");
        ThrowUtils.throwIf(StringUtils.isBlank(token), ErrorCode.HEADER_PARAMS_ERROR);
        Object userIdByToken = StpUtil.getLoginIdByToken(token);
        ThrowUtils.throwIf(Objects.isNull(userIdByToken), ErrorCode.NOT_LOGIN_ERROR);
        Long userId = Long.valueOf(userIdByToken.toString());
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId));
    }

    public static User getLoginUser(String token) {
        ThrowUtils.throwIf(StringUtils.isBlank(token), ErrorCode.HEADER_PARAMS_ERROR);
        Object userIdByToken = StpUtil.getLoginIdByToken(token);
        ThrowUtils.throwIf(Objects.isNull(userIdByToken), ErrorCode.NOT_LOGIN_ERROR);
        Long userId = Long.valueOf(userIdByToken.toString());
        String role = StpUtil.getLoginDeviceByToken(token);
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId)
                .eq(User::getUserRole, role));
    }

    public static Long getLoginUserId() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String token = request.getHeader("satoken");
        ThrowUtils.throwIf(StringUtils.isBlank(token), ErrorCode.HEADER_PARAMS_ERROR);
        Object userIdByToken = StpUtil.getLoginIdByToken(token);
        ThrowUtils.throwIf(Objects.isNull(userIdByToken), ErrorCode.NOT_LOGIN_ERROR);
        return Long.valueOf(userIdByToken.toString());
    }
}
