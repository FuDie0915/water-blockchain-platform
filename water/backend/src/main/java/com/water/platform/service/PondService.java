package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.PondMapper;
import com.water.platform.model.dto.req.AuditReq;
import com.water.platform.model.dto.req.BatchAuditReq;
import com.water.platform.model.dto.req.PondCreateReq;
import com.water.platform.model.dto.req.PondStatusUpdateReq;
import com.water.platform.model.dto.req.PondUpdateReq;
import com.water.platform.model.entity.Pond;
import com.water.platform.utils.TokenUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.water.platform.base.common.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.fisco.bcos.sdk.client.Client;
import com.water.platform.annotation.Lock;

import java.util.List;

@Service
public class PondService {

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private ManagerFarmerService managerFarmerService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.water.platform.mapper.UserMapper userMapper;

    @Autowired
    private ChainStoreService chainStoreService;

    public BaseResponse<Boolean> create(PondCreateReq req) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        Pond pond = new Pond();
        org.springframework.beans.BeanUtils.copyProperties(req, pond);
        pond.setUserId(userId);
        pond.setAuditStatus(1);
        pond.setCreateTime(DateUtil.date());
        pond.setUpdateTime(DateUtil.date());
        pondMapper.insert(pond);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<List<Pond>> list() {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        List<Pond> list = pondMapper.selectList(new LambdaQueryWrapper<Pond>()
                .eq(Pond::getUserId, userId)
                .orderByDesc(Pond::getId));
        return ResultUtils.success(list);
    }

    public BaseResponse<Boolean> update(PondUpdateReq req) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        Pond pond = pondMapper.selectById(req.getId());
        ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在");
        ThrowUtils.throwIf(!pond.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该养殖池");
        if (req.getPondName() != null) pond.setPondName(req.getPondName());
        if (req.getBreedType() != null) pond.setBreedType(req.getBreedType());
        if (req.getArea() != null) pond.setArea(req.getArea());
        if (req.getDepth() != null) pond.setDepth(req.getDepth());
        if (req.getStartDate() != null) pond.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) pond.setEndDate(req.getEndDate());
        if (req.getWaterTemp() != null) pond.setWaterTemp(req.getWaterTemp());
        if (req.getSalinity() != null) pond.setSalinity(req.getSalinity());
        if (req.getPh() != null) pond.setPh(req.getPh());
        if (req.getDoValue() != null) pond.setDoValue(req.getDoValue());
        if (req.getNh3n() != null) pond.setNh3n(req.getNh3n());
        if (req.getNo2() != null) pond.setNo2(req.getNo2());
        // 如果养殖池已审核通过，修改后重置为待审核
        if (pond.getAuditStatus() != null && pond.getAuditStatus() == 1) {
        pond.setAuditStatus(1);
        }
        pond.setUpdateTime(DateUtil.date());
        pondMapper.updateById(pond);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> updateStatus(PondStatusUpdateReq req) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        Pond pond = pondMapper.selectById(req.getId());
        ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在");
        ThrowUtils.throwIf(!pond.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该养殖池");
        pond.setStatus(req.getStatus());
        pond.setUpdateTime(DateUtil.date());
        pondMapper.updateById(pond);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> delete(Long id) {
        Long userId = TokenUtil.getLoginUserId();
        managerFarmerService.validateFarmerBound(userId);
        Pond pond = pondMapper.selectById(id);
        ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在");
        ThrowUtils.throwIf(!pond.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该养殖池");
        pondMapper.deleteById(id);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<PageResponse<Pond>> managerList(Long farmerId, Integer auditStatus, Long pageNum, Long pageSize) {
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<Pond> wrapper = new LambdaQueryWrapper<Pond>()
                .in(Pond::getUserId, farmerIds)
                .orderByDesc(Pond::getId);
        if (farmerId != null) {
            wrapper.eq(Pond::getUserId, farmerId);
        }
        if (auditStatus != null) {
            wrapper.eq(Pond::getAuditStatus, auditStatus);
        }
        Page<Pond> page = pondMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ResultUtils.success(new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    public BaseResponse<Boolean> audit(AuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        Pond pond = pondMapper.selectById(req.getId());
        ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "记录不存在");
        managerFarmerService.validateManagerFarmer(managerId, pond.getUserId());
        pond.setAuditStatus(req.getAuditStatus());
        pond.setUpdateTime(DateUtil.date());
        pondMapper.updateById(pond);
        String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
        notificationService.send(pond.getUserId(), "养殖池审核" + result, "您的养殖池\"" + pond.getPondName() + "\"审核" + result, "AUDIT", pond.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> batchAudit(BatchAuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        for (Long id : req.getIds()) {
            Pond pond = pondMapper.selectById(id);
            ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在，ID：" + id);
            managerFarmerService.validateManagerFarmer(managerId, pond.getUserId());
            pond.setAuditStatus(req.getAuditStatus());
            pond.setUpdateTime(DateUtil.date());
            pondMapper.updateById(pond);
            String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
            notificationService.send(pond.getUserId(), "养殖池审核" + result, "您的养殖池\"" + pond.getPondName() + "\"审核" + result, "AUDIT", pond.getId());
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    @Lock(lockClass = Client.class)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> upChain(List<Long> ids) {
        Long managerId = TokenUtil.getLoginUserId();
        for (Long id : ids) {
            Pond pond = pondMapper.selectById(id);
            ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在，ID：" + id);
            managerFarmerService.validateManagerFarmer(managerId, pond.getUserId());
            ThrowUtils.throwIf(pond.getAuditStatus() == null || pond.getAuditStatus() != 1,
                    ErrorCode.PARAMS_ERROR, "养殖池未审核通过，不能上链，ID：" + id);
            String hash = chainStoreService.upChain("pond", pond.getId(), pond);
            pond.setChainTxHash(hash);
            pondMapper.updateById(pond);
        }
        return ResultUtils.success(Boolean.TRUE);
    }
}
