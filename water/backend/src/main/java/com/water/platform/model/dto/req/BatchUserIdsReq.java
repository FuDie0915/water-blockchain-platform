package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@ApiModel(description = "批量用户ID参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BatchUserIdsReq {
    @ApiModelProperty(value = "用户ID列表", required = true)
    @NotEmpty
    @Size(max = 100, message = "单次最多操作100条")
    private List<Long> userIds;
}
