package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.FeedCreateReq;
import com.water.platform.model.entity.FeedRecord;
import com.water.platform.service.ExportService;
import com.water.platform.service.FeedRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/feed")
@Slf4j
@Api(tags = "投喂记录管理接口")
public class FeedApi {

    @Autowired
    private FeedRecordService feedRecordService;

    @Autowired
    private ExportService exportService;

    @PostMapping("/create")
    @ApiOperation("新增投喂记录")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> create(@RequestBody @Validated FeedCreateReq req) {
        return feedRecordService.create(req);
    }

    @GetMapping("/list")
    @ApiOperation("投喂记录列表")
    @AuthCheck(roleType = UserRole.FARMERS)
    public PageResponse<FeedRecord> list(@RequestParam(required = false) Long pondId,
                                         @RequestParam Long pageNum,
                                         @RequestParam Long pageSize) {
        return feedRecordService.list(pondId, pageNum, pageSize);
    }

    @GetMapping("/manager/list")
    @ApiOperation("监管局查看管辖养殖户的投喂记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<PageResponse<FeedRecord>> managerList(@RequestParam(required = false) Long farmerId,
                                                              @RequestParam(required = false) Integer auditStatus,
                                                              @RequestParam Long pageNum,
                                                              @RequestParam Long pageSize) {
        return feedRecordService.managerList(farmerId, auditStatus, pageNum, pageSize);
    }

    @PostMapping("/manager/audit")
    @ApiOperation("监管局审核投喂记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> audit(@RequestBody @Validated AuditReq req) {
        return feedRecordService.audit(req);
    }

    @PostMapping("/manager/batchAudit")
    @ApiOperation("监管局批量审核投喂记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> batchAudit(@RequestBody @Validated BatchAuditReq req) {
        return feedRecordService.batchAudit(req);
    }

    @PostMapping("/update")
    @ApiOperation("修改投喂记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> update(@RequestBody @Validated com.water.platform.model.dto.req.FeedUpdateReq req) {
        return feedRecordService.update(req);
    }

    @PostMapping("/delete")
    @ApiOperation("删除投喂记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> delete(@RequestParam Long id) {
        return feedRecordService.delete(id);
    }

    @GetMapping("/manager/export")
    @ApiOperation("监管局导出投喂记录Excel")
    @AuthCheck(roleType = UserRole.MANAGER)
    public void exportFeedRecords(HttpServletResponse response, @RequestParam(required = false) Long farmerId) throws Exception {
        exportService.exportFeedRecords(response, farmerId);
    }

    @PostMapping("/manager/upChain")
    @ApiOperation("监管局上链投喂记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestBody @Validated BatchAuditReq req) {
        return feedRecordService.upChain(req.getIds());
    }
}
