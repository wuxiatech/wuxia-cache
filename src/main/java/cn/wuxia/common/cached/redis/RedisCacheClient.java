/*
 * Created on :2016年4月13日
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.cached.redis;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.util.ArrayUtil;
import cn.wuxia.common.util.StringUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 简单的操作，复杂的操作需要直接操作jedis
 * 推荐使用redisson
 * @author songlin
 */
public class RedisCacheClient implements CacheClient {
    private static Logger logger = LoggerFactory.getLogger(RedisCacheClient.class);

    private Jedis jedis;

    private int expiredTime = 0;

    private String password;

    private JedisPool jedisPool;

    @Override
    public void init(String... server) {
        String host;
        String port = null;
        if (ArrayUtil.isNotEmpty(server)) {
            host = StringUtil.substringBefore(server[0], ":");
            port = StringUtil.substringAfter(server[0], ":");
        } else {
            host = "127.0.0.1";
        }

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        port = StringUtil.isBlank(port) ? "6379" : port;
        if (StringUtil.isNotBlank(password)) {
            jedisPool = new JedisPool(config, host, Integer.valueOf(port), 2000, password);
        } else {
            jedisPool = new JedisPool(config, host, Integer.valueOf(port));
        }
        jedis = jedisPool.getResource();
    }


    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.jedis = jedisPool.getResource();
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean containKey(String key, String namespace) {
        return containKey(namespace + ":" + key);
    }

    @Override
    public boolean containKey(String key) {
        key = RedisUtils.formatKey(key);
        return BooleanUtils.toBooleanDefaultIfNull(jedis.exists(key), false);
    }

    @Override
    public void add(String key, Object value, int expiredTime) {
        set(key, value, expiredTime);
    }

    @Override
    public void add(String key, Object value, String namespace) {
        set(namespace + ":" + key, value);
    }

    @Override
    public void add(String key, Object value) {
        set(key, value);

    }

    @Override
    public void set(String key, Object value, int expiredTime) {
        if (value == null)
            return;
        key = RedisUtils.formatKey(key);
        final byte[] keyf = key.getBytes();
        final byte[] valuef = new ObjectsTranscoder().serialize(value);
        jedis.setex(keyf, expiredTime, valuef);
    }

    @Override
    public void set(String key, Object value, String namespace) {
        set(namespace + ":" + key, value);
    }

    @Override
    public void set(String key, Object value) {
        set(key, value, expiredTime);
    }

    @Override
    public void replace(String key, Object value, int expiredTime) {
        set(key, value, expiredTime);
    }

    @Override
    public void replace(String key, Object value, String namespace) {
        set(namespace + ":" + key, value);
    }

    @Override
    public void replace(String key, Object value) {
        set(key, value);
    }

    @Override
    public <T> T get(String key) {
        
        key = RedisUtils.formatKey(key);
        byte[] value = jedis.get(key.getBytes());
        return (T) new ObjectsTranscoder().deserialize(value);
    }

    @Override
    public long incr(String key) {
        key = RedisUtils.formatKey(key);
        return jedis.incr(key);
    }

    @Override
    public long incr(String key, String namespace) {
        return incr(namespace + ":" + key);
    }

    @Override
    public long incr(String key, long by) {
        return jedis.incrBy(key, by);
    }

    @Override
    public long incr(String key, long by, long defaultValue) {
        key = RedisUtils.formatKey(key);
        Long r = jedis.incrBy(key, by);
        if (r == null) return defaultValue;
        return r;
    }

    @Override
    public long incr(String key, long by, long defaultValue, String namespace) {
        return incr(namespace + ":" + key, by, defaultValue);
    }

    @Override
    public long decr(String key) {
        key = RedisUtils.formatKey(key);
        return jedis.decr(key);
    }

    @Override
    public long decr(String key, String namespace) {
        return decr(namespace + ":" + key);
    }

    @Override
    public long decr(String key, long by) {
        key = RedisUtils.formatKey(key);
        return jedis.incrBy(key, by);
    }

    @Override
    public long decr(String key, long by, long defaultValue) {
        key = RedisUtils.formatKey(key);
        Long r = jedis.decrBy(key, by);
        if (r == null) return defaultValue;
        return r;
    }

    @Override
    public long decr(String key, long by, long defaultValue, String namespace) {
        return decr(namespace + ":" + key, by, defaultValue);
    }

    @Override
    public void delete(String key) {
        key = RedisUtils.formatKey(key);
        jedis.del(key);
    }

    @Override
    public void flushAll() {

    }

    @Override
    public void flushAll(String[] servers) {
        // TODO Auto-generated method stub
    }

    @Override
    public void shutdown() {
        jedis.close();
        jedis.disconnect();
    }

    @Override
    public void add(String key, Object value, int expiredTime, String namespace) {
        set(namespace + ":" + key, value, expiredTime);
    }

    @Override
    public void set(String key, Object value, int expiredTime, String namespace) {
        set(namespace + ":" + key, value, expiredTime);

    }

    @Override
    public void replace(String key, Object value, int expiredTime, String namespace) {
        set(namespace + ":" + key, value, expiredTime);

    }

    @Override
    public <T> T get(String key, String namespace) {
        return get(namespace + ":" + key);
    }

    @Override
    public void delete(String key, String namespace) {
        delete(namespace + ":" + key);
    }

    @Override
    public void flush(String namespace) {
        Set<String> set = jedis.hkeys(namespace + "*");
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String keyStr = it.next();
            System.out.println(keyStr);
            jedis.del(keyStr);
        }
    }

}
