package com.boge.demo.controller;

import com.boge.demo.commons.MD5Utils;
import com.boge.demo.commons.RedisUtils;
import com.boge.demo.dataobject.PromoteDO;
import com.boge.demo.mq.MqProducer;
import com.boge.demo.response.EmBusinessMyError;
import com.boge.demo.response.ResponseType;
import com.boge.demo.service.ItemService;
import com.boge.demo.service.OrderService;
import com.boge.demo.service.PromoteService;
import com.boge.demo.controller.VO.OrderVO;
import com.boge.demo.service.model.OrderModel;
import com.boge.demo.service.model.UserModel;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.boge.demo.controller.BaseController.logger;
import static com.boge.demo.controller.BaseController.responsehead;


/**
 * 提交订单核心模块
 */

@Controller
@CrossOrigin
public class OrderController {


    @Autowired
    private ItemService itemService;

    @Autowired
    RedisUtils redisUtils;
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private PromoteService promoteService;

    //队列泄洪
    private ExecutorService executorService;

    //谷歌guava令牌桶
    private RateLimiter rateLimiter;

    @Autowired
    private OrderService orderService;

    //初始化队列池、令牌桶
    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);

        //单机器限200 tps
        rateLimiter = RateLimiter.create(200);
    }

    //生成token，验证用户是否合法
    @ResponseBody
    @RequestMapping(value = "/getToken", method = RequestMethod.POST, consumes = {responsehead})
    public ResponseType getToken(@RequestParam(name = "username") String username, @RequestParam(name = "itemid") Integer itemid) {
        Map<String, Object> resp = new HashMap<>();
        logger.info(username + "  " + itemid);
        int promoteid = 0;
        PromoteDO promoteDO = promoteService.selectByItemId(itemid);
        if (promoteDO != null) {
            promoteid = promoteDO.getPromoteid();
        }
        //1、初步完成数据校验,用户是否已登录，产品是否处于活动时间，下单数量是否正确，该产品库存是否大于下单数量
        // logger.info(redisUtils.get(username));
        //判断用户是否合法
        UserModel userModel = (UserModel) redisUtils.get("loginid_" + username);
        if (userModel == null && !userModel.getTelphone().equals("")) {
            resp.put("errorcode", EmBusinessMyError.USER_NOT_EXIST.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_NOT_EXIST.getErrorMsg());
            return ResponseType.Create("fail", resp);
        }

        //该用户是否已登录
        String loginstr = MD5Utils.getSaltMD5AStr(username, "LOGIN_USER");
        if (redisUtils.hasKey(username) && redisUtils.get(username).equals(loginstr)) {
            //生成秒该用户的杀令牌
            String SKilltoken = promoteService.generateSKillToken(promoteDO, promoteid, itemid, userModel.getId());
            if (SKilltoken == null) {
                resp.put("errorcode", EmBusinessMyError.TOKEN_BUILD_ERROR.getErrorCode());
                resp.put("errorMsg", EmBusinessMyError.TOKEN_BUILD_ERROR.getErrorMsg());
                return ResponseType.Create("fail", resp);
            }
            return ResponseType.Create(SKilltoken);
        }
        return null;
    }



    //订单列表
    @ResponseBody
    @RequestMapping(value = "/getOrders" ,method = RequestMethod.GET)
    public ResponseType getOrders(@RequestParam(name = "username") String username) {
        Map<String, Object> resp = new HashMap<>();
      //  logger.info(username + "  " + itemid);

        //1、初步完成数据校验,用户是否已登录，产品是否处于活动时间，下单数量是否正确，该产品库存是否大于下单数量
        // logger.info(redisUtils.get(username));
        //判断用户是否合法
        UserModel userModel = (UserModel) redisUtils.get("loginid_" + username);
        if (userModel == null && !userModel.getTelphone().equals("")) {
            resp.put("errorcode", EmBusinessMyError.USER_NOT_EXIST.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_NOT_EXIST.getErrorMsg());
            return ResponseType.Create("fail", resp);
        }

        //该用户是否已登录
       /* String loginstr = MD5Utils.getSaltMD5AStr(username, "LOGIN_USER");
        if (redisUtils.hasKey(username) && redisUtils.get(username).equals(loginstr)) {
                resp.put("errorcode", EmBusinessMyError.USER_LOGIN_LOSE.getErrorCode());
                resp.put("errorMsg", EmBusinessMyError.USER_LOGIN_LOSE.getErrorMsg());
                return ResponseType.Create("fail", resp);
        }*/
        List<OrderVO> list= orderService.getAllOrders();

        return ResponseType.Create(list);
    }

    //提交订单，校验放在controller
    @ResponseBody
    @RequestMapping(value = "/addorder", method = RequestMethod.POST, consumes = {responsehead})
    public ResponseType SubmitOrder(@RequestParam(name = "username") String username, @RequestParam(name = "itemid") Integer itemid,
                                    @RequestParam(name = "amount") Integer amount,
                                    @RequestParam(name = "promoteToken") String promoteToken, HttpServletRequest request) {
        Map<String, Object> resp = new HashMap<>();
        //   logger.info(username+" "+itemid+" "+amount+" "+promoteToken);

        //超出令牌桶tps限制，拦截限流
        if (!rateLimiter.tryAcquire()) {
            logger.info(rateLimiter.acquire());
            resp.put("errorcode", EmBusinessMyError.SO_busy.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.SO_busy.getErrorMsg());
            return ResponseType.Create("fail", resp);
        }


        OrderModel orderModel = new OrderModel();
        final int[] num = {0};
        int promoteid = 0;
        PromoteDO promoteDO = promoteService.selectByItemId(itemid);
        if (promoteDO != null) {
            promoteid = promoteDO.getPromoteid();
        }
        //1、初步完成数据校验,用户是否已登录，产品是否处于活动时间，下单数量是否正确，该产品库存是否大于下单数量
        // logger.info(redisUtils.get(username));
        //判断用户是否合法
        UserModel userModel = (UserModel) redisUtils.get("loginid_" + username);
        if (userModel == null && !userModel.getTelphone().equals("")) {
            resp.put("errorcode", EmBusinessMyError.USER_NOT_EXIST.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_NOT_EXIST.getErrorMsg());
            return ResponseType.Create("fail", resp);
        }
        if (promoteDO != null) {
            if (promoteToken != null) {
                String redispromoteToken = (String) redisUtils.get("promote_toke_" + itemid + userModel.getId());
                if (redispromoteToken == null) {
                    // throw new BusinessException(EmBusinessMyError.TOKEN_ERROR,"令牌校验失败");
                    resp.put("errorcode", EmBusinessMyError.TOKEN_ERROR.getErrorCode());
                    resp.put("errorMsg", EmBusinessMyError.TOKEN_ERROR.getErrorMsg());
                    return ResponseType.Create("fail", resp);
                }
                if (!StringUtils.equals(promoteToken, redispromoteToken)) {
                    resp.put("errorcode", EmBusinessMyError.TOKEN_ERROR.getErrorCode());
                    resp.put("errorMsg", EmBusinessMyError.TOKEN_ERROR.getErrorMsg());
                    return ResponseType.Create("fail", resp);
                }
            }
        }
        orderModel.setAmount(amount);
        orderModel.setItemid(itemid);
        orderModel.setPromoteid(promoteid);
        orderModel.setUserid(userModel.getId());
        orderModel.setUsername(username);

        //该用户是否已登录
        String loginstr = MD5Utils.getSaltMD5AStr(username, "LOGIN_USER");
        if (redisUtils.hasKey(username) && redisUtils.get(username).equals(loginstr)) {
            //用户已登录时，下单数量初步校验
            if (amount < 1 || amount > 1000000) {
                resp.put("errorcode", EmBusinessMyError.ORDER_AMOUNT_NOT_VALUEABLE.getErrorCode());
                resp.put("errorMsg", EmBusinessMyError.ORDER_AMOUNT_NOT_VALUEABLE.getErrorMsg());
                return ResponseType.Create("fail", resp);
            }
            //判断库存是否充足
            int redisstock = (int) redisUtils.get("promote_item_stock" + orderModel.getItemid());
            if (redisstock <= 0) {
                resp.put("errorcode", EmBusinessMyError.STOCK_TOO_LOW.getErrorCode());
                resp.put("errorMsg", EmBusinessMyError.STOCK_TOO_LOW.getErrorMsg());
                return ResponseType.Create("fail", resp);
            }

            //队列泄洪,拥塞窗口为20
            Future<Object> future = executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    //加入库存流水init状态
                    String stcokLogId = itemService.initStockLog(itemid, amount);

                    //进入下单逻辑，使用rocketmq消息队列，削峰异步扣库存
                    //返回前端下单结果
                    if (mqProducer.asyncTransacionReduceStock(orderModel, stcokLogId)) {
                        num[0] = 1;
                        return ResponseType.Create(1);
                    } else {
                        resp.put("errorCode", EmBusinessMyError.ORDER_SUBMIT_FAIL.getErrorCode());
                        resp.put("errorMsg", EmBusinessMyError.ORDER_SUBMIT_FAIL.getErrorMsg());
                        return ResponseType.Create("fail", resp);
                    }
                }
            });
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp.put("errorCode", EmBusinessMyError.ORDER_SUBMIT_FAIL.getErrorCode());
                resp.put("errorMsg", EmBusinessMyError.ORDER_SUBMIT_FAIL.getErrorMsg());
                return ResponseType.Create("fail", resp);
            } catch (ExecutionException e) {
                e.printStackTrace();
                resp.put("errorCode", EmBusinessMyError.ORDER_SUBMIT_FAIL.getErrorCode());
                resp.put("errorMsg", EmBusinessMyError.ORDER_SUBMIT_FAIL.getErrorMsg());
                return ResponseType.Create("fail", resp);
            }
        } else {
            resp.put("errorcode", EmBusinessMyError.USER_LOGIN_LOSE.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_LOGIN_LOSE.getErrorMsg());
            return ResponseType.Create("fail", resp);
        }
        return ResponseType.Create(num[0]);
    }


}
