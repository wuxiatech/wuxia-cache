package cn.wuxia.common.cached.redis;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.spring.cache.CacheImpl;
import cn.wuxia.common.spring.enums.CacheNameEnum;
import cn.wuxia.common.util.ArrayUtil;
import cn.wuxia.common.util.PropertiesUtils;
import cn.wuxia.common.util.StringUtil;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Configuration
@EnableCaching
@Conditional(JRedisCacheManager.InitJRedisCondition.class)
@PropertySource(value = {"classpath:redis.properties", "classpath:properties/redis.properties"}, ignoreResourceNotFound = true)
@Slf4j
public class JRedisCacheManager {

    @Value("${redis.hosts:127.0.0.1:6379}")
    private String hosts;
    @Value("${redis.auth:}")
    private String password;
    @Value("${redis.dbIndex:0}")
    private int dbIndex;
    @Value("${redis.master:}")
    private String master;
    @Value("${redis.mode:'single'}")
    private String mode;


    @Lazy
    @Bean(destroyMethod = "shutdown")
    public CacheClient jredisClient() {
        String[] servers = StringUtil.split(hosts, ",");
        log.info("init redis mode:{}", mode);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        CacheClient cacheClient = null;
        switch (mode) {
            case "cluster":
                Set<HostAndPort> nodes = new HashSet<HostAndPort>();
                for (String server : servers) {
                    String host = StringUtil.substringBefore(server, ":");
                    String port = StringUtil.substringAfter(server, ":");
                    HostAndPort hostAndPort = new HostAndPort(host, Integer.parseInt(port));
                    nodes.add(hostAndPort);
                }
                JedisCluster jedisCluster;
                if (StringUtil.isNotBlank(password)) {
                    jedisCluster = new JedisCluster(nodes, 10000, 2000, 5, password, master, config);
                } else {
                    jedisCluster = new JedisCluster(nodes, 10000, config);
                }
                cacheClient = new RedisClusterCacheClient(jedisCluster);
                break;
            case "sentinel":
                break;
            default:
                String host;
                String port = null;
                if (ArrayUtil.isNotEmpty(servers)) {
                    host = StringUtil.substringBefore(servers[0], ":");
                    port = StringUtil.substringAfter(servers[0], ":");
                } else {
                    host = "127.0.0.1";
                }
                port = StringUtil.isBlank(port) ? "6379" : port;
                JedisPool jedisPool;
                if (StringUtil.isNotBlank(password)) {
                    jedisPool = new JedisPool(config, host, Integer.valueOf(port), 2000, password, dbIndex);
                } else {
                    jedisPool = new JedisPool(config, host, Integer.valueOf(port), dbIndex);
                }
                cacheClient = new RedisCacheClient(jedisPool);
                break;
        }
        log.info("实现类：{}", cacheClient.getClass());
        return cacheClient;
    }

    @Conditional(InitCacheManageCondition.class)
    @Bean
    @Lazy
    public CacheManager cacheManager() {
        CacheClient jredisClient = jredisClient();
        if (jredisClient == null) {
            throw new RuntimeException("无法获取CacheClient:jredisClient");
        }
        boolean disableCache = false;
        Set<CacheImpl> caches = Sets.newConcurrentHashSet();
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_1_DAY.getCacheName(), CacheNameEnum.CACHE_1_DAY.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_4_HOUR.getCacheName(), CacheNameEnum.CACHE_4_HOUR.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_2_HOUR.getCacheName(), CacheNameEnum.CACHE_2_HOUR.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_1_HOUR.getCacheName(), CacheNameEnum.CACHE_1_HOUR.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_30_MINUTES.getCacheName(), CacheNameEnum.CACHE_30_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_10_MINUTES.getCacheName(), CacheNameEnum.CACHE_10_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_2_MINUTES.getCacheName(), CacheNameEnum.CACHE_2_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_1_MINUTES.getCacheName(), CacheNameEnum.CACHE_1_MINUTES.getExpiredTime(), disableCache));
        caches.add(new CacheImpl(jredisClient, CacheNameEnum.CACHE_30_SECONDS.getCacheName(), CacheNameEnum.CACHE_30_SECONDS.getExpiredTime(), disableCache));
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        log.info("初始化成功spring cache manager， 实现类是：{}", CacheImpl.class.getName());
        return cacheManager;

    }

    public static class InitCacheManageCondition implements Condition {


        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
                Properties properties = PropertiesUtils.loadProperties("classpath:properties/application.properties", "classpath:application.properties");
                if (properties.isEmpty()) {
                    return false;
                } else if (StringUtil.equalsIgnoreCase("jredis", properties.getProperty("spring.cache"))) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static class InitJRedisCondition implements Condition {

        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
                Properties properties = PropertiesUtils.loadProperties("classpath:properties/redis.properties", "classpath:redis.properties");
                if (properties.isEmpty()) {
                    return false;
                } else if (StringUtil.equalsIgnoreCase(properties.getProperty("cache.enable"), "true")) {
                    return true;
                } else {
                    return false;
                }
//                Class clazz = ClassLoaderUtil.loadClass("");
            } catch (Exception e) {
                return false;
            }
        }
    }

}
