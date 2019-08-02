package com.boge.demo.service.Impl;

import com.boge.demo.dataobject.StockDO;
import com.boge.demo.mapper.StockDOMapper;
import com.boge.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.service.Impl
 * @date:2019/7/26
 */
@Service
public class StockServiceImpl implements StockService {
    @Autowired
    private StockDOMapper stockDOMapper;

    @Override
    public List<StockDO> getAllStcok() {

        return null;
    }

    @Override
    public int getStockByItemId(Integer itemId) {
        return 0;
    }

    @Override
    public int updateStock(Integer itemid, Integer amount) {
        return 0;
    }
}
