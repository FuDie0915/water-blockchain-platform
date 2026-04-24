package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.ManagerFarmerMapper;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.dto.req.BindApplyReq;
import com.water.platform.model.dto.resp.BindStatusResp;
import com.water.platform.model.entity.ManagerFarmer;
import com.water.platform.model.entity.User;
import com.water.platform.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagerFarmerService {

    @Autowired
    private ManagerFarmerMapper managerFarmerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChainStoreService chainStoreService;

    /**
     * 获取所有已注册监管局列表
     */
    public BaseResponse<List<User>> listManagers() {
        List<User> managers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getUserRole, "manager")
                .eq(User::getUserStatus, 1));
        return ResultUtils.success(managers);
    }

    /**
     * 养殖户申请绑定监管局
     */
    public BaseResponse<Boolean> apply(BindApplyReq req) {
        Long farmerId = TokenUtil.getLoginUserId();
        // 校验目标用户确实是监管局
        User manager = userMapper.selectById(req.getManagerId());
        ThrowUtils.throwIf(manager == null, ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");
        ThrowUtils.throwIf(!"manager".equals(manager.getUserRole()), ErrorCode.PARAMS_ERROR, "目标用户不是监管局");
        // 校验养殖户是否已有绑定（status != 2 的记录，即待审核或已通过的记录）
        ManagerFarmer existing = managerFarmerMapper.selectOne(new LambdaQueryWrapper<ManagerFarmer>()
                .eq(ManagerFarmer::getFarmerId, farmerId)
                .ne(ManagerFarmer::getStatus, 2));
        ThrowUtils.throwIf(existing != null, ErrorCode.PARAMS_ERROR, "您已有绑定记录，不能重复申请");
        ManagerFarmer bind = new ManagerFarmer();
        bind.setManagerId(req.getManagerId());
        bind.setFarmerId(farmerId);
        bind.setStatus(0);
        bind.setCreateTime(DateUtil.date());
        bind.setUpdateTime(DateUtil.date());
        managerFarmerMapper.insert(bind);
        notificationService.send(req.getManagerId(), "新的绑定申请", "养殖户申请与您建立绑定关系，请及时审核", "BINDING", bind.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 养殖户查看自己的绑定状态
     */
    public BaseResponse<BindStatusResp> getStatus() {
        Long farmerId = TokenUtil.getLoginUserId();
        ManagerFarmer bind = managerFarmerMapper.selectOne(new LambdaQueryWrapper<ManagerFarmer>()
                .eq(ManagerFarmer::getFarmerId, farmerId)
                .orderByDesc(ManagerFarmer::getId)
                .last("LIMIT 1"));
        if (bind == null) {
            return ResultUtils.success(null);
        }
        BindStatusResp resp = new BindStatusResp();
        resp.setId(bind.getId());
        resp.setManagerId(bind.getManagerId());
        resp.setStatus(bind.getStatus());
        User manager = userMapper.selectById(bind.getManagerId());
        if (manager != null) {
            resp.setManagerName(manager.getUserName());
        }
        return ResultUtils.success(resp);
    }

    /**
     * 监管局查看所有绑定申请
     */
    public BaseResponse<List<com.water.platform.model.dto.resp.BindingResp>> managerList() {
        Long managerId = TokenUtil.getLoginUserId();
        List<ManagerFarmer> list = managerFarmerMapper.selectList(new LambdaQueryWrapper<ManagerFarmer>()
                .eq(ManagerFarmer::getManagerId, managerId)
                .orderByDesc(ManagerFarmer::getId));
        List<com.water.platform.model.dto.resp.BindingResp> resps = list.stream().map(mf -> {
            com.water.platform.model.dto.resp.BindingResp resp = new com.water.platform.model.dto.resp.BindingResp();
            resp.setId(mf.getId());
            resp.setManagerId(mf.getManagerId());
            resp.setFarmerId(mf.getFarmerId());
            resp.setStatus(mf.getStatus());
            resp.setCreateTime(mf.getCreateTime());
            resp.setUpdateTime(mf.getUpdateTime());
            User farmer = userMapper.selectById(mf.getFarmerId());
            if (farmer != null) {
                resp.setFarmerName(farmer.getUserName());
                resp.setFarmerAccount(farmer.getUserAccount());
            }
            User manager = userMapper.selectById(mf.getManagerId());
            if (manager != null) {
                resp.setManagerName(manager.getUserName());
            }
            return resp;
        }).collect(java.util.stream.Collectors.toList());
        return ResultUtils.success(resps);
    }

    /**
     * 监管局同意绑定
     */
    public BaseResponse<Boolean> approve(Long id) {
        Long managerId = TokenUtil.getLoginUserId();
        ManagerFarmer bind = managerFarmerMapper.selectById(id);
        ThrowUtils.throwIf(bind == null, ErrorCode.NOT_FOUND_ERROR, "绑定记录不存在");
        ThrowUtils.throwIf(!bind.getManagerId().equals(managerId), ErrorCode.NO_AUTH_ERROR, "无权操作该绑定记录");
        ThrowUtils.throwIf(bind.getStatus() != 0, ErrorCode.PARAMS_ERROR, "该绑定记录不在待审核状态");
        bind.setStatus(1);
        bind.setUpdateTime(DateUtil.date());
        managerFarmerMapper.updateById(bind);
        User manager = userMapper.selectById(managerId);
        String managerName = manager != null ? manager.getUserName() : "监管局";
        notificationService.send(bind.getFarmerId(), "绑定申请已通过", "您与监管局\"" + managerName + "\"的绑定申请已通过", "BINDING", bind.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 监管局拒绝绑定
     */
    public BaseResponse<Boolean> reject(Long id) {
        Long managerId = TokenUtil.getLoginUserId();
        ManagerFarmer bind = managerFarmerMapper.selectById(id);
        ThrowUtils.throwIf(bind == null, ErrorCode.NOT_FOUND_ERROR, "绑定记录不存在");
        ThrowUtils.throwIf(!bind.getManagerId().equals(managerId), ErrorCode.NO_AUTH_ERROR, "无权操作该绑定记录");
        ThrowUtils.throwIf(bind.getStatus() != 0, ErrorCode.PARAMS_ERROR, "该绑定记录不在待审核状态");
        bind.setStatus(2);
        bind.setUpdateTime(DateUtil.date());
        managerFarmerMapper.updateById(bind);
        User manager = userMapper.selectById(managerId);
        String managerName = manager != null ? manager.getUserName() : "监管局";
        notificationService.send(bind.getFarmerId(), "绑定申请已拒绝", "您与监管局\"" + managerName + "\"的绑定申请已拒绝", "BINDING", bind.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 公共方法：校验养殖户是否已绑定通过审核的监管局，返回绑定的 managerFarmer 记录
     */
    public ManagerFarmer validateFarmerBound(Long farmerId) {
        ManagerFarmer bind = managerFarmerMapper.selectOne(new LambdaQueryWrapper<ManagerFarmer>()
                .eq(ManagerFarmer::getFarmerId, farmerId)
                .eq(ManagerFarmer::getStatus, 1));
        ThrowUtils.throwIf(bind == null, ErrorCode.NO_AUTH_ERROR, "您还未绑定监管局或绑定未通过审核，无法新增记录");
        return bind;
    }

    /**
     * 公共方法：获取监管局管辖的所有 farmerId 列表
     */
    public List<Long> getManagedFarmerIds(Long managerId) {
        List<ManagerFarmer> binds = managerFarmerMapper.selectList(new LambdaQueryWrapper<ManagerFarmer>()
                .eq(ManagerFarmer::getManagerId, managerId)
                .eq(ManagerFarmer::getStatus, 1));
        return binds.stream().map(ManagerFarmer::getFarmerId).collect(Collectors.toList());
    }

    /**
     * 公共方法：校验监管局是否有权审核某个 farmer 的记录
     */
    public void validateManagerFarmer(Long managerId, Long farmerId) {
        ManagerFarmer bind = managerFarmerMapper.selectOne(new LambdaQueryWrapper<ManagerFarmer>()
                .eq(ManagerFarmer::getManagerId, managerId)
                .eq(ManagerFarmer::getFarmerId, farmerId)
                .eq(ManagerFarmer::getStatus, 1));
        ThrowUtils.throwIf(bind == null, ErrorCode.NO_AUTH_ERROR, "该养殖户不在您的管辖范围内");
    }

    /**
     * 管理员查看所有绑定关系（分页+筛选）
     */
    public BaseResponse<PageResponse<com.water.platform.model.dto.resp.BindingResp>> adminList(
            Long managerId, Long farmerId, Integer status, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<ManagerFarmer> wrapper = new LambdaQueryWrapper<ManagerFarmer>()
                .orderByDesc(ManagerFarmer::getId);
        if (managerId != null) wrapper.eq(ManagerFarmer::getManagerId, managerId);
        if (farmerId != null) wrapper.eq(ManagerFarmer::getFarmerId, farmerId);
        if (status != null) wrapper.eq(ManagerFarmer::getStatus, status);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ManagerFarmer> page =
                managerFarmerMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper);
        java.util.List<com.water.platform.model.dto.resp.BindingResp> resps = page.getRecords().stream().map(mf -> {
            com.water.platform.model.dto.resp.BindingResp resp = new com.water.platform.model.dto.resp.BindingResp();
            resp.setId(mf.getId());
            resp.setManagerId(mf.getManagerId());
            resp.setFarmerId(mf.getFarmerId());
            resp.setStatus(mf.getStatus());
            resp.setCreateTime(mf.getCreateTime());
            resp.setUpdateTime(mf.getUpdateTime());
            User manager = userMapper.selectById(mf.getManagerId());
            if (manager != null) resp.setManagerName(manager.getUserName());
            User farmer = userMapper.selectById(mf.getFarmerId());
            if (farmer != null) resp.setFarmerName(farmer.getUserName());
            return resp;
        }).collect(java.util.stream.Collectors.toList());
        return ResultUtils.success(new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), resps));
    }

    /**
     * 监管局主动解除与养殖户的绑定关系
     */
    public BaseResponse<Boolean> managerUnbind(Long id) {
        Long managerId = TokenUtil.getLoginUserId();
        ManagerFarmer bind = managerFarmerMapper.selectById(id);
        ThrowUtils.throwIf(bind == null, ErrorCode.NOT_FOUND_ERROR, "绑定记录不存在");
        ThrowUtils.throwIf(!bind.getManagerId().equals(managerId), ErrorCode.NO_AUTH_ERROR, "无权操作该绑定记录");
        ThrowUtils.throwIf(bind.getStatus() == null || bind.getStatus() != 1,
                ErrorCode.PARAMS_ERROR, "该绑定记录未通过审批，无法解除");
        managerFarmerMapper.deleteById(bind.getId());
        notificationService.send(bind.getFarmerId(), "监管局已解除绑定",
                "您与监管局的绑定关系已被解除", "BINDING", bind.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 养殖户主动解绑当前监管局
     */
    public BaseResponse<Boolean> unbind() {
        Long farmerId = TokenUtil.getLoginUserId();
        ManagerFarmer bind = managerFarmerMapper.selectOne(new LambdaQueryWrapper<ManagerFarmer>()
                .eq(ManagerFarmer::getFarmerId, farmerId)
                .eq(ManagerFarmer::getStatus, 1));
        ThrowUtils.throwIf(bind == null, ErrorCode.NOT_FOUND_ERROR, "当前没有已通过的绑定关系");
        // 逻辑删除：MP 配置了 logic-delete-field: isDelete，removeById 会自动 SET isDelete=1
        managerFarmerMapper.deleteById(bind.getId());
        notificationService.send(bind.getManagerId(), "养殖户已解绑",
                "您管辖的养殖户已与您解除绑定关系", "BINDING", bind.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    @com.water.platform.annotation.Lock(lockClass = org.fisco.bcos.sdk.client.Client.class)
    public BaseResponse<Boolean> upChain(Long id) {
        Long managerId = TokenUtil.getLoginUserId();
        ManagerFarmer bind = managerFarmerMapper.selectById(id);
        ThrowUtils.throwIf(bind == null, ErrorCode.NOT_FOUND_ERROR, "绑定记录不存在");
        ThrowUtils.throwIf(!bind.getManagerId().equals(managerId), ErrorCode.NO_AUTH_ERROR, "无权操作该绑定记录");
        ThrowUtils.throwIf(bind.getStatus() == null || bind.getStatus() != 1,
                ErrorCode.PARAMS_ERROR, "该绑定记录未通过审批，不能上链");

        User farmer = userMapper.selectById(bind.getFarmerId());
        User manager = userMapper.selectById(bind.getManagerId());
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("bindId", bind.getId());
        data.put("managerId", bind.getManagerId());
        data.put("managerName", manager != null ? manager.getUserName() : null);
        data.put("farmerId", bind.getFarmerId());
        data.put("farmerName", farmer != null ? farmer.getUserName() : null);
        data.put("farmerAccount", farmer != null ? farmer.getUserAccount() : null);
        data.put("status", bind.getStatus());
        data.put("createTime", bind.getCreateTime());

        String hash = chainStoreService.upChain("manager_farmer", bind.getId(), data);
        bind.setChainTxHash(hash);
        managerFarmerMapper.updateById(bind);
        return ResultUtils.success(Boolean.TRUE);
    }
}
