package com.dockyard.springdockerapp.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * RedisConfig.java
 *
 * Configures how Spring Boot uses Redis as a cache.
 *
 * @Configuration tells Spring this class contains bean definitions
 * @EnableCaching activates Spring caching support across the app
 * Without @EnableCaching the @Cacheable annotations in ProductService
 * are completely ignored and no caching happens
 *
 * This uses RedisSerializer.json() which is the recommended
 * non-deprecated approach in Spring Data Redis 4.0
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configures the Redis Cache Manager.
     *
     * Decides:
     *   - How long cached data lives before expiring (TTL)
     *   - How data is serialised when stored in Redis
     *   - What happens when a cache key is not found
     *
     * @param connectionFactory Spring auto-creates this from application.yml
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()

                // Cache entries expire after 10 minutes
                // After expiry the next request goes to the database
                // and the fresh result is cached again with a new TTL
                .entryTtl(Duration.ofMinutes(10))

                // Use String serialiser for cache keys
                // Keys stored as readable strings like "products::1"
                // instead of binary data which is hard to debug in Redis CLI
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.string()))

                // Use JSON serialiser for cache values
                // RedisSerializer.json() is the recommended non-deprecated
                // way in Spring Data Redis 4.0
                // Values stored as JSON strings readable in Redis CLI
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.json()))

                // Do not cache null values
                // If database returns null we skip caching
                // Next request will go to the database again
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

}