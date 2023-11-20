package com.cereixeira.job.queryalfresco.writer;

import com.cereixeira.batch.utils.report.OutputReport;
import com.cereixeira.job.queryalfresco.constants.BeanName;
import com.cereixeira.job.queryalfresco.dto.InputRecordDTO;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public class QueryWriter implements ItemWriter<List<InputRecordDTO>> {

    @Autowired
    @Qualifier(BeanName.OUTPUT_REPORT)
    protected OutputReport<InputRecordDTO> outputReportUpdateNode;

    @Override
    public void write(List<? extends List<InputRecordDTO>> list) {

        for(List<InputRecordDTO> dtoList : list){
            for(InputRecordDTO dto : dtoList){
                outputReportUpdateNode.addLine(dto);
            }
        }
    }
}
