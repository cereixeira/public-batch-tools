package com.cereixeira.connectors;

import com.cereixeira.connectors.config.FTPConnectorConfig;
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
@TestPropertySource(locations = "classpath:ftp-connector-application-test.properties")
@ContextConfiguration(classes = {FTPConnectorConfig.class})
public class FTPConnectorIT {

    //path FTP
    private static final String FTP_MIGRACION_PATH = "./migracion";

    @Qualifier("FTPRemoteConnector")
    @Autowired
    private IRemoteConnector ftpConnector;
    private static Path targetFolderPath;
    private static Path testFtpDownloadFolderPath;
    
    @BeforeClass
    public static void beforeClass() throws IOException {
        targetFolderPath = Paths.get("target");
        testFtpDownloadFolderPath = Files.createTempDirectory(targetFolderPath, "TestFTPDownload");
    }

    @After
    public void cleanUp() throws Exception {
        ftpConnector.close();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        Files.walk(testFtpDownloadFolderPath)
                .map(Path::toFile)
                .forEach(File::delete);
        
        Files.deleteIfExists(testFtpDownloadFolderPath);
    }
    
    @Test
    public void downloadFromFTPToFS_OK() throws Exception {
        String ftpFilePath = FTP_MIGRACION_PATH+"/blank.PDF";
        Path localFilePath = Paths.get(testFtpDownloadFolderPath.toString(), "test.pdf");
        ftpConnector.downloadToFS(ftpFilePath, localFilePath.toString());

        Assert.assertTrue(localFilePath.toFile().exists());
    }
    
    @Test(expected = RuntimeException.class)
    public void downloadFromFTPToFS_ERROR() throws Exception {
        String fakeFile = testFtpDownloadFolderPath.toAbsolutePath()+"/fakeFile.txt";
        ftpConnector.downloadToFS("testError.txt", fakeFile);
    }

    @Test
    public void getControlFiles_OK() throws Exception {
        List<String> lista = ftpConnector.getFileNameBySuffix(FTP_MIGRACION_PATH, ".csv");
        Assert.assertNotNull(lista);
        Assert.assertEquals(2, lista.size());
    }

    @Test
    public void getControlFiles_KO() throws Exception {
        List<String> lista = ftpConnector.getFileNameBySuffix(FTP_MIGRACION_PATH, ".tmp");
        Assert.assertNotNull(lista);
        Assert.assertEquals(0, lista.size());
    }
}
