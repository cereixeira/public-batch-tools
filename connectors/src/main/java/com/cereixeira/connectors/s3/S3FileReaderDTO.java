package com.cereixeira.connectors.s3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.SimpleBinaryBufferedReaderFactory;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class S3FileReaderDTO<T> {
    Logger logger = LogManager.getLogger(S3FileReaderDTO.class);

    private final static String DELIMITER = ";";
    @Autowired
    private S3RemoteConnector s3RemoteConnector;

    SimpleBinaryBufferedReaderFactory bufferedReaderFactory = new SimpleBinaryBufferedReaderFactory() {
        @Override
        public BufferedReader create(Resource resource, String s) {
            logger.info("#getFileItemReader - create - resource: {}", resource.toString());
            try {
                InputStreamReader isr = new InputStreamReader(resource.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(isr);
                logger.info("#getFileItemReader - create - bufferedReader: {}", bufferedReader);
                return bufferedReader;
            } catch (Exception e) {
                logger.error("#getFileItemReader", e);
                throw new RuntimeException(e);
            }
        }
    };

    public FlatFileItemReader<T> getFileItemReader(String remoteFilePath, Class<T> inputDTOClass, String[] fields, Integer[] csvPositions) {
        logger.debug("#getFileItemReader - remoteFilePath:{}", remoteFilePath);
        InputStream is = null;
        try {
            is = s3RemoteConnector.getInputStreamFromRemote(remoteFilePath);
        } catch (Exception e) {
            logger.error("#getFileItemReader - remoteFilePath:{}", remoteFilePath, e );
            throw new RuntimeException(e);
        }
        S3Resource resource = new S3Resource(is);
        return new FlatFileItemReaderBuilder<T>()
                .name(inputDTOClass.getName())
                .resource( resource )
                //.encoding("UTF-8")
                .bufferedReaderFactory(bufferedReaderFactory)
                .linesToSkip(1)
                .delimited()
                .delimiter(DELIMITER)
                // campos en csv que se leerán y mapearan con names(fields)
                .includedFields(csvPositions)
                // nombre de los campos en dto
                .names(fields)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<T>() {{
                    setTargetType(inputDTOClass);
                }})
                .strict(false)
                .build();
    }

    public void setS3RemoteConnector(S3RemoteConnector s3RemoteConnector) {
        this.s3RemoteConnector = s3RemoteConnector;
    }
}
