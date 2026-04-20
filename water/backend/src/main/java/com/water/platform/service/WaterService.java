package com.water.platform.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.water.platform.annotation.Lock;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.chain.call.WaterRawCall;
import com.water.platform.chain.util.ChainUtil;
import com.water.platform.constant.ContractType;
import com.water.platform.constant.UserRole;
import com.water.platform.mapper.UserMapper;
import com.water.platform.mapper.WaterDataMapper;
import com.water.platform.mapper.PondMapper;
import com.water.platform.model.dto.resp.WaterStep1InfoResp;
import com.water.platform.model.entity.CompanyCert;
import com.water.platform.model.entity.User;
import com.water.platform.model.entity.UserContract;
import com.water.platform.model.entity.WaterData;
import com.water.platform.utils.AESUtil;
import com.water.platform.utils.BeanConvertUtils;
import com.water.platform.utils.TokenUtil;
import com.water.platform.mapper.CompanyCertMapper;
import com.water.platform.model.dto.resp.CompanyCertResp;
import com.water.platform.model.dto.resp.WaterDataResp;
import com.water.platform.model.dto.req.*;
import com.water.platform.service.NotificationService;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：devon
 * @date ：2024/11/15 9:30
 * @description：水质业务逻辑类
 * @version: 1.0
 */
@Service
public class WaterService {

    @Autowired
    private UserService userService;
    @Autowired
    private UserContractService userContractService;
    @Autowired
    private CompanyCertMapper companyCertMapper;
    @Autowired
    private WaterDataMapper waterDataMapper;
    @Autowired
    private WarningThresholdService warningThresholdService;
    @Autowired
    private ManagerFarmerService managerFarmerService;
    @Autowired
    private PondMapper pondMapper;
    @Autowired(required = false)
    private Client client;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private com.water.platform.chain.config.SystemConfig systemConfig;

    /**
     * 检查区块链是否启用
     */
    public boolean isBlockchainEnabled() {
        return client != null && systemConfig.isEnabled();
    }

    /**
     * 获取 WaterRawCall 合约调用对象
     * 使用当前登录用户自己的区块链私钥签名交易
     */
    private WaterRawCall getWaterRawCall() {
        if (!isBlockchainEnabled()) {
            throw new com.water.platform.base.exception.BusinessException(1, "区块链功能已禁用");
        }
        UserContract userContract = userContractService.query(new UserContractQueryReq(ContractType.WATER)).getData();
        ThrowUtils.throwIf(userContract == null, ErrorCode.NOT_FOUND_ERROR);
        Long userId = TokenUtil.getLoginUserId();
        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return WaterRawCall.load(userContract.getContractAddress(), client,
                client.getCryptoSuite().createKeyPair(user.getPrivateKey()));
    }

    public BaseResponse<Boolean> commit(PermissionCommitReq permissionCommitReq) {
        Long userId = TokenUtil.getLoginUserId();
        boolean exists = companyCertMapper.exists(new LambdaQueryWrapper<CompanyCert>()
                .eq(CompanyCert::getUserId, userId)
                .and(companyCertLambdaQueryWrapper -> {
                    companyCertLambdaQueryWrapper.eq(CompanyCert::getStatus, 0).or().eq(CompanyCert::getStatus, 1);
                }));
        ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR);
        CompanyCert companyCert = new CompanyCert(null, userId, permissionCommitReq.getImageUrl(), 0, DateUtil.date(), null);
        companyCertMapper.insert(companyCert);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<List<CompanyCertResp>> companyQuery() {
        Long userId = TokenUtil.getLoginUserId();
        List<CompanyCert> companyCert = companyCertMapper.selectList(new LambdaQueryWrapper<CompanyCert>()
                .eq(CompanyCert::getUserId, userId)
                .orderByDesc(CompanyCert::getId));
        User user = userMapper.selectById(userId);
        if (companyCert != null) {
            List<CompanyCertResp> companyCertResps = BeanConvertUtils.convertListTo(companyCert, CompanyCertResp::new);
            companyCertResps.forEach(companyCertResp -> companyCertResp.setCompanyName(user.getUserName()));
            return ResultUtils.success(companyCertResps);
        }
        else return ResultUtils.success(null);
    }


