package com.water.platform.model.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class PondExportModel {
    @ExcelProperty("养殖池ID")
    private Long id;
    @ExcelProperty("塘名")
    private String pondName;
    @ExcelProperty("养殖品种")
    private String breedType;
    @ExcelProperty("养殖面积(亩)")
    private Double area;
    @ExcelProperty("水深(米)")
    private Double depth;
    @ExcelProperty("状态")
    private String statusText;
    @ExcelProperty("养殖户ID")
    private Long userId;
    @ExcelProperty("创建时间")
    private Date createTime;
}
