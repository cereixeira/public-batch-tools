package com.cereixeira.batch.ingestion.config;

import com.cereixeira.job.entry.s3.config.S3EntryJobConfig;
import com.cereixeira.job.executeblock.s3.config.S3ExecutionBlockJobConfig;
import com.cereixeira.job.report.config.ReportConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration

@PropertySource(value = "classpath:spring_batch-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/spring_batch-application.properties", ignoreResourceNotFound = true)

@PropertySource(value = "classpath:batch_ingestion-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/batch_ingestion-application.properties", ignoreResourceNotFound = true)

@Import({S3ExecutionBlockJobConfig.class, S3EntryJobConfig.class, ReportConfig.class})

@ComponentScan(basePackages = "com.cereixeira.batch.ingestion")
public class S3IngestionConfig {

}
