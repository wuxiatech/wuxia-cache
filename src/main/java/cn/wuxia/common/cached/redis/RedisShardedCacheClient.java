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
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 简单的操作，复杂的操作需要直接操作jedis
 * songlin.li
 */
public class RedisShardedCacheClient implements CacheClient {
    private static Logger logger = LoggerFactory.getLogger(RedisShardedCacheClient.class);

    private ShardedJedis shardedJedis;

    private int expiredTime = 0;

    private String password;

    private ShardedJedisPool jedisPool;

    @Override
    public void init(String... servers) {

        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        if (ArrayUtil.isNotEmpty(servers)) {
            for (String server : servers) {
                String host = StringUtil.substringBefore(server, ":");
                String port = StringUtil.substringAfter(server, ":");
                JedisShardInfo shardInfo = new JedisShardInfo(host, port);
                shards.add(shardInfo);
            }
        } else {
            JedisShardInfo shardInfo = new JedisShardInfo("127.0.0.1");
            shards.add(shardInfo);
        }

        if (StringUtil.isNotBlank(password)) {
            for (JedisShardInfo shardInfo : shards) {
                shardInfo.setPassword(password);
            }
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        ShardedJedisPool sjp = new ShardedJedisPool(config, shards);
        shardedJedis = sjp.getResource();
    }

    public ShardedJedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(ShardedJedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.shardedJedis = jedisPool.getResource();
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
        return BooleanUtils.toBooleanDefaultIfNull(shardedJedis.exists(key), false);
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
        final byte[] keyf = key.getBytes();
        final byte[] valuef = new ObjectsTranscoder().serialize(value);
        shardedJedis.setex(keyf, expiredTime, valuef);
    }

    @Override
    public void set(String key, Object value, String namespace) {
        set(namespace + ":" + key, value);
    }

    @Override
    public void set(String key, Object value) {
        if (value == null)
            return;
        final byte[] keyf = key.getBytes();
//        if (value instanceof List) {
//            final byte[] valuef = new ListTranscoder().serialize(value);
//            shardedJedis.set(keyf, valuef);
//        } else {
        final byte[] valuef = new ObjectsTranscoder().serialize(value);
        shardedJedis.set(keyf, valuef);
//        }
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
        byte[] value = shardedJedis.get(key.getBytes());
        return (T) new ObjectsTranscoder().deserialize(value);
    }

    @Override
    public long incr(String key) {
        return shardedJedis.incr(key);
    }

    @Override
    public long incr(String key, String namespace) {
        return incr(namespace + ":" + key);
    }

    @Override
    public long incr(String key, long by) {
        return shardedJedis.incrBy(key, by);
    }

    @Override
    public long incr(String key, long by, long defaultValue) {
        Long r = shardedJedis.incrBy(key, by);
        if (r == null) return defaultValue;
        return r;
    }

    @Override
    public long incr(String key, long by, long defaultValue, String namespace) {
        return incr(namespace + ":" + key, by, defaultValue);
    }

    @Override
    public long decr(String key) {
        return shardedJedis.decr(key);
    }

    @Override
    public long decr(String key, String namespace) {
        return decr(namespace + ":" + key);
    }

    @Override
    public long decr(String key, long by) {
        return shardedJedis.incrBy(key, by);
    }

    @Override
    public long decr(String key, long by, long defaultValue) {
        Long r = shardedJedis.decrBy(key, by);
        if (r == null) return defaultValue;
        return r;
    }

    @Override
    public long decr(String key, long by, long defaultValue, String namespace) {
        return decr(namespace + ":" + key, by, defaultValue);
    }

    @Override
    public void delete(String key) {
        shardedJedis.del(key);
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
        shardedJedis.close();
        shardedJedis.disconnect();
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
        Set<String> set = shardedJedis.hkeys(namespace + "*");
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String keyStr = it.next();
            System.out.println(keyStr);
            shardedJedis.del(keyStr);
        }
    }

}
