package com.boge.demo.response;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

//全局异常处理类
@ControllerAdvice
public class TopExceptionHandler {


    //全局异常以json格式返回，忽略系统异常提示界面
    @ResponseBody
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseType constraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("errorCode", EmBusinessMyError.HVAE_ERROR.getErrorCode());
        responseData.put("errorMsg", EmBusinessMyError.HVAE_ERROR.getErrorMsg());
        return ResponseType.Create("fail", responseData);
    }


    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseType IllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("errorCode", EmBusinessMyError.HVAE_ERROR.getErrorCode());
        responseData.put("errorMsg", EmBusinessMyError.HVAE_ERROR.getErrorMsg());
        return ResponseType.Create("fail", responseData);
    }

    @ResponseBody
    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseType noHandlerFoundException(Exception ex) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("errorCode", EmBusinessMyError.NOT_PAGE.getErrorCode());
        responseData.put("errorMsg", EmBusinessMyError.NOT_PAGE.getErrorMsg());
        return ResponseType.Create("fail", responseData);
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseType unknownException(Exception ex) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("errorCode", EmBusinessMyError.HVAE_ERROR.getErrorCode());
        responseData.put("errorMsg", EmBusinessMyError.HVAE_ERROR.getErrorMsg());
        return ResponseType.Create("fail", responseData);
    }


}