package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.HarvestRecordMapper;
import com.water.platform.mapper.PondMapper;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.HarvestCreateReq;
import com.water.platform.model.entity.HarvestRecord;
import com.water.platform.model.entity.Pond;
import com.water.platform.utils.TokenUtil;
import org.fisco.bcos.sdk.client.Client;
import com.water.platform.annotation.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HarvestRecordService {

    @Autowired
    private HarvestRecordMapper harvestRecordMapper;

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private ManagerFarmerService managerFarmerService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChainStoreService chainStoreService;

    public BaseResponse<Boolean> create(HarvestCreateReq req) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        validatePondOwner(req.getPondId(), userId);
        HarvestRecord record = new HarvestRecord();
        org.springframework.beans.BeanUtils.copyProperties(req, record);
        record.setUserId(userId);
        record.setAuditStatus(0);
        record.setCreateTime(DateUtil.date());
        harvestRecordMapper.insert(record);
        return ResultUtils.success(Boolean.TRUE);
    }

    public PageResponse<HarvestRecord> list(Long pondId, Long pageNum, Long pageSize) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        LambdaQueryWrapper<HarvestRecord> wrapper = new LambdaQueryWrapper<HarvestRecord>()
                .eq(HarvestRecord::getUserId, userId)
                .orderByDesc(HarvestRecord::getId);
        if (pondId != null) {
            wrapper.eq(HarvestRecord::getPondId, pondId);
        }
        Page<HarvestRecord> page = harvestRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    private void validatePondOwner(Long pondId, Long userId) {
        Pond pond = pondMapper.selectById(pondId);
        ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在");
        ThrowUtils.throwIf(!pond.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该养殖池");
    }

    public BaseResponse<PageResponse<HarvestRecord>> managerList(Long farmerId, Integer auditStatus, Long pageNum, Long pageSize) {
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<HarvestRecord> wrapper = new LambdaQueryWrapper<HarvestRecord>()
                .in(HarvestRecord::getUserId, farmerIds)
                .orderByDesc(HarvestRecord::getId);
        if (farmerId != null) {
            wrapper.eq(HarvestRecord::getUserId, farmerId);
        }
        if (auditStatus != null) {
            wrapper.eq(HarvestRecord::getAuditStatus, auditStatus);
        }
        Page<HarvestRecord> page = harvestRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ResultUtils.success(new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    public BaseResponse<Boolean> audit(AuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        HarvestRecord record = harvestRecordMapper.selectById(req.getId());
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "记录不存在");
        managerFarmerService.validateManagerFarmer(managerId, record.getUserId());
        record.setAuditStatus(req.getAuditStatus());
        harvestRecordMapper.updateById(record);
        String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
        notificationService.send(record.getUserId(), "收获记录审核" + result, "您的收获记录审核" + result, "AUDIT", record.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> batchAudit(BatchAuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        for (Long id : req.getIds()) {
            HarvestRecord record = harvestRecordMapper.selectById(id);
            ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "收获记录不存在，ID：" + id);
            managerFarmerService.validateManagerFarmer(managerId, record.getUserId());
            record.setAuditStatus(req.getAuditStatus());
            harvestRecordMapper.updateById(record);
            String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
            notificationService.send(record.getUserId(), "收获记录审核" + result, "您的收获记录审核" + result, "AUDIT", record.getId());
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    @Lock(lockClass = Client.class)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> upChain(List<Long> ids) {
        Long managerId = TokenUtil.getLoginUserId();
        for (Long id : ids) {
            HarvestRecord record = harvestRecordMapper.selectById(id);
            ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "收获记录不存在，ID：" + id);
            managerFarmerService.validateManagerFarmer(managerId, record.getUserId());
            ThrowUtils.throwIf(record.getAuditStatus() == null || record.getAuditStatus() != 1,
                    ErrorCode.PARAMS_ERROR, "收获记录未审核通过，不能上链，ID：" + id);
            String hash = chainStoreService.upChain("harvest_record", record.getId(), record);
            record.setChainTxHash(hash);
            harvestRecordMapper.updateById(record);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> update(com.water.platform.model.dto.req.HarvestUpdateReq req) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        HarvestRecord record = harvestRecordMapper.selectById(req.getId());
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "记录不存在");
        ThrowUtils.throwIf(!record.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该记录");
        ThrowUtils.throwIf(record.getAuditStatus() != 0, ErrorCode.PARAMS_ERROR, "已审核的记录不可修改");
        if (req.getPondId() != null) record.setPondId(req.getPondId());
        if (req.getHarvestDate() != null) record.setHarvestDate(req.getHarvestDate());
        if (req.getManager() != null) record.setManager(req.getManager());
        if (req.getBatchNo() != null) record.setBatchNo(req.getBatchNo());
        if (req.getSpec() != null) record.setSpec(req.getSpec());
        if (req.getTotalWeight() != null) record.setTotalWeight(req.getTotalWeight());
        if (req.getSurvivalRate() != null) record.setSurvivalRate(req.getSurvivalRate());
        if (req.getBuyerInfo() != null) record.setBuyerInfo(req.getBuyerInfo());
        if (req.getQualityResult() != null) record.setQualityResult(req.getQualityResult());
        if (req.getRemark() != null) record.setRemark(req.getRemark());
        harvestRecordMapper.updateById(record);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> delete(Long id) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        HarvestRecord record = harvestRecordMapper.selectById(id);
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "记录不存在");
        ThrowUtils.throwIf(!record.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该记录");
        ThrowUtils.throwIf(record.getAuditStatus() != 0, ErrorCode.PARAMS_ERROR, "已审核的记录不可删除");
        harvestRecordMapper.deleteById(id);
        return ResultUtils.success(Boolean.TRUE);
    }
}
