package com.water.platform.model.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SeedExportModel {
    @ExcelProperty("记录ID")
    private Long id;
    @ExcelProperty("塘号")
    private Long pondId;
    @ExcelProperty("养殖户ID")
    private Long userId;
    @ExcelProperty("投放日期")
    private Date recordDate;
    @ExcelProperty("苗种品种")
    private String seedType;
    @ExcelProperty("苗种规格")
    private String seedSpec;
    @ExcelProperty("投放重量(kg)")
    private Double weight;
    @ExcelProperty("审核状态")
    private String auditStatusText;
}
