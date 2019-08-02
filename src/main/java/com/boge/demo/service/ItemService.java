package com.boge.demo.service;

import com.boge.demo.controller.VO.ItemVO;
import com.boge.demo.service.model.ItemModel;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ItemService {

    List<ItemModel> getAllItem();

    ItemVO getItemInfo(Integer id);

    String initStockLog(Integer itemId, Integer amount);
}
