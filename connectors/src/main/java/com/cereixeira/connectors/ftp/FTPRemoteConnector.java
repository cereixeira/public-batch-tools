package com.cereixeira.connectors.ftp;

import com.cereixeira.connectors.AbstractFTPRemoteConnector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Component("FTPRemoteConnector")
public class FTPRemoteConnector extends AbstractFTPRemoteConnector {
    private final Logger logger = LogManager.getLogger(FTPRemoteConnector.class);
    @Value("${connector.ftp.server}")
    private String ftpServer;
    @Value("${connector.ftp.port}")
    private int ftpPort;
    @Value("${connector.ftp.user}")
    private String ftpUser;
    @Value("${connector.ftp.pass}")
    private String ftpPass;
    @Value("${connector.ftp.localmode}")
    private String localMode;


    public static final String ACTIVE_MODE = "ACTIVE_MODE";
    public static final String PASSIVE_MODE = "PASSIVE_MODE";
    private volatile FTPClient ftpClient;

    
    private FTPClient getFtpClientConnection() throws IOException {
        if (ftpClient == null) {
            synchronized (FTPRemoteConnector.class) {
                if (ftpClient == null || !ftpClient.isConnected()) {
                    logger.debug("#getFtpClient - Nueva conexion "+ftpServer+":"+ftpPort);
                    ftpClient = new FTPClient();
                    ftpClient.setRemoteVerificationEnabled(false);
                    ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
                    ftpClient.connect(ftpServer, ftpPort);
                    
                    if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                        ftpClient.disconnect();
                        throw new RuntimeException("#getFtpClientConnection - Exception in connecting to FTP Server. Host: "+ftpServer+":"+ftpPort);
                    }
                    if(PASSIVE_MODE.equals(localMode)) {
                        // usar para los Test
                        ftpClient.enterLocalPassiveMode();
                    } else{
                        // usar para ejecución desde máquina (necesario para Debian en Docker)
                        ftpClient.enterLocalActiveMode();
                    }
                    
                    if( !ftpClient.login(ftpUser, ftpPass) ) {
                        throw new RuntimeException("#getFtpClientConnection - Could not login to FTP Server. user>"+ftpUser);
                    }
                    
                }
            }
        }
        return ftpClient;
    }
    @Override
    public void close() throws IOException {
        logger.debug("#closeFtpClient()");
        if(ftpClient != null) {
            ftpClient.logout();
            ftpClient.disconnect();
            ftpClient = null;
        }
    }
    
    /**
     * Descarga fichero desde FTP hasta FS local
     * @param ftpFilePath
     * @param fsFilePath
     * @throws IOException
     */
    @Override
    public void downloadToFS(String ftpFilePath, String fsFilePath) throws IOException {
        logger.trace("#downloadFromFTPToFS - ftpFilePath"+ftpFilePath+", fsFilePath:"+fsFilePath);
        
        if( StringUtils.isAllBlank(ftpFilePath) || StringUtils.isAllBlank(fsFilePath) ){
            throw new RuntimeException("Error al descargar fichero desde FTP, " +
                    "ftpFilePath"+ftpFilePath+", fsFilePath:"+fsFilePath);
        }
    
        FileOutputStream fos = null;
        OutputStream os = null;
        try {
            fos = getFileOutputStream(fsFilePath);
            os = new BufferedOutputStream(fos);
            getFtpClientConnection().setFileType(FTP.BINARY_FILE_TYPE);
            boolean exito = getFtpClientConnection().retrieveFile(ftpFilePath, os);
            if(!exito){
                throw new RuntimeException("Error al descargar fichero desde FTP "+ftpFilePath+" hacia maquina local "+fsFilePath);
            }
        } finally {
            if (os != null){
                os.close();
            }
            if(fos != null){
                fos.close();
            }
            close();
        }
    }
    /**
     * Se obtiene un FileOutputStream a partir del path del fichero en FileSystem.
     */
    public static FileOutputStream getFileOutputStream(String fsFilePath) throws IOException {
        Path localFilePath = Paths.get(fsFilePath);
        Path localFolderPath = localFilePath.getParent();
        if(!Files.exists(localFolderPath)){
            Files.createDirectories(localFolderPath);
        }
        Files.createFile(localFilePath);
        return new FileOutputStream(localFilePath.toFile(),true);
    }

    @Override
    public InputStream getInputStreamFromRemote(String remoteFilePath) throws Exception {
        return null;
    }

    @Override
    public OutputStream getOutputStream(String remoteFilePath) throws Exception {
        return null;
    }

    @Override
    public List<String> getFileNameBySuffix(String remoteFolderPath, String suffix) throws Exception {
        FTPFileFilter filter = new FTPFileFilter() {
            @Override
            public boolean accept(FTPFile ftpFile) {
                return (ftpFile.isFile()
                        && (ftpFile.getName().endsWith(suffix) ));
            }
        };
        FTPFile[] ftpFileArray = null;
        try {
            ftpFileArray = getFtpClientConnection().listFiles(remoteFolderPath, filter);
        } finally {
            close();
        }
        return Arrays.stream(ftpFileArray)
                .map(f -> f.getName())
                .collect(Collectors.toList());
    }
}
