package com.cereixeira.job.report.processor;


import com.cereixeira.databases.repository.EntryEntity;
import com.cereixeira.job.report.dto.OutputReportEntryDTO;
import org.springframework.batch.item.ItemProcessor;

public class ReportItemProcessor implements ItemProcessor<EntryEntity, OutputReportEntryDTO> {
    @Override
    public OutputReportEntryDTO process(EntryEntity entryEntity) throws Exception {

        OutputReportEntryDTO ouputReportEntryDTO = new OutputReportEntryDTO();

        ouputReportEntryDTO.setData(entryEntity.getUniqueRef());
        ouputReportEntryDTO.setNodeRef(entryEntity.getNodeRef());
        ouputReportEntryDTO.setCompleted(Boolean.toString(entryEntity.isCompleted()));

        return ouputReportEntryDTO;
    }
}
