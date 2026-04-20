package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "seed_record")
@ApiModel(description = "苗种投放记录")
@Data
public class SeedRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @ApiModelProperty(value = "养殖户ID")
    private Long userId;

    @ApiModelProperty(value = "投放日期")
    private Date recordDate;

    @ApiModelProperty(value = "责任人")
    private String manager;

    @ApiModelProperty(value = "苗种品种")
    private String seedType;

    @ApiModelProperty(value = "苗种规格")
    private String seedSpec;

    @ApiModelProperty(value = "投放重量（kg）")
    private Double weight;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "审核状态（0=待审核, 1=已通过, 2=已拒绝）")
    private Integer auditStatus;

    @ApiModelProperty(value = "逻辑删除")
    private Integer isDelete;

    private String chainTxHash;
}
