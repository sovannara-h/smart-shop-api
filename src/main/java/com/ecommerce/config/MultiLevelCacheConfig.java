package com.ecommerce.config;

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

import com.github.benmanes.caffeine.cache.Caffeine;

// Restaurant avec différents endroits de stockage
@Configuration
@EnableCaching
public class MultiLevelCacheConfig {

    // Configuration du "petit plateau de travail" (Caffeine)
    // C'est comme le plan de travail du chef, tout près de lui
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)    // Les ingrédients restent frais 5 minutes
            .initialCapacity(50)                      // On prépare de la place pour 50 ingrédients
            .maximumSize(100)                         // On peut garder maximum 100 ingrédients
            .recordStats();                           // On note combien on utilise chaque ingrédient

        cacheManager.setCaffeine(caffeine);
        cacheManager.setAllowNullValues(false); // On n'accepte pas les plats vides

        return cacheManager;
    }

    // Configuration des "frigos connectés" (Redis Cluster)
    @Bean
    public RedisClusterConfiguration redisClusterConfiguration() {
        RedisClusterConfiguration config = new RedisClusterConfiguration();

        // On installe 3 frigos dans différents endroits de la cuisine
        config.addClusterNode(new RedisNode("localhost", 6379));  // Frigo 1
        config.addClusterNode(new RedisNode("localhost", 6380));  // Frigo 2
        config.addClusterNode(new RedisNode("localhost", 6381));  // Frigo 3

        config.setMaxRedirects(3);  // Si un frigo est plein, on essaie 3 fois d'en trouver un autre

        return config;
    }

    // On branche les frigos au système électrique
    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisClusterConfiguration clusterConfiguration) {
        return new LettuceConnectionFactory(clusterConfiguration);
    }

    // On organise comment ranger les choses dans les frigos
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))        // Par défaut, les ingrédients sont bons 30 minutes
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
            )
            .disableCachingNullValues();             // On ne garde pas les plats vides

        // Règles spéciales pour différents types d'ingrédients
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofHours(24)));     // Les produits sont bons 24h
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(12)));   // Les catégories 12h
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));      // Les utilisateurs 30min

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .enableStatistics()                       // On compte combien on utilise chaque frigo
            .build();
    }

    // On organise tout le système de stockage (plateau + frigos)
    @Bean
    @Primary
    public CacheManager compositeCacheManager(
            CaffeineCacheManager caffeineCacheManager,
            RedisCacheManager redisCacheManager) {
            
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager(
            caffeineCacheManager,    // D'abord on regarde sur le plateau
            redisCacheManager       // Ensuite dans les frigos
        );
        
        compositeCacheManager.setFallbackToNoOpCache(false);  // Si on ne trouve pas, on va à l'entrepôt
        
        return compositeCacheManager;
    }
}
