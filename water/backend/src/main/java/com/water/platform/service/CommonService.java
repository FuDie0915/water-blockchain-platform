package com.water.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.BusinessException;
import com.water.platform.model.dto.resp.IndexDataResp;
import com.water.platform.utils.HttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author ：devon
 * @date ：2024/11/15 9:31
 * @description：公共业务逻辑类
 * @version: 1.0
 */
@Service
public class CommonService {
    @Autowired
    private ObjectMapper objectMapper;


    public BaseResponse<IndexDataResp> indexData() {
        try {
            OkHttpClient client = HttpUtils.getClient();
            // [{
            //        "nodeId": "81097ab18e65a6005a5d0220812993336785bded4de371e04ded6c21a3f70e267b0a7922fd4305b7276ae3392b2f69d582b7e1cdd02ea9dae426d6508a30640d",
            //        "blockNumber": 688,
            //        "pbftView": 3221,
            //        "status": 1,
            //        "latestStatusUpdateTime": "2024-11-22 15:03:10"
            //    }]
            String responseBodyJson = client.newCall(new Request.Builder().url(HttpUtils.getFrontBaseUrl() + "/1/web3/getNodeStatusList").get().build())
                    .execute()// 进行调用
                    .body().string(); // 获取响应体json字符串
            List<IndexDataResp.NodeStatus> nodeStatuses = objectMapper.readValue(responseBodyJson, new TypeReference<List<IndexDataResp.NodeStatus>>() {
            });

            // {"txSum":74490,"blockNumber":688,"failedTxSum":18}
            responseBodyJson = client.newCall(new Request.Builder().url(HttpUtils.getFrontBaseUrl() + "/1/web3/transaction-total").get().build())
                    .execute()// 进行调用
                    .body().string(); // 获取响应体json字符串
            HashMap<String, Long> tranMap = objectMapper.readValue(responseBodyJson, new TypeReference<HashMap<String, Long>>() {
            });
            IndexDataResp indexDataResp = new IndexDataResp();
            indexDataResp.setBlockCount(tranMap.get("blockNumber"));
            indexDataResp.setNodeCount((long) nodeStatuses.size());
            indexDataResp.setTranCount(tranMap.get("txSum"));
            indexDataResp.setTranCount2(tranMap.getOrDefault("failedTxSum", 0L));
            indexDataResp.setNodeStatusList(nodeStatuses);
            return ResultUtils.success(indexDataResp);
        } catch (Exception e) {
            throw new BusinessException(99999, "获取webase-front首页数据失败");
        }
    }
}
