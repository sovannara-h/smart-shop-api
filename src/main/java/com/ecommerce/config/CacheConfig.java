package com.ecommerce.config;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configuration du cache Redis multi-niveaux.
     * Optimisé pour performance et scalabilité.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuration TTL par défaut
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Configuration par cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        
        // Cache recommandations: 1 heure
        cacheConfigs.put("recommendations", config);
        
        // Cache produits: 24 heures
        cacheConfigs.put("products", config.entryTtl(Duration.ofHours(24)));
        
        // Cache utilisateurs: 12 heures
        cacheConfigs.put("users", config.entryTtl(Duration.ofHours(12)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }

    /**
     * Pool de connexions Redis optimisé
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(
        @Value("${spring.redis.host}") String host,
        @Value("${spring.redis.port}") int port
    ) {
        LettucePoolingClientConfiguration poolConfig = LettucePoolingClientConfiguration.builder()
            .poolConfig(new GenericObjectPoolConfig<Object>())
            .commandTimeout(Duration.ofSeconds(1))
            .shutdownTimeout(Duration.ofSeconds(2))
            .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(
            new RedisStandaloneConfiguration(host, port),
            poolConfig
        );
        return factory;
    }
}

















// package fr.epitech.ecommerce.config;

// import java.io.IOException;
// import java.util.concurrent.Executor;
// import java.util.concurrent.ThreadPoolExecutor;
// import java.util.concurrent.TimeUnit;

// import javax.servlet.Filter;
// import javax.servlet.FilterChain;
// import javax.servlet.ServletException;
// import javax.servlet.ServletRequest;
// import javax.servlet.ServletResponse;
// import javax.servlet.http.HttpServletResponse;

// import org.springframework.cache.CacheManager;
// import org.springframework.cache.caffeine.CaffeineCacheManager;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.scheduling.annotation.EnableAsync;
// import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
// import org.springframework.web.filter.CharacterEncodingFilter;
// import org.springframework.web.filter.CommonsRequestLoggingFilter;

// import com.github.benmanes.caffeine.cache.Caffeine;

// import io.micrometer.core.instrument.MeterRegistry;
// import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
// import lombok.extern.slf4j.Slf4j;

// /**
//  * Configuration des optimisations de performance.
//  * Cette classe gère :
//  * - Le cache local (Caffeine)
//  * - Les pools de threads
//  * - L'encodage des caractères
//  * - Le logging des requêtes
//  * - Les métriques de performance
//  */
// @Configuration
// @EnableAsync
// @Slf4j
// public class CacheConfig {

//     /**
//      * Configure le cache local Caffeine pour les données fréquemment accédées.
//      * Plus rapide que Redis pour les petits volumes de données.
//      */
//     @Bean
//     public CacheManager localCacheManager() {
//         CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
//         cacheManager.setCaffeine(Caffeine.newBuilder()
//             .maximumSize(10_000)                    // Limite de taille en mémoire
//             .expireAfterWrite(30, TimeUnit.MINUTES)    // Expiration après écriture
//             .expireAfterAccess(1, TimeUnit.DAYS)    // Expire si pas accéder depuis
//             .softValues()                           // Garbage collector de libère la mémoire si besoin
//             .recordStats()                          // Active les statistiques
//             .removalListener((key, value, cause) -> {
//                 log.debug("Cache eviction: key={}, cause={}", key, cause);
//             }));
            
//         return cacheManager;
//     }

//     /**
//      * Configure le pool de threads pour les tâches asynchrones.
//      * Optimisé pour les opérations I/O intensives.
//      */
//     @Bean
//     public Executor asyncExecutor() {
//         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
//         executor.setCorePoolSize(4);                // Threads minimum
//         executor.setMaxPoolSize(10);                // Threads maximum
//         executor.setQueueCapacity(500);             // Taille de la file d'attente
//         executor.setThreadNamePrefix("Async-");     // Préfixe pour identification
//         executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
//         // Configuration des timeouts
//         executor.setKeepAliveSeconds(60);           // Durée de vie des threads excédentaires
//         executor.setAwaitTerminationSeconds(60);    // Timeout d'arrêt
        
//         return executor;
//     }

//     /**
//      * Configure le filtre d'encodage pour gérer correctement les caractères UTF-8.
//      * Important pour les données internationales.
//      */
//     @Bean
//     public Filter characterEncodingFilter() {
//         CharacterEncodingFilter filter = new CharacterEncodingFilter();
//         filter.setEncoding("UTF-8");
//         filter.setForceEncoding(true);  // Force l'encodage même si déjà défini
//         return filter;
//     }

//     /**
//      * Configure le logging des requêtes HTTP pour le debugging.
//      * Attention à la performance en production.
//      */
//     @Bean
//     public CommonsRequestLoggingFilter requestLoggingFilterCache() {
//         CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
//         filter.setIncludeQueryString(true);     // Log les paramètres
//         filter.setIncludePayload(true);         // Log le corps des requêtes
//         filter.setMaxPayloadLength(10000);      // Limite la taille des logs
//         filter.setIncludeHeaders(false);        // N'inclut pas les headers sensibles
//         filter.setBeforeMessagePrefix("Request [");
//         filter.setAfterMessagePrefix("Response [");
//         return filter;
//     }

//     /**
//      * Configure le registry pour les métriques de performance.
//      * Utilisé par Prometheus/Grafana pour le monitoring.
//      */
//     @Bean
//     public MeterRegistry meterRegistryCache() {
//         return new SimpleMeterRegistry();
//     }

//     /**
//      * Configure la compression des réponses HTTP.
//      * Réduit la bande passante utilisée.
//      */
//     @Bean
//     public Filter compressionFilter() {
//         return new Filter() {
//             @Override
//             public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//                     throws IOException, ServletException {
//                 HttpServletResponse httpResponse = (HttpServletResponse) response;
//                 httpResponse.addHeader("Content-Encoding", "gzip");
//                 chain.doFilter(request, response);
//             }
//         };
//     }
// }