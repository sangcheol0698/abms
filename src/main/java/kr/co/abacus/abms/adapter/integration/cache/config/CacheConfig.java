package kr.co.abacus.abms.adapter.integration.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.MINUTES;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache("commonCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, MINUTES)
                        .maximumSize(1000)
                        .build());

        return cacheManager;
    }

}
