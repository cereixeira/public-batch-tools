package com.cereixeira.job.queryalfresco.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutputRecordDTO{
    private final static String[] fields = {"uuid", "existe", "nombre"};
    private String uuid;
    private String existe;
    private String nombre;


    public static String[] getFields(){
        return fields;
    }


}
