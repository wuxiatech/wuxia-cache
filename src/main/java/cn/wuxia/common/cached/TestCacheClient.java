package cn.wuxia.common.cached;

import cn.wuxia.common.cached.redis.RedisShardedCacheClient;
import cn.wuxia.common.util.ClassLoaderUtil;
import com.google.common.collect.Lists;

public class TestCacheClient {
    public static void main(String[] args) {
        //testmemcached();
        testredis();
    }

    public static void testmemcached() {
        //        String cacheImpl = "cn.ishare.common.cached.redis.RedisCacheClient";
        String cacheImpl = "cn.wuxia.common.cached.redis.XMemcachedClient";
        CacheClient cacheClient = null;
        try {
            cacheClient = (CacheClient) ClassLoaderUtil.loadClass(cacheImpl).newInstance();
            cacheClient.init("127.0.0.1:11211");
            cacheClient.set("classcn.zuji.nfyy.follow.core.form.service.impl.FormComponentServiceImpl.findEnabledByFormId61YjaBNPQhODSP3wZoDNyA30",
                    456);
            System.out.println(cacheClient.get(
                    "classcn.zuji.nfyy.follow.core.form.service.impl.FormComponentServiceImpl.findEnabledByFormId61YjaBNPQhODSP3wZoDNyA30") + "");
            cacheClient
                    .delete("classcn.zuji.nfyy.follow.core.form.service.impl.FormComponentServiceImpl.findEnabledByFormId61YjaBNPQhODSP3wZoDNyA30");
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            cacheClient.shutdown();
        }
    }

    public static void testredis() {
        //      String cacheImpl = "cn.ishare.common.cached.redis.RedisCacheClient";
        String cacheImpl = "cn.wuxia.common.cached.redis.RedisCacheClient";
        RedisShardedCacheClient cacheClient = null;
        try {
            cacheClient = (RedisShardedCacheClient) ClassLoaderUtil.loadClass(cacheImpl).newInstance();
            cacheClient.setPassword("test123");
            cacheClient.init("127.0.0.1:6379");
            cacheClient.set("classcn.zuji.nfyy.follow.core.form.service.impl.FormComponentServiceImpl.findEnabledByFormId61YjaBNPQhODSP3wZoDNyA30",
                    Lists.newArrayList("abc", "123"), "1Houce");
            Object value = cacheClient.get(
                    "classcn.zuji.nfyy.follow.core.form.service.impl.FormComponentServiceImpl.findEnabledByFormId61YjaBNPQhODSP3wZoDNyA30", "1Houce");
//            cacheClient
//                    .delete("classcn.zuji.nfyy.follow.core.form.service.impl.FormComponentServiceImpl.findEnabledByFormId61YjaBNPQhODSP3wZoDNyA30");
            System.out.println(value);

        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            cacheClient.shutdown();
        }
    }
}
