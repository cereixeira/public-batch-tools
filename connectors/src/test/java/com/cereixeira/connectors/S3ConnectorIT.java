package com.cereixeira.connectors;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.cereixeira.connectors.s3.S3RemoteConnector;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class S3ConnectorIT {
    // S3
    private static final String AWS_ACCESS_KEY = "ASIAQINN34V7CRDEI2P7";
    private static final String AWS_SECRET_KEY = "3GgcE4oqQJOkpibxBSOHvH10UC8oXgHOLd42KPPX";
    private static final String SESSION_TOKEN = "FwoGZXIvYXdzEGkaDHZtbz1sJZMBuQGWCSKNAgf4bF194f6n/2Tu+VTkoxi9Z4uYp5nGdbfY5ir8pAhqzG9IJ/j4ejsuRY6SXrRXRXqEWDsp3qrmP/8PkjChMRUgUUeBdcXmmBorXOqI3mNrq5Q4UBG12z52FCoyucfnwoy0w6XsyUYUcBPwOeGCDkdwF8m6f//pcsPssNC0Br++tQH+vbuR2K7pner8o/fErwta3V+Idc1L31MpgF7rWr63051T5ZRixEJRLmDSwuVd/JmLe+ollUeLJgTzpNJLZu/s53sKzC95fLsIpTC88zAAGU+mePdsW5iSm4sH5daG2GgFo7KXN2q+VXswyNH2sMfRP1TBUuFTsYiurxXoV0RC/PLfMshclmW+X4FeKIqG3p8GMivbRMM3blvbGDrN0JJ/A3mwBVBU5y7003PTLj8MkIH9x3OUAD4Twpd0/nMM";
    private static final String BUCKET_NAME = "a3s-transfer-018081375614-eu-central-1";
    private static final String FOLDER_PATH = "migration/DEV/TEST_VARIOS";
    private static final String FILE_PATH = FOLDER_PATH+"/UBSCARTULINA_KEA2_20220623115352_5.zip";

    
    private S3RemoteConnector s3Connector;
    private static Path targetFolderPath;
    private static Path testFtpDownloadFolderPath;

    
    @BeforeClass
    public static void beforeClass() throws IOException {
        targetFolderPath = Paths.get("target");
        testFtpDownloadFolderPath = Files.createTempDirectory(targetFolderPath, "TestFTPDownload");
    }
    
    @Before
    public void setUp() {
        s3Connector = new S3RemoteConnector();
        s3Connector.setWithCredentials(true);
        s3Connector.setBucketName(BUCKET_NAME);
        s3Connector.setAwsAccessKey(AWS_ACCESS_KEY);
        s3Connector.setAwsSecretKey(AWS_SECRET_KEY);
        s3Connector.setSessionToken(SESSION_TOKEN);
    }
    
    @After
    public void cleanUp() throws Exception {
        s3Connector.close();
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
        Path testLocalZIPFile = Files.createTempFile(testFtpDownloadFolderPath, "","_binarios.zip");
        s3Connector.downloadToFS(FILE_PATH, testLocalZIPFile.toAbsolutePath().toString());
        Assert.assertTrue(new File(testLocalZIPFile.toAbsolutePath().toString()).exists());
    }

    @Test(expected = AmazonS3Exception.class)
    public void downloadToFS_ERROR() throws Exception {
        Path testLocalZIPFile = Files.createTempFile(testFtpDownloadFolderPath, "","_binarios.zip");
        s3Connector.downloadToFS("migration/testSimple/fake.zip", testLocalZIPFile.toAbsolutePath().toString());
    }

}
