package com.cereixeira.connectors.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration

@PropertySource(value = "classpath:s3-connector-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/s3-connector-application.properties", ignoreResourceNotFound = true)

@ComponentScan(basePackages = {
        "com.cereixeira.connectors.s3"
})
public class S3ConnectorConfig {
}
