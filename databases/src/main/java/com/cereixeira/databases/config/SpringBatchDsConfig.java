package com.cereixeira.databases.config;

import com.cereixeira.databases.constants.DatabasesConstants;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
@Configuration
public class SpringBatchDsConfig {

    @Bean
    @ConfigurationProperties("springbatch.datasource")
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = DatabasesConstants.BEAN_SPRING_BATCH_DS)
    @ConfigurationProperties("springbatch.datasource")
    public HikariDataSource inputDataSource() {
        return  batchDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

}
