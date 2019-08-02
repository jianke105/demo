package com.boge.demo.controller.VO;



import com.boge.demo.service.model.PromoteModel;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemVO implements Serializable{

    private Integer id;

    private String itemname;


    private BigDecimal price;


    private String des;


    private String itemimg;


    private Integer sales;

    private Integer stocknum;

    private Integer userid;


    private PromoteVO promoteVO;

    public PromoteVO getPromoteVO() {
        return promoteVO;
    }

    public void setPromoteVO(PromoteVO promoteVO) {
        this.promoteVO = promoteVO;
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


}
