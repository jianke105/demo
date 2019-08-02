package com.boge.demo.dataobject;

public class StocklogDO {
    private String stocklogid;

    private Integer itemId;

    private Integer amount;

    private Integer status;

    public String getStocklogid() {
        return stocklogid;
    }

    public void setStocklogid(String stocklogid) {
        this.stocklogid = stocklogid == null ? null : stocklogid.trim();
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}