package com.cereixeira.job.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutputReportEntryDTO {
    private final static String[] FIELDS = {"data", "nodeRef", "completed"};
    private String data;
    private String nodeRef;
    private String completed;

    public static String[] getFields(){
        return FIELDS;
    }
}
