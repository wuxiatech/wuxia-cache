/*
 * Created on :2016年4月13日
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.cached.redis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.util.StringUtil;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 简单的操作，复杂的操作需要直接操作jedis
 * songlin.li
 */
public class RedisCacheClient implements CacheClient {
    private static Logger logger = LoggerFactory.getLogger(RedisCacheClient.class);

    private ShardedJedis jedis;

    private int expiredTime = 0;

    public void init(String... servers) {
        if (ArrayUtils.isEmpty(servers)) {
            logger.error("初始化失败，没找到redis服务端");
            return;
        }

        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        for (String server : servers) {
            String host = StringUtil.substringBefore(server, ":");
            String port = StringUtil.substringAfter(server, ":");
            shards.add(new JedisShardInfo(host, port));
        }
        ShardedJedisPool sjp = new ShardedJedisPool(new JedisPoolConfig(), shards);
        jedis = sjp.getResource();
    }

    public ShardedJedis getJedis() {
        return jedis;
    }

    public void setJedis(ShardedJedis jedis) {
        this.jedis = jedis;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    @Override
    public boolean containKey(String key) {
        return BooleanUtils.toBooleanDefaultIfNull(jedis.exists(key), false);
    }

    @Override
    public void add(String key, Object value, int expiredTime) {
        set(key, value, expiredTime);
    }

    @Override
    public void add(String key, Object value) {
        set(key, value);

    }

    @Override
    public void set(String key, Object value, int expiredTime) {
        if (value == null)
            return;
        final byte[] keyf = key.getBytes();
        final byte[] valuef = new ObjectsTranscoder().serialize(value);
        jedis.setex(keyf, expiredTime, valuef);
    }

    @Override
    public void set(String key, Object value) {
        if (value == null)
            return;
        final byte[] keyf = key.getBytes();
        if (value instanceof List) {
            final byte[] valuef = new ListTranscoder().serialize(value);
            jedis.set(keyf, valuef);
        } else {
            final byte[] valuef = new ObjectsTranscoder().serialize(value);
            jedis.set(keyf, valuef);
        }
    }

    @Override
    public void replace(String key, Object value, int expiredTime) {
        set(key, value, expiredTime);
    }

    @Override
    public void replace(String key, Object value) {
        set(key, value);
    }

    @Override
    public <T> T get(String key) {
        byte[] value = jedis.get(key.getBytes());
        return (T) new ObjectsTranscoder().deserialize(value);
    }

    @Override
    public long incr(String key) {
        return jedis.incr(key);
    }

    @Override
    public long incr(String key, long by) {
        return jedis.incrBy(key, by);
    }

    @Override
    public long incr(String key, long by, long defaultValue) {
        Long r = jedis.incrBy(key, by);
        if (r == null) return defaultValue;
        return r;
    }

    @Override
    public long decr(String key) {
        return jedis.decr(key);
    }

    @Override
    public long decr(String key, long by) {
        return jedis.incrBy(key, by);
    }

    @Override
    public long decr(String key, long by, long defaultValue) {
        Long r = jedis.decrBy(key, by);
        if (r == null) return defaultValue;
        return r;
    }

    @Override
    public void delete(String key) {
        jedis.del(key);
        jedis.del(key.getBytes());
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
        set(key, value, expiredTime);
    }

    @Override
    public void set(String key, Object value, int expiredTime, String namespace) {
        set(key, value, expiredTime);

    }

    @Override
    public void replace(String key, Object value, int expiredTime, String namespace) {
        set(key, value, expiredTime);

    }

    @Override
    public <T> T get(String key, String namespace) {
        return get(key);
    }

    @Override
    public void delete(String key, String namespace) {
        delete(key);
    }

    @Override
    public void flush(String namespace) {
        // TODO Auto-generated method stub
        //jedis.fl
    }

}
