package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@ApiModel(description = "公告发布请求")
@Data
public class AnnouncementPublishReq {

    @ApiModelProperty(value = "公告标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "公告内容")
    @NotBlank(message = "内容不能为空")
    private String content;

    @ApiModelProperty(value = "目标角色(farmers/manager/all)")
    @NotBlank(message = "目标角色不能为空")
    private String targetRole;
}
