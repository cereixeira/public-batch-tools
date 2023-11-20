package com.cereixeira.job.entry.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class InputEntryDTO {

    @Size(min = 5, message = "The field 'fileName' is ${validatedValue}, it can not be less than {min}")
    protected String fileName;

    @NotBlank(message = "the field 'id' can not be blank")
    protected String id;
//    @NotBlank(message = "The field 'refPath' can not be blank'")
//    protected String controlFile;
//    @NotBlank(message = "The field 'executionName' can not be blank'")
//    protected String executionName;
//
//    @NotBlank(message = "The field 'filePath' can not be blank'")
//    protected String filePath;

}
