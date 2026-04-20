package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.resp.AdminDashboardResp;
import com.water.platform.model.dto.resp.FarmerDashboardResp;
import com.water.platform.model.dto.resp.ManagerDashboardResp;
import com.water.platform.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Slf4j
@Api(tags = "数据统计看板")
public class DashboardApi {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin")
    @ApiOperation("管理员看板")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<AdminDashboardResp> adminDashboard() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/manager")
    @ApiOperation("监管局看板")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<ManagerDashboardResp> managerDashboard() {
        return dashboardService.managerDashboard();
    }

    @GetMapping("/farmer")
    @ApiOperation("养殖户看板")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<FarmerDashboardResp> farmerDashboard() {
        return dashboardService.farmerDashboard();
    }
}
