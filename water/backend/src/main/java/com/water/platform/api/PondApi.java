package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.PondCreateReq;
import com.water.platform.model.dto.req.PondStatusUpdateReq;
import com.water.platform.model.dto.req.PondUpdateReq;
import com.water.platform.model.entity.Pond;
import com.water.platform.service.ExportService;
import com.water.platform.service.PondService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@RestController
@RequestMapping("/pond")
@Slf4j
@Api(tags = "养殖池管理接口")
public class PondApi {

    @Autowired
    private PondService pondService;

    @Autowired
    private ExportService exportService;

    @PostMapping("/create")
    @ApiOperation("新增养殖池")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> create(@RequestBody @Validated PondCreateReq req) {
        return pondService.create(req);
    }

    @GetMapping("/list")
    @ApiOperation("获取当前用户养殖池列表")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<List<Pond>> list() {
        return pondService.list();
    }

    @PostMapping("/update")
    @ApiOperation("更新养殖池信息")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> update(@RequestBody @Validated PondUpdateReq req) {
        return pondService.update(req);
    }

    @PostMapping("/update/status")
    @ApiOperation("更新养殖池状态")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> updateStatus(@RequestBody @Validated PondStatusUpdateReq req) {
        return pondService.updateStatus(req);
    }

    @PostMapping("/delete")
    @ApiOperation("删除养殖池")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> delete(@RequestParam Long id) {
        return pondService.delete(id);
    }

    @GetMapping("/manager/list")
    @ApiOperation("监管局查看管辖养殖户的养殖池")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<PageResponse<Pond>> managerList(@RequestParam(required = false) Long farmerId,
                                                        @RequestParam(required = false) Integer auditStatus,
                                                        @RequestParam Long pageNum,
                                                        @RequestParam Long pageSize) {
        return pondService.managerList(farmerId, auditStatus, pageNum, pageSize);
    }

    @PostMapping("/manager/audit")
    @ApiOperation("监管局审核养殖池")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> audit(@RequestBody @Validated AuditReq req) {
        return pondService.audit(req);
    }

    @PostMapping("/manager/batchAudit")
    @ApiOperation("监管局批量审核养殖池")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> batchAudit(@RequestBody @Validated BatchAuditReq req) {
        return pondService.batchAudit(req);
    }

    @GetMapping("/manager/export")
    @ApiOperation("监管局导出养殖池Excel")
    @AuthCheck(roleType = UserRole.MANAGER)
    public void exportPonds(HttpServletResponse response, @RequestParam(required = false) Long farmerId) throws Exception {
        exportService.exportPonds(response, farmerId);
    }

    @PostMapping("/manager/upChain")
    @ApiOperation("监管局上链养殖池")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestBody @Validated BatchAuditReq req) {
        return pondService.upChain(req.getIds());
    }
}
