package com.water.platform.chain.util;

import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.stereotype.Component;

/**
 * @author ：devon
 * @date ：2024/10/12 16:33
 * @description：区块链工具类
 * @version: 1.0
 */
@Component
@Slf4j
public class ChainUtil {
    /**
     * 判断交易回执是否成功
     * @param transactionReceipt
     */
    public static void check(TransactionReceipt transactionReceipt) {
        if (!transactionReceipt.getStatus().equals("0x0")) {
            log.error("操作区块链失败:" + transactionReceipt.getStatus());
            log.error(transactionReceipt.getMessage());
            throw new BusinessException(ErrorCode.CHAIN_CALL_FAILED);
        }
    }

    public static void handleException(Exception e) {
        log.error("操作区块链失败:" + e.getMessage());
        throw new BusinessException(1, e.getMessage());
    }


//    /**
//     * 拷贝版权实体数据，适用于合约返回结构体数组
//     *
//     * @param chainCopyrightList 二维数组
//     * @return
//     */
//    public static List<Copyright> changeChainCopyrightToJava(List<List> chainCopyrightList) {
//        List<Copyright> copyrights = new ArrayList<>();
//        for (List chainCopyright : chainCopyrightList) { // 此处List chainCopyright  其实表示的是结构体
//            Copyright copyright = new Copyright();
//            copyright.setCopyrightId(((Utf8String) chainCopyright.get(0)).getValue());
//            copyright.setName(((Utf8String) chainCopyright.get(1)).getValue());
//            copyright.setDescription(((Utf8String) chainCopyright.get(2)).getValue());
//            copyright.setBoundResource(((Utf8String) chainCopyright.get(3)).getValue());
//            copyright.setResourceType(((Utf8String) chainCopyright.get(4)).getValue());
//            copyright.setStatus(((Utf8String) chainCopyright.get(5)).getValue());
//            copyright.setRemark(((Utf8String) chainCopyright.get(6)).getValue());
//            copyright.setCreateTime(new Date(((Uint256) chainCopyright.get(7)).getValue().longValue()));
//            copyright.setEffectiveTime(new Date(((Uint256) chainCopyright.get(8)).getValue().longValue()));
//            copyright.setUpdateTime(new Date(((Uint256) chainCopyright.get(9)).getValue().longValue()));
//            copyright.setCreateName(((Utf8String) chainCopyright.get(10)).getValue());
//            copyright.setOwnerName(((Utf8String) chainCopyright.get(11)).getValue());
//            copyrights.add(copyright);
//        }
//        return copyrights;
//    }
//
//    /**
//     * 拷贝版权转让记录实体数据，适用于合约返回结构体数组
//     *
//     * @param chainTranRecordList 二维数组
//     * @return
//     */
//    public static List<CopyrightTranRecord> changeTranRecordToJava(List<List> chainTranRecordList) {
//        List<CopyrightTranRecord> tranRecordList = new ArrayList<>();
//        for (List chainTranRecord : chainTranRecordList) {
//            CopyrightTranRecord tranRecord = new CopyrightTranRecord();
//            tranRecord.setTranId(((Utf8String) chainTranRecord.get(0)).getValue());
//            tranRecord.setCopyrightId(((Utf8String) chainTranRecord.get(1)).getValue());
//            tranRecord.setOriginalOwnerName(((Utf8String) chainTranRecord.get(2)).getValue());
//            tranRecord.setNewOwnerName(((Utf8String) chainTranRecord.get(3)).getValue());
//            tranRecord.setTransferRemark(((Utf8String) chainTranRecord.get(4)).getValue());
//            tranRecord.setTranTime(new Date(((Uint256) chainTranRecord.get(5)).getValue().longValue()));
//            tranRecordList.add(tranRecord);
//        }
//        return tranRecordList;
//    }
//
//    /**
//     * 拷贝版权转让记录实体数据，适用于合约返回结构体数组
//     *
//     * @param chainLicenseList 二维数组
//     * @return
//     */
//    public static List<CopyrightLicenseRecord> changeLicenseToJava(List<List> chainLicenseList) {
//        List<CopyrightLicenseRecord> licenseList = new ArrayList<>();
//        for (List chainLicense : chainLicenseList) {
//            CopyrightLicenseRecord license = new CopyrightLicenseRecord();
//            license.setLicenseRecordId(((Utf8String) chainLicense.get(0)).getValue());
//            license.setCopyrightId(((Utf8String) chainLicense.get(1)).getValue());
//            license.setLicenseName(((Utf8String) chainLicense.get(2)).getValue());
//            license.setLicenseTime(new Date(((Uint256) chainLicense.get(3)).getValue().longValue()));
//            license.setValid(((Bool) chainLicense.get(4)).getValue());
//            licenseList.add(license);
//        }
//        return licenseList;
//    }
}
