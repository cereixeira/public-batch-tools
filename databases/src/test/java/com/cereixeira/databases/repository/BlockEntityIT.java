package com.cereixeira.databases.repository;

import com.cereixeira.databases.config.DatabasesConfig;
import org.apache.commons.lang3.RandomStringUtils;
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
public class BlockEntityIT {

    @Autowired
    private ExecutionRepository executionRepository;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private EntryRepository entryRepository;

    @Test
    public void save() {
        ExecutionEntity execution = new ExecutionEntity();
        execution.setName(RandomStringUtils.randomAlphabetic(10));
        execution.setInputPath("test");
        executionRepository.save(execution);

        BlockEntity block = new BlockEntity();
        block.setRefPath("test");
        block.setDateIni(new Date());
        block.setDateEnd(new Date());
        block.setCompleted(true);
        block.setExecution(execution);
        blockRepository.save(block);

        EntryEntity entryEntity = new EntryEntity();
        entryEntity.setCompleted(false);
        entryEntity.setInputData(RandomStringUtils.randomAlphabetic(55));
        entryEntity.setOutputData(RandomStringUtils.randomAlphabetic(55));
        entryEntity.setNodeRef("1111-2222");
        entryEntity.setUniqueRef(RandomStringUtils.randomAlphabetic(55));
        entryEntity.setDateIni(new Date());
        entryEntity.setDateEnd(new Date());
        entryEntity.setBlock(block);
        entryRepository.save(entryEntity);

        entryRepository.delete(entryEntity);
        blockRepository.delete(block);
        executionRepository.delete(execution);
    }
}
