package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.BindApplyReq;
import com.water.platform.model.dto.resp.BindStatusResp;
import com.water.platform.model.entity.ManagerFarmer;
import com.water.platform.model.entity.User;
import com.water.platform.service.ManagerFarmerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bind")
@Slf4j
@Api(tags = "监管局-养殖户绑定管理接口")
public class BindApi {

    @Autowired
    private ManagerFarmerService managerFarmerService;

    @GetMapping("/managers")
    @ApiOperation("获取所有已注册监管局列表")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<List<User>> listManagers() {
        return managerFarmerService.listManagers();
    }

    @PostMapping("/apply")
    @ApiOperation("养殖户申请绑定监管局")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> apply(@RequestBody @Validated BindApplyReq req) {
        return managerFarmerService.apply(req);
    }

    @GetMapping("/status")
    @ApiOperation("养殖户查看自己的绑定状态")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<BindStatusResp> getStatus() {
        return managerFarmerService.getStatus();
    }

    @GetMapping("/manager/list")
    @ApiOperation("监管局查看所有绑定申请")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<List<ManagerFarmer>> managerList() {
        return managerFarmerService.managerList();
    }

    @PostMapping("/manager/approve")
    @ApiOperation("监管局同意绑定")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> approve(@RequestParam Long id) {
        return managerFarmerService.approve(id);
    }

    @PostMapping("/manager/reject")
    @ApiOperation("监管局拒绝绑定")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> reject(@RequestParam Long id) {
        return managerFarmerService.reject(id);
    }

    @GetMapping("/admin/list")
    @ApiOperation("管理员查看所有绑定关系")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<PageResponse<com.water.platform.model.dto.resp.BindingResp>> adminList(
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) Integer status,
            @RequestParam Long pageNum,
            @RequestParam Long pageSize) {
        return managerFarmerService.adminList(managerId, farmerId, status, pageNum, pageSize);
    }

    @PostMapping("/unbind")
    @ApiOperation("养殖户主动解绑监管局")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> unbind() {
        return managerFarmerService.unbind();
    }

    @PostMapping("/manager/unbind")
    @ApiOperation("监管局主动解除绑定关系")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> managerUnbind(@RequestParam Long id) {
        return managerFarmerService.managerUnbind(id);
    }

    @PostMapping("/manager/upChain")
    @ApiOperation("监管局上链绑定记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestParam Long id) {
        return managerFarmerService.upChain(id);
    }
}
