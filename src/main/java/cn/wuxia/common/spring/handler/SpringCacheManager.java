package cn.wuxia.common.spring.handler;

import cn.wuxia.common.cached.memcached.MemcachedCacheManager;
import cn.wuxia.common.cached.redis.JRedisCacheManager;
import cn.wuxia.common.cached.redis.RedissonSpringCacheManager;
import cn.wuxia.common.util.PropertiesUtils;
import cn.wuxia.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    final static Properties properties = PropertiesUtils.loadProperties("classpath:properties/application.properties", "classpath:application.properties");

    public static class InitCacheManageCondition implements Condition {
        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
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


    public static class InitMemcachedManagerCondition implements Condition {


        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
                if (properties.isEmpty()) {
                    return false;
                } else if (StringUtil.equalsIgnoreCase("memcache", properties.getProperty("spring.cache"))) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static class InitMemcachedCondition implements Condition {
        private static final boolean memcachedPresent = ClassUtils.isPresent("net.rubyeye.xmemcached.MemcachedClient", SpringCacheManager.class.getClassLoader());

        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            return memcachedPresent;
        }
    }


    public static class InitJRedisCacheManagerCondition implements Condition {


        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {

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
        private static final boolean jedisPresent = ClassUtils.isPresent("redis.clients.jedis.Jedis", SpringCacheManager.class.getClassLoader());

        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            return jedisPresent;
        }
    }

    public static class InitRedissonCacheManagerCondition implements Condition {


        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
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

    public static class InitRedissonCondition implements Condition {
        private static final boolean redissonPresent = ClassUtils.isPresent("org.redisson.api.RedissonClient", SpringCacheManager.class.getClassLoader());

        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            return redissonPresent;
        }
    }


}
