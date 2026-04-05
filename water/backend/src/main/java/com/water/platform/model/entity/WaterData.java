package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * @author ：devon
 * @date ：2024/11/13 22:24
 * @description：水数据模型
 * @version: 1.0
 */
@TableName(value = "water_data")
@ApiModel(description = "水数据模型")
@AllArgsConstructor
@Data
public class WaterData {

    @TableId(type = IdType.AUTO)
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

    private boolean isOnChain;

    public boolean getIsOnChain() {
        return isOnChain;
    }
}
