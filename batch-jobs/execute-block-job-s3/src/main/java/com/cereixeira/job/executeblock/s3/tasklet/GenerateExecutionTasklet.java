package com.cereixeira.job.executeblock.s3.tasklet;

import com.cereixeira.databases.repository.ExecutionEntity;
import com.cereixeira.databases.repository.ExecutionRepository;
import com.cereixeira.job.executeblock.s3.constants.JobParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GenerateExecutionTasklet implements Tasklet {
    private Logger logger = LogManager.getLogger(GenerateExecutionTasklet.class);
    @Autowired
    private ExecutionRepository executionRepository;


    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
        logger.debug("#execute");

        String executionName = stepContribution.getStepExecution().getJobParameters().getString(JobParam.CXT_EXECUTION_NAME);
        String s3FolderPath = stepContribution.getStepExecution().getJobParameters().getString(JobParam.CXT_S3_FOLDER_PATH);

        List<ExecutionEntity> list = executionRepository.findByName(executionName);

        if(list.isEmpty()) {
            ExecutionEntity entity = new ExecutionEntity();
            entity.setInputPath(s3FolderPath);
            entity.setName(executionName);
            executionRepository.save(entity);
            logger.info("#execute - creado nuevo registro execution:{}",executionName);
        }else{
            logger.info("#execute - Ya existe registro execution:{}",executionName);
        }

        return  RepeatStatus.FINISHED;
    }
}
