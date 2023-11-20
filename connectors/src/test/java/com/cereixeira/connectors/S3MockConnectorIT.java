package com.cereixeira.connectors;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.cereixeira.connectors.s3.S3RemoteConnector;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class S3MockConnectorIT {

    private S3RemoteConnector s3Connector;
    private static Path targetFolderPath;
    private static Path testFtpDownloadFolderPath;
    private static final String BUCKET_NAME = "a3s-transfer-018081375614-eu-central-1";
    private static final String FOLDER_PATH = "migration/DEV";
    private static final String CSV_FILE_PATH = FOLDER_PATH+"/test_01.csv";

    
    @BeforeClass
    public static void beforeClass() throws IOException {
        targetFolderPath = Paths.get("target");
        testFtpDownloadFolderPath = Files.createTempDirectory(targetFolderPath, "TestFTPDownload");
    }
    
    @Before
    public void setUp() {
        EndpointConfiguration endpoint = new EndpointConfiguration("http://localhost:8001", "us-west-2");
        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();

        if(client.listBuckets().isEmpty()){
            client.createBucket(BUCKET_NAME);
            client.putObject(BUCKET_NAME, CSV_FILE_PATH, "contents");
        }

        s3Connector = new S3RemoteConnector();
        s3Connector.setS3client(client);
        s3Connector.setBucketName(BUCKET_NAME);
    }
    
    @After
    public void cleanUp() throws Exception {
        if(s3Connector != null) {
            s3Connector.close();
        }
    }
    
    @AfterClass
    public static void afterClass() throws IOException {
        Files.walk(testFtpDownloadFolderPath)
                .map(Path::toFile)
                .forEach(File::delete);
        Files.deleteIfExists(testFtpDownloadFolderPath);
    }
    
    @Test
    public void getControlFiles() throws Exception {
        List<String> lista = s3Connector.getFileNameBySuffix(FOLDER_PATH, ".csv");

        Assert.assertNotNull(lista);
        Assert.assertFalse(lista.isEmpty());
    }

    @Test
    public void getControlFiles_ERROR() throws Exception {
        List<String> lista = s3Connector.getFileNameBySuffix("FAKE", ".csv");
        Assert.assertNotNull(lista);
        Assert.assertTrue(lista.isEmpty());
    }

    @Test
    public void downloadToFS_OK() throws Exception {
        //given(this.remoteService.someCall()).willReturn("mock");
        Path testLocalFile = Files.createTempFile(testFtpDownloadFolderPath, "","_test.csv");
        s3Connector.downloadToFS(CSV_FILE_PATH, testLocalFile.toAbsolutePath().toString());
        Assert.assertTrue(new File(testLocalFile.toAbsolutePath().toString()).exists());

    }

    @Test(expected = AmazonS3Exception.class)
    public void downloadToFS_ERROR() throws Exception {
        Path testLocalZIPFile = Files.createTempFile(testFtpDownloadFolderPath, "","_test.csv");
        s3Connector.downloadToFS("migration/testSimple/FAKE.csv", testLocalZIPFile.toAbsolutePath().toString());
    }

}
