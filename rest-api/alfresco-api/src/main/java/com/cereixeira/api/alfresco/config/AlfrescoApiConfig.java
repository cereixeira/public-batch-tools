package com.cereixeira.api.alfresco.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:alfresco-api-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/alfresco-api-application.properties", ignoreResourceNotFound = true)

@ComponentScan(basePackages = {
        "com.cereixeira.api.alfresco"
})
public class AlfrescoApiConfig {
}
