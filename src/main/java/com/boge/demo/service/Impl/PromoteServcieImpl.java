package com.boge.demo.service.Impl;

import com.boge.demo.commons.RedisUtils;
import com.boge.demo.dataobject.PromoteDO;
import com.boge.demo.mapper.PromoteDOMapper;
import com.boge.demo.response.BusinessException;
import com.boge.demo.response.EmBusinessMyError;
import com.boge.demo.service.PromoteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 下单令牌，也可做成秒杀令牌
 *
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.service.Impl
 * @date:2019/7/27
 */
@Service
public class PromoteServcieImpl implements PromoteService {

    @Autowired
    private PromoteDOMapper promoteDOMapper;

    @Autowired
    private RedisUtils redisUtils;

    //生成秒杀令牌
    @Override
    public String generateSKillToken(PromoteDO promoteDO, Integer promoteid, Integer itemid, Integer userid) {
        // PromoteDO promoteDO = promoteDOMapper.selectByPrimaryKey(promoteid);

        if ((Integer) redisUtils.get("promote_item_maxToken" + itemid) < 1) {
            return null;
        }
        if (promoteDO != null) {
            if (!(promoteDO.getIsaddorder() == 2)) {
                return null;
            }
            String token = UUID.randomUUID().toString().replace("-", "");
            //生成的令牌存入redis
            redisUtils.set("promote_toke_" + itemid + userid, token, 600);
            //令牌桶中令牌数量减1
            redisUtils.decr("promote_item_maxToken" + itemid, 1);


            return token;
        }


        String token = UUID.randomUUID().toString().replace("-", "");

        redisUtils.set("promote_toke_" + itemid + userid, token, 600);

        return token;
    }

    @Override
    public PromoteDO selectByItemId(Integer itemid) {
        PromoteDO list = promoteDOMapper.selectByItemId(itemid);


        return list;
    }


}
