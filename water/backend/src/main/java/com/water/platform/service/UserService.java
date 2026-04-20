package com.water.platform.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.dto.req.AdminCreateUserReq;
import com.water.platform.model.dto.req.AdminUserUpdateReq;
import com.water.platform.model.dto.req.BatchStatusUpdateReq;
import com.water.platform.model.dto.req.BatchUserIdsReq;
import com.water.platform.model.dto.req.FarmerProfileReq;
import com.water.platform.model.dto.req.ManagerProfileReq;
import com.water.platform.model.dto.req.UserLoginReq;
import com.water.platform.model.dto.req.UserProfileUpdateReq;
import com.water.platform.model.dto.req.UserRegisterReq;
import com.water.platform.model.dto.req.UserStatusUpdateReq;
import com.water.platform.model.dto.resp.UserLoginResp;
import com.water.platform.model.dto.resp.UserResp;
import com.water.platform.model.entity.User;
import com.water.platform.utils.BeanConvertUtils;
import com.water.platform.utils.ImageCaptchaUtil;
import com.water.platform.utils.PasswordUtil;
import com.water.platform.utils.TokenUtil;
import com.water.platform.constant.UserRole;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
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
    @Autowired(required = false)
    private Client client;
    @Autowired
    private com.water.platform.chain.config.SystemConfig systemConfig;

    /**
     * 检查区块链是否启用
     */
    public boolean isBlockchainEnabled() {
        return client != null && systemConfig.isEnabled();
    }

    /**
     * 系统初始化：自动创建管理员账户 admin/123456
     */
    @PostConstruct
    private void init() {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, "admin")
                .eq(User::getUserRole, UserRole.ADMIN));
        if (count == 0) {
            User admin = new User();
            admin.setUserId(IdUtil.getSnowflakeNextId());
            admin.setUserAccount("admin");
            admin.setUserPassword(PasswordUtil.encrypt("123456"));
            admin.setUserName("管理员");
            admin.setUserRole(UserRole.ADMIN);
            if (isBlockchainEnabled()) {
                CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
                admin.setAccountAddress(keyPair.getAddress());
                admin.setPrivateKey(keyPair.getHexPrivateKey());
            } else {
                admin.setAccountAddress("0x0000000000000000000000000000000000000000");
                admin.setPrivateKey("");
            }
            admin.setUserStatus(1);
            userMapper.insert(admin);
        }
    }

    // ==================== 注册 ====================

    /**
     * 养殖户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<UserLoginResp> registerFarmers(UserRegisterReq req) {
        return registerRole(req, UserRole.FARMERS, "养殖户");
    }

    /**
     * 监管局注册
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<UserLoginResp> registerManager(UserRegisterReq req) {
        return registerRole(req, UserRole.MANAGER, "监管局");
    }

    /**
     * 通用注册逻辑
     */
    private BaseResponse<UserLoginResp> registerRole(UserRegisterReq req, String role, String displayName) {
        // 验证码校验
        ThrowUtils.throwIf(!ImageCaptchaUtil.verify(req.getCaptchaKey(), req.getCaptchaCode()),
                ErrorCode.PARAMS_ERROR, "验证码错误或已过期");

        synchronized (req.getUserAccount().intern()) {
            // 检查该账号是否已注册任何角色
            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, req.getUserAccount()));
            ThrowUtils.throwIf(existing != null, ErrorCode.PARAMS_ERROR, "该账号已注册");

            // 创建用户
            User newUser = new User();
            newUser.setUserId(IdUtil.getSnowflakeNextId());
            newUser.setUserAccount(req.getUserAccount());
            newUser.setUserPassword(PasswordUtil.encrypt(req.getUserPassword()));
            newUser.setUserName(displayName);
            newUser.setUserRole(role);
            // 监管局注册后需要管理员审核，养殖户直接启用
            newUser.setUserStatus(UserRole.MANAGER.equals(role) ? 0 : 1);
            if (isBlockchainEnabled()) {
                CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
                newUser.setAccountAddress(keyPair.getAddress());
                newUser.setPrivateKey(keyPair.getHexPrivateKey());
            } else {
                newUser.setAccountAddress("0x0000000000000000000000000000000000000000");
                newUser.setPrivateKey("");
            }
            newUser.setExtInfo(req.getExtInfo());
            userMapper.insert(newUser);

            // 注册后自动登录
            StpUtil.login(newUser.getUserId(), role);
            UserLoginResp resp = new UserLoginResp();
            UserResp userResp = new UserResp();
            BeanUtils.copyProperties(newUser, userResp);
            resp.setUserResp(userResp);
            resp.setToken(StpUtil.getTokenInfo().getTokenValue());
            return ResultUtils.success(resp);
        }
    }

    // ==================== 登录 ====================

    /**
     * 管理员登录
     */
    public BaseResponse<UserLoginResp> loginAdmin(UserLoginReq req) {
        return loginRole(req, UserRole.ADMIN);
    }

    /**
     * 养殖户登录
     */
    public BaseResponse<UserLoginResp> loginFarmers(UserLoginReq req) {
        return loginRole(req, UserRole.FARMERS);
    }

    /**
     * 监管局登录
     */
    public BaseResponse<UserLoginResp> loginManager(UserLoginReq req) {
        return loginRole(req, UserRole.MANAGER);
    }

    /**
     * 通用登录逻辑
     */
    private BaseResponse<UserLoginResp> loginRole(UserLoginReq req, String role) {
        String account = req.getUserAccount();
        String password = req.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isAnyBlank(account, password), ErrorCode.PARAMS_ERROR);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, account)
                .eq(User::getUserRole, role));
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_FOUND_ERROR, "账号或密码错误");
        ThrowUtils.throwIf(!PasswordUtil.verify(password, user.getUserPassword()), ErrorCode.NOT_FOUND_ERROR, "账号或密码错误");
        // 自动迁移：如果密码是明文，登录成功后自动加密存储
        if (!PasswordUtil.isEncrypted(user.getUserPassword())) {
            user.setUserPassword(PasswordUtil.encrypt(password));
            userMapper.updateById(user);
        }
        ThrowUtils.throwIf(user.getUserStatus() != null && user.getUserStatus() == 0, ErrorCode.PARAMS_ERROR, "账号已被禁用");
        StpUtil.login(user.getUserId(), role);
        UserLoginResp resp = new UserLoginResp();
        UserResp userResp = new UserResp();
        BeanUtils.copyProperties(user, userResp);
        resp.setUserResp(userResp);
        resp.setToken(StpUtil.getTokenInfo().getTokenValue());
        return ResultUtils.success(resp);
    }

    // ==================== 用户管理 ====================

    /**
     * 用户修改自己的信息（姓名、密码）
     */
    public BaseResponse<Boolean> updateProfile(UserProfileUpdateReq req) {
        User currentUser = TokenUtil.getLoginUser();
        if (StringUtils.isNotBlank(req.getUserName())) {
            currentUser.setUserName(req.getUserName());
        }
        if (StringUtils.isNotBlank(req.getUserPassword())) {
            ThrowUtils.throwIf(StringUtils.isBlank(req.getOldPassword()), ErrorCode.PARAMS_ERROR, "修改密码需要提供旧密码");
            ThrowUtils.throwIf(!PasswordUtil.verify(req.getOldPassword(), currentUser.getUserPassword()), ErrorCode.PARAMS_ERROR, "旧密码错误");
            currentUser.setUserPassword(PasswordUtil.encrypt(req.getUserPassword()));
        }
        if (req.getExtInfo() != null) {
            String role = currentUser.getUserRole();
            if (UserRole.FARMERS.equals(role)) {
                com.alibaba.fastjson.JSONObject.parseObject(req.getExtInfo(), FarmerProfileReq.class);
                currentUser.setExtInfo(req.getExtInfo());
            } else if (UserRole.MANAGER.equals(role)) {
                com.alibaba.fastjson.JSONObject.parseObject(req.getExtInfo(), ManagerProfileReq.class);
                currentUser.setExtInfo(req.getExtInfo());
            } else {
                currentUser.setExtInfo(req.getExtInfo());
            }
        }
        userMapper.updateById(currentUser);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 管理员分页查看所有用户
     */
    public BaseResponse<PageResponse<UserResp>> adminListUsers(long pageNum, long pageSize) {
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<User>()
                .orderByDesc(User::getUserId));
        PageResponse<UserResp> pageResponse = new PageResponse<>(0, new ArrayList<>());
        BeanUtils.copyProperties(page, pageResponse);
        pageResponse.setData(BeanConvertUtils.convertListTo(page.getRecords(), UserResp::new));
        return ResultUtils.success(pageResponse);
    }

    /**
     * 管理员修改用户状态（启用/禁用）
     */
    public BaseResponse<Boolean> adminUpdateStatus(UserStatusUpdateReq req) {
        ThrowUtils.throwIf(req.getUserStatus() != 0 && req.getUserStatus() != 1, ErrorCode.PARAMS_ERROR, "userStatus 必须为 0 或 1");
        User user = userMapper.selectById(req.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        user.setUserStatus(req.getUserStatus());
        userMapper.updateById(user);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 管理员修改用户信息（姓名、账号）
     */
    public BaseResponse<Boolean> adminUpdateUser(AdminUserUpdateReq req) {
        User user = userMapper.selectById(req.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        if (StringUtils.isNotBlank(req.getUserName())) {
            user.setUserName(req.getUserName());
        }
        if (StringUtils.isNotBlank(req.getUserAccount())) {
            // 检查新账号是否被其他用户占用
            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, req.getUserAccount())
                    .ne(User::getUserId, req.getUserId()));
            ThrowUtils.throwIf(existing != null, ErrorCode.PARAMS_ERROR, "该账号已被占用");
            user.setUserAccount(req.getUserAccount());
        }
        if (req.getExtInfo() != null) {
            user.setExtInfo(req.getExtInfo());
        }
        userMapper.updateById(user);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 管理员创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<UserResp> adminCreateUser(AdminCreateUserReq req) {
        String role = req.getUserRole();
        // 校验角色类型
        ThrowUtils.throwIf(!role.equals(UserRole.ADMIN)
                && !role.equals(UserRole.FARMERS)
                && !role.equals(UserRole.MANAGER), ErrorCode.PARAMS_ERROR, "无效的角色类型，必须是 admin/farmers/manager");

        synchronized (req.getUserAccount().intern()) {
            // 检查该账号是否已注册此角色
            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, req.getUserAccount())
                    .eq(User::getUserRole, role));
            ThrowUtils.throwIf(existing != null, ErrorCode.PARAMS_ERROR, "该账号已存在此角色");

            // 创建用户
            User newUser = new User();
            newUser.setUserId(IdUtil.getSnowflakeNextId());
            newUser.setUserAccount(req.getUserAccount());
            newUser.setUserPassword(PasswordUtil.encrypt(req.getUserPassword()));
            newUser.setUserName(req.getUserName());
            newUser.setUserRole(role);
            newUser.setUserStatus(1);
            CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
            newUser.setAccountAddress(keyPair.getAddress());
            newUser.setPrivateKey(keyPair.getHexPrivateKey());
            newUser.setExtInfo(req.getExtInfo());
            userMapper.insert(newUser);

            // 返回用户信息（不自动登录）
            UserResp userResp = new UserResp();
            BeanUtils.copyProperties(newUser, userResp);
            return ResultUtils.success(userResp);
        }
    }

    /**
     * 管理员删除用户
     */
    public BaseResponse<Boolean> adminDeleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(UserRole.ADMIN.equals(user.getUserRole()), ErrorCode.PARAMS_ERROR, "不能删除管理员账户");
        userMapper.deleteById(userId);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 管理员批量修改用户状态
     */
    public BaseResponse<Boolean> batchUpdateStatus(BatchStatusUpdateReq req) {
        ThrowUtils.throwIf(req.getUserStatus() != 0 && req.getUserStatus() != 1, ErrorCode.PARAMS_ERROR, "userStatus 必须为 0 或 1");
        for (Long userId : req.getUserIds()) {
            User user = userMapper.selectById(userId);
            if (user == null || UserRole.ADMIN.equals(user.getUserRole())) {
                continue;
            }
            user.setUserStatus(req.getUserStatus());
            userMapper.updateById(user);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 管理员批量删除用户
     */
    public BaseResponse<Boolean> batchDeleteUsers(BatchUserIdsReq req) {
        for (Long userId : req.getUserIds()) {
            User user = userMapper.selectById(userId);
            if (user == null || UserRole.ADMIN.equals(user.getUserRole())) {
                continue;
            }
            userMapper.deleteById(userId);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    // ==================== 查询 ====================

    /**
     * 获取当前登录用户信息
     */
    public BaseResponse<UserLoginResp> getCurrentUserInfo(String token) {
        return ResultUtils.success(new UserLoginResp(
                BeanConvertUtils.convertTo(TokenUtil.getLoginUser(token), UserResp::new),
                TokenUtil.getToken()));
    }

    /**
     * 按 userId 查询用户列表（兼容旧逻辑，现在每个 userId 只会返回 0 或 1 条）
     */
    public List<User> selectUserListById(Long userId) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId));
    }

    /**
     * 按 userAccount 查询所有角色记录
     */
    public List<User> selectUserListByAccount(String userAccount) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount));
    }

    /**
     * 退出登录
     */
    public BaseResponse<Boolean> logout(String token) {
        StpUtil.logoutByTokenValue(token);
        return ResultUtils.success(Boolean.TRUE);
    }
}
