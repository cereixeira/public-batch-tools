package com.cereixeira.connectors;

import com.cereixeira.connectors.config.SFTPConnectorConfig;
import com.jcraft.jsch.SftpException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:sftp-connector-application-test.properties")
@ContextConfiguration(classes = {SFTPConnectorConfig.class})
public class SFTPConnectorIT {

    private static final String SFTP_MIGRATION_PATH = "/home/migracion";
    @Qualifier("SFTPRemoteConnector")
    @Autowired
    private IRemoteConnector sftpConnector;
    private static Path targetFolderPath;
    private static Path testFtpDownloadFolderPath;

    
    @BeforeClass
    public static void beforeClass() throws IOException {
        targetFolderPath = Paths.get("target");
        testFtpDownloadFolderPath = Files.createTempDirectory(targetFolderPath, "TestFTPDownload");
    }
    
    @Before
    public void setUp() {

    }
    
    @After
    public void cleanUp() throws Exception {
        sftpConnector.close();
    }
    
    @AfterClass
    public static void afterClass() throws IOException {
        Files.walk(testFtpDownloadFolderPath)
                .map(Path::toFile)
                .forEach(File::delete);
    
        Files.deleteIfExists(testFtpDownloadFolderPath);
    }
    
    @Test
    public void getControlFiles_OK() throws Exception {
        List<String> lista = sftpConnector.getFileNameBySuffix(SFTP_MIGRATION_PATH, ".csv");
        Assert.assertNotNull(lista);
        Assert.assertEquals(2, lista.size());
    }
    
    @Test
    public void getControlFiles_ERROR() throws Exception {
        List<String> lista = sftpConnector.getFileNameBySuffix("/home", ".csv");
        Assert.assertNotNull(lista);
        Assert.assertTrue(lista.isEmpty());
    }

    @Test
    public void downloadFromFTPToFS_OK() throws Exception {
        String ftpFilePath = SFTP_MIGRATION_PATH+"/blank.PDF";
        Path localFilePath = Paths.get(testFtpDownloadFolderPath.toString(), "test.pdf");
        sftpConnector.downloadToFS(ftpFilePath, localFilePath.toString());
        Assert.assertTrue(localFilePath.toFile().exists());
    }

    @Test(expected = SftpException.class)
    public void downloadFromFTPToFS_ERROR() throws Exception {
        String fakeFile = testFtpDownloadFolderPath.toString()+"/fakeFile.txt";
        sftpConnector.downloadToFS("testError.txt", fakeFile);
    }

}
