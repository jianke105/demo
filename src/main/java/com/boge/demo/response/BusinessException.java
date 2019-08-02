package com.boge.demo.response;

//包装器业务异常类实现，业务相关的异常，而非全局
public class BusinessException extends Exception implements MyError {
    private MyError myError;

    //直接接收MyError的传参，用于构造业务异常
    public BusinessException(MyError myError) {
        super();
        this.myError = myError;
    }

    //接收自定义ErrorMsg的方式构造业务异常
    public BusinessException(MyError myError, String ErrorMsg) {
        super();
        this.myError = myError;
        this.myError.setErrogrMsg(ErrorMsg);
    }

    @Override
    public int getErrorCode() {
        return this.myError.getErrorCode();
    }

    @Override
    public String getErrorMsg() {
        return this.myError.getErrorMsg();
    }

    @Override
    public MyError setErrogrMsg(String errorMsg) {
        this.myError.setErrogrMsg(errorMsg);
        return this;
    }
}
