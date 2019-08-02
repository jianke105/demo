package com.boge.demo.mapper;

import com.boge.demo.dataobject.StocklogDO;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface StocklogDOMapper {
    int deleteByPrimaryKey(String stocklogid);

    int insert(StocklogDO record);

    int insertSelective(StocklogDO record);

    StocklogDO selectByPrimaryKey(String stocklogid);

    int updateByPrimaryKeySelective(StocklogDO record);

    int updateByPrimaryKey(StocklogDO record);

    // void initStockLog(Integer itemId,Integer amount);
}