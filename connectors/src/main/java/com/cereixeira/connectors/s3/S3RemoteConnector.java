package com.cereixeira.connectors.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.cereixeira.connectors.IRemoteConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class S3RemoteConnector implements IRemoteConnector {
    private final Logger logger = LogManager.getLogger(S3RemoteConnector.class);
    @Value("${connector.s3.bucketname}")
    private String bucketName;
    @Value("${connector.s3.withcredentials}")
    boolean withCredentials = false;
    @Value("${connector.s3.awsaccesskey}")
    private String awsAccessKey;
    @Value("${connector.s3.awssecretkey}")
    private String awsSecretKey;
    @Value("${connector.s3.sessiontoken}")
    private String sessionToken;

    private AmazonS3 s3client;
    private Regions region = Regions.EU_CENTRAL_1;

    @PreDestroy
    public void _close() {
        close();
    }

    public void setS3client(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    private AmazonS3 getS3Client() {
        if (s3client == null) {
            synchronized (this) {
                if (s3client == null) {
                    logger.debug("#getS3Client - Nueva conexion");

                    ClientConfiguration cc = new ClientConfiguration()
                            .withMaxErrorRetry (2)
                            .withConnectionTimeout (10000)
                            .withSocketTimeout (10000)
                            .withTcpKeepAlive (true);

                    if( withCredentials ) {
                        // Se ejecutará en test locales, ya que son necesarias credenciales
                        BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
                                awsAccessKey,
                                awsSecretKey,
                                sessionToken);

                        s3client = AmazonS3ClientBuilder.standard()
                                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                                .withRegion(region)
                                .withClientConfiguration(cc)
                                .build();
                    } else{
                        // Se ejecutará en entornos AWS, ya que allí no son necesarias credenciales
                        s3client = AmazonS3ClientBuilder.standard()
                                .withRegion(region)
                                .withClientConfiguration(cc)
                                .build();
                    }
                }
            }
        }
        return s3client;
    }
    @Override
    public void close(){
        logger.debug("#close");
        if(s3client != null){
            s3client.shutdown();
            s3client = null;
        }
    }

    @Override
    public void downloadToFS(String remoteFilePath, String localFilePath) throws Exception {
        logger.trace("#downloadToFS - remoteFilePath: "+remoteFilePath+", localFilePath: "+localFilePath);
        FileOutputStream fos = null;
        S3ObjectInputStream s3is = null;
        try {
            S3Object o = getS3Client().getObject(bucketName, remoteFilePath);
            s3is = o.getObjectContent();
            fos = new FileOutputStream(new File(localFilePath));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
        } catch(Exception e){
            logger.error("#downloadToFS - remoteFilePath: "+remoteFilePath+", localFilePath:"+localFilePath);
            throw e;
        } finally{
            if(s3is != null){
                s3is.close();
            }
            if(fos != null){
                fos.close();
            }
        }
    }
    @Override
    public OutputStream getOutputStream(String remoteFilePath) throws Exception {
        logger.trace("#getOutputStream -  remoteFilePath: "+remoteFilePath);
        OutputStream os = null;
        S3ObjectInputStream s3is = null;
        try {
            S3Object o = getS3Client().getObject(bucketName, remoteFilePath);
            s3is = o.getObjectContent();
            os = new ByteArrayOutputStream();
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                os.write(read_buf, 0, read_len);
            }

        } catch(Exception e){
            logger.error("#getOutputStream - remoteFilePath: "+remoteFilePath);
            throw e;
        } finally{
            if(s3is != null){
                s3is.close();
            }
            if(os != null){
                os.close();
            }
        }
        return os;
    }


    public boolean exist(String remoteFilePath) {
        logger.trace("#exist -  remoteFilePath: "+remoteFilePath);
        return getS3Client().doesObjectExist(bucketName, remoteFilePath);
    }
    public long getLength(String remoteFilePath) {
        ObjectMetadata om = getS3Client().getObjectMetadata(bucketName,remoteFilePath);
        return om.getContentLength();
    }
    @Override
    public InputStream getInputStreamFromRemote(String remoteFilePath) throws Exception {
        logger.trace("#getInputStreamFromRemote -  remoteFilePath: "+remoteFilePath);
        InputStream is;
        try {
            S3Object o = getS3Client().getObject(bucketName, remoteFilePath);
            is = o.getObjectContent();
            return is;

        } catch(Exception e){
            logger.error("#getInputStreamFromRemote - remoteFilePath: "+remoteFilePath);
            throw e;
        }
    }

    @Override
    public List<String> getFileNameBySuffix(String remoteFolderPath, String suffix) {
        logger.trace("#getFilesBySuffix - remoteFolderPath: "+remoteFolderPath+", suffix: "+suffix);
        List<String> controlFiles = new ArrayList<>();
        ListObjectsRequest lor = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(remoteFolderPath);

        ObjectListing objectListing = getS3Client().listObjects( lor );
        List<S3ObjectSummary> list = objectListing.getObjectSummaries();
        while (objectListing.isTruncated()) {
            objectListing = getS3Client().listNextBatchOfObjects (objectListing);
            list.addAll (objectListing.getObjectSummaries());
        }

        for(S3ObjectSummary os : list) {
            String name = os.getKey();
            if(name.endsWith(suffix.toLowerCase()) || name.endsWith(suffix.toUpperCase())){
                //se guarda solo el nombre del fichero
                controlFiles.add(name.substring(name.lastIndexOf("/")+1));
            }
        }
        return controlFiles;
    }

    public void moveObject(String key, String newKey){
        // crear carpeta destino
        putObject(key, "");

        // se obtienen los ficheros de la carpeta origen
        List<S3ObjectSummary> listS3 = getObjects(key);
        for(S3ObjectSummary s3Object: listS3){
            String objectKey = s3Object.getKey();

            // se evita coger el objeto que representa la carpeta
            if(!objectKey.equals(key)) {
                String fileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);

                // crear fichero destino
                getS3Client().copyObject(bucketName, objectKey, bucketName, newKey + fileName);
                // borrar fichero origen
                getS3Client().deleteObject(bucketName, objectKey);
            }
        }

        // eliminar carpeta origen (debe estar vacía)
        getS3Client().deleteObject(bucketName,key);

    }
    public List<S3ObjectSummary> getObjects(String remoteFolderPath) {
        logger.trace("#getFilesBySuffix - remoteFolderPath: "+remoteFolderPath);
        List<String> controlFiles = new ArrayList<>();
        ListObjectsRequest lor = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(remoteFolderPath);

        ObjectListing objectListing = getS3Client().listObjects( lor );
        List<S3ObjectSummary> list = objectListing.getObjectSummaries();
        while (objectListing.isTruncated()) {
            objectListing = getS3Client().listNextBatchOfObjects (objectListing);
            list.addAll (objectListing.getObjectSummaries());
        }
        return list;
    }
    public void putObject(String key, String content){
        logger.info("#putObject - bucketName: "+bucketName+", key: "+key+", content: "+content);
        getS3Client().putObject(bucketName,key, content);
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }
    
    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setWithCredentials(boolean withCredentials) {
        this.withCredentials = withCredentials;
    }

    public void setWithCredentials(String withCredentials) {
        this.withCredentials = Boolean.parseBoolean(withCredentials);
    }
}
