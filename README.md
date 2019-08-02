集成rocketmq的电商管理系统后端
1、新增了rocketmq来异步同步扣减库存，减少单用户下单锁的持有时间，增加并发。
2、启动前先开启redis，配置rocketmq，并启动nameserver、broker、开启Queue。
