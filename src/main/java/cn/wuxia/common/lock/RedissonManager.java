package cn.wuxia.common.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedissonManager {
    @Value("${redis.hosts:127.0.0.1:6379}")
    private String hosts;
    @Value("${redis.auth:}")
    private String password;


    @Bean
    public Redisson getSingleRedisson() {
        Config config = new Config();
        //实例化redisson
        config.useSingleServer().setAddress("redis://" + hosts).setPassword(password);
        //得到redisson对象
        return (Redisson) Redisson.create(config);
    }

    @Bean
    public RedissonClient getClusterRedisson() {
        String[] nodes = hosts.split(",");
        //redisson版本是3.5，集群的ip前面要加上“redis://”，不然会报错，3.2版本可不加
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = "redis://" + nodes[i];
        }
        RedissonClient redisson = null;
        Config config = new Config();
        config.useClusterServers() //这是用的集群server
                .setScanInterval(2000) //设置集群状态扫描时间
                .addNodeAddress(nodes)
                .setPassword(password);
        try {
            redisson = Redisson.create(config);
        } catch (Exception e) {
            log.warn("初始化Redisson出错， {}", e.getMessage());
        }
        //可通过打印redisson.getConfig().toJSON().toString()来检测是否配置成功
        return redisson;
    }

}