    @Lock(lockClass = Client.class)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> verify(PermissionVerifyReq permissionVerifyReq) {
        CompanyCert companyCert = companyCertMapper.selectOne(new LambdaQueryWrapper<CompanyCert>()
                .eq(CompanyCert::getId, permissionVerifyReq.getId())
                .eq(CompanyCert::getStatus, 0));
        ThrowUtils.throwIf(companyCert == null, ErrorCode.VERIFY_FAILED);
        companyCert.setStatus(permissionVerifyReq.getStatus());
        companyCertMapper.updateById(companyCert);
        if (permissionVerifyReq.getStatus() == 1 && isBlockchainEnabled()) { // 审核通过许可证上链
            WaterRawCall waterRawCall = getWaterRawCall();
            TransactionReceipt transactionReceipt = waterRawCall.uploadCompanyCert(BigInteger.valueOf(companyCert.getId()), BigInteger.valueOf(companyCert.getUserId()),
                    companyCert.getImageUrl(), BigInteger.valueOf(companyCert.getCreateTime().getTime()));
            ChainUtil.check(transactionReceipt);
        }
        String certResult = permissionVerifyReq.getStatus() == 1 ? "通过" : "未通过";
        String extra = (permissionVerifyReq.getStatus() == 1 && isBlockchainEnabled()) ? "，许可证已上链" : "";
        notificationService.send(companyCert.getUserId(), "排污许可证审核" + certResult, "您的排污许可证审核" + certResult + extra, "AUDIT", companyCert.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<CompanyCertResp> isOnChain(Long permissionId) {
        // 1. 从数据库查许可证
        CompanyCert companyCert = companyCertMapper.selectById(permissionId);
        ThrowUtils.throwIf(companyCert == null, ErrorCode.NOT_FOUND_ERROR, "许可证不存在");

        // 2. 从链上读取该用户的许可证列表
        boolean found = false;
        if (isBlockchainEnabled()) {
            WaterRawCall waterRawCall = getWaterRawCall();
            try {
                DynamicArray<WaterRawCall.Struct1> chainCerts = waterRawCall.getCompanyCertByUserId(BigInteger.valueOf(companyCert.getUserId()));
                // 3. 在链上数据中查找匹配的许可证
                if (chainCerts != null && chainCerts.getValue() != null) {
                    for (WaterRawCall.Struct1 chainCert : chainCerts.getValue()) {
                        if (chainCert.id.longValue() == permissionId.longValue()) {
                            found = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // 链上读取失败，视为未找到
                found = false;
            }
        }

        // 4. 返回结果
        CompanyCertResp resp = BeanConvertUtils.convertTo(companyCert, CompanyCertResp::new);
        User user = userMapper.selectById(companyCert.getUserId());
        if (user != null) {
            resp.setCompanyName(user.getUserName());
        }
        resp.setIsOnChain(found);
        return ResultUtils.success(resp);
    }

    /**
     * 监管局查询所有用户的许可证
     */
    public BaseResponse<List<CompanyCertResp>> managerAllQuery(Integer status) {
        LambdaQueryWrapper<CompanyCert> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CompanyCert::getStatus, status);
        }
        wrapper.orderByDesc(CompanyCert::getId);
        List<CompanyCert> certs = companyCertMapper.selectList(wrapper);
        List<CompanyCertResp> resps = BeanConvertUtils.convertListTo(certs, CompanyCertResp::new);
        // 为每条记录填充企业名称
        resps.forEach(resp -> {
            User certUser = userMapper.selectById(resp.getUserId());
            if (certUser != null) {
                resp.setCompanyName(certUser.getUserName());
            }
        });
        return ResultUtils.success(resps);
    }

    /**
     * 养殖户删除许可证（仅允许删除被拒绝的）
     */
    public BaseResponse<Boolean> deleteCert(Long certId) {
        Long userId = TokenUtil.getLoginUserId();
        CompanyCert cert = companyCertMapper.selectOne(new LambdaQueryWrapper<CompanyCert>()
                .eq(CompanyCert::getId, certId)
                .eq(CompanyCert::getUserId, userId));
        ThrowUtils.throwIf(cert == null, ErrorCode.NOT_FOUND_ERROR, "许可证不存在");
        ThrowUtils.throwIf(cert.getStatus() != 2, ErrorCode.PARAMS_ERROR, "仅允许删除被拒绝的许可证");
        companyCertMapper.deleteById(certId);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 删除水质数据（仅允许删除未上链的）
     */
    public BaseResponse<Boolean> deleteWaterData(Long dataId) {
        Long userId = TokenUtil.getLoginUserId();
        WaterData waterData = waterDataMapper.selectOne(new LambdaQueryWrapper<WaterData>()
                .eq(WaterData::getId, dataId)
                .eq(WaterData::getUserId, userId)
                .eq(WaterData::getIsOnChain, false));
        ThrowUtils.throwIf(waterData == null, ErrorCode.NOT_FOUND_ERROR, "水质数据不存在或已上链");
        waterDataMapper.deleteById(dataId);
        return ResultUtils.success(Boolean.TRUE);
    }


    public BaseResponse<Boolean> gen(WaterDataTypeReq waterDataTypeReq) {
        Long userId = TokenUtil.getLoginUserId();
        WaterData waterData = new WaterData(null, userId, waterDataTypeReq.getDataType(),
                String.valueOf(NumberUtil.round(Math.random() * 9 + 1, 2)), "正常", null, DateUtil.date(), false, 1);
        waterDataMapper.insert(waterData);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> collect(WaterDataCollectReq waterDataCollectReq){
        // 自动预警判断：如果配置了阈值，根据数据值自动判断是否异常
        String status = waterDataCollectReq.getStatus();
        Long managerId = null;
        try {
            Double value = Double.parseDouble(waterDataCollectReq.getData());
            // 查找该养殖户绑定的监管局ID
            try {
                com.water.platform.model.entity.ManagerFarmer bind =
                        managerFarmerService.validateFarmerBound(waterDataCollectReq.getUserId());
                if (bind != null) {
                    managerId = bind.getManagerId();
                }
            } catch (Exception ignored) {
                // 养殖户未绑定监管局，不做预警判断
            }
            if (managerId != null && warningThresholdService.isAbnormal(waterDataCollectReq.getDataType(), value, managerId)) {
                status = "异常";
            }
        } catch (NumberFormatException ignored) {
        }
        WaterData waterData = new WaterData(null, waterDataCollectReq.getUserId(),
                waterDataCollectReq.getDataType(), waterDataCollectReq.getData(), status, null, DateUtil.date(), false, 1);
        waterDataMapper.insert(waterData); // 入库
        if ("异常".equals(status) && managerId != null) {
            notificationService.send(waterDataCollectReq.getUserId(), "水质异常预警", "您的养殖池水质数据异常，请及时关注", "WARNING");
            notificationService.send(managerId, "管辖养殖户水质异常", "您管辖的养殖户水质数据出现异常，请及时处理", "WARNING");
        }
        return ResultUtils.success(Boolean.TRUE); // 返回
    }

    public PageResponse<WaterDataResp> page(Integer dataType, Long pageNum, Long pageSize) {
        Long userId = TokenUtil.getLoginUserId();
        Page<WaterData> page = waterDataMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<WaterData>()
                .eq(WaterData::getUserId, userId)
                .eq(WaterData::getDataType, dataType)
                .orderByDesc(WaterData::getId));
        PageResponse<WaterDataResp> pageResponse = new PageResponse<>(0, new ArrayList<>());
        BeanUtils.copyProperties(page, pageResponse);
        pageResponse.setData(BeanConvertUtils.convertListTo(page.getRecords(), WaterDataResp::new));
        return pageResponse;
    }

    /**
     * 水数据上链
     * @param waterDataUpChainReq 水数据上链入参
     * @return 上链结果
     */
    @Lock(lockClass = Client.class) // 加锁
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public BaseResponse<Boolean> upChain(WaterDataUpChainReq waterDataUpChainReq) {
        if (!isBlockchainEnabled()) {
            throw new com.water.platform.base.exception.BusinessException(1, "区块链功能已禁用");
        }
        Long userId = TokenUtil.getLoginUserId();
        List<Long> idList = waterDataUpChainReq.getId();
        List<WaterData> waterDataList = waterDataMapper.selectList(new LambdaQueryWrapper<WaterData>()
                .eq(WaterData::getUserId, userId)
                .eq(WaterData::getIsOnChain, false)
                .in(WaterData::getId, idList));
        ThrowUtils.throwIf(CollectionUtil.isEmpty(waterDataList) ||
                idList.size() != waterDataList.size(), ErrorCode.OPERATION_ERROR);
        WaterRawCall waterRawCall = getWaterRawCall();
        for (WaterData waterData : waterDataList) {
            String encryptData = AESUtil.encrypt(waterData.getData());
            TransactionReceipt transactionReceipt = waterRawCall.insertWaterData(BigInteger.valueOf(waterData.getId()), BigInteger.valueOf(waterData.getUserId()),
                    BigInteger.valueOf(waterData.getDataType()), encryptData, waterData.getStatus(), BigInteger.valueOf(waterData.getTime().getTime()));
            ChainUtil.check(transactionReceipt);
            waterData.setHash(transactionReceipt.getTransactionHash());
            waterData.setOnChain(true);
            waterDataMapper.updateById(waterData);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<WaterStep1InfoResp> step1Info() {
        User currentUser = TokenUtil.getLoginUser();
        List<User> users = userService.selectUserListByAccount(currentUser.getUserAccount());
        ThrowUtils.throwIf(CollectionUtil.isEmpty(users), ErrorCode.NOT_FOUND_ERROR);
        User farmersUser = users.stream()
                .filter(u -> u.getUserRole().equals(UserRole.FARMERS))
                .findFirst().orElse(null);
        User managerUser = users.stream()
                .filter(u -> u.getUserRole().equals(UserRole.MANAGER))
                .findFirst().orElse(null);
        ThrowUtils.throwIf(farmersUser == null || managerUser == null, ErrorCode.NOT_FOUND_ERROR,
                "当前账号需要同时注册养殖户和监管局角色");
        WaterStep1InfoResp resp = new WaterStep1InfoResp(
                farmersUser.getUserAccount(), farmersUser.getAccountAddress(),
                managerUser.getUserAccount(), managerUser.getAccountAddress());
        return ResultUtils.success(resp);
    }

    /**
     * 养殖户手动上报水质数据（需监管局审核）
     */
    public BaseResponse<Boolean> manual(com.water.platform.model.dto.req.WaterDataManualReq req) {
        Long userId = TokenUtil.getLoginUserId();
        // 1. 校验养殖户已绑定监管局
        com.water.platform.model.entity.ManagerFarmer bind = managerFarmerService.validateFarmerBound(userId);
        // 2. 校验养殖池归属
        com.water.platform.model.entity.Pond pond = pondMapper.selectById(req.getPondId());
        ThrowUtils.throwIf(pond == null, ErrorCode.NOT_FOUND_ERROR, "养殖池不存在");
        ThrowUtils.throwIf(!pond.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该养殖池");
        // 3. 根据监管局阈值判断是否异常
        String status = "正常";
        try {
            Double value = Double.parseDouble(req.getData());
            if (warningThresholdService.isAbnormal(req.getDataType(), value, bind.getManagerId())) {
                status = "异常";
            }
        } catch (NumberFormatException ignored) {
        }
        // 4. 入库（待审核状态）
        WaterData waterData = new WaterData(null, userId, req.getDataType(), req.getData(), status, null, cn.hutool.core.date.DateUtil.date(), false, 0);
        waterDataMapper.insert(waterData);
        if ("异常".equals(status)) {
            notificationService.send(userId, "水质异常预警", "您上报的水质数据异常，请及时关注", "WARNING");
            notificationService.send(bind.getManagerId(), "管辖养殖户水质异常", "您管辖的养殖户水质数据出现异常，请及时处理", "WARNING");
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 监管局查看管辖养殖户的水质数据
     */
    public BaseResponse<PageResponse<WaterDataResp>> managerDataList(Long farmerId, Integer auditStatus, Long pageNum, Long pageSize) {
        Long managerId = TokenUtil.getLoginUserId();
        java.util.List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<WaterData> wrapper = new LambdaQueryWrapper<WaterData>()
                .in(WaterData::getUserId, farmerIds)
                .orderByDesc(WaterData::getId);
        if (farmerId != null) wrapper.eq(WaterData::getUserId, farmerId);
        if (auditStatus != null) wrapper.eq(WaterData::getAuditStatus, auditStatus);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<WaterData> page =
                waterDataMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper);
        PageResponse<WaterDataResp> pageResponse = new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), new java.util.ArrayList<>());
        if (page.getRecords() != null) {
            java.util.List<WaterDataResp> resps = new java.util.ArrayList<>();
            for (WaterData wd : page.getRecords()) {
                WaterDataResp resp = new WaterDataResp();
                org.springframework.beans.BeanUtils.copyProperties(wd, resp);
                resps.add(resp);
            }
            pageResponse.setData(resps);
        }
        return ResultUtils.success(pageResponse);
    }

    /**
     * 监管局审核水质数据
     */
    public BaseResponse<Boolean> auditWaterData(com.water.platform.model.dto.req.AuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        WaterData waterData = waterDataMapper.selectById(req.getId());
        ThrowUtils.throwIf(waterData == null, ErrorCode.NOT_FOUND_ERROR, "水质数据不存在");
        managerFarmerService.validateManagerFarmer(managerId, waterData.getUserId());
        ThrowUtils.throwIf(waterData.getAuditStatus() == null || waterData.getAuditStatus() != 0,
                ErrorCode.PARAMS_ERROR, "该数据不在待审核状态");
        waterData.setAuditStatus(req.getAuditStatus());
        waterDataMapper.updateById(waterData);
        String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
        notificationService.send(waterData.getUserId(), "水质数据审核" + result, "您的水质数据审核" + result, "AUDIT", waterData.getId());
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 监管局批量审核水质数据
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> batchAuditWaterData(com.water.platform.model.dto.req.BatchAuditReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        for (Long id : req.getIds()) {
            WaterData waterData = waterDataMapper.selectById(id);
            ThrowUtils.throwIf(waterData == null, ErrorCode.NOT_FOUND_ERROR, "水质数据不存在，ID：" + id);
            managerFarmerService.validateManagerFarmer(managerId, waterData.getUserId());
            ThrowUtils.throwIf(waterData.getAuditStatus() == null || waterData.getAuditStatus() != 0,
                    ErrorCode.PARAMS_ERROR, "该数据不在待审核状态，ID：" + id);
            waterData.setAuditStatus(req.getAuditStatus());
            waterDataMapper.updateById(waterData);
            String result = req.getAuditStatus() == 1 ? "通过" : "未通过";
            notificationService.send(waterData.getUserId(), "水质数据审核" + result, "您的水质数据审核" + result, "AUDIT", waterData.getId());
        }
        return ResultUtils.success(Boolean.TRUE);
    }
}
