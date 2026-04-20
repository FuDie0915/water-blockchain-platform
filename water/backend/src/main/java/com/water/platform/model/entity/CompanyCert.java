package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author ：devon
 * @date ：2024/11/13 22:29
 * @description：企业许可证模型
 * @version: 1.0
 */
@TableName(value = "company_cert")
@ApiModel(description = "企业许可证模型")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyCert {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "排污许可证图像路径")
    private String imageUrl;

    @ApiModelProperty(value = "状态(0:待审批、1:通过并上链、2:不通过)")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    private String chainTxHash;
}
