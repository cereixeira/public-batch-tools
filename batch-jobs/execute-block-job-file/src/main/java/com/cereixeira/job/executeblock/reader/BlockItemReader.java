package com.cereixeira.job.executeblock.reader;


import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.ExecutionEntity;
import com.cereixeira.job.executeblock.constants.JobParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;

import java.io.File;
import java.util.List;

public class BlockItemReader implements ItemReader<BlockEntity> {
    private static final Logger logger = LoggerFactory.getLogger(BlockItemReader.class);

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
    private StepExecution stepExecution;

    private int index=0;

    @Override
    synchronized public BlockEntity read() {
        logger.debug("#read - index:{} -  thread:{}",index, Thread.currentThread().getId());
        BlockEntity block = null;

        // reads from step context (loaded by listener) the new control files
        List<File> files = (List<File>) stepExecution.getExecutionContext().get(JobParam.CXT_INPUT_FILE_LIST);
        // reads from step context (loaded by listener) the ExecutionEntity
        ExecutionEntity executionEntity = (ExecutionEntity) stepExecution.getExecutionContext().get(JobParam.CXT_EXECUTION_ENTITY);

        // iterates for the new control files.
        // the index is used by iteration variable for synchronization reasons, multiple-thread are supported
        if(index < files.size()) {
            File file = files.get(index++);
            block = new BlockEntity();
            block.setExecution(executionEntity);
            block.setRefPath(file.getPath());
            //entity.setDateIni(new Date());
            block.setCompleted(false);
        }
        return block;
    }
}
