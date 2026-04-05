package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author ：devon
 * @date ：2024/11/14 22:24
 * @description：水数据输出模型
 * @version: 1.0
 */
@ApiModel(description = "水数据输出模型")
@Data
public class WaterDataResp {

    @ApiModelProperty(value = "ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "数据类型(0:TDS、1:浑浊)", example = "0")
    private Integer dataType;

    @ApiModelProperty(value = "数据")
    private String data;

    @ApiModelProperty(value = "状态(正常、异常)")
    private String status;

    @ApiModelProperty(value = "哈希值")
    private String hash;

    @ApiModelProperty(value = "采集时间")
    private Date time;

    @ApiModelProperty(value = "是否上链")
    private boolean isOnChain;

    public void setIsOnChain(boolean isOnChain) {
        this.isOnChain = isOnChain;
    }
}
