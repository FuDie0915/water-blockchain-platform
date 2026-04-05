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
import com.water.platform.mapper.WaterDataMapper;
import com.water.platform.model.dto.resp.UserLoginResp;
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
    private Client client;

    private WaterRawCall getWaterRawCall(String userType) {
        UserContract userContract = userContractService.query(new UserContractQueryReq(ContractType.WATER)).getData();
        ThrowUtils.throwIf(userContract == null, ErrorCode.NOT_FOUND_ERROR);
        List<User> users = userService.selectUserListById(userContract.getUserId());
        User user = users.stream().filter(u -> u.getUserRole().equals(userType)).collect(Collectors.toList()).get(0);
        return WaterRawCall.load(userContract.getContractAddress(), client, client.getCryptoSuite().createKeyPair(user.getPrivateKey()));
    }

    public BaseResponse<UserLoginResp> waterUserLogin(UserLoginReq userLoginReq) {
        return userService.waterUserLogin(userLoginReq);
    }

    public BaseResponse<Boolean> commit(PermissionCommitReq permissionCommitReq) {
        Long userId = TokenUtil.getLoginUserId();
        boolean exists = companyCertMapper.exists(new LambdaQueryWrapper<CompanyCert>()
                .eq(CompanyCert::getUserId, userId)
                .and(companyCertLambdaQueryWrapper -> {
                    companyCertLambdaQueryWrapper.eq(CompanyCert::getStatus, 0).or().eq(CompanyCert::getStatus, 1);
                }));
        ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR);
        CompanyCert companyCert = new CompanyCert(null, userId, permissionCommitReq.getImageUrl(), 0, DateUtil.date());
        companyCertMapper.insert(companyCert);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<List<CompanyCertResp>> companyQuery() {
        Long userId = TokenUtil.getLoginUserId();
        List<CompanyCert> companyCert = companyCertMapper.selectList(new LambdaQueryWrapper<CompanyCert>()
                .eq(CompanyCert::getUserId, userId)
                .orderByDesc(CompanyCert::getId));
        List<User> users = userService.selectUserListById(userId);
        User user = users.stream().filter(u -> u.getUserRole().equals(UserRole.COMPANY)).collect(Collectors.toList()).get(0);
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
        if (permissionVerifyReq.getStatus() == 1) { // 审核通过许可证上链
            WaterRawCall waterRawCall = getWaterRawCall(UserRole.MANAGER);
            TransactionReceipt transactionReceipt = waterRawCall.uploadCompanyCert(BigInteger.valueOf(companyCert.getId()), BigInteger.valueOf(companyCert.getUserId()),
                    companyCert.getImageUrl(), BigInteger.valueOf(companyCert.getCreateTime().getTime()));
            ChainUtil.check(transactionReceipt);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<CompanyCertResp> isOnChain(Long permissionId) {
        CompanyCert companyCert = companyCertMapper.selectOne(new LambdaQueryWrapper<CompanyCert>()
                .eq(CompanyCert::getId, permissionId)
                .eq(CompanyCert::getStatus, 1));
        ThrowUtils.throwIf(companyCert == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(BeanConvertUtils.convertTo(companyCert, CompanyCertResp::new));
    }


    public BaseResponse<Boolean> gen(WaterDataTypeReq waterDataTypeReq) {
        Long userId = TokenUtil.getLoginUserId();
        WaterData waterData = new WaterData(null, userId, waterDataTypeReq.getDataType(),
                String.valueOf(NumberUtil.round(Math.random() * 9 + 1, 2)), "正常", null, DateUtil.date(), false);
        waterDataMapper.insert(waterData);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<Boolean> collect(WaterDataCollectReq waterDataCollectReq){
        WaterData waterData = new WaterData(null, waterDataCollectReq.getUserId(),
                waterDataCollectReq.getDataType(), waterDataCollectReq.getStatus(), waterDataCollectReq.getData(),null, DateUtil.date(), false);
        waterDataMapper.insert(waterData); // 入库
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
        Long userId = TokenUtil.getLoginUserId();
        List<Long> idList = waterDataUpChainReq.getId();
        List<WaterData> waterDataList = waterDataMapper.selectList(new LambdaQueryWrapper<WaterData>()
                .eq(WaterData::getUserId, userId)
                .eq(WaterData::getIsOnChain, false)
                .in(WaterData::getId, idList));
        ThrowUtils.throwIf(CollectionUtil.isEmpty(waterDataList) ||
                idList.size() != waterDataList.size(), ErrorCode.OPERATION_ERROR);
        WaterRawCall waterRawCall = getWaterRawCall(UserRole.MANAGER);
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
        Long userId = TokenUtil.getLoginUserId();
        List<User> users = userService.selectUserListById(userId);
        ThrowUtils.throwIf(CollectionUtil.isEmpty(users), ErrorCode.NOT_FOUND_ERROR);
        User companyUser = users.stream().filter(u -> u.getUserRole().equals(UserRole.COMPANY)).collect(Collectors.toList()).get(0);
        User managerUser = users.stream().filter(u -> u.getUserRole().equals(UserRole.MANAGER)).collect(Collectors.toList()).get(0);
        WaterStep1InfoResp waterStep1InfoResp = new WaterStep1InfoResp(companyUser.getUserAccount(), companyUser.getUserPassword(), companyUser.getAccountAddress(),
                managerUser.getUserAccount(), managerUser.getUserPassword(), managerUser.getAccountAddress());
        return ResultUtils.success(waterStep1InfoResp);
    }
}
