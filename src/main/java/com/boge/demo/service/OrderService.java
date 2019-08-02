package com.boge.demo.service;

import com.boge.demo.response.BusinessException;
import com.boge.demo.controller.VO.OrderVO;
import com.boge.demo.service.model.OrderModel;

import java.util.List;

public interface OrderService {

    int addOrder(OrderModel orderModel, String stockLogId) throws BusinessException;

    boolean asyncDecreaStock(OrderModel orderModel);

    List<OrderVO> getAllOrders();
}
