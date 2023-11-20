package com.cereixeira.job.entry.s3.listener;

import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.EntryRepository;
import com.cereixeira.databases.repository.ExecutionRepository;
import com.cereixeira.job.entry.s3.constants.JobParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class EntryJobListener extends JobExecutionListenerSupport {
    Logger logger = LogManager.getLogger(EntryJobListener.class);
    @Autowired
    private ExecutionRepository executionRepository;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private EntryRepository entryRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.debug("#beforeStep - jobParameters:{}", jobExecution.getJobParameters());

        // Gets the idBlock from job parameters
        String idBlock = jobExecution.getJobParameters().getString(JobParam.ID_BLOCK);
        BlockEntity blockEntity = blockRepository.findById(Long.valueOf(idBlock)).get();

        blockEntity.setDateIni(new Date());
        blockRepository.save(blockEntity);
        // Sets the BlockEntity as step context
        jobExecution.getExecutionContext().put(JobParam.CXT_BLOCK_ENTITY_NOT_COMPLETED, blockEntity);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long idBlock = Long.valueOf(jobExecution.getJobParameters().getString(JobParam.ID_BLOCK));
        Optional<BlockEntity> optBlockEntity = blockRepository.findById(idBlock);
        BlockEntity blockEntity = optBlockEntity.get();

        //todas las entradas
        Long countEntries = entryRepository.countByBlock(blockEntity);
        Long countCompleted = entryRepository.countByBlockAndCompletedTrue(blockEntity);
        Float sumWritingDuration = entryRepository.sumWritingDurationByBlock(idBlock);
        Float sumFileSizes = entryRepository.sumFileSizesByBlock(idBlock);
        boolean isCompleted = countEntries.intValue() > 0 && countCompleted.intValue() == countEntries.intValue();

        blockEntity.setValidEntries(countEntries.intValue());
        blockEntity.setCompletedEntries(countCompleted.intValue());
        blockEntity.setCompleted( isCompleted );
        blockEntity.setWritingDuration(sumWritingDuration);
        blockEntity.setFileSizes(sumFileSizes);

        Date dateEnd = new Date();
        blockEntity.setDateEnd(dateEnd);
        long diffInMillies = Math.abs(dateEnd.getTime() - blockEntity.getDateIni().getTime());
        blockEntity.setProcessingDuration(diffInMillies/1000f);

        blockRepository.save(blockEntity);

    }

}
