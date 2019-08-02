package com.boge.demo.service;

/**
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo.service
 * @date:2019/7/13
 */
public interface CacheServcie {

    void setLocalCache(String key, Object value);

    Object getLocalCache(String key);

    void delLocalCache(String key);
}
