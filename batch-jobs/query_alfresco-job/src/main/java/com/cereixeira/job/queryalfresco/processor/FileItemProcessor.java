package com.cereixeira.job.queryalfresco.processor;

import com.cereixeira.job.queryalfresco.dto.InputRecordDTO;
import com.cereixeira.job.queryalfresco.dto.OutputRecordDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class FileItemProcessor implements ItemProcessor<List<InputRecordDTO>, OutputRecordDTO> {

    private static final Logger logger = LoggerFactory.getLogger(FileItemProcessor.class);

    @Override
    public OutputRecordDTO process(final List<InputRecordDTO> recordDTOList) throws Exception {
        return null;
    }

}
