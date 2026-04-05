package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.resp.UserLoginResp;
import com.water.platform.model.dto.resp.WaterStep1InfoResp;
import com.water.platform.service.WaterService;
import com.water.platform.model.dto.resp.CompanyCertResp;
import com.water.platform.model.dto.resp.WaterDataResp;
import com.water.platform.model.dto.req.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ：devon
 * @date ：2024/11/14 14:11
 * @description：水质业务接口
 * @version: 1.0
 */
@RestController
@RequestMapping("/water")
@Slf4j
@Api(tags = "水质业务接口")
public class WaterApi {

    @Autowired
    private WaterService waterService;

    @PostMapping("/user/login")
    @ApiOperation("企业或监管局登录")
    public BaseResponse<UserLoginResp> userLogin(@RequestBody @Validated UserLoginReq userLoginReq) {
        return waterService.waterUserLogin(userLoginReq);
    }

    @PostMapping("/step1Info")
    @ApiOperation("水质步骤1信息")
    public BaseResponse<WaterStep1InfoResp> step1Info() {
        return waterService.step1Info();
    }

    @PostMapping("/permission/commit")
    @ApiOperation("企业提交许可证")
    @AuthCheck(roleType = UserRole.COMPANY)
    public BaseResponse<Boolean> commit(@RequestBody @Validated PermissionCommitReq permissionCommitReq) {
        return waterService.commit(permissionCommitReq);
    }

    @GetMapping("/permission/company/query")
    @ApiOperation("企业查询许可证")
    @AuthCheck(roleType = UserRole.COMPANY)
    public BaseResponse<List<CompanyCertResp>> query() {
        return waterService.companyQuery();
    }

    @GetMapping("/permission/manager/query")
    @ApiOperation("监管局查询许可证")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<List<CompanyCertResp>> managerQuery() {
        return waterService.companyQuery();
    }


    @PostMapping("/permission/verify")
    @ApiOperation("监管局审批许可证")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> verify(@RequestBody @Validated PermissionVerifyReq permissionVerifyReq) {
        return waterService.verify(permissionVerifyReq);
    }

    @PostMapping("/permission/isOnChain/{permissionId}")
    @ApiOperation("许可证链上比对")
    public BaseResponse<CompanyCertResp> isOnChain(@PathVariable ("permissionId") Long permissionId) {
        return waterService.isOnChain(permissionId);
    }

    @PostMapping("/data/gen")
    @ApiOperation("水数据生成")
    public BaseResponse<Boolean> gen(@RequestBody @Validated WaterDataTypeReq waterDataTypeReq) {
        return waterService.gen(waterDataTypeReq);
    }

    @PostMapping("/data/collect")
    @ApiOperation("水数据采集(设备数据采集)")
    public BaseResponse<Boolean> collect(@RequestBody @Validated WaterDataCollectReq waterDataCollectReq) throws Exception {
        return waterService.collect(waterDataCollectReq);
    }

    @GetMapping("/data/page")
    @ApiOperation("水数据分页查询")
    public PageResponse<WaterDataResp> page(@RequestParam Integer DataType, @RequestParam Long pageNum, @RequestParam Long pageSize) {
        return waterService.page(DataType, pageNum, pageSize);
    }

    @PostMapping("/data/upChain")
    @ApiOperation("水数据上链")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestBody @Validated WaterDataUpChainReq waterDataUpChainReq) {
        return waterService.upChain(waterDataUpChainReq);
    }
}
