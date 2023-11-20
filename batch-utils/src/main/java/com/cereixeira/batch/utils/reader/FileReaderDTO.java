package com.cereixeira.batch.utils.reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Component;

@Component
public class FileReaderDTO<T> {
    Logger logger = LogManager.getLogger(FileReaderDTO.class);

    private final static String DELIMITER = ";";

    public FlatFileItemReader<T> getFileItemReader(String filePath, Class<T> inputDTOClass, String[] fields, Integer[] csvPositions){
        logger.debug("#getFileItemReader - filePath:{}", filePath);

        PathResource inputFilePathResource = new PathResource(filePath);

        return new FlatFileItemReaderBuilder<T>()
                .name(inputDTOClass.getName())
                .resource(inputFilePathResource)
                //.encoding("UTF-8")
                //.bufferedReaderFactory(new FileBufferReaderFactory())
                .linesToSkip(1)
                .delimited()
                .delimiter(DELIMITER)
                // campos en csv que se leeran y mapearan con names(fields)
                .includedFields(csvPositions)
                // nombre de los campos en dto
                .names(fields)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<T>() {{
                    setTargetType(inputDTOClass);
                }})
                .strict(false)
                .build();
    }
}
