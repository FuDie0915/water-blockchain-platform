package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.PondMapper;
import com.water.platform.mapper.SeedRecordMapper;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.SeedCreateReq;
import com.water.platform.model.entity.Pond;
import com.water.platform.model.entity.SeedRecord;
import com.water.platform.utils.TokenUtil;
import org.fisco.bcos.sdk.client.Client;
import com.water.platform.annotation.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeedRecordService {

    @Autowired
    private SeedRecordMapper seedRecordMapper;

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private ManagerFarmerService managerFarmerService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChainStoreService chainStoreService;

    public BaseResponse<Boolean> create(SeedCreateReq req) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        validatePondOwner(req.getPondId(), userId);
        SeedRecord record = new SeedRecord();
        org.springframework.beans.BeanUtils.copyProperties(req, record);
        record.setUserId(userId);
        record.setAuditStatus(0);
        record.setCreateTime(DateUtil.date());
        seedRecordMapper.insert(record);
        return ResultUtils.success(Boolean.TRUE);
    }

    public PageResponse<SeedRecord> list(Long pondId, Long pageNum, Long pageSize) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        LambdaQueryWrapper<SeedRecord> wrapper = new LambdaQueryWrapper<SeedRecord>()
                .eq(SeedRecord::getUserId, userId)
                .orderByDesc(SeedRecord::getId);
        if (pondId != null) {
            wrapper.eq(SeedRecord::getPondId, pondId);
        }
        Page<SeedRecord> page = seedRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    private void validatePondOwner(Long pondId, Long userId) {
        Pond pond = pondMapper.selectById(pondId);
        ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在");
        ThrowUtils.throwIf(!pond.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该养殖池");
    }

    public BaseResponse<PageResponse<SeedRecord>> managerList(Long farmerId, Integer auditStatus, Long pageNum, Long pageSize) {
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<SeedRecord> wrapper = new LambdaQueryWrapper<SeedRecord>()
                .in(SeedRecord::getUserId, farmerIds)
                .orderByDesc(SeedRecord::getId);
        if (farmerId != null) {
            wrapper.eq(SeedRecord::getUserId, farmerId);
        }
        if (auditStatus != null) {
            wrapper.eq(SeedRecord::getAuditStatus, auditStatus);
        }
        Page<SeedRecord> page = seedRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ResultUtils.success(new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    public BaseResponse<Boolean> audit(AuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        SeedRecord record = seedRecordMapper.selectById(req.getId());
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "记录不存在");
        managerFarmerService.validateManagerFarmer(managerId, record.getUserId());
        record.setAuditStatus(req.getAuditStatus());
        seedRecordMapper.updateById(record);
        String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
        notificationService.send(record.getUserId(), "苗种投放记录审核" + result, "您的苗种投放记录审核" + result, "AUDIT", record.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> batchAudit(BatchAuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        for (Long id : req.getIds()) {
            SeedRecord record = seedRecordMapper.selectById(id);
            ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "苗种记录不存在，ID：" + id);
            managerFarmerService.validateManagerFarmer(managerId, record.getUserId());
            record.setAuditStatus(req.getAuditStatus());
            seedRecordMapper.updateById(record);
            String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
            notificationService.send(record.getUserId(), "苗种投放记录审核" + result, "您的苗种投放记录审核" + result, "AUDIT", record.getId());
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    @Lock(lockClass = Client.class)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> upChain(List<Long> ids) {
        Long managerId = TokenUtil.getLoginUserId();
        for (Long id : ids) {
            SeedRecord record = seedRecordMapper.selectById(id);
            ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "苗种记录不存在，ID：" + id);
            managerFarmerService.validateManagerFarmer(managerId, record.getUserId());
            ThrowUtils.throwIf(record.getAuditStatus() == null || record.getAuditStatus() != 1,
                    ErrorCode.PARAMS_ERROR, "苗种记录未审核通过，不能上链，ID：" + id);
            String hash = chainStoreService.upChain("seed_record", record.getId(), record);
            record.setChainTxHash(hash);
            seedRecordMapper.updateById(record);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> update(com.water.platform.model.dto.req.SeedUpdateReq req) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        SeedRecord record = seedRecordMapper.selectById(req.getId());
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "记录不存在");
        ThrowUtils.throwIf(!record.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该记录");
        ThrowUtils.throwIf(record.getAuditStatus() != 0, ErrorCode.PARAMS_ERROR, "已审核的记录不可修改");
        if (req.getPondId() != null) record.setPondId(req.getPondId());
        if (req.getRecordDate() != null) record.setRecordDate(req.getRecordDate());
        if (req.getManager() != null) record.setManager(req.getManager());
        if (req.getSeedType() != null) record.setSeedType(req.getSeedType());
        if (req.getSeedSpec() != null) record.setSeedSpec(req.getSeedSpec());
        if (req.getWeight() != null) record.setWeight(req.getWeight());
        if (req.getRemark() != null) record.setRemark(req.getRemark());
        seedRecordMapper.updateById(record);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> delete(Long id) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        SeedRecord record = seedRecordMapper.selectById(id);
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "记录不存在");
        ThrowUtils.throwIf(!record.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该记录");
        ThrowUtils.throwIf(record.getAuditStatus() != 0, ErrorCode.PARAMS_ERROR, "已审核的记录不可删除");
        seedRecordMapper.deleteById(id);
        return ResultUtils.success(Boolean.TRUE);
    }
}
