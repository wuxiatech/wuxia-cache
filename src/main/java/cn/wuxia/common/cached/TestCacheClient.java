package cn.wuxia.common.cached;

import cn.wuxia.common.util.ClassLoaderUtil;

public class TestCacheClient {
    public static void main(String[] args) {
        //testmemcached();
        testredis();
    }

    public static void testmemcached() {
        //        String cacheImpl = "cn.ishare.common.cached.redis.RedisCacheClient";
        String cacheImpl = "cn.ishare.common.cached.memcached.XMemcachedClient";
        CacheClient cacheClient = null;
        try {
            cacheClient = (CacheClient) ClassLoaderUtil.loadClass(cacheImpl).newInstance();
            cacheClient.init("192.168.1.10:11211");
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
        String cacheImpl = "cn.ishare.common.cached.redis.RedisCacheClient";
        CacheClient cacheClient = null;
        try {
            cacheClient = (CacheClient) ClassLoaderUtil.loadClass(cacheImpl).newInstance();
            cacheClient.init("192.168.1.10:6379");
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
}
