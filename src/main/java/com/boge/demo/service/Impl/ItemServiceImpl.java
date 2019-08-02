package com.boge.demo.service.Impl;

import com.boge.demo.commons.CommonUtils;
import com.boge.demo.commons.RedisUtils;
import com.boge.demo.controller.VO.ItemVO;
import com.boge.demo.controller.VO.PromoteVO;
import com.boge.demo.dataobject.ItemDO;
import com.boge.demo.dataobject.PromoteDO;
import com.boge.demo.dataobject.StockDO;
import com.boge.demo.dataobject.StocklogDO;
import com.boge.demo.mapper.ItemDOMapper;
import com.boge.demo.mapper.PromoteDOMapper;
import com.boge.demo.mapper.StockDOMapper;
import com.boge.demo.mapper.StocklogDOMapper;
import com.boge.demo.response.BusinessException;
import com.boge.demo.response.EmBusinessMyError;
import com.boge.demo.response.ResponseType;
import com.boge.demo.service.CacheServcie;
import com.boge.demo.service.ItemService;
import com.boge.demo.service.model.ItemModel;

import com.boge.demo.service.model.PromoteModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private PromoteDOMapper promoteDOMapper;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private CacheServcie cacheServcie;

    @Autowired
    private StockDOMapper stockDOMapper;

    @Autowired
    private StocklogDOMapper stocklogDOMapper;

    public final static Logger logger = Logger.getLogger(ItemServiceImpl.class);

    //获取商品列表信息
    @Override
    public List<ItemModel> getAllItem() {
        List<ItemDO> itemlist = itemDOMapper.getAllItems();
        List<ItemModel> listItemModel = new ArrayList<ItemModel>();

        List<StockDO> listStock = stockDOMapper.getAllStcok();

        for (int i = 0; i < itemlist.size(); i++) {
            ItemModel itemModel = new ItemModel();
            itemModel = converItem(itemlist.get(i), listStock);
            listItemModel.add(itemModel);
        }
        logger.info(listItemModel.get(0).getItemname());
        return listItemModel;
    }

    //获取单个商品信息
    @Override
    public ItemVO getItemInfo(Integer id) {
        logger.info(id);


        ItemVO itemVO = new ItemVO();
        ItemModel itemModel = new ItemModel();
        DateTime dateTime = new DateTime();
        //首先从本地缓存中取
        Object object = cacheServcie.getLocalCache("item_" + id);
        if (object != null) {
            itemModel = (ItemModel) object;
            itemVO = converFromItemModel(itemModel);
            logger.info("从本地缓存中拿到了item");
            return itemVO;
        }
        //其次从redis中获取
        Object object1 = redisUtils.get("item_" + id);

        if (object1 != null) {
            itemModel = (ItemModel) object1;
            itemVO = converFromItemModel(itemModel);
            logger.info("从redis拿到了item");
            return itemVO;

        } else {
            PromoteDO promoteDO = new PromoteDO();
            PromoteModel promoteModel = new PromoteModel();
            ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
            promoteDO = promoteDOMapper.selectByItemId(id);


            StockDO itemStock = stockDOMapper.getStockByItemId(id);
            logger.info("从数据库中拿到item");
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());

            //如果不是活动商品，itemModel的promoteModel属性返回null给前端
            if (promoteDO == null) {
                itemModel = converItem2(itemDO, itemStock.getStocknum());
                logger.info(itemModel.getItemname());
                itemModel.setPromoteModel(null);
                //存入redis
                redisUtils.set("item_" + id, itemModel, 600);
                redisUtils.set("item_exist_" + id, "yes", 600);
                //存入本地缓存
                cacheServcie.setLocalCache("item_" + id, itemModel);
                itemVO = converFromItemModel(itemModel);

                return itemVO;
            } else {
                itemModel = converItem2(itemDO, itemStock.getStocknum());
                itemModel.setPromoteModel(converFromDO(promoteDO));
                itemVO = converFromItemModel(itemModel);
                //redis存入相关商品信息
                redisUtils.set("item_" + id, itemModel, 600);
                redisUtils.set("item_exist_" + id, "yes", 600);
                cacheServcie.setLocalCache("item_" + id, itemModel);


                //更新数据库中的活动信息，isaddorder字段刷新
                if (promoteDO.getStarttime().before(timestamp) && promoteDO.getLasttime().after(timestamp)) {
                    promoteDOMapper.updateIsAddorder(id, 2);
                } else if (promoteDO.getLasttime().before(timestamp)) {
                    promoteDOMapper.updateIsAddorder(id, 3);
                } else {
                    promoteDOMapper.updateIsAddorder(id, 1);
                }
                return itemVO;
            }

        }

    }

    //库存流水初始化
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StocklogDO stocklogDO = new StocklogDO();
        stocklogDO.setAmount(amount);
        stocklogDO.setItemId(itemId);
        stocklogDO.setStocklogid(UUID.randomUUID().toString().replace("-", ""));
        stocklogDO.setStatus(1);
        stocklogDOMapper.insertSelective(stocklogDO);
        return stocklogDO.getStocklogid();
    }


    public ItemModel converItem(ItemDO itemDO, List<StockDO> listStock) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        for (int j = 0; j < listStock.size(); j++) {
            if (listStock.get(j).getItemid() == itemDO.getId()) {
                itemModel.setStocknum(listStock.get(j).getStocknum());
            } else {
                itemModel.setStocknum(0);
            }
        }
        return itemModel;

    }

    public ItemModel converItem2(ItemDO itemDO, Integer itemStock) {
        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setStocknum(itemStock);
        return itemModel;

    }


    public PromoteModel converFromDO(PromoteDO promoteDO) {
        PromoteModel promoteModel = new PromoteModel();
        promoteModel.setIsaddorder(promoteDO.getIsaddorder());
        promoteModel.setItemid(promoteDO.getItemid());
        promoteModel.setLasttime(new DateTime(promoteDO.getLasttime()));
        //  promoteModel.setLasttime(promoteDO.getLasttime().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        promoteModel.setStarttime(new DateTime(promoteDO.getStarttime()));
        promoteModel.setPromoteprice(promoteDO.getPromoteprice());
        promoteModel.setPromoteid(promoteDO.getPromoteid());
        return promoteModel;

    }

    public ItemVO converFromItemModel(ItemModel itemModel) {
        ItemVO itemVO = new ItemVO();
        PromoteVO promoteVO = new PromoteVO();
        itemVO.setId(itemModel.getId());
        itemVO.setDes(itemModel.getDes());
        itemVO.setItemimg(itemModel.getItemimg());
        itemVO.setItemname(itemModel.getItemname());
        itemVO.setPrice(itemModel.getPrice());
        itemVO.setSales(itemModel.getSales());
        itemVO.setStocknum(itemModel.getStocknum());
        itemVO.setUserid(itemModel.getUserid());
        if (itemModel.getPromoteModel() == null) {
            itemVO.setPromoteVO(null);
        } else {
            promoteVO.setIsaddorder(itemModel.getPromoteModel().getIsaddorder());
            promoteVO.setItemid(itemModel.getPromoteModel().getItemid());
            promoteVO.setPromoteid(itemModel.getPromoteModel().getPromoteid());
            promoteVO.setPromoteprice(itemModel.getPromoteModel().getPromoteprice());
            promoteVO.setStarttime(itemModel.getPromoteModel().getStarttime().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            promoteVO.setLasttime(itemModel.getPromoteModel().getLasttime().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoteVO(promoteVO);
        }
        return itemVO;
    }


}
