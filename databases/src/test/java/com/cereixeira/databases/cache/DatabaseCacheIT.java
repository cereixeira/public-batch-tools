package com.cereixeira.databases.cache;

import com.cereixeira.databases.config.DatabasesConfig;
import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.ExecutionEntity;
import com.cereixeira.databases.repository.ExecutionRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {DatabasesConfig.class})
public class DatabaseCacheIT {

    @Autowired
    private DatabaseCache databaseCache;
    @Autowired
    private ExecutionRepository executionRepository;
    @Autowired
    private BlockRepository blockRepository;

    @Test
    public void save() throws Exception {
        ExecutionEntity executionEntity = newExecution();
        BlockEntity blockEntity = newBlock(executionEntity);

        Long id = blockEntity.getId();
        BlockEntity block_01 = databaseCache.getBlockEntity(id);
        // El valor debe ser el mismo
        Assert.assertEquals(blockEntity.isCompleted(), block_01.isCompleted());

        // se cambia valor true
        blockEntity.setCompleted(true);
        blockRepository.save(blockEntity);

        BlockEntity block_02 = databaseCache.getBlockEntity(id);
        // el valor ha cambiado pero la cache mantiene el anterior
        Assert.assertNotEquals(blockEntity.isCompleted(), block_02.isCompleted());

    }

    private BlockEntity newBlock(ExecutionEntity execution) {
        BlockEntity block = new BlockEntity();
        block.setRefPath("uno");
        block.setDateIni(new Date());
        block.setDateEnd(new Date());
        block.setCompleted(false);
        block.setExecution(execution);
        blockRepository.save(block);
        return block;
    }

    private ExecutionEntity newExecution() {
        ExecutionEntity execution = new ExecutionEntity();
        execution.setName(RandomStringUtils.randomAlphabetic(10));
        execution.setInputPath("test");
        executionRepository.save(execution);
        return execution;
    }
}
