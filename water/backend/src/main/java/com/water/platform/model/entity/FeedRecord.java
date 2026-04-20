package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "feed_record")
@ApiModel(description = "投喂记录")
@Data
public class FeedRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @ApiModelProperty(value = "养殖户ID")
    private Long userId;

    @ApiModelProperty(value = "投喂日期")
    private Date feedDate;

    @ApiModelProperty(value = "饲料品牌")
    private String feedBrand;

    @ApiModelProperty(value = "投喂用量（kg）")
    private Double feedAmount;

    @ApiModelProperty(value = "责任人")
    private String manager;

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
