package com.cereixeira.job.entry.config;


import com.cereixeira.batch.utils.config.BatchUtilsConfig;
import com.cereixeira.batch.utils.exception.StepSkipRetryException;
import com.cereixeira.batch.utils.reader.FileReaderDTO;
import com.cereixeira.databases.config.DatabasesConfig;
import com.cereixeira.job.entry.constants.EntryJobBeanName;
import com.cereixeira.job.entry.constants.JobParam;
import com.cereixeira.job.entry.dto.InputEntryDTO;
import com.cereixeira.job.entry.dto.OutputEntryDTO;
import com.cereixeira.job.entry.listener.EntryJobListener;
import com.cereixeira.job.entry.listener.EntryStepListener;
import com.cereixeira.job.entry.listener.SkipListener;
import com.cereixeira.job.entry.positions.InputEntryPosition;
import com.cereixeira.job.entry.processor.EntryItemProcessor;
import com.cereixeira.job.entry.writer.EntryWriter;
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

@Import({ DatabasesConfig.class, BatchUtilsConfig.class
})
@ComponentScan(basePackages = "com.cereixeira.job.entry")
public class EntryJobConfig {

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

    @Bean(name = EntryJobBeanName.ENTRY_SIMPLE_JOB_LAUNCHER)
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
    private FileReaderDTO fileReaderDTO;

    @Autowired
    private EntryJobListener entryJobListener;

    @Autowired
    private EntryStepListener entryStepListener;
    @Autowired
    private SkipListener skipListener;

    //--------------------------------------------------------------------------------------

    @Bean(name = EntryJobBeanName.ENTRY_JOB)
    public Job ingestionEntryJob() throws Exception {
        return jobBuilderFactory.get(EntryJobBeanName.ENTRY_JOB)
                .incrementer(new RunIdIncrementer())
                .listener(entryJobListener)
                .flow(step1GenerateEntry())
                .end()
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor generateEntryThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("stepEntryTask-");
        taskExecutor.setCorePoolSize( taskCorePoolSize );
        taskExecutor.setMaxPoolSize( taskMaxPoolSize );
        taskExecutor.setQueueCapacity( queueSize );
        return taskExecutor;
    }
    @Bean(name = EntryJobBeanName.STEP1_GENERATE_ENTRY)
    public Step step1GenerateEntry() {
        return stepBuilderFactory.get(EntryJobBeanName.STEP1_GENERATE_ENTRY)
                .<InputEntryDTO, OutputEntryDTO> chunk(chunk)
                .reader(entryReader(null))
                .processor(entryProcessor())
                .writer(entryWriter())
                .faultTolerant()
                    .skip(Throwable.class)
                    .skipLimit(Integer.MAX_VALUE)
                    //.skipPolicy(new AlwaysSkipItemSkipPolicy()) // no compatible with retry mechanism
                    .retryLimit(retryTime)
                    .retry(StepSkipRetryException.class)
                .listener(entryStepListener)
                .listener(skipListener)
                .taskExecutor(generateEntryThreadPoolTaskExecutor())
                .build();
    }
    @Bean
    @StepScope
    public FlatFileItemReader<InputEntryDTO> entryReader(@Value("#{jobParameters["+ JobParam.REF_PATH +"]}") String filePath) {
        return fileReaderDTO.getFileItemReader(filePath, InputEntryDTO.class, InputEntryPosition.getNameArray(), InputEntryPosition.getIndexArray());
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
