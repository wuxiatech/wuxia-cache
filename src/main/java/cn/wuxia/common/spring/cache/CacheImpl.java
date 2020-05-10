/*
 * Created on :31 Aug, 2015
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.spring.cache;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.cached.memcached.MemcachedUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

/**
 * [ticket id]
 * 重写实现spring @Cacheable 支持 Memcache, Redis等
 *
 * @author songlin
 * @ Version : V<Ver.No> <31 Aug, 2015>
 */
@NoArgsConstructor
@AllArgsConstructor
public class CacheImpl implements Cache {
    private static Logger logger = LoggerFactory.getLogger(CacheImpl.class);

    private CacheClient cacheClient;

    private String cacheName;

    private int expiredTime = 0;

    private boolean disableCache = false;

    public CacheClient getCacheClient() {
        return cacheClient;
    }

    public void setCacheClient(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this.cacheClient;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (disableCache) {
            loggerMsg();
            return null;
        }

        if (MemcachedUtils.hasControlChar(key.toString())) {
            key = key.toString().replaceAll("\\s*", "");
        }
        logger.debug("get:{}, cachename:{}", key, this.cacheName);

        Object object = this.cacheClient.get(key.toString(), this.cacheName);
        return (object != null ? new SimpleValueWrapper(object) : null);
    }

    @Override
    public void put(Object key, Object value) {
        if (disableCache) {
            loggerMsg();
            return;
        }
        if (value == null) {
            return;
        }
        if (MemcachedUtils.hasControlChar(key.toString())) {
            key = key.toString().replaceAll("\\s*", "");
        }
        logger.debug("set:{}, cachename:{}", key, this.cacheName);
        this.cacheClient.set(key.toString(), value, this.expiredTime, this.cacheName);
    }

    @Override
    public void evict(Object key) {
        if (disableCache) {
            return;
        }
        if (MemcachedUtils.hasControlChar(key.toString())) {
            key = key.toString().replaceAll("\\s*", "");
        }
        logger.debug("delete:{}, cachename:{}", key, this.cacheName);
        this.cacheClient.delete(key.toString(), this.cacheName);
    }

    @Override
    public void clear() {
        if (disableCache) {
            return;
        }
        this.cacheClient.flush(this.cacheName);
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isDisableCache() {
        return disableCache;
    }

    public void setDisableCache(boolean disableCache) {
        this.disableCache = disableCache;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        if (disableCache) {
            loggerMsg();
            return null;
        }
        ValueWrapper valueWrapper = get(key);
        if (valueWrapper != null) {
            return (T) valueWrapper.get();
        }
        return null;
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper v = get(key);
        if (v == null) {
            put(key, value);
            return new SimpleValueWrapper(value);
        } else {
            return v;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> arg1) {
        return (T) get(key.toString());
    }

    public void loggerMsg() {
        if (disableCache) {
            logger.warn("缓存{}已被禁用", cacheName);
        }
    }
}
