package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@ApiModel(description = "批量修改用户状态参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BatchStatusUpdateReq {
    @ApiModelProperty(value = "用户ID列表", required = true)
    @NotEmpty
    @Size(max = 100, message = "单次最多操作100条")
    private List<Long> userIds;

    @ApiModelProperty(value = "用户状态：0=禁用，1=启用", required = true)
    @NotNull
    private Integer userStatus;
}
