package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.AdminCreateUserReq;
import com.water.platform.model.dto.req.AdminUserUpdateReq;
import com.water.platform.model.dto.req.BatchStatusUpdateReq;
import com.water.platform.model.dto.req.BatchUserIdsReq;
import com.water.platform.model.dto.req.UserLoginReq;
import com.water.platform.model.dto.req.UserProfileUpdateReq;
import com.water.platform.model.dto.req.UserRegisterReq;
import com.water.platform.model.dto.req.UserStatusUpdateReq;
import com.water.platform.model.dto.resp.UserLoginResp;
import com.water.platform.model.dto.resp.UserResp;
import com.water.platform.service.ExportService;
import com.water.platform.service.UserService;
import com.water.platform.utils.ImageCaptchaUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author ：devon
 * @date ：2024/10/10 15:50
 * @description：用户接口
 * @version: 1.0
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户管理模块")
public class UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private ExportService exportService;

    // ==================== 注册 ====================

    @GetMapping("/captcha")
    @ApiOperation("获取图形验证码")
    public BaseResponse<ImageCaptchaUtil.CaptchaResult> getCaptcha() {
        return ResultUtils.success(ImageCaptchaUtil.generate());
    }

    @PostMapping("/register/farmers")
    @ApiOperation("养殖户注册")
    public BaseResponse<UserLoginResp> registerFarmers(@RequestBody @Validated UserRegisterReq req) {
        return userService.registerFarmers(req);
    }

    @PostMapping("/register/manager")
    @ApiOperation("监管局注册")
    public BaseResponse<UserLoginResp> registerManager(@RequestBody @Validated UserRegisterReq req) {
        return userService.registerManager(req);
    }

    // ==================== 登录 ====================

    @PostMapping("/login/admin")
    @ApiOperation("管理员登录")
    public BaseResponse<UserLoginResp> loginAdmin(@RequestBody @Validated UserLoginReq req) {
        return userService.loginAdmin(req);
    }

    @PostMapping("/login/farmers")
    @ApiOperation("养殖户登录")
    public BaseResponse<UserLoginResp> loginFarmers(@RequestBody @Validated UserLoginReq req) {
        return userService.loginFarmers(req);
    }

    @PostMapping("/login/manager")
    @ApiOperation("监管局登录")
    public BaseResponse<UserLoginResp> loginManager(@RequestBody @Validated UserLoginReq req) {
        return userService.loginManager(req);
    }

    // ==================== 用户管理 ====================

    @PostMapping("/updateProfile")
    @ApiOperation("修改个人信息（姓名、密码）")
    public BaseResponse<Boolean> updateProfile(@RequestBody @Validated UserProfileUpdateReq req) {
        return userService.updateProfile(req);
    }

    @GetMapping("/admin/list")
    @ApiOperation("管理员查看所有用户列表")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<PageResponse<UserResp>> adminListUsers(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return userService.adminListUsers(pageNum, pageSize);
    }

    @PostMapping("/admin/updateStatus")
    @ApiOperation("管理员修改用户状态（启用/禁用）")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> adminUpdateStatus(@RequestBody @Validated UserStatusUpdateReq req) {
        return userService.adminUpdateStatus(req);
    }

    @PostMapping("/admin/updateUser")
    @ApiOperation("管理员修改用户信息")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> adminUpdateUser(@RequestBody @Validated AdminUserUpdateReq req) {
        return userService.adminUpdateUser(req);
    }

    @PostMapping("/admin/createUser")
    @ApiOperation("管理员创建用户")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<UserResp> adminCreateUser(@RequestBody @Validated AdminCreateUserReq req) {
        return userService.adminCreateUser(req);
    }

    @PostMapping("/admin/deleteUser")
    @ApiOperation("管理员删除用户")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> adminDeleteUser(@RequestParam Long userId) {
        return userService.adminDeleteUser(userId);
    }

    @PostMapping("/admin/batchUpdateStatus")
    @ApiOperation("管理员批量修改用户状态")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> batchUpdateStatus(@RequestBody @Validated BatchStatusUpdateReq req) {
        return userService.batchUpdateStatus(req);
    }

    @PostMapping("/admin/batchDelete")
    @ApiOperation("管理员批量删除用户")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> batchDeleteUsers(@RequestBody @Validated BatchUserIdsReq req) {
        return userService.batchDeleteUsers(req);
    }

    // ==================== 通用 ====================

    @GetMapping("/getLoginInfoByToken/{token}")
    @ApiOperation("获取登录用户信息(各类token通用)")
    public BaseResponse<UserLoginResp> getCurrentUserInfo(@PathVariable("token") String token) {
        return userService.getCurrentUserInfo(token);
    }

    @GetMapping("/logout/{token}")
    @ApiOperation("退出登录(各类token通用)")
    public BaseResponse<Boolean> logout(@PathVariable("token") String token) {
        return userService.logout(token);
    }

    @GetMapping("/admin/export")
    @ApiOperation("管理员导出用户列表Excel")
    @AuthCheck(roleType = UserRole.ADMIN)
    public void exportUsers(HttpServletResponse response) throws Exception {
        exportService.exportUsers(response);
    }
}
