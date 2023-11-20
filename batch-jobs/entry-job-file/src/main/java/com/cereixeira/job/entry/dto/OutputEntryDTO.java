package com.cereixeira.job.entry.dto;

import com.cereixeira.databases.repository.EntryEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OutputEntryDTO {

    private EntryEntity entryEntity;

    private Map metadata;

    private String filePath;

    //TODO: add new parameters
}
