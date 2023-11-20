package com.cereixeira.batch.ingestion.config;

import com.cereixeira.job.entry.config.EntryJobConfig;
import com.cereixeira.job.executeblock.config.ExecutionBlockJobConfig;
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

@Import({ExecutionBlockJobConfig.class, EntryJobConfig.class, ReportConfig.class})

//@Import({S3ExecutionBlockJobConfig.class, S3EntryJobConfig.class, ReportConfig.class})

@ComponentScan(basePackages = "com.cereixeira.batch.ingestion")
public class FileIngestionConfig {



}
