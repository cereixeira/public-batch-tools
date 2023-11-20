package com.cereixeira.job.entry.s3;


import com.cereixeira.databases.cache.DatabaseCache;
import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.ExecutionEntity;
import com.cereixeira.job.entry.api.IEntryJobLauncher;
import com.cereixeira.job.entry.api.dto.IParamEntryDTO;
import com.cereixeira.job.entry.s3.constants.JobParam;
import com.cereixeira.job.entry.s3.constants.S3EntryJobBeanName;
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
import java.util.List;
import java.util.Map;

@Component
@Qualifier(S3EntryJobBeanName.ENTRY_JOB_LAUNCHER_S3)
public class S3EntryJobLauncher implements IEntryJobLauncher {
    private Logger logger = LogManager.getLogger(S3EntryJobLauncher.class);
    @Autowired
    @Qualifier(S3EntryJobBeanName.ENTRY_SIMPLE_JOB_LAUNCHER)
    private org.springframework.batch.core.launch.JobLauncher entrySimpleJobLauncher;

    @Autowired
    @Qualifier(S3EntryJobBeanName.ENTRY_JOB)
    private Job ingestionEntryJob;

    @Autowired
    private DatabaseCache ingestionCache;
    @Autowired
    private BlockRepository blockRepository;

    /*
    Generate a data records in third table ENTRY.
    These records contain data about each record from datasource
     */
    @Override
    public void run(IParamEntryDTO inputParamDTO) {
        logger.info("###########################################################################");
        logger.info("#run() - START - inputParamDTO:{}", inputParamDTO);
        try {
            // the Execution entity has to exist in database
            ExecutionEntity executionEntity = ingestionCache.getExecutionEntity(inputParamDTO.getExecuteName());

            // find the Block entity belongs to Execution with completed "false"
            List<BlockEntity> blocksNotCompleted = blockRepository.findByExecutionAndCompleted(executionEntity, false);
            List<BlockEntity> blocksCompleted = blockRepository.findByExecutionAndCompleted(executionEntity, true);
            int numTotal = blocksNotCompleted.size()+blocksCompleted.size();
            if( !blocksNotCompleted.isEmpty() ){
                logger.info("#run -  {}/{} blocks completed.", blocksCompleted.size(), numTotal);
                // iterate on uncompleted Block to finish them
                for (BlockEntity blockNotCompleted : blocksNotCompleted) {
                    runBlockEntries(blockNotCompleted);
                }
            } else if(blocksNotCompleted.isEmpty() && !blocksCompleted.isEmpty()){
                logger.info("#run - {}/{} blocks completed. No blocks found to be processed", numTotal,numTotal);
            }

        } catch(Exception e){
            logger.error("#run - ERROR:{}",e.getMessage(), e);
        } finally {
            logger.info("#run() - END");
            logger.info("###########################################################################\n\n");
        }
    }

    private void runBlockEntries(BlockEntity blockNotCompleted) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        logger.debug("#runBlockEntries - blockNotCompleted:{}, ", blockNotCompleted);
        Map<String, JobParameter> param = new HashMap<>();
        param.put(JobParam.TIMESTAMP, new JobParameter(new Date()));
        // config: getFilePathResource() --> entryReader()
        param.put(JobParam.REF_PATH, new JobParameter(blockNotCompleted.getRefPath()));
        // EntryStepListener: set BlockEntity -> Processor
        param.put(JobParam.ID_BLOCK, new JobParameter(blockNotCompleted.getId()));

        JobParameters jobParameters = new JobParameters(param);

        JobExecution jobExecution = entrySimpleJobLauncher.run(ingestionEntryJob, jobParameters);

        logger.info("#run - Job:{}, Status:{}", jobExecution.getJobInstance().getJobName(), jobExecution.getStatus().name());
    }


}
