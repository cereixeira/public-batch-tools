package com.cereixeira.databases.config;

import com.cereixeira.databases.constants.DatabasesConstants;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CustomDsConfig {

    @Bean
    @ConfigurationProperties("custom.datasource")
    public DataSourceProperties inputDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean(name = DatabasesConstants.BEAN_CUSTOM_DS)
    @ConfigurationProperties("custom.datasource")
    public HikariDataSource inputDataSource() {
        return  inputDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

}
