package com.cereixeira.job.executeblock.s3.dto;

import com.cereixeira.job.executeblock.api.dto.IParamExecuteBlockDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParamExecuteBlockDTO implements IParamExecuteBlockDTO {
    private String executeName;
    private String fileSuffix;
    private String folderPath;
}
