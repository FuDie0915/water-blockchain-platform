package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.MedicineCreateReq;
import com.water.platform.model.entity.MedicineRecord;
import com.water.platform.service.ExportService;
import com.water.platform.service.MedicineRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/medicine")
@Slf4j
@Api(tags = "用药记录管理接口")
public class MedicineApi {

    @Autowired
    private MedicineRecordService medicineRecordService;

    @Autowired
    private ExportService exportService;

    @PostMapping("/create")
    @ApiOperation("新增用药记录")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> create(@RequestBody @Validated MedicineCreateReq req) {
        return medicineRecordService.create(req);
    }

    @GetMapping("/list")
    @ApiOperation("用药记录列表")
    @AuthCheck(roleType = UserRole.FARMERS)
    public PageResponse<MedicineRecord> list(@RequestParam(required = false) Long pondId,
                                             @RequestParam Long pageNum,
                                             @RequestParam Long pageSize) {
        return medicineRecordService.list(pondId, pageNum, pageSize);
    }

    @GetMapping("/manager/list")
    @ApiOperation("监管局查看管辖养殖户的用药记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<PageResponse<MedicineRecord>> managerList(@RequestParam(required = false) Long farmerId,
                                                                  @RequestParam(required = false) Integer auditStatus,
                                                                  @RequestParam Long pageNum,
                                                                  @RequestParam Long pageSize) {
        return medicineRecordService.managerList(farmerId, auditStatus, pageNum, pageSize);
    }

    @PostMapping("/manager/audit")
    @ApiOperation("监管局审核用药记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> audit(@RequestBody @Validated AuditReq req) {
        return medicineRecordService.audit(req);
    }

    @PostMapping("/manager/batchAudit")
    @ApiOperation("监管局批量审核用药记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> batchAudit(@RequestBody @Validated BatchAuditReq req) {
        return medicineRecordService.batchAudit(req);
    }

    @PostMapping("/update")
    @ApiOperation("修改用药记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> update(@RequestBody @Validated com.water.platform.model.dto.req.MedicineUpdateReq req) {
        return medicineRecordService.update(req);
    }

    @PostMapping("/delete")
    @ApiOperation("删除用药记录（仅待审核状态）")
    @AuthCheck(roleType = UserRole.FARMERS)
    public BaseResponse<Boolean> delete(@RequestParam Long id) {
        return medicineRecordService.delete(id);
    }

    @GetMapping("/manager/export")
    @ApiOperation("监管局导出用药记录Excel")
    @AuthCheck(roleType = UserRole.MANAGER)
    public void exportMedicineRecords(HttpServletResponse response, @RequestParam(required = false) Long farmerId) throws Exception {
        exportService.exportMedicineRecords(response, farmerId);
    }

    @PostMapping("/manager/upChain")
    @ApiOperation("监管局上链用药记录")
    @AuthCheck(roleType = UserRole.MANAGER)
    public BaseResponse<Boolean> upChain(@RequestBody @Validated BatchAuditReq req) {
        return medicineRecordService.upChain(req.getIds());
    }
}
