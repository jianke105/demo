package com.boge.demo.controller;



import com.boge.demo.response.BusinessException;
import com.boge.demo.response.EmBusinessMyError;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class BaseController {
    //log4j日志类
    public final static Logger logger = Logger.getLogger(BaseController.class);
    //跨域请求头
    final static String responsehead = "application/x-www-form-urlencoded";




    //统一业务异常处理类
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object HandlerBusinessException(Exception ex, HttpServletRequest req){

        Map<String, Object> responseData = new HashMap<>();
        if(ex instanceof BusinessException) {
            //向下转型
            BusinessException businessException = (BusinessException) ex;
            responseData.put("errCode", businessException.getErrorCode());
            responseData.put("errMsg", businessException.getErrorMsg());
        }else {
            responseData.put("errCode", EmBusinessMyError.UNKNOWN_ERROR.getErrorCode());
            responseData.put("errMsg", EmBusinessMyError.UNKNOWN_ERROR.getErrorMsg());
        }
        return responseData;
    }

}
