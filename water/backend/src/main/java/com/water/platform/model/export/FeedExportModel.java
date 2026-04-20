package com.water.platform.model.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class FeedExportModel {
    @ExcelProperty("记录ID")
    private Long id;
    @ExcelProperty("塘号")
    private Long pondId;
    @ExcelProperty("养殖户ID")
    private Long userId;
    @ExcelProperty("投喂日期")
    private Date feedDate;
    @ExcelProperty("饲料品牌")
    private String feedBrand;
    @ExcelProperty("投喂用量(kg)")
    private Double feedAmount;
    @ExcelProperty("审核状态")
    private String auditStatusText;
}
