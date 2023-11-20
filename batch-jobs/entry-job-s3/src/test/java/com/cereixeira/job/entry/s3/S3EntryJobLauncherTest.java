package com.cereixeira.job.entry.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.cereixeira.connectors.s3.S3FileReaderDTO;
import com.cereixeira.connectors.s3.S3RemoteConnector;
import com.cereixeira.job.entry.s3.dto.OutputEntryDTO;
import com.cereixeira.job.entry.s3.dto.ParamEntryDTO;
import com.cereixeira.job.entry.s3.writer.EntryWriter;
import io.findify.s3mock.S3Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = {
        "classpath:alfresco-api-application-test.properties",
        "classpath:databases-application-test.properties",
        "classpath:entry-application-test.properties",
        "classpath:s3-connector-application-test.properties",
        "classpath:spring_batch-application-test.properties"
        }, properties = {
//        "connector.s3.withcredentials=true",
//        "connector.s3.awsaccesskey=ASIAQINN34V7H3KWMTEK",
//        "connector.s3.awssecretkey=UzF3Q3aBez2qLo1h02IzJaq1y6DpKgDvQ+YdJEy1",
//        "connector.s3.sessiontoken=FwoGZXIvYXdzEKH//////////wEaDD482NCuhXpk7ZVDpCKNAqo9ozBnc9bbykgggR2kG8SWCapSQFalaO2o4liHyGRQg4J2uJEPQBZ17qG1wP4jlCT+gRzObHMMgIuG3MNg8HgEru243gcbvwsF/90B7Wk5rZwgKnYWjBofXMwpYEHtP59NSAYEQJX1lfXGUT9toq5vlLxrVmvY8GuY3UHDzq2i46ApfFpU3we5lN0SZUbkivuZ+4Ny5TfT+YHpxN8QffAcv8C6JYrXpYGzqOtqkS3037UNeopM3wMwhl4cOeijtdR9Ns2M2SvesneQyqfayE4IxAO8qspQoqFfnNdTxHZ/2A60u3nNlIMFSrXGX9JgCVcSe9EHgQsa/OU0WGmMelYBa5/8JGqF9vPod2PRKIbonKQGMiuOhLouxJ1eMpPOUBkv8k++SasItJZJNu+HFFNnypU0rqzB+zubPuYVJ7nb"
}
    )
@SpringBootApplication(scanBasePackages="com.cereixeira.job.entry.s3")
public class S3EntryJobLauncherTest {
    private static final String BUCKET_NAME = "a3s-transfer-018081375614-eu-central-1";
    private static final String FOLDER_PATH = "migration/DEV/TEST_VARIOS";
    private static final String CSV_FILE_PATH = FOLDER_PATH+"/test_01.csv";
    private static final String PDF_FILE_PATH = FOLDER_PATH+"/test0001.pdf";
    private S3RemoteConnector s3Connector;
    private S3Mock api;

    //@SpyBean
    @Autowired
    private S3FileReaderDTO s3FileReaderDTO;
    @Autowired
    private ItemWriter<OutputEntryDTO> entryWriter;
    @Autowired
    private S3EntryJobLauncher launcher;

    @Before
    public void setUp() {
        api = new S3Mock.Builder().withPort(8002).withInMemoryBackend().build();
        api.start();
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration("http://localhost:8002", "us-west-2");
        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();

        client.createBucket(BUCKET_NAME);

        File csvFile = Paths.get("target/test-classes/AWS/test_01.csv").toFile();
        client.putObject(BUCKET_NAME, CSV_FILE_PATH, csvFile);
        File pdfFile = Paths.get("target/test-classes/AWS/test0001.pdf").toFile();
        client.putObject(BUCKET_NAME, PDF_FILE_PATH, pdfFile);
        s3Connector = new S3RemoteConnector();
        s3Connector.setS3client(client);
        s3Connector.setBucketName(BUCKET_NAME);

        s3FileReaderDTO.setS3RemoteConnector(s3Connector);
        ((EntryWriter)entryWriter).setS3RemoteConnector(s3Connector);
        //given(s3FileReaderDTO.getFileItemReader(Mockito.anyString(),Mockito.any(), Mockito.any(String[].class), Mockito.any(Integer[].class))).willReturn(null);
    }
    @Test
    public void test(){

        ParamEntryDTO inputParamDTO = new ParamEntryDTO();
        inputParamDTO.setExecuteName("dos");

        launcher.run(inputParamDTO);
    }
}
