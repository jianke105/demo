package com.boge.demo.mapper;

import com.boge.demo.dataobject.StockDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockDOMapper {
    int deleteByPrimaryKey(Integer stockid);

    int insert(StockDO record);

    int insertSelective(StockDO record);

    StockDO selectByPrimaryKey(Integer stockid);

    int updateByPrimaryKeySelective(StockDO record);

    int updateByPrimaryKey(StockDO record);

    //下单减库存
    int updateStock(@Param("itemid") Integer itemid, @Param("amount") Integer amount);

    //获取所有商品库存
    List<StockDO> getAllStcok();

    //获取当个商品库存
    StockDO getStockByItemId(Integer itemId);
}