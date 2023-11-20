package com.cereixeira.connectors.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration

@PropertySource(value = "classpath:sftp-connector-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/sftp-connector--application.properties", ignoreResourceNotFound = true)

@ComponentScan(basePackages = {
        "com.cereixeira.connectors.sftp"
})
public class SFTPConnectorConfig {
}
