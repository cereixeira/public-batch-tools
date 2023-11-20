package com.cereixeira.batch.utils.writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class FileWriterDTO<T>{
    Logger logger = LogManager.getLogger(FileWriterDTO.class);

    private final static String DELIMITER = ";";



    public FlatFileItemWriter<T> getFileItemWriter(String filePath, Class<T> outputDTOClass, String[] fields){
        logger.debug("#getFileItemReader - filePath:{}", filePath);

        FlatFileHeaderCallback headerCallback = new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                String header = Arrays.stream(fields)
                        .collect(Collectors.joining(DELIMITER));
                writer.write(header);
            }
        };

        PathResource inputFilePathResource = new PathResource(filePath);

        return new FlatFileItemWriterBuilder<T>()
                .name(outputDTOClass.getName())
                .resource(inputFilePathResource)
                .append(true)
                .headerCallback(headerCallback)
                .lineAggregator(new DelimitedLineAggregator<T>(){{
                    setDelimiter(DELIMITER);
                    setFieldExtractor(new BeanWrapperFieldExtractor<T>(){{
                        setNames(fields);
                    }});
                }})
                .build();
    }
}
