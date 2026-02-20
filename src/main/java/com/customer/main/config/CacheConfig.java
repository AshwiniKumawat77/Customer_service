package com.customer.main.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Cache configuration for Customer Service (real-time home loan).
 * Uses Caffeine in-memory cache; can switch to Redis for distributed cache.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_CUSTOMERS = "customers";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_CUSTOMERS);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
