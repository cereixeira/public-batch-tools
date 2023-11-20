package com.cereixeira.job.report;

import com.cereixeira.job.report.constants.BeanName;
import com.cereixeira.job.report.constants.JobParam;
import com.cereixeira.job.report.dto.ParamReportEntryDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReportEntryJobLauncher {
    private Logger logger = LogManager.getLogger(ReportEntryJobLauncher.class);

    @Autowired
    @Qualifier(BeanName.REPORT_ENTRY_SIMPLE_JOB_LAUNCHER)
    private org.springframework.batch.core.launch.JobLauncher jobLauncher;

    @Autowired
    @Qualifier(BeanName.REPORT_ENTRY_JOB)
    private Job ingestionReportJob;

    public void run(ParamReportEntryDTO paramReportEntryDTO) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        logger.debug("#run()");

        Map<String, JobParameter> param = new HashMap<>();
        // The timestamp + executionName params assure a new instance for this job
        param.put(JobParam.TIMESTAMP, new JobParameter(new Date()));
        param.put(JobParam.EXECUTION_NAME, new JobParameter(paramReportEntryDTO.getExecuteName()));
        param.put(JobParam.FOLDER_PATH, new JobParameter(paramReportEntryDTO.getFolderPath()));
        JobParameters jobParameters = new JobParameters(param);

        JobExecution jobExecution = jobLauncher.run(ingestionReportJob, jobParameters);

        logger.info("#run - Job:{}, Status:{}", jobExecution.getJobInstance().getJobName(), jobExecution.getStatus().name());
    }
}
