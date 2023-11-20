package com.cereixeira.job.entry.s3.dto;

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

    @NotBlank(message = "the field 'typology' can not be blank")
    protected String typology;

    @NotBlank(message = "the field 'type' can not be blank")
    protected String type;


//    @NotBlank(message = "The field 'refPath' can not be blank'")
//    protected String controlFile;
//    @NotBlank(message = "The field 'executionName' can not be blank'")
//    protected String executionName;
//
//    @NotBlank(message = "The field 'filePath' can not be blank'")
//    protected String filePath;

}
