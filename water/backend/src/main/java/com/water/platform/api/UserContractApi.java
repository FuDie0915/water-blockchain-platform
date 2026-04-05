package com.water.platform.api;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.utils.BeanConvertUtils;
import com.water.platform.mapper.UserContractMapper;
import com.water.platform.model.dto.req.UserContractCreateReq;
import com.water.platform.model.dto.req.UserContractQueryReq;
import com.water.platform.model.entity.UserContract;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author ：devon
 * @date ：2024/11/12 15:00
 * @description：用户合约接口
 * @version: 1.0
 */
@RestController
@RequestMapping("/user_contract")
@Slf4j
@Api(tags = "用户合约接口")
public class UserContractApi {
    @Autowired
    private UserContractMapper userContractMapper;


    @PostMapping("/create")
    @ApiOperation("绑定")
    public BaseResponse<Object> create(@RequestBody UserContractCreateReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        String lock = "userContract:" + userId;
        int row = 0;
        synchronized (lock.intern()) {
            UserContract userContract = userContractMapper.selectOne(new LambdaQueryWrapper<UserContract>()
                    .eq(UserContract::getUserId, userId)
                    .eq(UserContract::getContractType, req.getContractType()));
            if (userContract == null) {
                UserContract uc = BeanConvertUtils.convertTo(req, UserContract::new);
                uc.setUserId(userId);
                row = userContractMapper.insert(uc);
            } else {
                UserContract uc = BeanConvertUtils.convertTo(req, UserContract::new);
                row = userContractMapper.update(uc, new LambdaUpdateWrapper<UserContract>()
                        .eq(UserContract::getUserId, userId)
                        .eq(UserContract::getContractType, req.getContractType()));
            }
        }
        return row == 1 ? ResultUtils.success(Boolean.TRUE) : new BaseResponse<>(99999, "绑定失败");
    }

    @GetMapping("/query")
    @ApiOperation("查询")
    public BaseResponse<UserContract> query(UserContractQueryReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        UserContract userContract = userContractMapper.selectOne(new LambdaQueryWrapper<UserContract>()
                .eq(UserContract::getUserId, userId)
                .eq(UserContract::getContractType, req.getContractType()));
        return ResultUtils.success(userContract);
    }
}
