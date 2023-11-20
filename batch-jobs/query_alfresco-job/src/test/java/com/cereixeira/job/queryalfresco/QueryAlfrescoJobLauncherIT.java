package com.cereixeira.job.queryalfresco;

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
        "classpath:spring_batch-application.properties",
        "classpath:databases-application.properties",
        "classpath:query_alfresco-application-test.properties"
})
@SpringBootApplication(scanBasePackages="com.cereixeira.job.queryalfresco")
public class QueryAlfrescoJobLauncherIT {

    @Autowired
    private QueryAlfrescoJobLauncher launcher;

    @Test
    public void test() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        launcher.run();
    }
}
