package com.cereixeira.job.executeblock;

import com.cereixeira.job.executeblock.api.IExecutionBlockJobLauncher;
import com.cereixeira.job.executeblock.api.dto.IParamExecuteBlockDTO;
import com.cereixeira.job.executeblock.constants.ExecuteBlockJobBeanName;
import com.cereixeira.job.executeblock.constants.JobParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Qualifier(ExecuteBlockJobBeanName.EXECUTION_JOB_LAUNCHER)
public class ExecutionBlockJobLauncher implements IExecutionBlockJobLauncher {
    private Logger logger = LogManager.getLogger(ExecutionBlockJobLauncher.class);

    @Autowired
    @Qualifier(ExecuteBlockJobBeanName.EXECUTION_BLOCK_SIMPLE_JOB_LAUNCHER)
    private org.springframework.batch.core.launch.JobLauncher executionBlockSimpleJobLauncher;

    @Autowired
    @Qualifier(ExecuteBlockJobBeanName.EXECUTION_BLOCK_JOB)
    private Job executionBlockJob;

    /**
    Generate a data record in first table EXECUTION.
    This record contains data about input parameters. Ingestion name and input path
     */
    @Override
    public void run(IParamExecuteBlockDTO inputParamDTO) {
        logger.debug("#run()");
        Map<String, JobParameter> param = new HashMap<>();
        // The timestamp + executionName params assure a new instance for this job
        param.put(JobParam.CXT_TIMESTAMP, new JobParameter(new Date()));
        param.put(JobParam.CXT_EXECUTION_NAME, new JobParameter(inputParamDTO.getExecuteName()));
        param.put(JobParam.CXT_FILE_SUFFIX, new JobParameter(inputParamDTO.getFileSuffix()));
        param.put(JobParam.CXT_FOLDER_PATH, new JobParameter(inputParamDTO.getFolderPath()));
        JobParameters jobParameters = new JobParameters(param);

        JobExecution jobExecution = null;
        try {
            jobExecution = executionBlockSimpleJobLauncher.run(executionBlockJob, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("#run - Job:{}, Status:{}", jobExecution.getJobInstance().getJobName(), jobExecution.getStatus().name());
    }
}
