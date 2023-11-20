package com.cereixeira.job.queryalfresco;

import com.cereixeira.job.queryalfresco.constants.BeanName;
import com.cereixeira.job.queryalfresco.constants.ParamName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class QueryAlfrescoJobLauncher {
    private Logger logger = LogManager.getLogger(QueryAlfrescoJobLauncher.class);

    @Value("${job.queryalfresco.folderpath}")
    private String folderPath;

    @Value("${job.queryalfresco.query}")
    private String query;

    @Autowired
    @Qualifier(BeanName.QUERY_ALFRESCO_SIMPLE_JOB_LAUNCHER)
    private org.springframework.batch.core.launch.JobLauncher jobLauncher;

    @Autowired
    @Qualifier(BeanName.QUERY_ALFRESCO_JOB)
    private Job queryAlfrescoJob;

    public void run() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        Map<String, JobParameter> param = new HashMap<>();
        param.put(ParamName.TIMESTAMP, new JobParameter(new Date()));
        param.put(ParamName.FOLDER_PATH, new JobParameter(folderPath));
        param.put(ParamName.QUERY, new JobParameter(query));
        JobParameters jobParameters = new JobParameters(param);

        JobExecution jobExecution = jobLauncher.run(queryAlfrescoJob, jobParameters);

        logger.info("#run - Job:{}, Status:{}", jobExecution.getJobInstance().getJobName(), jobExecution.getStatus().name());
    }
}
