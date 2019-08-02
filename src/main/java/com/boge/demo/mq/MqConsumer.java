package com.boge.demo.mq;

import com.alibaba.fastjson.JSON;
import com.boge.demo.controller.BaseController;
import com.boge.demo.mapper.ItemDOMapper;
import com.boge.demo.mapper.StockDOMapper;
import org.apache.ibatis.javassist.bytecode.stackmap.BasicBlock;
import org.apache.log4j.Logger;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.mq
 * @date:2019/7/14
 */
@Component
public class MqConsumer {

    private DefaultMQPushConsumer consumer;

    public final static Logger logger = Logger.getLogger(MqConsumer.class);

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private StockDOMapper stockDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        consumer.subscribe(topicName, "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //消费端接收并处理消息，实现减库存操作
                Message message = msgs.get(0);
                String jsonString = new String(message.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                logger.info("消费端接收成功！" + itemId + "  " + amount);
                try {
                    stockDOMapper.updateStock(itemId, amount);
                } catch (Exception e) {
                    logger.error(e);
                }

                logger.info("消费成功！");
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
}
