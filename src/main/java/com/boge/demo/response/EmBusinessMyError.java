package com.boge.demo.response;

//异常枚举
public enum EmBusinessMyError implements MyError {
    USER_NOT_EXIST(2001, "用户不存在"),
    USER_OR_PSD_ERROR(2002, "用户名或密码不正确"),
    USER_HAS_EXIST(2003, "用户已被使用"),
    TELPHONE_HAS_EXIST(2004, "手机号已注册"),
    OTPCODE_ERROR(2005, "验证码错误"),


    PRODUCTS_NOT_EXIST(3002, "商品不存在"),
    USER_NOT_LOGIN(3004, "用户未登录"),
    USER_LOGIN_LOSE(3005, "用户已过期"),

    ORDER_SUBMIT_FAIL(3003, "订单提交失败"),

    UNKNOWN_ERROR(1001, "未知错误"),

    GET_ERROR(1002, "抱歉，刷新过快,稍后重试"),

    HVAE_ERROR(5001, "访问的页面遇到问题"),
    NOT_PAGE(4004, "访问的页面不存在"),

    PROMOTE_ORDER_NOT_START(3006, "秒杀活动尚未开始"),
    PROMOTE_ORDER_END_YET(3007, "秒杀活动已经结束"),

    PARMAS_NOT_VALUEABLE(3001, "参数不合法"),

    //订单相关
    ORDER_AMOUNT_NOT_VALUEABLE(3011, "下单数量不合法"),
    STOCK_TOO_LOW(3012, "库存不足"),

    INIT_REDIS_STOCK_ERROR(3013, "初始化redis库存失败"),
    INIT_REDIS_FAIL(3013, "redis库存未初始化"),

    SEND_MSG_ERROR(3015, "异步消息发送失败"),

    TOKEN_BUILD_ERROR(3016, "令牌生成失败"),
    TOKEN_ERROR(3016, "令牌校验失败"),

    VERIFYCODE_ERROR(3017, "验证码错误"),
    VERIFYCODE_PASS(3018, "验证码已过期"),

    SO_busy(3019, "活动太火爆，请稍后再试"),;


    private int ErrorCode;
    private String ErrorMsg;

    private EmBusinessMyError(int ErrorCode, String ErrorMsg) {
        this.ErrorCode = ErrorCode;
        this.ErrorMsg = ErrorMsg;
    }

    @Override
    public int getErrorCode() {
        return this.ErrorCode;
    }

    @Override
    public String getErrorMsg() {
        return this.ErrorMsg;
    }

    //修改通用错误码的错误信息，自定义错误信息
    @Override
    public MyError setErrogrMsg(String errorMsg) {
        this.ErrorMsg = ErrorMsg;
        return this;
    }
}
