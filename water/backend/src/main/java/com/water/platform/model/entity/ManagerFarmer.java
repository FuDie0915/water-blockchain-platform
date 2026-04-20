package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "manager_farmer")
@ApiModel(description = "监管局-养殖户绑定关系")
@Data
public class ManagerFarmer {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "监管局用户ID")
    private Long managerId;

    @ApiModelProperty(value = "养殖户用户ID")
    private Long farmerId;

    @ApiModelProperty(value = "状态（0=待审核, 1=已通过, 2=已拒绝）")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private Integer isDelete;

    private String chainTxHash;
}
