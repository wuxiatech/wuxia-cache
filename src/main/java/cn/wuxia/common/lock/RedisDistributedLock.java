package cn.wuxia.common.lock;

import cn.wuxia.common.cached.redis.RedisCacheClient;
import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * 基于Redis分布式锁的实现
 */
public class RedisDistributedLock {


    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryLock(RedisCacheClient redisCacheClient, String lockKey, String requestId, int expireTime) {
        return tryLock(redisCacheClient.getJedisPool().getResource(), lockKey, requestId, expireTime);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param jedis      Redis客户端
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryLock(Jedis jedis, String lockKey, String requestId, int expireTime) {

        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;

    }


    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 释放分布式锁
     *
     * @param jedis     Redis客户端
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean tryUnlock(Jedis jedis, String lockKey, String requestId) {

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;

    }

    /**
     * 释放分布式锁
     *
     * @param redisCacheClient Redis客户端
     * @param lockKey          锁
     * @param requestId        请求标识
     * @return 是否释放成功
     */
    public static boolean tryUnlock(RedisCacheClient redisCacheClient, String lockKey, String requestId) {
        return tryUnlock(redisCacheClient.getJedisPool().getResource(), lockKey, requestId);
    }
}
