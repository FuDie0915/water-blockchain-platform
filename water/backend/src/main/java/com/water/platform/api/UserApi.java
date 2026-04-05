package com.water.platform.api;

import com.water.platform.base.common.BaseResponse;
import com.water.platform.model.dto.req.UserCreateReq;
import com.water.platform.model.dto.resp.UserLoginResp;
import com.water.platform.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/loginOrRegister")
    @ApiOperation("平台登录或注册")
    public BaseResponse<UserLoginResp> create(@RequestBody @Validated UserCreateReq userCreateReq) {
        return userService.create(userCreateReq, false);
    }

    @GetMapping("/getLoginInfoByToken/{token}")
    @ApiOperation("获取登录用户信息(各类token通用)")
    public BaseResponse<UserLoginResp> getCurrentUserInfo(@PathVariable ("token") String token) {
        return userService.getCurrentUserInfo(token);
    }

    @GetMapping("/logout/{token}")
    @ApiOperation("退出登录(各类token通用)")
    public BaseResponse<Boolean> logout(@PathVariable ("token") String token) {
        return userService.logout(token);
    }



//    @GetMapping("/page")
//    @ApiOperation("分页查询用户")
////    @AuthCheck
//    public PageResponse<UserResp> page(int pageNum, int pageSize) {
//        Page<User> userTestPage = userMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<User>()
//                .orderByDesc(User::getUserId));
//        PageResponse<UserResp> pageResponse = new PageResponse<>(0, new ArrayList<>());
//        BeanUtils.copyProperties(userTestPage, pageResponse); // 浅拷贝，不涉及内部复杂对象属性复制
//        List<UserResp> userResps = BeanConvertUtils.convertListTo(userTestPage.getRecords(), UserResp::new);
//        pageResponse.setData(userResps);
//        return pageResponse;
//    }



//    /**
//    获取登录用户
//     */
//    private User getLoginUser(HttpServletRequest request) {
//        String token = request.getHeader("sa-token");
//        ThrowUtils.throwIf(StringUtils.isBlank(token), ErrorCode.HEADER_PARAMS_ERROR);
//        Object userIdByToken = StpUtil.getLoginIdByToken(token);
//        ThrowUtils.throwIf(Objects.isNull(userIdByToken), ErrorCode.NOT_LOGIN_ERROR);
//        Long userId = Long.valueOf(userIdByToken.toString());
//        return userMapper.selectOne(new LambdaQueryWrapper<User>()
//                .eq(User::getUserId, userId));
//    }

}
