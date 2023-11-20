package com.cereixeira.job.entry.s3;

import com.cereixeira.job.entry.s3.dto.ParamEntryDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = {
        "classpath:alfresco-api-application-test.properties",
        "classpath:databases-application-test.properties",
        "classpath:entry-application-test.properties",
        "classpath:s3-connector-application-test.properties",
        "classpath:spring_batch-application-test.properties"
        }, properties = {
        "connector.s3.withcredentials=true",
        "connector.s3.awsaccesskey=ASIAQINN34V7H3KWMTEK",
        "connector.s3.awssecretkey=UzF3Q3aBez2qLo1h02IzJaq1y6DpKgDvQ+YdJEy1",
        "connector.s3.sessiontoken=FwoGZXIvYXdzEKH//////////wEaDD482NCuhXpk7ZVDpCKNAqo9ozBnc9bbykgggR2kG8SWCapSQFalaO2o4liHyGRQg4J2uJEPQBZ17qG1wP4jlCT+gRzObHMMgIuG3MNg8HgEru243gcbvwsF/90B7Wk5rZwgKnYWjBofXMwpYEHtP59NSAYEQJX1lfXGUT9toq5vlLxrVmvY8GuY3UHDzq2i46ApfFpU3we5lN0SZUbkivuZ+4Ny5TfT+YHpxN8QffAcv8C6JYrXpYGzqOtqkS3037UNeopM3wMwhl4cOeijtdR9Ns2M2SvesneQyqfayE4IxAO8qspQoqFfnNdTxHZ/2A60u3nNlIMFSrXGX9JgCVcSe9EHgQsa/OU0WGmMelYBa5/8JGqF9vPod2PRKIbonKQGMiuOhLouxJ1eMpPOUBkv8k++SasItJZJNu+HFFNnypU0rqzB+zubPuYVJ7nb"
}
    )
@SpringBootApplication(scanBasePackages="com.cereixeira.job.entry.s3")
public class S3EntryJobLauncherIT {
    @Autowired
    private S3EntryJobLauncher launcher;
    @Test
    public void test(){
        ParamEntryDTO inputParamDTO = new ParamEntryDTO();
        inputParamDTO.setExecuteName("dos");

        launcher.run(inputParamDTO);
    }
}
