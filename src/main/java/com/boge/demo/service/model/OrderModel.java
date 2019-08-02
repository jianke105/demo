package com.boge.demo.service.model;


//前端需传入参数
public class OrderModel {

    private String username;
    private Integer itemid;
    private Integer amount;
    private Integer Promoteid;
    private Integer userid;


    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getPromoteid() {
        return Promoteid;
    }

    public void setPromoteid(Integer promoteid) {
        Promoteid = promoteid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getItemid() {
        return itemid;
    }

    public void setItemid(Integer itemid) {
        this.itemid = itemid;
    }

    @Override
    public String toString() {
        return "OrderModel{" +
                "username='" + username + '\'' +
                ", itemid=" + itemid +
                ", amount=" + amount +
                ", Promoteid=" + Promoteid +
                ", userid=" + userid +
                '}';
    }
}