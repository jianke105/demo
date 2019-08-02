package com.boge.demo.service;

import com.boge.demo.dataobject.PromoteDO;
import com.boge.demo.response.BusinessException;

import java.util.List;

/**
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.service
 * @date:2019/7/27
 */
public interface PromoteService {

    String generateSKillToken(PromoteDO promoteDO, Integer promoteid, Integer itemid, Integer userid);

    PromoteDO selectByItemId(Integer itemid);
}
