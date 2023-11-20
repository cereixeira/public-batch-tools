package com.cereixeira.job.entry.s3.config;

import com.cereixeira.api.alfresco.config.AlfrescoApiConfig;
import com.cereixeira.batch.utils.config.BatchUtilsConfig;
import com.cereixeira.batch.utils.exception.StepSkipRetryException;
import com.cereixeira.connectors.config.S3ConnectorConfig;
import com.cereixeira.connectors.s3.S3FileReaderDTO;
import com.cereixeira.databases.config.DatabasesConfig;
import com.cereixeira.job.entry.s3.constants.JobParam;
import com.cereixeira.job.entry.s3.constants.S3EntryJobBeanName;
import com.cereixeira.job.entry.s3.dto.InputEntryDTO;
import com.cereixeira.job.entry.s3.dto.OutputEntryDTO;
import com.cereixeira.job.entry.s3.listener.EntryJobListener;
import com.cereixeira.job.entry.s3.listener.EntryStepListener;
import com.cereixeira.job.entry.s3.listener.SkipListener;
import com.cereixeira.job.entry.s3.positions.InputEntryPosition;
import com.cereixeira.job.entry.s3.processor.EntryItemProcessor;
import com.cereixeira.job.entry.s3.writer.EntryWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableBatchProcessing

@PropertySource(value = "classpath:spring_batch-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/spring_batch-application.properties", ignoreResourceNotFound = true)

@PropertySource(value = "classpath:entry-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/entry-application.properties", ignoreResourceNotFound = true)

@Import({ DatabasesConfig.class, BatchUtilsConfig.class, S3ConnectorConfig.class, AlfrescoApiConfig.class
})
@ComponentScan(basePackages = "com.cereixeira.job.entry.s3")
public class S3EntryJobConfig {
    Logger logger = LogManager.getLogger(S3EntryJobConfig.class);
    @Value("${job.entry.chunk}")
    private int chunk;
    @Value("${job.entry.task.corepoolsize}")
    private int taskCorePoolSize;
    @Value("${job.entry.task.maxpoolsize}")
    private int taskMaxPoolSize;
    @Value("${job.entry.task.queuesize}")
    private int queueSize;

    @Value("${job.entry.step.retrytime}")
    private int retryTime;

    @Autowired
    private JobRepository jobRepository;

    @Bean(name = S3EntryJobBeanName.ENTRY_SIMPLE_JOB_LAUNCHER)
    public SimpleJobLauncher jobLauncher() {
        final SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Autowired
    private S3FileReaderDTO s3FileReaderDTO;

    @Autowired
    private EntryJobListener entryJobListener;

    @Autowired
    private EntryStepListener entryStepListener;

    @Autowired
    private SkipListener skipListener;

    //--------------------------------------------------------------------------------------

    @Bean(name = S3EntryJobBeanName.ENTRY_JOB)
    public Job ingestionEntryJob() throws Exception {
        return jobBuilderFactory.get(S3EntryJobBeanName.ENTRY_JOB)
                .incrementer(new RunIdIncrementer())
                .listener(entryJobListener)
                .flow(step1GenerateEntry())
                .end()
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor generateEntryThreadPoolTaskExecutor() {
        logger.info("#generateEntryThreadPoolTaskExecutor - stepEntryTask-, taskCorePoolSize:{}, taskMaxPoolSize:{}, queueSize:{}",
                taskCorePoolSize,taskMaxPoolSize,queueSize);

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("stepEntryTask-");
        taskExecutor.setCorePoolSize( taskCorePoolSize );
        taskExecutor.setMaxPoolSize( taskMaxPoolSize );
        taskExecutor.setQueueCapacity( queueSize );
        return taskExecutor;
    }
    @Bean(name = S3EntryJobBeanName.STEP1_GENERATE_ENTRY)
    public Step step1GenerateEntry() {
        return stepBuilderFactory.get(S3EntryJobBeanName.STEP1_GENERATE_ENTRY)
                .<InputEntryDTO, OutputEntryDTO> chunk(chunk)
                .reader(entryReader(null))
                .processor(entryProcessor())
                .writer(entryWriter())
                .faultTolerant()
                    .skip(Throwable.class)
                    .skipLimit(Integer.MAX_VALUE)
                    .retryLimit(retryTime)
                    .retry(StepSkipRetryException.class)
                .listener(entryStepListener)
                .listener(skipListener)
                .taskExecutor(generateEntryThreadPoolTaskExecutor())
                .build();
    }
    @Bean
    @StepScope
    public FlatFileItemReader<InputEntryDTO> entryReader(@Value("#{jobParameters["+ JobParam.REF_PATH +"]}") String filePath){
        FlatFileItemReader<InputEntryDTO> fileItemReader = s3FileReaderDTO.getFileItemReader(filePath, InputEntryDTO.class, InputEntryPosition.getNameArray(), InputEntryPosition.getIndexArray());
        return fileItemReader;
    }
    @Bean
    public EntryItemProcessor entryProcessor(){
        return new EntryItemProcessor();
    }

    @Bean
    public ItemWriter<OutputEntryDTO> entryWriter() {
        return new EntryWriter();
    }

}
