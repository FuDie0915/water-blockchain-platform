package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.ManagerAuditMapper;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.dto.req.ManagerAuditActionReq;
import com.water.platform.model.dto.req.ManagerAuditReq;
import com.water.platform.model.entity.ManagerAudit;
import com.water.platform.model.entity.User;
import com.water.platform.utils.TokenUtil;
import org.fisco.bcos.sdk.client.Client;
import com.water.platform.annotation.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerAuditService {

    @Autowired
    private ManagerAuditMapper managerAuditMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChainStoreService chainStoreService;

    /**
     * 监管局提交审核申请
     */
    public BaseResponse<Boolean> submit(ManagerAuditReq req) {
        Long userId = TokenUtil.getLoginUserId();
        // 检查是否已有待审核的申请
        ManagerAudit existing = managerAuditMapper.selectOne(new LambdaQueryWrapper<ManagerAudit>()
                .eq(ManagerAudit::getUserId, userId)
                .eq(ManagerAudit::getStatus, 0));
        ThrowUtils.throwIf(existing != null, ErrorCode.PARAMS_ERROR, "您已有待审核的申请，请等待管理员审核");
        // 检查是否已通过审核
        ManagerAudit approved = managerAuditMapper.selectOne(new LambdaQueryWrapper<ManagerAudit>()
                .eq(ManagerAudit::getUserId, userId)
                .eq(ManagerAudit::getStatus, 1));
        ThrowUtils.throwIf(approved != null, ErrorCode.PARAMS_ERROR, "您已通过审核，无需重复提交");

        ManagerAudit audit = new ManagerAudit();
        audit.setUserId(userId);
        audit.setInstitutionName(req.getInstitutionName());
        audit.setJurisdiction(req.getJurisdiction());
        audit.setPhone(req.getPhone());
        audit.setRemark(req.getRemark());
        audit.setStatus(0);
        audit.setCreateTime(DateUtil.date());
        managerAuditMapper.insert(audit);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 监管局查看自己的审核状态
     */
    public BaseResponse<ManagerAudit> getStatus() {
        Long userId = TokenUtil.getLoginUserId();
        ManagerAudit audit = managerAuditMapper.selectOne(new LambdaQueryWrapper<ManagerAudit>()
                .eq(ManagerAudit::getUserId, userId)
                .orderByDesc(ManagerAudit::getId)
                .last("LIMIT 1"));
        return ResultUtils.success(audit);
    }

    /**
     * 管理员查看待审核列表（支持按状态筛选，分页）
     */
    public BaseResponse<PageResponse<ManagerAudit>> adminList(Integer status, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<ManagerAudit> wrapper = new LambdaQueryWrapper<ManagerAudit>()
                .orderByDesc(ManagerAudit::getId);
        if (status != null) {
            wrapper.eq(ManagerAudit::getStatus, status);
        }
        Page<ManagerAudit> page = managerAuditMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ResultUtils.success(new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 管理员批准审核
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> approve(ManagerAuditActionReq req) {
        Long adminUserId = TokenUtil.getLoginUserId();
        ManagerAudit audit = managerAuditMapper.selectById(req.getId());
        ThrowUtils.throwIf(audit == null, ErrorCode.NOT_FOUND_ERROR, "审核申请不存在");
        ThrowUtils.throwIf(audit.getStatus() != 0, ErrorCode.PARAMS_ERROR, "该申请不在待审核状态");

        // 更新审核记录
        audit.setStatus(1);
        audit.setAuditUserId(adminUserId);
        audit.setAuditRemark(req.getAuditRemark());
        audit.setUpdateTime(DateUtil.date());
        managerAuditMapper.updateById(audit);

        // 启用监管局用户
        User user = userMapper.selectById(audit.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        user.setUserStatus(1);
        userMapper.updateById(user);

        notificationService.send(audit.getUserId(), "注册审批通过", "您的监管局注册申请已审批通过，账号已启用", "BINDING", audit.getId());

        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 管理员拒绝审核
     */
    public BaseResponse<Boolean> reject(ManagerAuditActionReq req) {
        Long adminUserId = TokenUtil.getLoginUserId();
        ManagerAudit audit = managerAuditMapper.selectById(req.getId());
        ThrowUtils.throwIf(audit == null, ErrorCode.NOT_FOUND_ERROR, "审核申请不存在");
        ThrowUtils.throwIf(audit.getStatus() != 0, ErrorCode.PARAMS_ERROR, "该申请不在待审核状态");

        audit.setStatus(2);
        audit.setAuditUserId(adminUserId);
        audit.setAuditRemark(req.getAuditRemark());
        audit.setUpdateTime(DateUtil.date());
        managerAuditMapper.updateById(audit);
        notificationService.send(audit.getUserId(), "注册审批未通过", "您的监管局注册申请未通过审批", "BINDING", audit.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    @Lock(lockClass = Client.class)
    public BaseResponse<Boolean> upChain(Long id) {
        ManagerAudit audit = managerAuditMapper.selectById(id);
        ThrowUtils.throwIf(audit == null, ErrorCode.NOT_FOUND_ERROR, "审核申请不存在");
        ThrowUtils.throwIf(audit.getStatus() == null || audit.getStatus() != 1,
                ErrorCode.PARAMS_ERROR, "该申请未通过审批，不能上链");
        String hash = chainStoreService.upChain("manager_audit", audit.getId(), audit);
        audit.setChainTxHash(hash);
        managerAuditMapper.updateById(audit);
        return ResultUtils.success(Boolean.TRUE);
    }
}
