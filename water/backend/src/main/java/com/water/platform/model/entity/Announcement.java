package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "announcement")
@ApiModel(description = "公告")
@Data
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "公告标题")
    private String title;

    @ApiModelProperty(value = "公告内容")
    private String content;

    @ApiModelProperty(value = "目标角色(farmers/manager/all)")
    private String targetRole;

    @ApiModelProperty(value = "发布人ID")
    private Long publisherId;

    @ApiModelProperty(value = "发布时间")
    private Date createTime;

    private String chainTxHash;
}
