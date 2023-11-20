package com.cereixeira.job.entry.processor;

import com.cereixeira.batch.utils.constants.Step;
import com.cereixeira.batch.utils.exception.StepSkipException;
import com.cereixeira.databases.cache.DatabaseCache;
import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.EntryEntity;
import com.cereixeira.databases.repository.EntryRepository;
import com.cereixeira.job.entry.constants.JobParam;
import com.cereixeira.job.entry.dto.InputEntryDTO;
import com.cereixeira.job.entry.dto.OutputEntryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.nio.file.Files;
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

    /**
     *
     * @param inputEntryDTO input Entry DTO.
     * @return Output Entry DTO
     * @throws Exception the exceptions are handled by SkipListener using mechanism skip() and retry()
     */
    @Override
    public OutputEntryDTO process(final InputEntryDTO inputEntryDTO) throws Exception {
        logger.debug("#process - inputEntryDTO:{}", inputEntryDTO);
        OutputEntryDTO outputEntryDTO = null;
        try {
            BlockEntity blockEntity = (BlockEntity) jobExecution.getExecutionContext().get(JobParam.CXT_BLOCK_ENTITY_NOT_COMPLETED);

            outputEntryDTO = doProcess(inputEntryDTO, blockEntity);

        } catch (Exception e) {
            logger.error("#process - [see logger error] - inputEntryDTO: {}",inputEntryDTO, e);
            throw e;
        }

        return outputEntryDTO;
    }

    private OutputEntryDTO doProcess(InputEntryDTO inputEntryDTO, BlockEntity blockEntity) throws Exception {
        OutputEntryDTO outputEntryDTO = null;

        final Path refPath = Paths.get(blockEntity.getRefPath());
        final String filePath =  refPath.getParent()+"/"+inputEntryDTO.getFileName();
        final String uniqueRef = getUniqueRef(blockEntity.getExecution().getName(),
                refPath.getFileName().toString(),
                inputEntryDTO.getId(),
                inputEntryDTO.getFileName());
        // validate inputEntryDTO parameters and uniqueRef in the running instance.
        if( validateInputFormat(inputEntryDTO, filePath) ) {
            EntryEntity entryEntity = getEntryEntity(inputEntryDTO, blockEntity, uniqueRef);
            if(entryEntity != null) {
                outputEntryDTO = new OutputEntryDTO();
                outputEntryDTO.setFilePath(filePath);
                outputEntryDTO.setEntryEntity(entryEntity);
                logger.info("#doProcess - created/retrieved - outputEntryDTO: {}",outputEntryDTO);
            }
        }
        return outputEntryDTO;
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
    private synchronized EntryEntity getEntryEntity(InputEntryDTO inputEntryDTO, BlockEntity blockEntity, String uniqueRef) throws Exception {
        logger.debug("#getEntryEntity - inputEntryDTO:{}, blockEntity:{}, uniqueRef:{}", inputEntryDTO, blockEntity, uniqueRef);
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
            }else{
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
//            String hash = DigestUtils.md5DigestAsHex(json.getBytes());
//            newEntryEntity.setUniqueHash(hash);
            entryEntity = entryRepository.save(newEntryEntity);
            logger.debug("#getEntryEntity - new entryEntity: {}", entryEntity);
        }
        return entryEntity;
    }

    private boolean validateInputFormat(InputEntryDTO inputEntryDTO, String filePath) {
        boolean validate = true;

        // Validate InputEntryDTO from properties annotations
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<InputEntryDTO>> errors = validator.validate(inputEntryDTO);

        // Validations from annotations
        if(!errors.isEmpty()){
            for(ConstraintViolation<InputEntryDTO> error : errors){
                logger.error("#validate - [see logger error] - inputEntryDTO: {}, error: {}", inputEntryDTO, error.getMessage());
                StepSkipException.printErrorLog(error.getMessage(), "Validation", inputEntryDTO.toString(), Step.PROCESSOR);
            }
            validate = false;
        }
        // Validations custom
        if( ! Files.isReadable(Paths.get(filePath)) ){
            String msgError = "The file path "+filePath+" does not exit";
            logger.error("#validate - [see logger error] - inputEntryDTO: {}, error: {}", inputEntryDTO, msgError);
            StepSkipException.printErrorLog(msgError, "Validation", inputEntryDTO.toString(), Step.PROCESSOR);
            validate = false;
        }

        return validate;
    }

    private String getUniqueRef(String executionName, String controlFile, String id, String fileName){
        return executionName +
                "_" + controlFile +
                "_" + id +
                "_" + fileName;
    }

}
