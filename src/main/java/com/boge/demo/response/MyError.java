package com.boge.demo.response;

//异常处理接口类
public interface MyError {
    public int getErrorCode();

    public String getErrorMsg();

    public MyError setErrogrMsg(String errorMsg);

}
