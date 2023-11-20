package com.cereixeira.batch.ingestion;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = {
        "classpath:spring_batch-application-test.properties",
        "classpath:databases-application-test.properties",
        "classpath:execute_block-application-test.properties",
        "classpath:entry-application-test.properties",
        "classpath:report_entry-application-test.properties",
        "classpath:alfresco-api-application-test.properties",
        "classpath:s3-connector-application-test.properties",
        "classpath:batch_ingestion-application-test.properties"
        },properties = {
        "job.ingestion.execution.name=109",
        "job.ingestion.input.folder.path=migration/DEV/informacion_fiscal_ubs_2022/",
        "job.ingestion.file.suffix=.csv",
        "job.ingestion.output.folder.path=target/test-classes/",
        "connector.s3.withcredentials=true",
        "connector.s3.awsaccesskey=ASIAQINN34V7FOKZFW5U",
        "connector.s3.awssecretkey=NAf01acukZmNY9+LFSBk1rhv88bBReW8iAI3cDNj",
        "connector.s3.sessiontoken=FwoGZXIvYXdzEOr//////////wEaDP7JSc/pNZBcJovsIiKNAuPAPIks8Q43AqfoaCLelnR8tymwVeGGeugZ7+KZZT7bVQdgAYvDmi5Z+hHXR87/Kb+frt7nAaIdhtAP2YpBxiRzVjNVKvk3v1s0dGxuOFPO1RQKFTTBoFMFO/LdsfroGlKj0THpZntbe1/Qrv+JQhUipMNQVv+r1OXRAYr5cmBeJwkBNF9i8iziDWnYHWJW4x6nG3vN+TvbQueBrwXIo4pmstMYk5sg9BlaOUHjZ3BUiiHwTn0gRhja5DMkEVgDM30jB1A2TLkRBg+Qou53Uf4P4fqpx77YyOGjYPemcK9V9hLkc174Nm/FPCUBt3KyMCCt2F8q9gD1HF6kHkAoxs9ViFj9lk2l3imHjYU1KO6wvKMGMiuKgDYnRsKMJNGo9ZRryPgoDWkL4bzyRNZPpeYef4HM0+MWOYnAE8i55jdt"
        }
        //"job.ingestion.input.folder.path=migration/DEV/TEST_VARIOS/",
        //"job.ingestion.input.folder.path=migration/DEV/informacion_fiscal_ubs_2022/",
    )
    @SpringBootTest(value = "com.cereixeira.batch.ingestion")
    public class S3IngestionIT {
    @Test
    public void testArgs() {

    }
}
