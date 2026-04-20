package com.water.platform.service;

import com.alibaba.fastjson.JSON;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.chain.call.KeyValueStoreRawCall;
import com.water.platform.chain.config.SystemConfig;
import com.water.platform.chain.util.ChainUtil;
import com.water.platform.constant.ContractType;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.dto.req.UserContractQueryReq;
import com.water.platform.model.entity.User;
import com.water.platform.model.entity.UserContract;
import com.water.platform.utils.TokenUtil;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ChainStoreService {

    @Autowired(required = false)
    private Client client;

    @Autowired
    private UserContractService userContractService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SystemConfig systemConfig;

    /**
     * 检查区块链是否启用
     */
    public boolean isBlockchainEnabled() {
        return client != null && systemConfig.isEnabled();
    }

    /**
     * 获取 KeyValueStoreRawCall 合约调用对象
     * 优先从 user_contract 表读取（前端绑定），没有则用配置文件默认地址
     */
    private KeyValueStoreRawCall getRawCall() {
        if (!isBlockchainEnabled()) {
            throw new com.water.platform.base.exception.BusinessException(1, "区块链功能已禁用");
        }
        String address = null;
        try {
            UserContract userContract = userContractService.query(
                    new UserContractQueryReq(ContractType.KEY_VALUE_STORE)).getData();
            if (userContract != null) {
                address = userContract.getContractAddress();
            }
        } catch (Exception ignored) {}

        if (address == null || address.isEmpty()) {
            address = systemConfig.getContract().getKeyValueStoreAddress();
        }
        ThrowUtils.throwIf(address == null || address.isEmpty(),
                ErrorCode.NOT_FOUND_ERROR, "KeyValueStore 合约地址未配置");

        Long userId = TokenUtil.getLoginUserId();
        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return KeyValueStoreRawCall.load(address, client,
                client.getCryptoSuite().createKeyPair(user.getPrivateKey()));
    }

    /**
     * 生成唯一 key：keccak256(tableName + recordId + timestamp)
     */
    public String generateKey(String tableName, Long recordId) {
        if (!isBlockchainEnabled()) {
            throw new com.water.platform.base.exception.BusinessException(1, "区块链功能已禁用");
        }
        String raw = tableName + "_" + recordId + "_" + System.currentTimeMillis();
        byte[] hash = client.getCryptoSuite().hash(raw.getBytes(StandardCharsets.UTF_8));
        return org.fisco.bcos.sdk.utils.Numeric.toHexString(hash);
    }

    /**
     * 上链：序列化 JSON → 调用 bindString → 返回 hash
     */
    public String upChain(String tableName, Long recordId, Object dataObj) {
        if (!isBlockchainEnabled()) {
            return null; // 区块链禁用时返回 null
        }
        String json = JSON.toJSONString(dataObj);
        String keyHex = generateKey(tableName, recordId);
        byte[] keyBytes = org.fisco.bcos.sdk.utils.Numeric.hexStringToByteArray(keyHex);

        KeyValueStoreRawCall rawCall = getRawCall();
        TransactionReceipt receipt = rawCall.bindString(keyBytes, json);
        ChainUtil.check(receipt);

        return keyHex;
    }

    /**
     * 从链上查询数据
     */
    public String getFromChain(String keyHex) {
        if (!isBlockchainEnabled()) {
            return null; // 区块链禁用时返回 null
        }
        byte[] keyBytes = org.fisco.bcos.sdk.utils.Numeric.hexStringToByteArray(keyHex);
        KeyValueStoreRawCall rawCall = getRawCall();
        try {
            return rawCall.getStringByKey(keyBytes);
        } catch (Exception e) {
            throw new com.water.platform.base.exception.BusinessException(1, "链上查询失败：" + e.getMessage());
        }
    }
}
