package com.cereixeira.connectors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface IRemoteConnector {
    
    void close() throws Exception;
    List<String> getFileNameBySuffix(String remoteFolderPath, String suffix) throws Exception;
    void downloadToFS(String remoteFilePath, String localFilePath) throws Exception;
    InputStream getInputStreamFromRemote(String remoteFilePath) throws Exception;
    OutputStream getOutputStream(String remoteFilePath) throws Exception;
}
