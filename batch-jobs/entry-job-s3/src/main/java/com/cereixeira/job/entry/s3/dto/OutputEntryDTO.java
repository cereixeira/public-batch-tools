package com.cereixeira.job.entry.s3.dto;

import com.cereixeira.databases.repository.EntryEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.MultiValueMap;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OutputEntryDTO {

    private EntryEntity entryEntity;
    //private S3Resource s3Resource;
    private MultiValueMap<String, Object> metadata;
    private String filePath;
    private InputEntryDTO input;

}
