/**
 * Copyright (c) 2005-2012 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License");
 */
package cn.wuxia.common.cached.memcached;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.google.common.collect.Lists;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.util.ListUtil;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;

/**
 * 不再维护及更新spyMemcached，请使用XMemcachedClient
 * 如使用本工具导致某些功能失效，请更改为XMemcachedClient提供支持
 * @author songlin.li
 * @see {@link XMemcachedClient 代替}
 */
@Deprecated
public class SpyMemcachedClient implements CacheClient, DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(SpyMemcachedClient.class);

    private MemcachedClient memcachedClient;

    private int expiredTime = 0;

    private long shutdownTimeout = 2500;

    private long updateTimeout = 2500;

    /**
     * Get method, the result of the conversion type and shielding abnormal, Null is returns only.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            MemcachedUtils.validateKey(key);
            return (T) memcachedClient.get(key);
        } catch (RuntimeException e) {
            handleException(e, key);
            return null;
        }
    }

    /**
     * GetBulk method, the result of the conversion type and shielding exception.
     */
    public <T> Map<String, T> getBulk(Collection<String> keys) {
        try {
            return (Map<String, T>) memcachedClient.getBulk(keys);
        } catch (RuntimeException e) {
            handleException(e, StringUtils.join(keys, ","));
            return null;
        }
    }

    /**
     * Asynchronous Set method does not consider the results of the implementation.
     */
    public void set(String key, Object value, int expiredTime) {
        if (value == null)
            return;
        try {
            MemcachedUtils.validateKey(key);
            memcachedClient.set(key, expiredTime, value);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    /**
     * Security Set method, updateTimeout seconds to return results, otherwise it returns false and cancel the operation.
     */
    public boolean safeSet(String key, Object value, int expiration) {
        if (value == null)
            return false;
        MemcachedUtils.validateKey(key);
        Future<Boolean> future = memcachedClient.set(key, expiration, value);
        try {
            return future.get(updateTimeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return false;
    }

    /**
     * Asynchronous Delete method does not consider the results of the implementation.
     */
    public void delete(String key) {
        try {
            MemcachedUtils.validateKey(key);
            memcachedClient.delete(key);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public long incr(String key) {
        return 0;
    }

    @Override
    public long incr(String key, long by) {
        return 0;
    }

    @Override
    public long incr(String key, long by, long defaultValue) {
        return 0;
    }

    @Override
    public long decr(String key) {
        return 0;
    }

    @Override
    public long decr(String key, long by) {
        return 0;
    }

    @Override
    public long decr(String key, long by, long defaultValue) {
        return 0;
    }

    /**
     * Secure Delete method, updateTimeout seconds to return results, and false otherwise cancel.
     */
    public boolean safeDelete(String key) {
        Future<Boolean> future = memcachedClient.delete(key);
        try {
            return future.get(updateTimeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return false;
    }

    /**
     * Incr method.
     */
    public long incr(String key, int by, long defaultValue) {
        try {
            MemcachedUtils.validateKey(key);
           return memcachedClient.incr(key, by, defaultValue);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return memcachedClient.incr(key, by, defaultValue);
    }

    /**
     * Decr method.
     */
    public long decr(String key, int by, long defaultValue) {
        try {
            MemcachedUtils.validateKey(key);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return memcachedClient.decr(key, by, defaultValue);
    }

    /**
     * The asynchronous the Incr methods, does not support the default value of -1 is returned if the key does not exist.
     */
    public Future<Long> asyncIncr(String key, int by) {
        try {
            MemcachedUtils.validateKey(key);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return memcachedClient.asyncIncr(key, by);
    }

    /**
     * The asynchronous the Decr method, does not support the default value of -1 is returned if the key does not exist.
     */
    public Future<Long> asyncDecr(String key, int by) {
        try {
            MemcachedUtils.validateKey(key);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return memcachedClient.asyncDecr(key, by);
    }

    private void handleException(Exception e, String key) {
        logger.warn("spymemcached client receive an exception with key:" + key, e);
    }

    @Override
    public void destroy() throws Exception {
        if (memcachedClient != null) {
            memcachedClient.shutdown(shutdownTimeout, TimeUnit.MILLISECONDS);
        }
    }

    public MemcachedClient getMemcachedClient() {
        return memcachedClient;
    }

    public void setMemcachedClient(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public void setUpdateTimeout(long updateTimeout) {
        this.updateTimeout = updateTimeout;
    }

    public void setShutdownTimeout(long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    /**
     * @return
     */
    public long getShutdownTimeout() {
        return shutdownTimeout;
    }

    /**
     * @return
     */
    public long getUpdateTimeout() {
        return updateTimeout;
    }

    /**
     * @return
     */
    public int getExpiredTime() {
        return expiredTime;
    }

    /**
     * @param int
     */
    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    @Override
    public boolean containKey(String key) {
        try {
            MemcachedUtils.validateKey(key);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        if (get(key) != null)
            return true;
        return false;
    }

    @Override
    public void set(String key, Object value) {

        try {
            MemcachedUtils.validateKey(key);
            this.set(key, value, expiredTime);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void replace(String key, Object value, int expiredTime) {
        try {
            MemcachedUtils.validateKey(key);
            memcachedClient.replace(key, expiredTime, value);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void replace(String key, Object value) {
        replace(key, value, expiredTime);
    }

    @Override
    public void flushAll() {
        memcachedClient.flush();
    }

    @Override
    public void flushAll(String[] servers) {
        // TODO Auto-generated method stub
    }

    @Override
    public void init(String... addrss) {
        if (ArrayUtils.isEmpty(addrss)) {
            throw new MemcachedException("初始化失败，没找到memcached服务端");
        }
        List<String> addrs = ListUtil.arrayToList(addrss);
        List<Integer> weights = Lists.newArrayList();
        for (int i = 0; i < addrs.size(); i++) {
            weights.add(i + 1);
        }
        ConnectionFactoryBuilder connectionFactoryBuilder = new ConnectionFactoryBuilder();

        try {
            memcachedClient = new MemcachedClient(connectionFactoryBuilder.build(), AddrUtil.getAddresses(addrs));
        } catch (IOException e) {
            logger.error("初始化失败", e.getMessage());
        }

    }

    @Override
    public void add(String key, Object value, int expiredTime) {
        if (value == null)
            return;
        try {
            MemcachedUtils.validateKey(key);
            memcachedClient.add(key, expiredTime, value);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void add(String key, Object value) {
        try {
            MemcachedUtils.validateKey(key);
            add(key, value, expiredTime);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        memcachedClient.shutdown();
    }

    @Override
    public void add(String key, Object value, int expiredTime, String namespace) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void set(String key, Object value, int expiredTime, String namespace) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void replace(String key, Object value, int expiredTime, String namespace) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T> T get(String key, String namespace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(String key, String namespace) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void flush(String namespace) {
        // TODO Auto-generated method stub
        
    }

  

}
