package com.cereixeira.job.report.config;

import com.cereixeira.batch.utils.config.BatchUtilsConfig;
import com.cereixeira.batch.utils.writer.FileWriterDTO;
import com.cereixeira.databases.config.DatabasesConfig;
import com.cereixeira.databases.repository.EntryEntity;
import com.cereixeira.job.report.constants.BeanName;
import com.cereixeira.job.report.constants.JobParam;
import com.cereixeira.job.report.constants.ReportConstants;
import com.cereixeira.job.report.dto.OutputReportEntryDTO;
import com.cereixeira.job.report.processor.ReportItemProcessor;
import com.cereixeira.job.report.reader.ReportEntryReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableBatchProcessing

@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/application.properties", ignoreResourceNotFound = true)

@PropertySource(value = "classpath:report_entry-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/report_entry-application.properties", ignoreResourceNotFound = true)

@Import({ DatabasesConfig.class, BatchUtilsConfig.class
})
@ComponentScan(basePackages = "com.cereixeira.job.report")
public class ReportConfig {

    @Value("${job.report.entry.chunk}")
    private int chunk;
    @Value("${job.report.entry.task.corepoolsize}")
    private int taskCorePoolSize;
    @Value("${job.report.entry.task.maxpoolsize}")
    private int taskMaxPoolSize;
    @Value("${job.report.entry.task.queuesize}")
    private int queueSize;

    @Autowired
    private JobRepository jobRepository;

    @Bean(name = BeanName.REPORT_ENTRY_SIMPLE_JOB_LAUNCHER)
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
    public ReportEntryReader reportEntryReader;
//    @Autowired
//    public ReportEntryWriter reportEntryWriter;
    @Autowired
    public FileWriterDTO fileWriterDTO;

    @Bean(name = BeanName.REPORT_ENTRY_JOB)
    public Job reportJob() throws Exception {
        return jobBuilderFactory.get(BeanName.REPORT_ENTRY_JOB)
                .incrementer(new RunIdIncrementer())
                .flow(step1GenerateReport())
                .end()
                .build();
    }
    @Bean
    public ThreadPoolTaskExecutor generateReportThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("reportTask-");
        taskExecutor.setCorePoolSize( taskCorePoolSize );
        taskExecutor.setMaxPoolSize( taskMaxPoolSize );
        taskExecutor.setQueueCapacity( queueSize );
        return taskExecutor;
    }
    @Bean
    public Step step1GenerateReport() throws Exception {
        return stepBuilderFactory.get("stepReadDBProcessorWriteFile")
                .<EntryEntity, OutputReportEntryDTO> chunk(chunk)
                .reader(databaseReader(null))
                .processor(reportProcessor())
                .writer(reportWriter(null, null))
                .taskExecutor(generateReportThreadPoolTaskExecutor())
                .build();
    }
    @Bean
    @StepScope
    public ItemReader<EntryEntity> databaseReader(@Value("#{jobParameters["+JobParam.EXECUTION_NAME+"]}") String executionName) throws Exception {
        return reportEntryReader.databaseReader(executionName);
    }

    @Bean
    public ReportItemProcessor reportProcessor() {
        return new ReportItemProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<OutputReportEntryDTO> reportWriter(@Value("#{jobParameters["+ JobParam.FOLDER_PATH+"]}") String folderPath,
                                                                 @Value("#{jobParameters["+JobParam.EXECUTION_NAME+"]}") String executionName) {
        //return reportEntryWriter.reportWriter(folderPath, executionName);
        String filePath = folderPath+ ReportConstants.REPORT_PREFIX_NAME+executionName+ReportConstants.REPORT_SUFFIX_NAME;
        return fileWriterDTO.getFileItemWriter(filePath, OutputReportEntryDTO.class, OutputReportEntryDTO.getFields());
    }
}
