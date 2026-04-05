package com.water.platform.base.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler(RuntimeException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "未登录");
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public BaseResponse<?> fileSizeLimitExceededException(RuntimeException e) {
        log.error("fileSizeLimitExceededException", e);
        return ResultUtils.error(ErrorCode.FILE_SIZE_ERROR, ErrorCode.FILE_SIZE_ERROR.getMessage());
    }

    @ExceptionHandler(FileNameLengthLimitExceededException.class)
    public BaseResponse<?> fileNameLengthLimitExceededException(RuntimeException e) {
        log.error("FileNameLengthLimitExceededException", e);
        return ResultUtils.error(ErrorCode.FILE_NAME_ERROR, ErrorCode.FILE_NAME_ERROR.getMessage());
    }


    // 全局异常拦截
    @ExceptionHandler
    public BaseResponse<?> handlerException(Exception e) {
        e.printStackTrace();
        log.error("SystemException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
