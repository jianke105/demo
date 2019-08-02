package com.boge.demo.service.model;


import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemModel implements Serializable {

    private Integer id;

    private String itemname;


    private BigDecimal price;


    private String des;


    private String itemimg;


    private Integer sales;

    private Integer stocknum;

    private Integer userid;


    private PromoteModel promoteModel;

    public PromoteModel getPromoteModel() {
        return promoteModel;
    }

    public void setPromoteModel(PromoteModel promoteModel) {
        this.promoteModel = promoteModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getItemimg() {
        return itemimg;
    }

    public void setItemimg(String itemimg) {
        this.itemimg = itemimg;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getStocknum() {
        return stocknum;
    }

    public void setStocknum(Integer stocknum) {
        this.stocknum = stocknum;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    @Override
    public String toString() {
        return "ItemModel{" +
                "id=" + id +
                ", itemname='" + itemname + '\'' +
                ", price=" + price +
                ", des='" + des + '\'' +
                ", itemimg='" + itemimg + '\'' +
                ", sales=" + sales +
                ", stocknum=" + stocknum +
                ", userid=" + userid +
                ", promoteModel=" + promoteModel +
                '}';
    }
}
