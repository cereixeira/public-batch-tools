package com.cereixeira.job.entry.s3.dto;

import com.cereixeira.job.entry.api.dto.IParamEntryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ParamEntryDTO implements IParamEntryDTO {
    private String executeName;
}
