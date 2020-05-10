package cn.wuxia.common.cached.redis;

import cn.wuxia.common.lock.RedissonDistributedLock;
import cn.wuxia.common.spring.enums.CacheNameEnum;
import cn.wuxia.common.util.PropertiesUtils;
import cn.wuxia.common.util.StringUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;
import org.redisson.spring.cache.CacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.Properties;

@Configuration
@EnableCaching
//@EnableRedissonHttpSession
@Conditional(RedissonSpringCacheManager.InitRedissonCondition.class)
@PropertySource(value = {"classpath:redis.properties", "classpath:properties/redis.properties"}, ignoreResourceNotFound = true)
@Slf4j
public class RedissonSpringCacheManager {

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
    public RedissonClient redissonClient() {

        Config config = new Config();
        switch (mode) {
            case "cluster":
                initClusterRedisson(config.useClusterServers());
                log.info("initClusterRedisson");
                break;
            case "sentinel":
                initSentinelRedisson(config.useSentinelServers());
                log.info("initSentinelRedisson");
                break;
            default:
                initSingleRedisson(config.useSingleServer());
                log.info("initSingleRedisson");
                break;
        }
        //得到redisson对象
        RedissonClient redisson = null;
        try {
            redisson = Redisson.create(config);
            log.info("初始化Redisson成功， {}", hosts);
        } catch (Exception e) {
            log.warn("初始化Redisson出错，{}, {}", hosts, e.getMessage());
        }
        //可通过打印redisson.getConfig().toJSON().toString()来检测是否配置成功
        return redisson;
    }

    public void initSingleRedisson(SingleServerConfig singleServerConfig) {
        //实例化redisson
        singleServerConfig.setAddress("redis://" + hosts)
                .setDatabase(dbIndex)
                .setConnectTimeout(100)
                .setTimeout(200)
                .setConnectionPoolSize(8)
                .setConnectionMinimumIdleSize(5)
                .setTcpNoDelay(true)
                .setPingConnectionInterval(30000)
                .setKeepAlive(true)
                .setRetryInterval(50);
        if (StringUtil.isNotBlank(password)) {
            singleServerConfig.setPassword(password);
        }
    }

    public void initClusterRedisson(ClusterServersConfig clusterServersConfig) {
        String[] nodes = hosts.split(",");
        //redisson版本是3.5，集群的ip前面要加上“redis://”，不然会报错，3.2版本可不加
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = "redis://" + nodes[i];
        }
        clusterServersConfig
                .setScanInterval(2000)
                .addNodeAddress(nodes)
                .setConnectTimeout(100)
                .setTimeout(200)
                .setTcpNoDelay(true)
                .setPingConnectionInterval(30000)
                .setKeepAlive(true)
                .setRetryInterval(50);
        if (StringUtil.isNotBlank(password)) {
            clusterServersConfig.setPassword(password);
        }
    }


    public void initSentinelRedisson(SentinelServersConfig sentinelServersConfig) {
        String[] nodes = hosts.split(",");
        //redisson版本是3.5，集群的ip前面要加上“redis://”，不然会报错，3.2版本可不加
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = "redis://" + nodes[i];
        }
        RedissonClient redisson = null;
        Config config = new Config();
        sentinelServersConfig
                .setScanInterval(2000)
                .setMasterName(master)
                .addSentinelAddress(nodes)
                .setReadMode(ReadMode.SLAVE)
                .setPassword(password)
                .setConnectTimeout(100)
                .setTimeout(200)
                .setTcpNoDelay(true)
                .setPingConnectionInterval(30000)
                .setKeepAlive(true)
                .setRetryInterval(50);
        if (StringUtil.isNotBlank(password)) {
            sentinelServersConfig.setPassword(password);
        }
    }


    /**
     * 装配locker类，并将实例注入到RedissLockUtil中
     *
     * @return
     */
    @Bean
    public RedissonDistributedLock distributedLocker() {
        RedissonDistributedLock locker = new RedissonDistributedLock(redissonClient());
        return locker;
    }

    public static class InitRedissonCondition implements Condition {

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

    @Conditional(InitCacheManageCondition.class)
    @Bean
    @Lazy
    public CacheManager cacheManager() {

        RedissonClient redissonClient = redissonClient();
        if (redissonClient == null) {
            throw new RuntimeException("无法获取CacheClient:redissonClient");
        }
        Map<String, CacheConfig> config = Maps.newHashMap();
        config.put(CacheNameEnum.CACHE_1_DAY.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_1_DAY.getTtl(), 12 * 60 * 60 * 1000));
        config.put(CacheNameEnum.CACHE_4_HOUR.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_4_HOUR.getTtl(), 2 * 60 * 60 * 1000));
        config.put(CacheNameEnum.CACHE_2_HOUR.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_2_HOUR.getTtl(), 60 * 60 * 1000));
        config.put(CacheNameEnum.CACHE_1_HOUR.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_1_HOUR.getTtl(), 30 * 60 * 1000));
        config.put(CacheNameEnum.CACHE_30_MINUTES.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_30_MINUTES.getTtl(), 15 * 60 * 1000));
        config.put(CacheNameEnum.CACHE_10_MINUTES.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_10_MINUTES.getTtl(), 5 * 60 * 1000));
        config.put(CacheNameEnum.CACHE_2_MINUTES.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_2_MINUTES.getTtl(), 1 * 60 * 1000));
        config.put(CacheNameEnum.CACHE_1_MINUTES.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_1_MINUTES.getTtl(), 30 * 1000));
        config.put(CacheNameEnum.CACHE_30_SECONDS.getCacheName(), new CacheConfig(CacheNameEnum.CACHE_30_SECONDS.getTtl(), 15 * 1000));
        org.redisson.spring.cache.RedissonSpringCacheManager redissonSpringCacheManager = new org.redisson.spring.cache.RedissonSpringCacheManager(redissonClient, config);
        log.info("初始化成功spring cache manager， 实现类是：{}", RedissonClient.class.getName());
        return redissonSpringCacheManager;

    }

    public static class InitCacheManageCondition implements Condition {


        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
                Properties properties = PropertiesUtils.loadProperties("classpath:properties/application.properties", "classpath:application.properties");
                if (properties.isEmpty()) {
                    return false;
                } else if (StringUtil.equalsIgnoreCase("redisson", properties.getProperty("spring.cache"))) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }
}
