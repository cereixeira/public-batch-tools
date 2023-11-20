package com.cereixeira.job.entry.listener;

import com.cereixeira.databases.repository.BlockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntryStepListener implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(EntryStepListener.class);

    @Autowired
    private BlockRepository blockRepository;
    @Override
    public void beforeStep(StepExecution stepExecution) {
//        // Gets the idBlock from job parameters
//        String idBlock = stepExecution.getJobExecution().getJobParameters().getString(JobParam.ID_BLOCK);
//        BlockEntity blockEntity = blockRepository.findById(Long.valueOf(idBlock)).get();
//        // Sets the BlockEntity as step context
//        stepExecution.getJobExecution().getExecutionContext().put(JobParam.BLOCK_ENTITY_NOT_COMPLETED, blockEntity);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
