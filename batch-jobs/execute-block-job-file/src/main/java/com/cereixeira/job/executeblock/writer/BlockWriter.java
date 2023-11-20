package com.cereixeira.job.executeblock.writer;

import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BlockWriter implements ItemWriter<BlockEntity> {
    private static final Logger logger = LoggerFactory.getLogger(BlockWriter.class);
    @Autowired
    private BlockRepository blockRepository;

    @Override
    public void write(List<? extends BlockEntity> list)  {
        for(BlockEntity entity : list){
            blockRepository.save(entity);
            logger.debug("#write - entity:{}",entity);
        }
    }
}
