package com.boge.demo.controller.VO;


import com.boge.demo.commons.ConverJackson;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.Date;


public class OrderVO {

    private String oderid;

    private Integer itemid;

    private String itemdes;

    private BigDecimal price;

    private Integer itemnum;

    private String itemname;

    private Date orderdate;

    private Integer userid;

    public String getOderid() {
        return oderid;
    }

    public void setOderid(String oderid) {
        this.oderid = oderid;
    }

    public Integer getItemid() {
        return itemid;
    }

    public void setItemid(Integer itemid) {
        this.itemid = itemid;
    }

    public String getItemdes() {
        return itemdes;
    }

    public void setItemdes(String itemdes) {
        this.itemdes = itemdes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getItemnum() {
        return itemnum;
    }

    public void setItemnum(Integer itemnum) {
        this.itemnum = itemnum;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }


    @JsonSerialize(using = ConverJackson.class)
    public Date getOrderdate() {
        return orderdate;
    }


    public void setOrderdate(Date orderdate) {
        this.orderdate = orderdate;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }
}