package config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redisson:
 * # 单节点配置
 * singleServerConfig:
 * # 连接空闲超时，单位：毫秒
 * idleConnectionTimeout: 10000
 * # 连接超时，单位：毫秒
 * connectTimeout: 10000
 * # 命令等待超时，单位：毫秒
 * timeout: 3000
 * # 命令失败重试次数,如果尝试达到 retryAttempts（命令失败重试次数） 仍然不能将命令发送至某个指定的节点时，将抛出错误。
 * # 如果尝试在此限制之内发送成功，则开始启用 timeout（命令等待超时） 计时。
 * retryAttempts: 3
 * # 命令重试发送时间间隔，单位：毫秒
 * retryInterval: 1500
 * # 密码
 * #password: redis.shbeta
 * # 单个连接最大订阅数量
 * subscriptionsPerConnection: 5
 * # 客户端名称
 * #clientName: axin
 * #  # 节点地址
 * address: redis://${spring.redis.host}:${spring.redis.port}
 * # 发布和订阅连接的最小空闲连接数
 * subscriptionConnectionMinimumIdleSize: 1
 * # 发布和订阅连接池大小
 * subscriptionConnectionPoolSize: 32
 * # 最小空闲连接数
 * connectionMinimumIdleSize: 8
 * # 连接池大小
 * connectionPoolSize: 16
 * # 数据库编号
 * database: 0
 * # DNS监测时间间隔，单位：毫秒
 * dnsMonitoringInterval: 5000
 * # 线程池数量,默认值: 当前处理核数量 * 2
 * #threads: 0
 * # Netty线程池数量,默认值: 当前处理核数量 * 2
 * #nettyThreads: 0
 */
@Configuration
public class RedisConfig {
    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redissonClient")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setNettyThreads(8).useSingleServer()
                .setSubscriptionConnectionMinimumIdleSize(1)
                .setSubscriptionConnectionPoolSize(32)
                .setConnectionMinimumIdleSize(8)
                .setConnectionPoolSize(16)
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();

        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        template.setKeySerializer(redisSerializer);
        template.setHashKeySerializer(redisSerializer);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
