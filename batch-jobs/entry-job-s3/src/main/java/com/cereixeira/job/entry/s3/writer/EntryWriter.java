package com.cereixeira.job.entry.s3.writer;

import com.cereixeira.api.alfresco.CreateDocumentResponse;
import com.cereixeira.api.alfresco.RestClient;
import com.cereixeira.batch.utils.exception.StepSkipRetryException;
import com.cereixeira.connectors.s3.S3RemoteConnector;
import com.cereixeira.connectors.s3.S3Resource;
import com.cereixeira.databases.repository.EntryEntity;
import com.cereixeira.databases.repository.EntryRepository;
import com.cereixeira.job.entry.s3.dto.InputEntryDTO;
import com.cereixeira.job.entry.s3.dto.OutputEntryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class EntryWriter implements ItemWriter<OutputEntryDTO> {
    private static final Logger logger = LoggerFactory.getLogger(EntryWriter.class);

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private S3RemoteConnector s3RemoteConnector;

    @Autowired
    private RestClient alfrescoRestClient;

    @Override
    public void write(List<? extends OutputEntryDTO> list){
        logger.debug("#write - list-size: {}",list.size());
        for (OutputEntryDTO outputEntryDTO : list) {
            try {
                doWrite(outputEntryDTO);
            } catch (StepSkipRetryException e) {
                throw e;
            } catch(Exception e){
                logger.error("#write - outputEntryDTO:{}",outputEntryDTO, e);
                throw e;
            }
        }
    }
    private EntryEntity doWrite(OutputEntryDTO outputEntryDTO) {
        logger.debug("#doWrite - outputEntryDTO:{}", outputEntryDTO);
        EntryEntity entryEntity = outputEntryDTO.getEntryEntity();
        Date dateIni = new Date();
        S3Resource resource = null;
        try {
            // FIXME: force random exception
            //if(getRandomBoolean()){throw new StepSkipRetryException("### TEST ### Forced error in the class EntryWriter.....");}

            resource = getS3Resource(outputEntryDTO.getFilePath(), outputEntryDTO.getInput());

            // business logic
            MultiValueMap<String, Object> body = getBodyWithS3File(outputEntryDTO, resource);

            // TODO: TEST
            CreateDocumentResponse res = alfrescoRestClient.createDocumentPortForObject(body);
            entryEntity.setNodeRef(res.getNodeId());
            //entryEntity.setNodeRef(getRandomNodeRef());


            entryEntity.setFileSize(Float.valueOf(resource.contentLength() / (1024)));
            entryEntity.setCompleted(true);
            entryEntity.setError(null);
            // TODO: END - business logic

        } catch (Throwable e){
            logger.error("#doWrite - entryEntity: {}",entryEntity);

            entryEntity.setCompleted(false);
            entryEntity.setError(e.getMessage());

            throw new StepSkipRetryException(e.getMessage(), "doWrite");
        } finally {
            //close the stream
            resource.close();
            Date dateEnd = new Date();
            long diffInMillies = Math.abs(dateEnd.getTime() - dateIni.getTime());
            entryEntity.setDateIni(dateIni);
            entryEntity.setDateEnd(dateEnd);
            entryEntity.setWritingDuration(diffInMillies/1000f);
            entryEntity.setFileSize(0f);
            entryRepository.save(entryEntity);
            logger.info("#doWrite - entryEntity: {}",entryEntity);
        }
        return entryEntity;
    }

    private S3Resource getS3Resource(String filePath, InputEntryDTO input) throws Exception {
        logger.debug("#getS3Resource - START - filePath:{}, input:{}", filePath, input);

        long contentLength = 0;
        try {
            contentLength = s3RemoteConnector.getLength(filePath);
        } catch(Exception e){
            logger.error("#getS3Resource - filePath:{}, input:{}", filePath, input);
        }
        InputStream is = s3RemoteConnector.getInputStreamFromRemote(filePath);
        S3Resource res = new S3Resource(is, input.getFileName(), contentLength);
        logger.debug("#getS3Resource - END - filePath:{}, input:{}", filePath, input);
        return res;
    }

    private MultiValueMap<String, Object> getBodyWithS3File(OutputEntryDTO outputEntryDTO, S3Resource resource) {
        MultiValueMap<String, Object> body = outputEntryDTO.getMetadata();
        body.add("filedata", resource);
        return body;
    }

    //TODO: usado para pruebas
    public String getRandomNodeRef() {
        return UUID.randomUUID().toString();
    }

    //TODO: usado para pruebas
    public boolean getRandomBoolean() {
        Random random = new Random();
        return random.nextBoolean();
    }

    public void setS3RemoteConnector(S3RemoteConnector s3RemoteConnector) {
        this.s3RemoteConnector = s3RemoteConnector;
    }
}
