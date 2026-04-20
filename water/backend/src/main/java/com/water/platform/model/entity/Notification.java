package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "notification")
@ApiModel(description = "站内信通知")
@Data
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "接收者用户ID")
    private Long userId;

    @ApiModelProperty(value = "通知标题")
    private String title;

    @ApiModelProperty(value = "通知内容")
    private String content;

    @ApiModelProperty(value = "通知类型（AUDIT/WARNING/ANNOUNCEMENT/BINDING）")
    private String type;

    @ApiModelProperty(value = "关联业务ID")
    private Long relatedId;

    @ApiModelProperty(value = "是否已读（0=未读，1=已读）")
    private Integer isRead;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
