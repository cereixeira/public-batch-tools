package com.cereixeira.databases.repository;

import com.cereixeira.databases.config.DatabasesConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {DatabasesConfig.class})

public class ExecutionEntityIT {

    @Autowired
    private ExecutionRepository executionRepository;
    @Autowired
    private BlockRepository blockRepository;

    @Test
    public void save() throws Exception {
        ExecutionEntity execution = new ExecutionEntity();
        execution.setName(RandomStringUtils.randomAlphabetic(10));
        execution.setInputPath("test");
        ExecutionEntity newExecution = executionRepository.save(execution);

        Assert.assertEquals(newExecution.getId(), executionRepository.getById(newExecution.getId()).getId());
        executionRepository.delete(newExecution);
    }
}
