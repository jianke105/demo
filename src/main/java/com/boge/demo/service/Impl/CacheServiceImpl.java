package com.boge.demo.service.Impl;

import com.boge.demo.service.CacheServcie;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * guava本地缓存,构造和存取类
 *
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.service.Impl
 * @date:2019/7/13
 */
@Service
public class CacheServiceImpl implements CacheServcie {

    private Cache<String, Object> commonCache = null;

    @PostConstruct
    public void init() {
        commonCache = CacheBuilder.newBuilder()
                //设置缓存初始容量
                .initialCapacity(10)
                //最大容量,超过容量按LRU规则删除
                .maximumSize(100)
                //过期时间
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void setLocalCache(String key, Object value) {
        commonCache.put(key, value);
    }

    @Override
    public Object getLocalCache(String key) {
        return commonCache.getIfPresent(key);
    }

    @Override
    public void delLocalCache(String key) {
        commonCache.invalidate(key);
    }
}
