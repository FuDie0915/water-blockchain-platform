package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.ManagerAuditActionReq;
import com.water.platform.model.dto.req.ManagerAuditReq;
import com.water.platform.model.entity.ManagerAudit;
import com.water.platform.service.ManagerAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager-audit")
@Slf4j
@Api(tags = "监管局审核管理接口")
public class ManagerAuditApi {

    @Autowired
    private ManagerAuditService managerAuditService;

    @PostMapping("/submit")
    @ApiOperation("监管局提交审核申请")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> submit(@RequestBody @Validated ManagerAuditReq req) {
        return managerAuditService.submit(req);
    }

    @GetMapping("/status")
    @ApiOperation("监管局查看自己审核状态")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<ManagerAudit> getStatus() {
        return managerAuditService.getStatus();
    }

    @GetMapping("/admin/list")
    @ApiOperation("管理员查看审核申请列表")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<PageResponse<ManagerAudit>> adminList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        return managerAuditService.adminList(status, pageNum, pageSize);
    }

    @PostMapping("/admin/approve")
    @ApiOperation("管理员批准审核")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> approve(@RequestBody @Validated ManagerAuditActionReq req) {
        return managerAuditService.approve(req);
    }

    @PostMapping("/admin/reject")
    @ApiOperation("管理员拒绝审核")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> reject(@RequestBody @Validated ManagerAuditActionReq req) {
        return managerAuditService.reject(req);
    }

    @PostMapping("/admin/upChain")
    @ApiOperation("管理员上链监管局审批记录")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> upChain(@RequestParam Long id) {
        return managerAuditService.upChain(id);
    }
}
