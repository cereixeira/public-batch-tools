package com.cereixeira.job.executeblock.listener;


import com.cereixeira.databases.cache.DatabaseCache;
import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.ExecutionEntity;
import com.cereixeira.databases.utils.FileUtils;
import com.cereixeira.job.executeblock.constants.JobParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class BlockStepListener implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(BlockStepListener.class);

    @Autowired
    private DatabaseCache ingestionCache;
    @Autowired
    private BlockRepository blockRepository;
    /*
    Tasks
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        String executionName = stepExecution.getJobParameters().getString(JobParam.CXT_EXECUTION_NAME);
        String fileSuffix = stepExecution.getJobParameters().getString(JobParam.CXT_FILE_SUFFIX);
        // gets the execution entity.
        ExecutionEntity executionEntity = ingestionCache.getExecutionEntity(executionName);

        // gets all the control files to process (CSV)
        List<File> files = FileUtils.getFilesBySuffix(executionEntity.getInputPath(), fileSuffix);

        List<File> newFiles = new ArrayList<>();
        for(File file : files) {
            // checks if the control file already exists in DB (BlockEntity)
            if(isNewFile(file, executionEntity)){
                newFiles.add(file);
            }
        }

        // puts in job context the new files (not exist in DB)
        stepExecution.getExecutionContext().put(JobParam.CXT_INPUT_FILE_LIST, newFiles);
        // puts in job context the execution entity.
        stepExecution.getExecutionContext().put(JobParam.CXT_EXECUTION_ENTITY, executionEntity);
    }

    private boolean isNewFile(File file, ExecutionEntity executionEntity) {
        List<BlockEntity> blockList = blockRepository.findByExecutionAndRefPath(executionEntity, file.getPath());
        return blockList.isEmpty();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
