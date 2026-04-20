package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "水质趋势数据项")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaterTrendItem {

    @ApiModelProperty(value = "日期（yyyy-MM-dd）")
    private String date;

    @ApiModelProperty(value = "当天数据总数")
    private Integer total;

    @ApiModelProperty(value = "当天异常数")
    private Integer abnormal;
}
