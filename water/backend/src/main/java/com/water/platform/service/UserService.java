package com.water.platform.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.constant.UserRole;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.dto.req.UserCreateReq;
import com.water.platform.model.dto.req.UserLoginReq;
import com.water.platform.model.dto.resp.UserLoginResp;
import com.water.platform.model.dto.resp.UserResp;
import com.water.platform.model.entity.User;
import com.water.platform.utils.BeanConvertUtils;
import com.water.platform.utils.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * @author ：devon
 * @date ：2024/10/14 9:23
 * @description：user业务逻辑类
 * @version: 1.0
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Client client;

    @PostConstruct
    private void init() {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, "admin")
                .eq(User::getUserRole, UserRole.ADMIN));
        if (count == 0) {
            createRoleUser("admin", "123456", "管理员", UserRole.ADMIN);
            Long adminCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, "admin")
                    .eq(User::getUserRole, UserRole.ADMIN));
            if (adminCount < 1) {
                System.exit(1);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<UserLoginResp> create(UserCreateReq userCreateReq, boolean isSystemHandle) {
        if (isSystemHandle) {
            createRoleUserIfAbsent("admin", "123456", "管理员", UserRole.ADMIN);
            return null;
        }

        String account = StringUtils.trimToEmpty(userCreateReq.getUserAccount());
        String password = StringUtils.defaultIfBlank(userCreateReq.getUserPassword(), "123456");
        String role = "admin".equalsIgnoreCase(account) ? UserRole.ADMIN : normalizeUserRole(userCreateReq.getUserRole());
        ThrowUtils.throwIf(StringUtils.isAnyBlank(account, password), ErrorCode.PARAMS_ERROR, "账号和密码不能为空");

        synchronized (account.intern()) {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, account)
                    .eq(User::getUserRole, role));
            if (user == null) {
                user = createRoleUser(account, password, defaultUserName(userCreateReq.getUserName(), role), role);
            }
            ThrowUtils.throwIf(!StringUtils.equals(user.getUserPassword(), password), ErrorCode.PARAMS_ERROR, "账号或密码错误");
            return doLogin(user, role);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> register(UserCreateReq userCreateReq) {
        String account = StringUtils.trimToEmpty(userCreateReq.getUserAccount());
        String password = StringUtils.trimToEmpty(userCreateReq.getUserPassword());
        String role = normalizeUserRole(userCreateReq.getUserRole());

        ThrowUtils.throwIf(StringUtils.isAnyBlank(account, password), ErrorCode.PARAMS_ERROR, "账号和密码不能为空");
        ThrowUtils.throwIf("admin".equalsIgnoreCase(account) || UserRole.ADMIN.equals(role), ErrorCode.NO_AUTH_ERROR, "管理员账号仅支持系统默认 admin 登录");

        synchronized (account.intern()) {
            User existed = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, account)
                    .eq(User::getUserRole, role));
            ThrowUtils.throwIf(ObjectUtil.isNotNull(existed), ErrorCode.OPERATION_ERROR, "当前角色账号已存在，请直接登录");
            createRoleUser(account, password, defaultUserName(userCreateReq.getUserName(), role), role);
        }

        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<UserLoginResp> getCurrentUserInfo(String token) {
        return ResultUtils.success(new UserLoginResp(
                BeanConvertUtils.convertTo(TokenUtil.getLoginUser(token), UserResp::new),
                TokenUtil.getToken()));
    }

    public BaseResponse<UserLoginResp> waterUserLogin(UserLoginReq userLoginReq) {
        String account = StringUtils.trimToEmpty(userLoginReq.getUserAccount());
        String password = StringUtils.trimToEmpty(userLoginReq.getUserPassword());
        ThrowUtils.throwIf(StringUtils.isAnyBlank(account, password), ErrorCode.PARAMS_ERROR, "账号和密码不能为空");

        if ("admin".equalsIgnoreCase(account)) {
            User adminUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, account)
                    .eq(User::getUserPassword, password)
                    .eq(User::getUserRole, UserRole.ADMIN));
            ThrowUtils.throwIf(ObjectUtil.isNull(adminUser), ErrorCode.NOT_FOUND_ERROR, "管理员账号或密码错误");
            return doLogin(adminUser, UserRole.ADMIN);
        }

        Integer loginType = userLoginReq.getLoginType();
        ThrowUtils.throwIf(ObjectUtil.isNull(loginType), ErrorCode.PARAMS_ERROR, "请选择登录端口");
        String role = loginType == 1 ? UserRole.MANAGER : UserRole.COMPANY;
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, account)
                .eq(User::getUserPassword, password)
                .eq(User::getUserRole, role));
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_FOUND_ERROR, "账号、密码或角色不匹配");
        return doLogin(user, role);
    }

    public List<User> selectUserListById(Long userId) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId));
    }

    public BaseResponse<Boolean> logout(String token) {
        StpUtil.logoutByTokenValue(token);
        return ResultUtils.success(Boolean.TRUE);
    }

    private BaseResponse<UserLoginResp> doLogin(User user, String role) {
        StpUtil.login(user.getUserId(), role);
        UserLoginResp userLoginResp = new UserLoginResp();
        UserResp userResp = new UserResp();
        BeanUtils.copyProperties(user, userResp);
        userLoginResp.setUserResp(userResp);
        userLoginResp.setToken(StpUtil.getTokenInfo().getTokenValue());
        return ResultUtils.success(userLoginResp);
    }

    private void createRoleUserIfAbsent(String account, String password, String userName, String role) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, account)
                .eq(User::getUserRole, role));
        if (ObjectUtil.isNull(user)) {
            createRoleUser(account, password, userName, role);
        }
    }

    private User createRoleUser(String account, String password, String userName, String role) {
        User newUser = new User();
        newUser.setUserId(IdUtil.getSnowflakeNextId());
        newUser.setUserAccount(account);
        newUser.setUserPassword(password);
        newUser.setUserName(defaultUserName(userName, role));
        newUser.setUserRole(role);
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
        newUser.setAccountAddress(keyPair.getAddress());
        newUser.setPrivateKey(keyPair.getHexPrivateKey());
        userMapper.insert(newUser);
        return newUser;
    }

    private String defaultUserName(String userName, String role) {
        if (StringUtils.isNotBlank(userName)) {
            return userName;
        }
        if (UserRole.ADMIN.equals(role)) {
            return "管理员";
        }
        if (UserRole.MANAGER.equals(role)) {
            return "环保监管局";
        }
        return "养殖户";
    }

    private String normalizeUserRole(String userRole) {
        if (StringUtils.isBlank(userRole)) {
            return UserRole.COMPANY;
        }
        if (UserRole.ADMIN.equalsIgnoreCase(userRole) || "administrator".equalsIgnoreCase(userRole)) {
            return UserRole.ADMIN;
        }
        if (UserRole.MANAGER.equalsIgnoreCase(userRole)
                || "monitor".equalsIgnoreCase(userRole)
                || "regulator".equalsIgnoreCase(userRole)) {
            return UserRole.MANAGER;
        }
        return UserRole.COMPANY;
    }
}
