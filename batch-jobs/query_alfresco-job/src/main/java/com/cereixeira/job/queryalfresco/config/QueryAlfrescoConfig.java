package com.cereixeira.job.queryalfresco.config;

import com.cereixeira.batch.utils.config.BatchUtilsConfig;
import com.cereixeira.batch.utils.report.OutputReport;
import com.cereixeira.batch.utils.writer.FileWriterDTO;
import com.cereixeira.databases.config.DatabasesConfig;
import com.cereixeira.job.queryalfresco.constants.BeanName;
import com.cereixeira.job.queryalfresco.constants.ParamName;
import com.cereixeira.job.queryalfresco.dto.InputRecordDTO;
import com.cereixeira.job.queryalfresco.reader.AlfrescoNodeReader;
import com.cereixeira.job.queryalfresco.writer.QueryWriter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@PropertySource(value = "classpath:spring_batch-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/spring_batch-application.properties", ignoreResourceNotFound = true)

@PropertySource(value = "classpath:query_alfresco-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/query_alfresco-application.properties", ignoreResourceNotFound = true)
@EnableBatchProcessing
@Import({ DatabasesConfig.class, BatchUtilsConfig.class
})
@ComponentScan(basePackages = "com.cereixeira.job.queryalfresco")
public class QueryAlfrescoConfig {

    @Value("${job.queryalfresco.task.chunk}")
    private int chunk;
    @Value("${job.queryalfresco.task.corepoolsize}")
    private int taskCorePoolSize;
    @Value("${job.queryalfresco.task.maxpoolsize}")
    private int taskMaxPoolSize;
    @Value("${job.queryalfresco.task.queuesize}")
    private int queueSize;
    @Autowired
    private JobRepository jobRepository;

    @Bean(name = BeanName.QUERY_ALFRESCO_SIMPLE_JOB_LAUNCHER)
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
    public FileWriterDTO fileWriterDTO;

//    @Autowired
//    public StepListener stepListener;

    //@Primary
    @Bean(name = BeanName.QUERY_ALFRESCO_JOB)
    public Job fileJob() throws Exception {
        return jobBuilderFactory.get(BeanName.QUERY_ALFRESCO_JOB)
                .incrementer(new RunIdIncrementer())
                .flow(stepReadAlfrescoProcessorWriteFile())
                .end()
                .build();
    }

    @Bean
    public Step stepReadAlfrescoProcessorWriteFile() throws Exception {
        return stepBuilderFactory.get("stepReadAlfrescoProcessorWriteFile")
                .<List<InputRecordDTO>, List<InputRecordDTO>> chunk( chunk )
                .reader(alfrescoReader())
                //.processor(docProcessor())
                .writer(reportWriter())
                .taskExecutor(threadPoolTaskExecutor())
                .build();
    }
    @Bean
    public ItemReader<List<InputRecordDTO>> alfrescoReader() {
        return new AlfrescoNodeReader();
    }

    @Bean
    public QueryWriter reportWriter(){
        return new QueryWriter();
    }

    @Bean(BeanName.OUTPUT_REPORT)
    @StepScope
    public OutputReport<InputRecordDTO> outputReport(@Value("#{jobParameters["+ ParamName.FOLDER_PATH +"]}") String folderPath) throws IOException {
        String filePath = folderPath+"ReportQueryAlfresco.csv";
        OutputReport<InputRecordDTO> outputReport = new OutputReport<>();
        String header = Arrays.stream(InputRecordDTO.getFields()).collect(Collectors.joining(";"));
        outputReport.init(filePath, header,null, null);
        return outputReport;
    }


    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("stepAlfrescoTask-");
        taskExecutor.setCorePoolSize(taskCorePoolSize);
        taskExecutor.setMaxPoolSize(taskMaxPoolSize);
        taskExecutor.setQueueCapacity(queueSize);
        return taskExecutor;
    }
}
