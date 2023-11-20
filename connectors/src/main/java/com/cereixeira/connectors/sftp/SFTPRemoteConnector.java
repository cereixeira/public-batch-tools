package com.cereixeira.connectors.sftp;

import com.cereixeira.connectors.AbstractFTPRemoteConnector;
import com.cereixeira.connectors.ftp.FTPRemoteConnector;
import com.jcraft.jsch.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Component("SFTPRemoteConnector")
public class SFTPRemoteConnector extends AbstractFTPRemoteConnector {
    private final Logger logger = LogManager.getLogger(SFTPRemoteConnector.class);
    @Value("${connector.sftp.server}")
    private String sftpServer;
    @Value("${connector.sftp.port}")
    private int sftpPort;
    @Value("${connector.sftp.user}")
    private String sftpUser;
    @Value("${connector.sftp.pass}")
    private String sftpPass;
    @Value("${connector.sftp.privatekeypath}")
    private String privateKeyPath;
    @Value("${connector.sftp.privatekeypass}")
    private String privateKeyPass;

    private Session session;
    private ChannelSftp sftpChannel;

    private ChannelSftp getSftpChannel() throws JSchException {
        if (sftpChannel == null) {
            synchronized (FTPRemoteConnector.class) {
                if (sftpChannel == null || !sftpChannel.isConnected()) {
                    logger.debug("#getSftpChannel - Nueva conexion "+sftpServer+":"+sftpPort+", user:"+sftpUser);
                    JSch jsch = new JSch();
                    File privateKey = new File(privateKeyPath);
                    if (privateKey.exists() && privateKey.isFile()) {
                        //jsch.addIdentity(getPrivateKeyPath());
                        jsch.addIdentity(privateKeyPath, sftpPass);
                    }
                    session = jsch.getSession(sftpUser, sftpServer, sftpPort);
                    session.setPassword(privateKeyPass);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();
                    Channel channel = session.openChannel("sftp");
                    channel.connect();
                    sftpChannel = (ChannelSftp) channel;
                }
            }
        }
        return sftpChannel;
    }
    @Override
    public void close() throws Exception {
        if(sftpChannel != null){
            sftpChannel.exit();
            sftpChannel.disconnect();
            sftpChannel=null;
        }
        if(session != null){
            session.disconnect();
            session=null;
        }
    }
    
    @Override
    public void downloadToFS(String ftpFilePath, String fsFilePath) throws Exception {
        logger.trace("#downloadFromFTPToFS - ftpFilePath"+ftpFilePath+", fsFilePath:"+fsFilePath);
    
        if( StringUtils.isAllBlank(ftpFilePath) || StringUtils.isAllBlank(fsFilePath) ){
            throw new RuntimeException("Error al descargar fichero desde FTP, " +
                    "ftpFilePath"+ftpFilePath+", fsFilePath:"+fsFilePath);
        }
        getSftpChannel().get(ftpFilePath, fsFilePath);
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
        logger.trace("#getControlFiles - ftpControlFolderPath: "+remoteFolderPath);
        ChannelSftp sftp = getSftpChannel();
        final List<String> controlFileList = new ArrayList<>();
        
        ChannelSftp.LsEntrySelector selector = new ChannelSftp.LsEntrySelector() {
            @Override
            public int select(ChannelSftp.LsEntry entry) {
                SftpATTRS attr = entry.getAttrs();
                if (!attr.isDir() && !attr.isLink() && entry.getFilename().endsWith(suffix)
                && entry.getFilename().startsWith("")) {
                    controlFileList.add(entry.getFilename());
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            }
        };
        
        sftp.ls(remoteFolderPath, selector);
        return controlFileList;
    }

}
