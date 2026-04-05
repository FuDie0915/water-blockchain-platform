package com.water.platform.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.dto.req.UserCreateReq;
import com.water.platform.model.dto.req.UserLoginReq;
import com.water.platform.model.dto.resp.UserLoginResp;
import com.water.platform.model.dto.resp.UserResp;
import com.water.platform.model.entity.User;
import com.water.platform.utils.BeanConvertUtils;
import com.water.platform.utils.TokenUtil;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.constant.UserRole;
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
    @Autowired
    private UserService proxy;

    @PostConstruct
    private void init() {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, "admin"));
        if (count == 0) {
            proxy.create(new UserCreateReq("admin", "管理员"), true);
            if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, "admin")) != 3) {
                System.exit(1);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<UserLoginResp> create(UserCreateReq userCreateReq, boolean isSystemHandle) {
        synchronized (userCreateReq.getUserAccount().intern()) {
            User user = BeanConvertUtils.convertTo(userCreateReq, User::new);
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserAccount, user.getUserAccount())
                    .eq(User::getUserRole, UserRole.ADMIN));
            if (user == null) {
                Long userId = IdUtil.getSnowflakeNextId(); //自动生成
                User newUser = new User();
                newUser.setUserId(userId);
                newUser.setUserAccount(userCreateReq.getUserAccount());
                newUser.setUserPassword("123456"); // 默认密码
                newUser.setUserName(userCreateReq.getUserName());
                newUser.setUserRole(UserRole.ADMIN);
                CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
                newUser.setAccountAddress(keyPair.getAddress()); // 无人机区块链账户地址
                newUser.setPrivateKey(keyPair.getHexPrivateKey()); // 无人机区块链账户私钥
                userMapper.insert(newUser);
                newUser.setUserRole(UserRole.COMPANY);
                newUser.setUserName("养殖户");
                keyPair = client.getCryptoSuite().createKeyPair();
                newUser.setAccountAddress(keyPair.getAddress()); // 企业区块链账户地址
                newUser.setPrivateKey(keyPair.getHexPrivateKey()); // 企业区块链账户私钥
                userMapper.insert(newUser);
                newUser.setUserName("环保监管局");
                newUser.setUserRole(UserRole.MANAGER);
                keyPair = client.getCryptoSuite().createKeyPair();
                newUser.setAccountAddress(keyPair.getAddress()); // 监管局区块链账户地址
                newUser.setPrivateKey(keyPair.getHexPrivateKey()); // 监管局区块链账户私钥
                userMapper.insert(newUser);
                user = newUser;
            }
            if (!isSystemHandle) {
                StpUtil.login(user.getUserId(), UserRole.ADMIN);
                UserLoginResp userLoginResp = new UserLoginResp();
                UserResp userResp = new UserResp();
                BeanUtils.copyProperties(user, userResp);
                userLoginResp.setUserResp(userResp);
                userLoginResp.setToken(StpUtil.getTokenInfo().getTokenValue());
                return ResultUtils.success(userLoginResp);
            }
            return null;
        }
    }

    public BaseResponse<UserLoginResp> getCurrentUserInfo(String token) {
        return ResultUtils.success(new UserLoginResp(
                BeanConvertUtils.convertTo(TokenUtil.getLoginUser(token), UserResp::new),
                TokenUtil.getToken()));
    }

    public BaseResponse<UserLoginResp> waterUserLogin(UserLoginReq userLoginReq) {
        String account = userLoginReq.getUserAccount();
        String password = userLoginReq.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isAnyBlank(account, password), ErrorCode.PARAMS_ERROR);
        Integer loginType = userLoginReq.getLoginType();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userLoginReq.getUserAccount())
                .eq(User::getUserPassword, userLoginReq.getUserPassword())
                .eq(loginType == 0, User::getUserRole, UserRole.COMPANY)
                .eq(loginType == 1, User::getUserRole, UserRole.MANAGER)
        );
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_FOUND_ERROR);
        String role = loginType == 0 ? UserRole.COMPANY : UserRole.MANAGER;
        StpUtil.login(user.getUserId(), role);

        UserLoginResp userLoginResp = new UserLoginResp();
        UserResp userResp = new UserResp();
        BeanUtils.copyProperties(user, userResp);
        userLoginResp.setUserResp(userResp);
        userLoginResp.setToken(StpUtil.getTokenInfo().getTokenValue());
        return ResultUtils.success(userLoginResp);
    }

    public List<User> selectUserListById(Long userId) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId));
    }

    public BaseResponse<Boolean> logout(String token) {
        StpUtil.logoutByTokenValue(token);
        return ResultUtils.success(Boolean.TRUE);
    }
}
