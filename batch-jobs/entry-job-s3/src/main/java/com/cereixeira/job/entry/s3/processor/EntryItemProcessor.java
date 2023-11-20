package com.cereixeira.job.entry.s3.processor;

import com.cereixeira.batch.utils.constants.Step;
import com.cereixeira.batch.utils.exception.StepSkipException;
import com.cereixeira.databases.cache.DatabaseCache;
import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.EntryEntity;
import com.cereixeira.databases.repository.EntryRepository;
import com.cereixeira.job.entry.s3.constants.JobParam;
import com.cereixeira.job.entry.s3.dto.InputEntryDTO;
import com.cereixeira.job.entry.s3.dto.OutputEntryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * This class has to be thread-safe.
 */
public class EntryItemProcessor implements ItemProcessor<InputEntryDTO, OutputEntryDTO> {
    private static final Logger logger = LoggerFactory.getLogger(EntryItemProcessor.class);
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.jobExecution = stepExecution.getJobExecution();
    }
    private JobExecution jobExecution;
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Autowired
    private DatabaseCache ingestionCache;
    @Autowired
    private EntryRepository entryRepository;
    @Autowired
    private BlockRepository blockRepository;

    @Override
    public OutputEntryDTO process(final InputEntryDTO inputEntryDTO) throws Exception {
        logger.debug("#process - inputEntryDTO:{}", inputEntryDTO);
        OutputEntryDTO outputEntryDTO = null;

        try {
            BlockEntity blockEntity = (BlockEntity) jobExecution.getExecutionContext().get(JobParam.CXT_BLOCK_ENTITY_NOT_COMPLETED);

            outputEntryDTO = doProcess(inputEntryDTO, blockEntity);

        } catch (Exception e) {
            outputEntryDTO = null;
            logger.error("#process - inputEntryDTO: {}",inputEntryDTO, e);
            throw e;
        }
        logger.info("#process - inputEntryDTO:{}, output:{}",inputEntryDTO, outputEntryDTO);
        return outputEntryDTO;
    }

    private OutputEntryDTO doProcess(InputEntryDTO inputEntryDTO, BlockEntity blockEntity) throws Exception{
        OutputEntryDTO outputEntryDTO = null;

        final Path refPath = Paths.get(blockEntity.getRefPath());
        final String fileName = refPath.getFileName().toString();
        final String filePath =  (refPath.getParent()+"/"+inputEntryDTO.getFileName()).replace("\\","/");
        final String uniqueRef = getUniqueRef(blockEntity.getExecution().getName(),
                fileName,
                inputEntryDTO.getId(),
                inputEntryDTO.getFileName());

        // validate inputEntryDTO parameters and uniqueRef in the running instance.
        if( validateInputFormat(inputEntryDTO, uniqueRef) ) {
            // get/create EntryEntity (reprocess doc./new doc.)
            EntryEntity entryEntity = getEntryEntity(inputEntryDTO, blockEntity, uniqueRef);
            if(entryEntity != null) {
                logger.debug("#process - process inputEntryDTO:{}, entryEntity",inputEntryDTO, entryEntity);

                outputEntryDTO = new OutputEntryDTO();
                outputEntryDTO.setInput(inputEntryDTO);
                outputEntryDTO.setFilePath(filePath);
                outputEntryDTO.setMetadata(getMetadata(inputEntryDTO, filePath));
                outputEntryDTO.setEntryEntity(entryEntity);
            }
        }
        return outputEntryDTO;
    }

    private MultiValueMap<String, Object> getMetadata(InputEntryDTO inputEntryDTO, String filePath) {
        // TODO: IMPLEMENTACIÓN DE OBTENCIÓN DE METADATOS
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("type", "xyz:type");
        body.add("xyz:id_client", inputEntryDTO.getId());
        body.add("xyz:type_client", inputEntryDTO.getType());
        body.add("cm:description", "test");
        body.add("cm:name", inputEntryDTO.getFileName());
        return body;
    }

    /**
     * Retrieves or Creates the EntryEntity object. Checking the uniqueRef in DB:
     * <ul><li>case 'exist & completed=true': Do nothing.</li>
     * <li>case 'exist & completed=false': Re-process the uncompleted entry.</li>
     * <li>case 'not exist': Creates a new entry.</li></ul>
     * @param inputEntryDTO input entry data
     * @param blockEntity block entry object
     * @param uniqueRef entry unique reference in DB
     * @return EntryEntity object
     * @throws Exception
     */
    synchronized private EntryEntity getEntryEntity(InputEntryDTO inputEntryDTO, BlockEntity blockEntity, String uniqueRef) throws Exception {
        logger.trace("#getEntryEntity - inputEntryDTO:{}, blockEntity:{}, uniqueRef:{}", inputEntryDTO, blockEntity, uniqueRef);
        EntryEntity entryEntity = null;

        List<EntryEntity> uniqueRefList = entryRepository.findByUniqueRef(uniqueRef);

        if(!uniqueRefList.isEmpty()) {
            // entry exists in DB.
            EntryEntity entryEntityDB = uniqueRefList.get(0);
            logger.debug("#getEntryEntity - Found in DB entryEntity:{}", entryEntityDB);

            if(!blockEntity.getId().equals(entryEntityDB.getBlock().getId())){
                // entry exists in DB but the block is different.
                throw new StepSkipException("The entry uniqueRef '"+uniqueRef+"' exists in DB for other block. " +
                        "Current block:"+blockEntity+", DB Entry block:"+entryEntityDB.getBlock(),
                        "Validation");
            }

            if(!entryEntityDB.isCompleted()){
                // entry exists, but it is NOT completed --> re-process
                entryEntity = entryEntityDB;
                logger.debug("#getEntryEntity - exist entryEntity but it is not completed, so it will be re-processed {}",entryEntityDB);
            } else{
                // entry exists, but it is completed --> do nothing
                entryEntity = null;
                logger.debug("#getEntryEntity - exist entryEntity and it is completed, so there is nothing to do {}",entryEntityDB);
            }
        } else{
            // entry is new, so fill basic values --> process
            EntryEntity newEntryEntity = new EntryEntity();
            newEntryEntity.setBlock(blockEntity);
            newEntryEntity.setUniqueRef(uniqueRef);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(inputEntryDTO);
            newEntryEntity.setInputData(json);
            entryEntity = entryRepository.save(newEntryEntity);
            logger.debug("#getEntryEntity - new entryEntity: {}", entryEntity);
        }
        return entryEntity;
    }

    private boolean validateInputFormat(InputEntryDTO inputEntryDTO, String uniqueRef) {
        boolean validate = true;

        // Validate InputEntryDTO from properties annotations
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<InputEntryDTO>> errors = validator.validate(inputEntryDTO);

        // Validations from annotations
        if(!errors.isEmpty()){
            for(ConstraintViolation<InputEntryDTO> error : errors){
                StepSkipException.printErrorLog(error.getMessage(), "Validation", inputEntryDTO.toString(), Step.PROCESSOR);
                validate = false;
            }
        }

        return validate;
    }

    private String getUniqueRef(String executionName, String controlFile, String id, String fileName){
        //TODO: IMPLEMENT USING FIELDS TO UNIQUELY IDENTIFY THE REGISTERS
        return executionName +
                "_" + controlFile +
                "_" + id +
                "_" + fileName;
    }

}
