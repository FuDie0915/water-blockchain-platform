package com.water.platform.model.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class MedicineExportModel {
    @ExcelProperty("记录ID")
    private Long id;
    @ExcelProperty("塘号")
    private Long pondId;
    @ExcelProperty("养殖户ID")
    private Long userId;
    @ExcelProperty("用药日期")
    private Date medicineDate;
    @ExcelProperty("药品通用名")
    private String medicineName;
    @ExcelProperty("用药目的")
    private String purpose;
    @ExcelProperty("用药剂量")
    private String dosage;
    @ExcelProperty("审核状态")
    private String auditStatusText;
}
