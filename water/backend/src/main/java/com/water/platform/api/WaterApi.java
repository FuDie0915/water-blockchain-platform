package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.resp.*;
import com.water.platform.model.entity.WarningThreshold;
import com.water.platform.service.WarningThresholdService;
import com.water.platform.service.ExportService;
import com.water.platform.service.WaterService;
import com.water.platform.model.dto.req.*;
import com.water.platform.model.dto.resp.WaterDataResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * @author ：devon
 * @date ：2024/11/14 14:11
 * @description：海水养殖场可信水质数据监管业务接口
 * @version: 1.0
 */
@RestController
@RequestMapping("/water")
@Slf4j
@Api(tags = "海水养殖场可信水质数据监管业务接口")
public class WaterApi {

    @Autowired
    private WaterService waterService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private WarningThresholdService warningThresholdService;

    @PostMapping("/step1Info")
    @ApiOperation("水质步骤1信息")
    public BaseResponse<WaterStep1InfoResp> step1Info() {
        return waterService.step1Info();
    }

    @PostMapping("/permission/commit")
    @ApiOperation("养殖户提交许可证")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> commit(@RequestBody @Validated PermissionCommitReq permissionCommitReq) {
        return waterService.commit(permissionCommitReq);
    }

    @GetMapping("/permission/company/query")
    @ApiOperation("养殖户查询许可证")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<List<CompanyCertResp>> query() {
        return waterService.companyQuery();
    }

    @GetMapping("/permission/manager/query")
    @ApiOperation("监管局查询许可证")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<List<CompanyCertResp>> managerQuery() {
        return waterService.companyQuery();
    }

    @GetMapping("/permission/manager/allQuery")
    @ApiOperation("监管局查询所有许可证")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<List<CompanyCertResp>> managerAllQuery(@RequestParam(required = false) Integer status) {
        return waterService.managerAllQuery(status);
    }

    @PostMapping("/permission/delete/{certId}")
    @ApiOperation("养殖户删除许可证（仅被拒绝的）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> deleteCert(@PathVariable("certId") Long certId) {
        return waterService.deleteCert(certId);
    }

    @PostMapping("/data/delete/{dataId}")
    @ApiOperation("删除水质数据（仅未上链的）")
    public BaseResponse<Boolean> deleteWaterData(@PathVariable("dataId") Long dataId) {
        return waterService.deleteWaterData(dataId);
    }


    @PostMapping("/permission/verify")
    @ApiOperation("监管局审批许可证")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> verify(@RequestBody @Validated PermissionVerifyReq permissionVerifyReq) {
        return waterService.verify(permissionVerifyReq);
    }

    @PostMapping("/permission/isOnChain/{permissionId}")
    @ApiOperation("许可证链上比对")
    public BaseResponse<CompanyCertResp> isOnChain(@PathVariable("permissionId") Long permissionId) {
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

    @PostMapping("/data/manual")
    @ApiOperation("养殖户手动上报水质数据（需监管局审核）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> manual(@RequestBody @Validated WaterDataManualReq req) {
        return waterService.manual(req);
    }

    @GetMapping("/data/page")
    @ApiOperation("水数据分页查询")
    public PageResponse<WaterDataResp> page(@RequestParam Integer DataType, @RequestParam(defaultValue = "1") Long pageNum, @RequestParam(defaultValue = "10") Long pageSize) {
        return waterService.page(DataType, pageNum, pageSize);
    }

    @PostMapping("/data/upChain")
    @ApiOperation("水数据上链")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestBody @Validated WaterDataUpChainReq waterDataUpChainReq) {
        return waterService.upChain(waterDataUpChainReq);
    }

    // ==================== 预警阈值配置 ====================

    @PostMapping("/threshold/create")
    @ApiOperation("监管局新建预警阈值配置（全部参数必填，只能创建一次）")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> createThreshold(@RequestBody @Validated WarningThresholdCreateReq req) {
        return warningThresholdService.createThreshold(req);
    }

    @PostMapping("/threshold/update")
    @ApiOperation("监管局修改预警阈值配置（只传需要修改的字段）")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> updateThreshold(@RequestBody @Validated WarningThresholdSaveReq req) {
        return warningThresholdService.updateThreshold(req);
    }

    @GetMapping("/threshold/get")
    @ApiOperation("监管局获取预警阈值配置")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<WarningThreshold> getThreshold() {
        return warningThresholdService.getThreshold();
    }

    @GetMapping("/data/manager/list")
    @ApiOperation("监管局查看管辖养殖户的水质数据（手动上报的待审核数据）")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<PageResponse<WaterDataResp>> managerDataList(
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam Long pageNum,
            @RequestParam Long pageSize) {
        return waterService.managerDataList(farmerId, auditStatus, pageNum, pageSize);
    }

    @PostMapping("/data/manager/audit")
    @ApiOperation("监管局审核水质数据")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> auditWaterData(@RequestBody @Validated AuditReq req) {
        return waterService.auditWaterData(req);
    }

    @PostMapping("/data/manager/batchAudit")
    @ApiOperation("监管局批量审核水质数据")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> batchAuditWaterData(@RequestBody @Validated BatchAuditReq req) {
        return waterService.batchAuditWaterData(req);
    }

    @GetMapping("/data/manager/export")
    @ApiOperation("监管局导出水质数据Excel")
    @AuthCheck(roleType = UserRole.MANAGER)
    public void exportWaterData(HttpServletResponse response,
                                @RequestParam(required = false) Long farmerId,
                                @RequestParam(required = false) Integer dataType) throws Exception {
        exportService.exportWaterData(response, farmerId, dataType);
    }
}
