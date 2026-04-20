package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "manager_audit")
@ApiModel(description = "监管局审核申请")
@Data
public class ManagerAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "监管局用户ID")
    private Long userId;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "管辖区域")
    private String jurisdiction;

    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "申请备注")
    private String remark;

    @ApiModelProperty(value = "审核状态(0=待审核,1=已通过,2=已拒绝)")
    private Integer status;

    @ApiModelProperty(value = "管理员审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "审核管理员ID")
    private Long auditUserId;

    @ApiModelProperty(value = "申请时间")
    private Date createTime;

    @ApiModelProperty(value = "审核时间")
    private Date updateTime;

    private String chainTxHash;
}
