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

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class AuthCacheConfig extends CachingConfigurerSupport {

    public static final String CACHE_NAME = "authCache";

    @Bean
    @Override
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {

            @Override
            @Nonnull
            protected Cache createConcurrentMapCache(@Nonnull final String name) {
                return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(2500, TimeUnit.MILLISECONDS)
                        .maximumSize(10).build().asMap(), false);
            }
        };

        cacheManager.setCacheNames(Collections.singletonList(CACHE_NAME));
        return cacheManager;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new CacheKeyGenerator();
    }
}