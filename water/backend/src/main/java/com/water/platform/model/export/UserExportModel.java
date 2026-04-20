package com.water.platform.model.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserExportModel {
    @ExcelProperty("用户ID")
    private String userId;
    @ExcelProperty("账号")
    private String userAccount;
    @ExcelProperty("用户名")
    private String userName;
    @ExcelProperty("角色")
    private String userRole;
    @ExcelProperty("状态")
    private String userStatusText;
}
