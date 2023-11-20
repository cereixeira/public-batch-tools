package com.cereixeira.job.executeblock;

import com.cereixeira.job.executeblock.dto.ParamExecuteBlockDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = {
        "classpath:databases-application-test.properties",
        "classpath:execute_block-application-test.properties",
        "classpath:spring_batch-application-test.properties"
})
@SpringBootApplication(scanBasePackages="com.cereixeira.job.executeblock")
public class ExecutionBlockJobLauncherIT {

    @Autowired
    private ExecutionBlockJobLauncher launcher;

    @Test
    public void test() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        ParamExecuteBlockDTO inputParamDTO = new ParamExecuteBlockDTO();
        inputParamDTO.setExecuteName("uno");
        inputParamDTO.setFileSuffix("ingestion.csv");
        inputParamDTO.setFolderPath("target/test-classes/");

        launcher.run(inputParamDTO);
    }
}
