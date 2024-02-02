package com.shuking.pairBackend.exception;

import com.shuking.pairBackend.common.ErrorCode;
import com.shuking.pairBackend.common.BaseResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class GlobalException {

    /**
     * 业务逻辑错误
     * @param e 错误
     * @return 统一请求体
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResult<String> businessExceptionHandler(BusinessException e) {
        log.error("businessException: {},{}",e.getMessage(),e.getDescription());
        return BaseResult.error(e.getMessage(), e.getCode(),e.getDescription());
    }

    /**
     * 捕获运行时异常 由虚拟机抛出
     * @param e 错误
     * @return  统一请求体
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus()
    public BaseResult<String> runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException: {}", (Object) e.getStackTrace());
        return BaseResult.error(e.getMessage(), ErrorCode.SYSTEM_ERROR.getCode(),"");
    }

}
