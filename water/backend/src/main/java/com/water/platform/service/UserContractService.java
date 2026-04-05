package com.water.platform.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.mapper.UserContractMapper;
import com.water.platform.model.dto.req.UserContractQueryReq;
import com.water.platform.model.entity.UserContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ：devon
 * @date ：2024/11/12 14:39
 * @description：用户合约业务逻辑类
 * @version: 1.0
 */
@Service
public class UserContractService {
    @Autowired
    private UserContractMapper userContractMapper;

    public BaseResponse<UserContract> query(UserContractQueryReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        UserContract userContract = userContractMapper.selectOne(new LambdaQueryWrapper<UserContract>()
                .eq(UserContract::getUserId, userId)
                .eq(UserContract::getContractType, req.getContractType()));
        return ResultUtils.success(userContract);
    }

}

