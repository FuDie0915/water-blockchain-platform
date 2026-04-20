package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.HarvestCreateReq;
import com.water.platform.model.entity.HarvestRecord;
import com.water.platform.service.ExportService;
import com.water.platform.service.HarvestRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/harvest")
@Slf4j
@Api(tags = "收获记录管理接口")
public class HarvestApi {

    @Autowired
    private HarvestRecordService harvestRecordService;

    @Autowired
    private ExportService exportService;

    @PostMapping("/create")
    @ApiOperation("新增收获记录")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> create(@RequestBody @Validated HarvestCreateReq req) {
        return harvestRecordService.create(req);
    }

    @GetMapping("/list")
    @ApiOperation("收获记录列表")
    @AuthCheck(roleType = UserRole.FARMERS)
    public PageResponse<HarvestRecord> list(@RequestParam(required = false) Long pondId,
                                            @RequestParam Long pageNum,
                                            @RequestParam Long pageSize) {
        return harvestRecordService.list(pondId, pageNum, pageSize);
    }

    @GetMapping("/manager/list")
    @ApiOperation("监管局查看管辖养殖户的收获记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<PageResponse<HarvestRecord>> managerList(@RequestParam(required = false) Long farmerId,
                                                                 @RequestParam(required = false) Integer auditStatus,
                                                                 @RequestParam Long pageNum,
                                                                 @RequestParam Long pageSize) {
        return harvestRecordService.managerList(farmerId, auditStatus, pageNum, pageSize);
    }

    @PostMapping("/manager/audit")
    @ApiOperation("监管局审核收获记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> audit(@RequestBody @Validated AuditReq req) {
        return harvestRecordService.audit(req);
    }

    @PostMapping("/manager/batchAudit")
    @ApiOperation("监管局批量审核收获记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> batchAudit(@RequestBody @Validated BatchAuditReq req) {
        return harvestRecordService.batchAudit(req);
    }

    @PostMapping("/update")
    @ApiOperation("修改收获记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> update(@RequestBody @Validated com.water.platform.model.dto.req.HarvestUpdateReq req) {
        return harvestRecordService.update(req);
    }

    @PostMapping("/delete")
    @ApiOperation("删除收获记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> delete(@RequestParam Long id) {
        return harvestRecordService.delete(id);
    }

    @GetMapping("/manager/export")
    @ApiOperation("监管局导出收获记录Excel")
    @AuthCheck(roleType = UserRole.MANAGER)
    public void exportHarvestRecords(HttpServletResponse response, @RequestParam(required = false) Long farmerId) throws Exception {
        exportService.exportHarvestRecords(response, farmerId);
    }

    @PostMapping("/manager/upChain")
    @ApiOperation("监管局上链收获记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestBody @Validated BatchAuditReq req) {
        return harvestRecordService.upChain(req.getIds());
    }
}
