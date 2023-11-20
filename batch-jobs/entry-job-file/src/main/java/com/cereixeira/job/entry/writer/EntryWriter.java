package com.cereixeira.job.entry.writer;


import com.cereixeira.batch.utils.exception.StepSkipRetryException;
import com.cereixeira.databases.repository.EntryEntity;
import com.cereixeira.databases.repository.EntryRepository;
import com.cereixeira.job.entry.dto.OutputEntryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class EntryWriter implements ItemWriter<OutputEntryDTO> {
    private static final Logger logger = LoggerFactory.getLogger(EntryWriter.class);

    @Autowired
    private EntryRepository entryRepository;

    @Override
    public void write(List<? extends OutputEntryDTO> list) throws Exception{
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

        try {
            // TODO: INI - business logic
            // handle file size Kb
            Path path = Paths.get(outputEntryDTO.getFilePath());
            entryEntity.setFileSize(Float.valueOf(path.toFile().length() / (1024)));
            FileInputStream fis = new FileInputStream(path.toFile());

            File tmpFile = File.createTempFile("test", ".pdf", path.toFile().getParentFile());
            FileOutputStream fos = new FileOutputStream(tmpFile);
            fos.write(fis.readAllBytes());

            // FIXME: INIT force random exception
            /*if(outputEntryDTO.getEntryEntity().getUniqueRef().contains("30006")){
                throw new StepSkipRetryException("### TEST ### Forced error in the class EntryWriter.....");
            }*/
            if(getRandomBoolean()){
                throw new NullPointerException("### TEST ### Forced error in the class EntryWriter.....");
            }
            // FIXME: END force random exception

            String nodeRef = getRandomNodeRef();

            entryEntity.setNodeRef(nodeRef);
            entryEntity.setCompleted(true);
            entryEntity.setError(null);
            // TODO: END - business logic

        } catch (Throwable e){
            logger.error("#doWrite - entryEntity: {}",entryEntity, e);

            entryEntity.setCompleted(false);
            entryEntity.setError(e.getMessage());

            throw new StepSkipRetryException(e.getMessage(), "doWrite");
        } finally {
            Date dateEnd = new Date();
            long diffInMillies = Math.abs(dateEnd.getTime() - dateIni.getTime());
            entryEntity.setDateIni(dateIni);
            entryEntity.setDateEnd(dateEnd);
            entryEntity.setWritingDuration(diffInMillies/1000f);
            entryRepository.save(entryEntity);
            logger.info("#doWrite - entryEntity: {}",entryEntity);
        }
        return entryEntity;
    }


    // TODO: usado para pruebas
    public String getRandomNodeRef() {
        return UUID.randomUUID().toString();
    }

    // TODO: usado para pruebas
    public boolean getRandomBoolean() {
        Random random = new Random();
        return random.nextBoolean();
    }

}
