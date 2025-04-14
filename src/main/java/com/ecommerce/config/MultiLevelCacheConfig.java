package com.ecommerce.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class MultiLevelCacheConfig {

  @Value("${spring.redis.cluster.timeout:2000}")
  private long redisTimeout;

  @Value("${spring.redis.password:}")
  private String redisPassword;

  @Bean
  public CaffeineCacheManager caffeineCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    Caffeine<Object, Object> caffeine =
        Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .initialCapacity(50)
            .maximumSize(100)
            .recordStats();

    cacheManager.setCaffeine(caffeine);
    cacheManager.setAllowNullValues(false);

    return cacheManager;
  }

  @Bean
  @Profile("dev")
  public RedisConnectionFactory standaloneRedisConnectionFactory(
      @Value("${spring.redis.host:localhost}") String host,
      @Value("${spring.redis.port:6379}") int port,
      @Value("${spring.redis.password:}") String password) {

    LettuceClientConfiguration clientConfig =
        LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(redisTimeout))
            .shutdownTimeout(Duration.ofMillis(1000))
            .clientOptions(
                ClientOptions.builder()
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .socketOptions(
                        SocketOptions.builder().connectTimeout(Duration.ofMillis(1000)).build())
                    .build())
            .build();

    org.springframework.data.redis.connection.RedisStandaloneConfiguration config =
        new org.springframework.data.redis.connection.RedisStandaloneConfiguration(host, port);

    if (password != null && !password.isEmpty()) {
      config.setPassword(RedisPassword.of(password));
    }

    return new LettuceConnectionFactory(config, clientConfig);
  }

  @Bean
  @Profile("prod")
  public RedisClusterConfiguration redisClusterConfiguration(
      @Value("${spring.redis.cluster.nodes}") String clusterNodes) {
    RedisClusterConfiguration config = new RedisClusterConfiguration();

    for (String node : clusterNodes.split(",")) {
      String[] parts = node.split(":");
      config.addClusterNode(new RedisNode(parts[0], Integer.parseInt(parts[1])));
    }

    config.setMaxRedirects(3);

    if (redisPassword != null && !redisPassword.isEmpty()) {
      config.setPassword(RedisPassword.of(redisPassword));
    }

    return config;
  }

  @Bean
  @Profile("prod")
  public RedisConnectionFactory clusterRedisConnectionFactory(
      RedisClusterConfiguration clusterConfiguration) {

    LettuceClientConfiguration clientConfig =
        LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(redisTimeout))
            .shutdownTimeout(Duration.ofMillis(1000))
            .clientOptions(
                ClientOptions.builder()
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .socketOptions(
                        SocketOptions.builder().connectTimeout(Duration.ofMillis(1000)).build())
                    .build())
            .build();

    return new LettuceConnectionFactory(clusterConfiguration, clientConfig);
  }

  @Bean
  public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration defaultConfig =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofHours(24)));
    cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(12)));
    cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigurations)
        .enableStatistics()
        .build();
  }

  @Bean
  @Primary
  public CacheManager compositeCacheManager(
      CaffeineCacheManager caffeineCacheManager, RedisCacheManager redisCacheManager) {

    CompositeCacheManager compositeCacheManager =
        new CompositeCacheManager(caffeineCacheManager, redisCacheManager);

    compositeCacheManager.setFallbackToNoOpCache(false);

    return compositeCacheManager;
  }
}
