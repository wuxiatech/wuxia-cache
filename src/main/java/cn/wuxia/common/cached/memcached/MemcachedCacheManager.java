package cn.wuxia.common.cached.memcached;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.spring.cache.CacheImpl;
import cn.wuxia.common.spring.enums.CacheNameEnum;
import cn.wuxia.common.spring.handler.SpringCacheManager;
import cn.wuxia.common.util.PropertiesUtils;
import cn.wuxia.common.util.StringUtil;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;
import net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

@Configuration
//@EnableCaching
@Conditional(SpringCacheManager.InitMemcachedCondition.class)
@PropertySource(value = {"classpath:memcached.properties", "classpath:properties/memcached.properties"}, ignoreResourceNotFound = true)
@Slf4j
public class MemcachedCacheManager {

    @Value("${memcached.servers:127.0.0.1:11211}")
    private String servers;
    @Value("${memcached.poolSize:2}")
    private int poolSize;
    @Value("${memcached.expiredTime:3000}")
    private int expiredTime;


    /**
     * <bean name="memcachedClient" class="net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean">
     * <property name="servers">
     * <!-- <value>${memcached.server2},${memcached.server3}</value> -->
     * <value>${memcached.server2}</value>
     * </property>
     * <!-- server's weights -->
     * <property name="weights">
     * <list>
     * <value>${memcached.weights2}</value>
     * <!-- <value>${memcached.weights3}</value> -->
     * </list>
     * </property>
     * <property name="connectionPoolSize" value="${memcached.poolSize}"></property>
     * <property name="failureMode" value="${memcached.failureMode}"></property>
     * <property name="commandFactory">
     * <bean class="net.rubyeye.xmemcached.command.TextCommandFactory"></bean>
     * </property>
     * <property name="sessionLocator">
     * <bean class="net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator"></bean>
     * </property>
     * <property name="transcoder">
     * <bean class="net.rubyeye.xmemcached.transcoders.SerializingTranscoder"/>
     * </property>
     * <!-- ByteBuffer allocator -->
     * <property name="bufferAllocator">
     * <bean class="net.rubyeye.xmemcached.buffer.SimpleBufferAllocator"></bean>
     * </property>
     * <property name="opTimeout" value="10000"/>
     * </bean>
     *
     * @return
     */

    @Lazy
    @Bean(destroyMethod = "shutdown")
    public CacheClient memcachedClient() {
        XMemcachedClientFactoryBean xMemcachedClientFactoryBean = new XMemcachedClientFactoryBean();
        String[] server = StringUtil.split(servers, ",");
        int[] weights = new int[server.length];
        for (int i = 0; i < server.length; i++) {
            weights[i] = i + 1;
        }
        /**
         * 使用的是空格分隔
         */
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddressMap(StringUtil.join(server, " ")), weights);
        builder.setConnectionPoolSize(poolSize);
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        builder.setFailureMode(true);
        net.rubyeye.xmemcached.MemcachedClient memcachedClient = null;
        try {
            memcachedClient = builder.build();
            log.info("初始化memcached成功，{}", servers);
        } catch (IOException e) {
            log.warn("初始化memcached失败, {}", servers);
        }
        XMemcachedClient xMemcachedClient = new XMemcachedClient();
        xMemcachedClient.setMemcachedClient(memcachedClient);
        xMemcachedClient.setExpiredTime(expiredTime);
        return xMemcachedClient;
    }


    @Conditional(SpringCacheManager.InitMemcachedManagerCondition.class)
    @Bean
    @Lazy
    public CacheManager cacheManager() {
        CacheClient memcachedClient = memcachedClient();
        boolean disableCache = false;
        Set<CacheImpl> caches = Sets.newConcurrentHashSet();
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_1_DAY.getCacheName(), CacheNameEnum.CACHE_1_DAY.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_4_HOUR.getCacheName(), CacheNameEnum.CACHE_4_HOUR.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_2_HOUR.getCacheName(), CacheNameEnum.CACHE_2_HOUR.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_1_HOUR.getCacheName(), CacheNameEnum.CACHE_1_HOUR.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_30_MINUTES.getCacheName(), CacheNameEnum.CACHE_30_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_10_MINUTES.getCacheName(), CacheNameEnum.CACHE_10_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_2_MINUTES.getCacheName(), CacheNameEnum.CACHE_2_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_1_MINUTES.getCacheName(), CacheNameEnum.CACHE_1_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(memcachedClient, CacheNameEnum.CACHE_30_SECONDS.getCacheName(), CacheNameEnum.CACHE_30_SECONDS.getExpiredTime(), disableCache));
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        log.info("初始化成功spring cache manager， 实现类是：{}", CacheImpl.class.getName());
        return cacheManager;

    }

}
