package com.boge.demo.service;

import com.boge.demo.dataobject.StockDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.service
 * @date:2019/7/26
 */
public interface StockService {
    List<StockDO> getAllStcok();

    int getStockByItemId(Integer itemId);

    int updateStock(Integer itemid, Integer amount);
}
