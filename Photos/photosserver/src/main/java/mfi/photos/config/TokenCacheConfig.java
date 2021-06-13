package mfi.photos.config;

import com.google.common.cache.CacheBuilder;
import mfi.photos.util.CacheKeyGenerator;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
class TokenCacheConfig extends CachingConfigurerSupport {

    @Bean
    @Override
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {

            @Override
            protected Cache createConcurrentMapCache(final String name) {
                return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(1500, TimeUnit.MILLISECONDS)
                        .maximumSize(10).build().asMap(), false);
            }
        };

        cacheManager.setCacheNames(Arrays.asList("tokenCache"));
        return cacheManager;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new CacheKeyGenerator();
    }
}