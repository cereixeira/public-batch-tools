package com.cereixeira.databases.config;

import com.cereixeira.databases.cache.DatabaseCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;

@Configuration
@Import({CustomDsConfig.class,CustomJpaConfig.class,SpringBatchDsConfig.class})
@PropertySource(value = "classpath:databases-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/databases-application.properties", ignoreResourceNotFound = true)
@ComponentScan(basePackages = {
        "com.cereixeira.databases"
})
@EnableCaching
public class DatabasesConfig {
    @Bean
    public CacheManager cacheManager() {

        return new ConcurrentMapCacheManager(DatabaseCache.GET_BLOCK_ENTITY,
                DatabaseCache.GET_EXECUTION_ENTITY);
    }
}
