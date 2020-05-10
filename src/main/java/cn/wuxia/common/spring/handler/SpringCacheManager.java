package cn.wuxia.common.spring.handler;

import cn.wuxia.common.cached.memcached.MemcachedCacheManager;
import cn.wuxia.common.cached.redis.JRedisCacheManager;
import cn.wuxia.common.cached.redis.RedissonSpringCacheManager;
import cn.wuxia.common.util.PropertiesUtils;
import cn.wuxia.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import java.util.Properties;

/**
 * @author songlin
 */
@Configuration
//@EnableRedissonHttpSession
@Conditional(SpringCacheManager.InitCacheManageCondition.class)
@Import({MemcachedCacheManager.class, JRedisCacheManager.class, RedissonSpringCacheManager.class})
@Slf4j
@EnableCaching
public class SpringCacheManager {


    @Value("${spring.cache}")
    private String cache;

    private MemcachedCacheManager memcachedCacheManager;
    private JRedisCacheManager jRedisCacheManager;
    private RedissonSpringCacheManager redissonSpringCacheManager;
    private static final boolean redissonPresent = ClassUtils.isPresent("org.redisson.api.RedissonClient", SpringCacheManager.class.getClassLoader());

//    public SpringCacheManager(MemcachedCacheManager memcachedCacheManager, JRedisCacheManager jRedisCacheManager, RedissonSpringCacheManager redissonSpringCacheManager) {
//        this.memcachedCacheManager = memcachedCacheManager;
//        this.jRedisCacheManager = jRedisCacheManager;
//        this.redissonSpringCacheManager = redissonSpringCacheManager;
//    }

    /**
     * <bean class="cn.wuxia.common.spring.cache.CacheImpl">
     * <property name="cacheClient" ref="xMemcachedClient"/>
     * <property name="cacheName" value="2MinutesData"/>
     * <!-- 缓存定义有效期为2分钟 -->
     * <property name="expiredTime" value="120"/>
     * <property name="disableCache" value="${cache.disable:false}"/>
     * </bean>
     *
     * @return
     */
    @Bean
    @Lazy
    CacheManager cacheManager() {
        switch (cache) {
            case "memcache": {
                return memcachedCacheManager.cacheManager();
            }
            case "jredis": {
                return jRedisCacheManager.cacheManager();
            }
            case "redisson":
                return redissonSpringCacheManager.cacheManager();
            default:
                throw new RuntimeException("仅支持spring.cache=memcache,redisson,jredis");
        }
    }

    public static class InitCacheManageCondition implements Condition {
        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
                Properties properties = PropertiesUtils.loadProperties("classpath:properties/application.properties", "classpath:application.properties");
                if (properties.isEmpty()) {
                    return false;
                } else if (StringUtil.isNotBlank(properties.getProperty("spring.cache"))) {
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
