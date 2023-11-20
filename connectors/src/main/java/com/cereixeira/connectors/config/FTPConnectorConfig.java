package com.cereixeira.connectors.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration

@PropertySource(value = "classpath:ftp-connector-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/ftp-connector--application.properties", ignoreResourceNotFound = true)

@ComponentScan(basePackages = {
        "com.cereixeira.connectors.ftp"
})
public class FTPConnectorConfig {
}
