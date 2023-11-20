package com.cereixeira.connectors.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class S3Resource implements Resource {
    private static final Logger logger = LoggerFactory.getLogger(S3Resource.class);
    private InputStream inputStream;
    private String filename;
    private long contentLength;

    public S3Resource(InputStream inputStream){
        this.inputStream = inputStream;
    }
    public S3Resource(InputStream inputStream, String filename, long contentLength){
        this.inputStream = inputStream;
        this.filename = filename;
        this.contentLength = contentLength;
    }

    public void close(){
        try {
            if(this.inputStream != null) {
                this.inputStream.close();
            }
        } catch (IOException e) {
            logger.error("#logger - inputStream.close()", e);
        }
    }

    @Override
    public boolean exists() {
        return this.inputStream != null;
    }

    @Override
    public boolean isReadable() {
        return Resource.super.isReadable();
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return this.inputStream;
    }
    @Override
    public long contentLength() throws IOException {
        return this.contentLength;
    }
    @Override
    public String getFilename() {
        return this.filename;
    }




    @Override
    public boolean isOpen() {
        return Resource.super.isOpen();
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public URL getURL() throws IOException {
        return null;
    }

    @Override
    public URI getURI() throws IOException {
        return null;
    }

    @Override
    public File getFile() throws IOException {
        return null;
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }


}
