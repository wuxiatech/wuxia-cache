/*
 * Created on :2017年8月3日
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 wuxia.gd.cn All right reserved.
 */
package cn.wuxia.common.test;

import cn.wuxia.common.cached.memcached.MemcachedUtils;
import cn.wuxia.common.cached.memcached.XMemcachedClient;
import cn.wuxia.common.cached.redis.ObjectsTranscoder;
import cn.wuxia.common.cached.redis.RedisCacheClient;
import cn.wuxia.common.cached.redis.RedisClusterCacheClient;
import cn.wuxia.common.lock.JedisDistributedLock;
import cn.wuxia.common.lock.RedisDistributedLock;
import cn.wuxia.common.lock.RedissonDistributedLock;
import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.SerializeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestCache {

    public static void main(String[] args) {
//        String key = "access_token";
//        key += "wx8c625ac17367e886";
//        System.out.println(key);
//        System.out.println(MemcachedUtils.shaKey(key));
//
//        XMemcachedClient mc = new XMemcachedClient();
//        mc.init("127.0.0.1:11211");
//        mc.add("abc", "10", 20);
//        //mc.incr("abc", 1, 1L);
//        boolean t = true;
//        while(t){
//            System.out.println(DateUtil.format(new Date(), "HH:mm:ss")+"="+mc.get("abc"));
//            if(mc.get("abc") == null){
//                t = false;
//            }
//        }
//        //        TestMemcachedServer ms = new TestMemcachedServer();
////                ms.start("127.0.0.1", 11211);
//        // String key = "hello_" + 1;
//        //        mc.set("abc", "1323", 60 * 60 * 1);
//        //        String result = mc.get("abc");
//        //        logger.debug("1:" + result);
//        //        mc.delete("abcd");
//        //        result = mc.get("abc");
//        //        logger.debug("2:" + result);
//        //        mc.delete(MemcachedUtils.shaKey("access_token"));
//
//        //        mc.set(MemcachedUtils.shaKey("access_token"), "aafsdfsdfsdfsdff");
//        //        System.out.println("1111111==="+mc.get(MemcachedUtils.shaKey("access_token")));
//         key = "classcn.daoming.basic.api.open.service.impl.AuthorizerAccountServiceImpl.findAuthorizerByAppidwxdc282971b0b8af0a";
//        //System.out.println("======"+(String)mc.get(key, "1DayData"));
//        //mc.memcachedClient.endWithNamespace();
//
//        // mc.set(key, "999999999999999999999");
//        // System.out.println("dm ********************hello_" + mc.get(key));
//        // XMemcachedClient mc1 = new XMemcachedClient();
//        // mc1.init(ad1);
//        // XMemcachedClient mc2 = new XMemcachedClient();
//        // mc2.init(ad2);
//        // XMemcachedClient mc3 = new XMemcachedClient(ad3);
//        // System.out.println(ad1+" ********************hello_" + mc1.get(key));
//        // System.out.println(ad2+" ********************hello_" + mc2.get(key));
//        // System.out.println(ad3+" ********************hello_" + mc3.get(key));
//        // String addrs = ad3 + "," + ad2;
//        // int[] a = new int[2];
//        // a[0] = 1;
//        // a[1] = 2;
//        // MemcachedClientBuilder builder = new XMemcachedClientBuilder(
//        // AddrUtil.getAddressMap(StringUtil.join(addrs, " ")), a);
//        try {
//            // MemcachedClient memcachedClient = builder.build();
//            // System.out.println(builder.isFailureMode());
//            // builder.setFailureMode(true);
//
//            // memcachedClient.set("key1", 0, "123");
//            // memcachedClient.set("key2", 0, "456");
//            // System.out.println(memcachedClient.getStateListeners());
//            // System.out.println(memcachedClient.getStats());
//            // System.out.println(memcachedClient.getServersDescription());
//            // close memcached client
//            // System.out.println("======================"+memcachedClient.get("key1"));
//            // System.out.println("======================"+memcachedClient.get("key2"));
//            // memcachedClient.shutdown();
//            mc.shutdown();
//            // mc2.memcachedClient.shutdown();
//            // mc3.memcachedClient.shutdown();
//        } catch (Exception e) {
//            System.err.println("Shutdown MemcachedClient fail");
//            e.printStackTrace();
//        }


    }

    @Test
    public void testlock() throws InterruptedException {
        RedisClusterCacheClient clusterCacheClient = new RedisClusterCacheClient();
        clusterCacheClient.setPassword("test123");
        clusterCacheClient.init(new String[]{"127.0.0.1:6379"});
        JedisDistributedLock redisDistributeLock = new JedisDistributedLock(clusterCacheClient.getJedisCluster());
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            new Thread(() -> {
                if (redisDistributeLock.tryLock("TEST_LOCK_KEY", "TEST_LOCK_VAL_" + finalI, 1000 * 100, 1000 * 20)) {
                    try {
                        System.out.println("get lock successfully with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                        Thread.sleep(2000);
                        if (!redisDistributeLock.tryUnLock("TEST_LOCK_KEY", "TEST_LOCK_VAL_" + finalI)) {
                            throw new RuntimeException("release lock fail");
                        }
                        System.out.println("release lock successfully with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("get lock fail with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                }
            }).start();
        }

        Thread.sleep(1000 * 1000);
    }


    @Test
    public void testlock2() throws InterruptedException {
        RedisCacheClient cacheClient = new RedisCacheClient();
        cacheClient.setPassword("test123");
        cacheClient.init(new String[]{"127.0.0.1:6379"});
        RedisDistributedLock redisDistributedLock = new RedisDistributedLock(cacheClient);
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            new Thread(() -> {
                if (redisDistributedLock.tryLock( "TEST_LOCK_KEY", "TEST_LOCK_VAL_" + finalI, 1000 * 100)) {
                    try {
                        System.out.println("get lock successfully with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                        Thread.sleep(2000);
                        if (!redisDistributedLock.tryUnlock("TEST_LOCK_KEY", "TEST_LOCK_VAL_" + finalI)) {
                            throw new RuntimeException("release lock fail");
                        }
                        System.out.println("release lock successfully with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("get lock fail with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                }
            }).start();
        }

        Thread.sleep(1000 * 100);
    }

    @Test
    public void testlock3() throws InterruptedException {

        RedissonDistributedLock redissonDistributedLock = new RedissonDistributedLock("redis://127.0.0.1:6379", "test123");
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            new Thread(() -> {
                if (redissonDistributedLock.lock("TEST_LOCK_KEY", TimeUnit.MILLISECONDS, 1000 * 100, 200)) {
                    try {
                        System.out.println("get lock successfully with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                        Thread.sleep(2000);
                        redissonDistributedLock.unlock("TEST_LOCK_KEY");
                        System.out.println("release lock successfully with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("get lock fail with lock value:-----" + "TEST_LOCK_VAL_" + finalI);
                }
            }).start();
        }

        Thread.sleep(1000 * 100);
    }

    @Test
    public void testSer() {
        A a = new A("abc", new Date(), DateUtil.newInstanceDate());
        byte[] b = new ObjectsTranscoder().serialize(a);
        Object c = new ObjectsTranscoder().deserialize(b);
        System.out.println(c);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class A implements Serializable {
        String a;
        Date b;
        Timestamp c;
    }
}
