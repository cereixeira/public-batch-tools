package com.cereixeira.job.entry;

import com.cereixeira.job.entry.dto.ParamEntryDTO;
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
        "classpath:entry-application-test.properties",
        "classpath:spring_batch-application-test.properties"
        }, properties = {}
)
@SpringBootApplication(scanBasePackages="com.cereixeira.job.entry")
public class EntryJobLauncherIT {
    @Autowired
    private EntryJobLauncher launcher;
    @Test
    public void test() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        ParamEntryDTO inputParamDTO = new ParamEntryDTO();
        inputParamDTO.setExecuteName("uno");

        launcher.run(inputParamDTO);
    }
}
