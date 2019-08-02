package com.boge.demo.controller;


import com.boge.demo.commons.RedisUtils;
import com.boge.demo.controller.VO.ItemVO;

import com.boge.demo.dataobject.StockDO;
import com.boge.demo.mapper.StockDOMapper;
import com.boge.demo.response.EmBusinessMyError;
import com.boge.demo.response.ResponseType;


import com.boge.demo.service.ItemService;
import com.boge.demo.service.model.ItemModel;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.*;

@CrossOrigin
@Controller
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemservice;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private StockDOMapper stockDOMapper;


    Map<String, Object> map = new HashMap<>();

    //商品列表页面
    @RequestMapping(value = "/getItemList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseType getAllList(@RequestParam("getitem") String getitem) {


        List<ItemModel> itemlist = new ArrayList<ItemModel>();

        if (StringUtils.equals(getitem, "getitemlist")) {


            itemlist = itemservice.getAllItem();
            if (itemlist != null) {
                return ResponseType.Create(itemlist);
            } else {
                map.put("errorCode", EmBusinessMyError.PRODUCTS_NOT_EXIST.getErrorCode());
                map.put("errorMsg", EmBusinessMyError.PRODUCTS_NOT_EXIST.getErrorMsg());
                return ResponseType.Create("fail", map);
            }
        } else {
            map.put("errorCode", EmBusinessMyError.PARMAS_NOT_VALUEABLE.getErrorCode());
            map.put("errorMsg", EmBusinessMyError.PARMAS_NOT_VALUEABLE.getErrorMsg());
            return ResponseType.Create("fail", map);
        }

    }


    //商品详情页
    @ResponseBody
    @RequestMapping(value = "/getitem", method = RequestMethod.GET)
    public ResponseType getItemInfo(@RequestParam("id") Integer id, HttpServletRequest request) {
        //ip方式限制用户刷新接口，有缺陷误伤
        /*String ip = CommonUtils.getRealIp(request);
        logger.info(ip);
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmm");
        String time = sdf.format(Calendar.getInstance().getTime());
        if(redisUtils.hasKey(time +"_"+ip+"_IP"+id+"_ID")){
            if((Integer)redisUtils.get(time +"_"+ip+"_IP"+id+"_ID")>=60){
                //调用验证码

                //抛出异常
                map.put("errorCode", EmBusinessMyError.GET_ERROR.getErrorCode());
                map.put("errorMsg", EmBusinessMyError.GET_ERROR.getErrorMsg());
                return ResponseType.Create("fail", map);
            }else {
                redisUtils.incr(time +"_"+ip+"_IP"+id+"_ID", 1);}
        }else {
            redisUtils.set(time +"_"+ip+"_IP"+id+"_ID",1,60);
        }*/

        ItemVO itemVO = new ItemVO();
        itemVO = itemservice.getItemInfo(id);
        if (itemVO == null) {
            map.put("errorCode", EmBusinessMyError.PRODUCTS_NOT_EXIST.getErrorCode());
            map.put("errorMsg", EmBusinessMyError.PRODUCTS_NOT_EXIST.getErrorMsg());
            return ResponseType.Create("fail", map);
        } else {

            //  redisUtils.set("promote_item_stock"+itemVO.getId(),itemVO.getStocknum(),600);
            return ResponseType.Create(itemVO);
        }
    }

    //初始化redis中商品库存数据
    @ResponseBody
    @RequestMapping(value = "/setstocktoredis", method = RequestMethod.POST)
    public ResponseType setStockToRedis() {
        List<StockDO> itemlist = new ArrayList<StockDO>();
        itemlist = stockDOMapper.getAllStcok();
        if (itemlist != null) {
            for (int i = 0; i < itemlist.size(); i++) {
                //redis初始库存
                redisUtils.set("promote_item_stock" + itemlist.get(i).getItemid(), itemlist.get(i).getStocknum(), 6000);
                //秒杀令牌桶数量，存入redis,也可单独商品设置，默认令牌桶限制令牌数为库存的2倍
                redisUtils.set("promote_item_maxToken" + itemlist.get(i).getItemid(), itemlist.get(i).getStocknum() * 2, 6000);
            }
            return ResponseType.Create("setsuccess");
        }
        map.put("errorCode", EmBusinessMyError.INIT_REDIS_STOCK_ERROR.getErrorCode());
        map.put("errorMsg", EmBusinessMyError.INIT_REDIS_STOCK_ERROR.getErrorMsg());
        return ResponseType.Create("fail", map);
    }


}
