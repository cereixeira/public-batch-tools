package com.cereixeira.job.executeblock.config;


import com.cereixeira.databases.config.DatabasesConfig;
import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.job.executeblock.constants.ExecuteBlockJobBeanName;
import com.cereixeira.job.executeblock.listener.BlockStepListener;
import com.cereixeira.job.executeblock.reader.BlockItemReader;
import com.cereixeira.job.executeblock.tasklet.GenerateExecutionTasklet;
import com.cereixeira.job.executeblock.writer.BlockWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@Configuration
@EnableBatchProcessing

@PropertySource(value = "classpath:spring_batch-spring_batch-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/spring_batch-spring_batch-application.properties", ignoreResourceNotFound = true)

@PropertySource(value = "classpath:execute_block-spring_batch-application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:config/execute_block-spring_batch-application.properties", ignoreResourceNotFound = true)

@Import({ DatabasesConfig.class
})
@ComponentScan(basePackages = "com.cereixeira.job.executeblock")
public class ExecutionBlockJobConfig {

    @Value("${job.executeblock.chunk}")
    private int chunk;
    @Value("${job.executeblock.task.corepoolsize}")
    private int taskCorePoolSize;
    @Value("${job.executeblock.task.maxpoolsize}")
    private int taskMaxPoolSize;
    @Value("${job.executeblock.task.queuesize}")
    private int queueSize;
    @Autowired
    private JobRepository jobRepository;

    @Bean(name = ExecuteBlockJobBeanName.EXECUTION_BLOCK_SIMPLE_JOB_LAUNCHER)
    public SimpleJobLauncher jobLauncher() {
        final SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private BlockStepListener blockStepListener;

    @Bean(name = ExecuteBlockJobBeanName.EXECUTION_BLOCK_JOB)
    public Job ingestionExecutionAndBlockJob() throws Exception {
        return jobBuilderFactory.get(ExecuteBlockJobBeanName.EXECUTION_BLOCK_JOB)
                .incrementer(new RunIdIncrementer())
                .start(step1GenerateExecution())
                .next(step2GenerateBlock())
                .build();
    }

    @Bean(name = ExecuteBlockJobBeanName.STEP1_GENERATE_EXECUTION)
    public Step step1GenerateExecution(){
        return stepBuilderFactory.get(ExecuteBlockJobBeanName.STEP1_GENERATE_EXECUTION)
                .tasklet(taskGenerateExecution())
                .build();
    }
    @Bean(name = ExecuteBlockJobBeanName.STEP2_GENERATE_BLOCK)
    public Step step2GenerateBlock() throws Exception {
        return stepBuilderFactory.get(ExecuteBlockJobBeanName.STEP2_GENERATE_BLOCK)
                .<BlockEntity, BlockEntity> chunk(chunk)
                .reader(blockReader())
                .writer(blockWriter())
                .listener(blockStepListener)
                .taskExecutor(generateBlockThreadPoolTaskExecutor())
                .build();
    }
    @Bean
    public Tasklet taskGenerateExecution(){
        return new GenerateExecutionTasklet();
    }

    @Bean
    public ItemReader<BlockEntity> blockReader() {
        return new BlockItemReader();
    }
    @Bean
    public ItemWriter<BlockEntity> blockWriter() {
        return new BlockWriter();
    }

    @Bean
    public ThreadPoolTaskExecutor generateBlockThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("stepBlockTask-");
        taskExecutor.setCorePoolSize( taskCorePoolSize );
        taskExecutor.setMaxPoolSize( taskMaxPoolSize );
        taskExecutor.setQueueCapacity( queueSize );
        return taskExecutor;
    }
}
