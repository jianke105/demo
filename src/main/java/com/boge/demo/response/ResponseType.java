package com.boge.demo.response;


//数据统一格式的返回类型，便于前台处理数据
//如果有数据，status为"success"，data返回json字符串；如果没有返回"fail"，data返回错误信息
public class ResponseType {
    private String status;
    private Object data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static ResponseType Create(Object object) {
        return ResponseType.Create("success", object);
    }

    public static ResponseType Create(String status, Object object) {
        ResponseType responseType = new ResponseType();
        responseType.setData(object);
        responseType.setStatus(status);
        return responseType;
    }
}
