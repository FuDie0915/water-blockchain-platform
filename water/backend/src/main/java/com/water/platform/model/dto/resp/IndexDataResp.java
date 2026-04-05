package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author ：devon
 * @date ：2024/11/14 15:11
 * @description：首页看板数据
 * @version: 1.0
 */
@ApiModel(description = "首页看板数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexDataResp {

    private Long blockCount; // 区块高度
    private Long nodeCount; // 节点数量
    private Long tranCount; // 交易数
    private Long tranCount2; // 未完成交易数
    private List<NodeStatus> nodeStatusList;


    @ApiModel(value = "节点状态")
    @Data
    @AllArgsConstructor
    public static class NodeStatus {
        private String nodeId; // 节点id
        private Long blockNumber;
        private Long pbftView; // pbft视图
        private Integer status; // 1正常
        private Date latestStatusUpdateTime; // 最后更新时间
    }
}
