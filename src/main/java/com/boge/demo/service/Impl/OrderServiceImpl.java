package com.boge.demo.service.Impl;

import com.boge.demo.commons.DateUtil;
import com.boge.demo.commons.RedisUtils;
import com.boge.demo.controller.BaseController;
import com.boge.demo.dataobject.*;
import com.boge.demo.mapper.*;
import com.boge.demo.mq.MqProducer;
import com.boge.demo.response.BusinessException;
import com.boge.demo.response.EmBusinessMyError;
import com.boge.demo.service.CacheServcie;
import com.boge.demo.service.OrderService;
import com.boge.demo.service.model.ItemModel;
import com.boge.demo.controller.VO.OrderVO;
import com.boge.demo.service.model.OrderModel;
import com.boge.demo.service.model.PromoteModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private PromoteDOMapper promoteDOMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CacheServcie cacheServcie;
    @Autowired
    private MqProducer producer;

    @Autowired
    private StocklogDOMapper stocklogDOMapper;

    public final static Logger logger = Logger.getLogger(BaseController.class);

    @Override
    @Transactional
    public int addOrder(OrderModel orderModel, String stockLogId) throws BusinessException {
        int num = 0;
        //1、使用redis验证产品是否存在
        ItemModel itemModel = new ItemModel();
        String itemexist = (String) redisUtils.get("item_exist_" + orderModel.getItemid());

        if (StringUtils.isEmpty(itemexist)) {
            //商品不存在或已下架
            throw new BusinessException(EmBusinessMyError.PRODUCTS_NOT_EXIST);
        }
        itemModel = (ItemModel) redisUtils.get("item_" + orderModel.getItemid());
        //2、使用redis验证产品数量是否大于下单数量
        if (redisUtils.get("promote_item_stock" + orderModel.getItemid()) == null) {
            //没有初始化redis库存
            throw new BusinessException(EmBusinessMyError.INIT_REDIS_FAIL);
        }

        if ((Integer) redisUtils.get("promote_item_stock" + orderModel.getItemid()) <= 0 || (Integer) redisUtils.get("promote_item_stock" + orderModel.getItemid()) < orderModel.getAmount()) {
            //库存不存在或已下架
            throw new BusinessException(EmBusinessMyError.STOCK_TOO_LOW);
        }
       /* if(itemModel.getStocknum()<=0&&itemModel.getStocknum()<orderModel.getAmount()){
            //库存不存在或已下架
            throw new BusinessException(EmBusinessMyError.STOCK_TOO_LOW);
        }*/


        //4、判断产品是否处于活动时间
        //获取下单日期
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        //joda类型
        DateTime timestamp1 = new DateTime(date);
        //5、生成订单号
        String orderNO = getOrderNOByTime();
        OrderDO orderDO1 = new OrderDO();

        PromoteModel promoteModel = itemModel.getPromoteModel();

        if (promoteModel != null) {

            if (promoteModel.getStarttime().isBefore(timestamp1) && promoteModel.getLasttime().isAfter(timestamp1)) {
                promoteModel.setIsaddorder(2);//有秒杀活动时数据库提交操作,以秒杀价格下单
                //1、落单减库存
                //  itemDOMapper.updateStock(orderModel.getItemid(),orderModel.getAmount());
                boolean decrease = DecreaseStock(orderModel);
                if (!decrease) {
                    throw new BusinessException(EmBusinessMyError.ORDER_SUBMIT_FAIL);
                }

                //2、新增订单
                orderDO1.setOderid(orderNO);
                orderDO1.setItemid(orderModel.getItemid());
                orderDO1.setItemdes(itemModel.getDes());
                orderDO1.setItemname(itemModel.getItemname());
                orderDO1.setItemnum(orderModel.getAmount());
                orderDO1.setOrderdate(timestamp);
                orderDO1.setPrice(promoteModel.getPromoteprice());
                orderDO1.setUserid(orderModel.getUserid());
                num = orderDOMapper.insertSelective(orderDO1);

                //更新销售量
                itemDOMapper.updateSales(orderModel.getItemid(), orderModel.getAmount());

                //设置库存流水状态为成功
                StocklogDO stocklogDO = stocklogDOMapper.selectByPrimaryKey(stockLogId);
                if (stocklogDO == null) {
                    throw new BusinessException(EmBusinessMyError.UNKNOWN_ERROR);

                }
                stocklogDO.setStatus(2);
                stocklogDOMapper.updateByPrimaryKeySelective(stocklogDO);

                /*//判断异步消息是否成功，失败则把redis减去的库存，加回去
                boolean  sendResult = asyncDecreaStock(orderModel);
                if(sendResult){

                }else{

                    redisUtils.incr("promote_item_stock"+orderModel.getItemid(),orderModel.getAmount());
                    throw new BusinessException(EmBusinessMyError.SEND_MSG_ERROR);
                }*/
                //更新redis,清除脏数据
                redisUtils.del("item_" + orderModel.getItemid());

                //更新本地缓存
                cacheServcie.delLocalCache("item_" + orderModel.getItemid());
                // return num;

            } else if (promoteModel.getLasttime().isBefore(timestamp1)) {
                promoteModel.setIsaddorder(3);
                promoteDOMapper.updateIsAddorder(promoteModel.getPromoteid(), 3);
                throw new BusinessException(EmBusinessMyError.PROMOTE_ORDER_END_YET);
            } else {
                promoteModel.setIsaddorder(1);
                promoteDOMapper.updateIsAddorder(promoteModel.getPromoteid(), 1);
                throw new BusinessException(EmBusinessMyError.PROMOTE_ORDER_NOT_START);
            }
        } else {//没有秒杀活动时数据库提交操作,以正常价格下单


            //在数据库中减库存,失败后抛异常
            boolean decrease = DecreaseStock(orderModel);
            if (!decrease) {
                throw new BusinessException(EmBusinessMyError.ORDER_SUBMIT_FAIL);
            }
            //   itemDOMapper.updateStock(orderModel.getItemid(),orderModel.getAmount());


            //2、新增订单,订单入库
            orderDO1.setOderid(orderNO);
            orderDO1.setItemid(orderModel.getItemid());
            orderDO1.setItemdes(itemModel.getDes());
            orderDO1.setItemname(itemModel.getItemname());
            orderDO1.setItemnum(orderModel.getAmount());
            orderDO1.setOrderdate(timestamp);
            orderDO1.setUserid(orderModel.getUserid());
            orderDO1.setPrice(itemModel.getPrice());
            num = orderDOMapper.insertSelective(orderDO1);

            //更新销售量
            itemDOMapper.updateSales(orderModel.getItemid(), orderModel.getAmount());


            //设置库存流水状态为成功
            StocklogDO stocklogDO = stocklogDOMapper.selectByPrimaryKey(stockLogId);
            if (stocklogDO == null) {
                throw new BusinessException(EmBusinessMyError.UNKNOWN_ERROR);
            }
            stocklogDO.setStatus(2);
            stocklogDOMapper.updateByPrimaryKeySelective(stocklogDO);


           /* //判断异步消息是否成功，失败则把redis减去的库存，加回去
            boolean  sendResult = asyncDecreaStock(orderModel);
            if(!sendResult){
                redisUtils.incr("promote_item_stock"+orderModel.getItemid(),orderModel.getAmount());
                throw new BusinessException(EmBusinessMyError.SEND_MSG_ERROR);
            }*/
            //更新redis,清除脏数据
            redisUtils.del("item_" + orderModel.getItemid());
            logger.info("活动中商品提交订单正常");
            //更新本地缓存
            cacheServcie.delLocalCache("item_" + orderModel.getItemid());
        }
        return num;
    }

    @Override
    public boolean asyncDecreaStock(OrderModel orderModel) {
        boolean sendResult = producer.asyncReduceStock(orderModel);
        return sendResult;
    }

    @Override
    public List<OrderVO> getAllOrders() {
        List<OrderDO> list = orderDOMapper.getAllOrders();
        List<OrderVO> listmodel = new ArrayList<OrderVO>();
        for(int i = 0;i<list.size();i++){
           OrderVO orderVO = ConverformDO(list.get(i));
           listmodel.add(orderVO);
        }

        return listmodel;
    }

    //优化后，从redis中减库存
    @Transactional
    public boolean DecreaseStock(OrderModel orderModel) {
        //1、落单减库存redis中
        long stocknum = redisUtils.decr("promote_item_stock" + orderModel.getItemid(), orderModel.getAmount());
        if (stocknum > 0) {
            // boolean  sendResult = producer.asyncReduceStock(orderModel.getItemid(),orderModel.getAmount());
          /* if(!sendResult){
               redisUtils.incr("promote_item_stock"+orderModel.getItemid(),orderModel.getAmount());
               return false;
           }*/
            return true;

        } else {
            redisUtils.incr("promote_item_stock" + orderModel.getItemid(), orderModel.getAmount());
            return false;
        }
    }


    //时间戳+6位随机数+3位分库分表位+商品id+4位随机数
    //分库分表位暂时默认002
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getOrderNOByTime() {

       /* SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        String newDate=sdf.format(new Date());
        String result="";
*/
        //订单号有18位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);

        //中间6位为自增序列
        //获取当前sequence

        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.selectByPrimaryKey("order_info");
        //判断改单是否为当天第一订单，如果是则更新sequence表的日期
        if (DateUtil.getDateSpace(DateUtil.Date2Str(sequenceDO.getCurrentdate()), DateUtil.Date2Str(DateUtil.getNow())) >= 1) {
            //  int num1= sequenceDOMapper.updateCurrentDate("order_info", new java.sql.Date(DateUtil.getNow().getTime()));

            sequenceDO.setCurrentvalue(0 + sequenceDO.getStep());
            sequenceDO.setCurrentdate(new java.sql.Date(DateUtil.getNow().getTime()));
            sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

            String sequenceStr = String.valueOf(sequence);
            for (int i = 0; i < 8 - sequenceStr.length(); i++) {
                stringBuilder.append(0);
            }
            stringBuilder.append(sequenceStr);
            //最后2位为分库分表位,暂时写死
            stringBuilder.append("02");

            return stringBuilder.toString();
        } else {
            sequence = sequenceDO.getCurrentvalue();
            sequenceDO.setCurrentvalue(sequenceDO.getCurrentvalue() + sequenceDO.getStep());
            sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
            String sequenceStr = String.valueOf(sequence);
            for (int i = 0; i < 8 - sequenceStr.length(); i++) {
                stringBuilder.append(0);
            }
            stringBuilder.append(sequenceStr);
            //最后2位为分库分表位,暂时写死
            stringBuilder.append("02");
            return stringBuilder.toString();
        }


    }

    public OrderVO ConverformDO(OrderDO orderDO){
        OrderVO orderVO = new OrderVO();
        if(orderDO!=null){
            BeanUtils.copyProperties(orderDO, orderVO);
        }
        return orderVO;
    }


}
