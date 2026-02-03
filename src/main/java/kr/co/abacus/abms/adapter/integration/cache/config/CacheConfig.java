package kr.co.abacus.abms.adapter.integration.cache.config;

import static java.util.concurrent.TimeUnit.*;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache("commonCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, HOURS)
                        .maximumSize(1000)
                        .build());

        return cacheManager;
    }

}
