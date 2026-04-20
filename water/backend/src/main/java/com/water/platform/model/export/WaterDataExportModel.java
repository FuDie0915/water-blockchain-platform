package com.water.platform.model.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class WaterDataExportModel {
    @ExcelProperty("数据ID")
    private Long id;
    @ExcelProperty("用户ID")
    private Long userId;
    @ExcelProperty("数据类型")
    private String dataTypeText;
    @ExcelProperty("数据值")
    private String data;
    @ExcelProperty("状态")
    private String status;
    @ExcelProperty("采集时间")
    private Date time;
    @ExcelProperty("是否上链")
    private String onChainText;
}
