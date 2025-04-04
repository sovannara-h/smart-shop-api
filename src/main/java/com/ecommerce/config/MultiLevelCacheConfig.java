package com.ecommerce.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class MultiLevelCacheConfig {

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
  public RedisClusterConfiguration redisClusterConfiguration() {
    RedisClusterConfiguration config = new RedisClusterConfiguration();

    config.addClusterNode(new RedisNode("localhost", 6379)); // Frigo 1
    config.addClusterNode(new RedisNode("localhost", 6380)); // Frigo 2
    config.addClusterNode(new RedisNode("localhost", 6381)); // Frigo 3

    config.setMaxRedirects(3);

    return config;
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory(
      RedisClusterConfiguration clusterConfiguration) {
    return new LettuceConnectionFactory(clusterConfiguration);
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
