package com.water.platform.model.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class HarvestExportModel {
    @ExcelProperty("记录ID")
    private Long id;
    @ExcelProperty("塘号")
    private Long pondId;
    @ExcelProperty("养殖户ID")
    private Long userId;
    @ExcelProperty("收获日期")
    private Date harvestDate;
    @ExcelProperty("收获批次")
    private String batchNo;
    @ExcelProperty("品种规格")
    private String spec;
    @ExcelProperty("总收获重量(kg)")
    private Double totalWeight;
    @ExcelProperty("成活率(%)")
    private Double survivalRate;
    @ExcelProperty("审核状态")
    private String auditStatusText;
}
