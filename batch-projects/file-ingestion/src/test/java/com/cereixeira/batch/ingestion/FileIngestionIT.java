package com.cereixeira.batch.ingestion;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = {
        "classpath:batch_ingestion-application-test.properties",
        "classpath:databases-application.properties",
        "classpath:entry-application.properties",
        "classpath:execute_block-application.properties",
        "classpath:report_entry-application.properties",
        "classpath:spring_batch-application.properties"
        }, properties = {
        "job.ingestion.execution.name=2",
        "job.ingestion.path=target/test-classes/",
        "job.ingestion.file.suffix=ingestion.csv"
        }
    )
@SpringBootTest(value = "com.cereixeira.batch.ingestion")
public class FileIngestionIT {
    @Test
    public void testArgs() {

    }
}
