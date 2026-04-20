package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "harvest_record")
@ApiModel(description = "收获记录")
@Data
public class HarvestRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @ApiModelProperty(value = "养殖户ID")
    private Long userId;

    @ApiModelProperty(value = "收获日期")
    private Date harvestDate;

    @ApiModelProperty(value = "责任人")
    private String manager;

    @ApiModelProperty(value = "收获批次")
    private String batchNo;

    @ApiModelProperty(value = "品种规格")
    private String spec;

    @ApiModelProperty(value = "总收获重量（kg）")
    private Double totalWeight;

    @ApiModelProperty(value = "成活率（%）")
    private Double survivalRate;

    @ApiModelProperty(value = "收购方信息")
    private String buyerInfo;

    @ApiModelProperty(value = "质量检测结果")
    private String qualityResult;

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
