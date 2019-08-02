package com.boge.demo.mq;

import com.alibaba.fastjson.JSON;

import com.boge.demo.dataobject.StocklogDO;
import com.boge.demo.mapper.StocklogDOMapper;
import com.boge.demo.response.BusinessException;
import com.boge.demo.service.OrderService;
import com.boge.demo.service.model.OrderModel;
import org.apache.log4j.Logger;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;



import javax.annotation.PostConstruct;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.mq
 * @date:2019/7/14
 */
@Component
public class MqProducer {
    private DefaultMQProducer producer;


    private TransactionMQProducer transactionMQProducer;

    public final static Logger logger = Logger.getLogger(MqProducer.class);

    @Autowired
    private OrderService orderService;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;
    @Autowired
    private StocklogDOMapper stocklogDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
       /* producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();*/

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                //创建订单
                OrderModel orderModel = (OrderModel) ((Map) o).get("ordermodel");
                String stockLogId = (String) ((Map) o).get("stockLogId");
                try {
                    int num = orderService.addOrder(orderModel, stockLogId);
                    if (num == 1) {
                        return LocalTransactionState.COMMIT_MESSAGE;
                    } else {
                        StocklogDO stocklogDO = stocklogDOMapper.selectByPrimaryKey(stockLogId);
                        stocklogDO.setStatus(3);
                        stocklogDOMapper.updateByPrimaryKeySelective(stocklogDO);
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    }
                } catch (BusinessException e) {
                    e.printStackTrace();
                    StocklogDO stocklogDO = stocklogDOMapper.selectByPrimaryKey(stockLogId);
                    stocklogDO.setStatus(3);
                    stocklogDOMapper.updateByPrimaryKeySelective(stocklogDO);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                //  return LocalTransactionState.COMMIT_MESSAGE;
            }

            //unknown状态时回调
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                String jsonString = new String(messageExt.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                String stockLogId = (String) map.get("stockLogId");
                StocklogDO stocklogDO = stocklogDOMapper.selectByPrimaryKey(stockLogId);
                if (stocklogDO == null) {
                    return LocalTransactionState.UNKNOW;
                }
                if (stocklogDO.getStatus() == 2) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else if (stocklogDO.getStatus() == 1) {
                    return LocalTransactionState.UNKNOW;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
    }

    //事务型同步扣减库存消息
    public boolean asyncTransacionReduceStock(OrderModel orderModel, String stockLogId) {

        TransactionSendResult sendResult = null;
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", orderModel.getItemid());
        bodyMap.put("amount", orderModel.getAmount());
        bodyMap.put("stockLog", stockLogId);

        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("ordermodel", orderModel);
        argsMap.put("stockLogId", stockLogId);
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            logger.info("消息发送成功！");
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) {
            return false;
        } else if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) {
            return true;
        } else {
            return false;
        }

    }


    //同步库存扣减消息
    public boolean asyncReduceStock(OrderModel orderModel) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", orderModel.getItemid());
        bodyMap.put("amount", orderModel.getAmount());
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            logger.info("消息发送成功！");
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
