package com.cereixeira.job.executeblock.s3.listener;


import com.cereixeira.connectors.s3.S3RemoteConnector;
import com.cereixeira.databases.cache.DatabaseCache;
import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.ExecutionEntity;
import com.cereixeira.job.executeblock.s3.constants.JobParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BlockStepListener implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(BlockStepListener.class);

    @Autowired
    private DatabaseCache ingestionCache;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private S3RemoteConnector s3RemoteConnector;
    @Override
    public void beforeStep(StepExecution stepExecution) {
        String executionName = stepExecution.getJobParameters().getString(JobParam.CXT_EXECUTION_NAME);
        String fileSuffix = stepExecution.getJobParameters().getString(JobParam.CXT_FILE_SUFFIX);
        // gets the execution entity.
        ExecutionEntity executionEntity = ingestionCache.getExecutionEntity(executionName);

        // gets all the control files to process (CSV)
        //List<File> files = FileUtils.getFilesBySuffix(executionEntity.getInputPath(), fileSuffix);
        List<String> csvFileNames = s3RemoteConnector.getFileNameBySuffix(executionEntity.getInputPath(),fileSuffix);

        List<String> newCsvFilePaths = new ArrayList<>();
        for(String csvFileName : csvFileNames) {
            // checks if the control file already exists in DB (BlockEntity)
            String csvFilePath = executionEntity.getInputPath()+csvFileName;
            if(isNewFile(csvFilePath, executionEntity)){
                newCsvFilePaths.add(csvFilePath);
            }
        }

        // puts in job context the new files (not exist in DB)
        stepExecution.getExecutionContext().put(JobParam.CXT_INPUT_FILE_LIST, newCsvFilePaths);
        // puts in job context the execution entity.
        stepExecution.getExecutionContext().put(JobParam.CXT_EXECUTION_ENTITY, executionEntity);
    }

    private boolean isNewFile(String s3filePath, ExecutionEntity executionEntity) {
        List<BlockEntity> blockList = blockRepository.findByExecutionAndRefPath(executionEntity, s3filePath);
        return blockList.isEmpty();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    public void setS3RemoteConnector(S3RemoteConnector s3RemoteConnector) {
        this.s3RemoteConnector = s3RemoteConnector;
    }
}
