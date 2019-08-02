package com.boge.demo.mapper;

import com.boge.demo.dataobject.OrderDO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDOMapper {
    int deleteByPrimaryKey(String oderid);

    int insert(OrderDO record);

    int insertSelective(OrderDO record);

    OrderDO selectByPrimaryKey(String oderid);

    int updateByPrimaryKeySelective(OrderDO record);

    int updateByPrimaryKey(OrderDO record);

    List<OrderDO> getAllOrders();
}