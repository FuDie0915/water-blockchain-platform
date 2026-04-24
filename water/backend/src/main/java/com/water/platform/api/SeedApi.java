package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.SeedCreateReq;
import com.water.platform.model.entity.SeedRecord;
import com.water.platform.service.ExportService;
import com.water.platform.service.SeedRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/seed")
@Slf4j
@Api(tags = "苗种投放管理接口")
public class SeedApi {

    @Autowired
    private SeedRecordService seedRecordService;

    @Autowired
    private ExportService exportService;

    @PostMapping("/create")
    @ApiOperation("新增苗种投放")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> create(@RequestBody @Validated SeedCreateReq req) {
        return seedRecordService.create(req);
    }

    @GetMapping("/list")
    @ApiOperation("苗种投放记录列表")
    @AuthCheck(roleType = UserRole.FARMERS)
    public PageResponse<SeedRecord> list(@RequestParam(required = false) Long pondId,
                                         @RequestParam(defaultValue = "1") Long pageNum,
                                         @RequestParam(defaultValue = "10") Long pageSize) {
        return seedRecordService.list(pondId, pageNum, pageSize);
    }

    @GetMapping("/manager/list")
    @ApiOperation("监管局查看管辖养殖户的苗种投放记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<PageResponse<SeedRecord>> managerList(@RequestParam(required = false) Long farmerId,
                                                              @RequestParam(required = false) Integer auditStatus,
                                                              @RequestParam Long pageNum,
                                                              @RequestParam Long pageSize) {
        return seedRecordService.managerList(farmerId, auditStatus, pageNum, pageSize);
    }

    @PostMapping("/manager/audit")
    @ApiOperation("监管局审核苗种投放记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> audit(@RequestBody @Validated AuditReq req) {
        return seedRecordService.audit(req);
    }

    @PostMapping("/manager/batchAudit")
    @ApiOperation("监管局批量审核苗种投放记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> batchAudit(@RequestBody @Validated BatchAuditReq req) {
        return seedRecordService.batchAudit(req);
    }

    @PostMapping("/update")
    @ApiOperation("修改苗种投放记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> update(@RequestBody @Validated com.water.platform.model.dto.req.SeedUpdateReq req) {
        return seedRecordService.update(req);
    }

    @PostMapping("/delete")
    @ApiOperation("删除苗种投放记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> delete(@RequestParam Long id) {
        return seedRecordService.delete(id);
    }

    @GetMapping("/manager/export")
    @ApiOperation("监管局导出苗种投放记录Excel")
    @AuthCheck(roleType = UserRole.MANAGER)
    public void exportSeedRecords(HttpServletResponse response, @RequestParam(required = false) Long farmerId) throws Exception {
        exportService.exportSeedRecords(response, farmerId);
    }

    @PostMapping("/manager/upChain")
    @ApiOperation("监管局上链苗种记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestBody @Validated BatchAuditReq req) {
        return seedRecordService.upChain(req.getIds());
    }
}
