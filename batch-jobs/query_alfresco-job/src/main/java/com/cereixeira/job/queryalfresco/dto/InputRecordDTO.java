package com.cereixeira.job.queryalfresco.dto;

import com.cereixeira.batch.utils.report.AbstractOutputDTO;
import lombok.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InputRecordDTO extends AbstractOutputDTO {
    public static final String DEL_SEMICOLON = ";";
    public final static String[] FIELDS = {"uuid","value"};

    private String uuid;
    private String value;

    public static String[] getFields(){
        return FIELDS;
    }

    @Override
    public String getLine()  {
        return Arrays.stream(FIELDS)
                .map(i -> getValueByFieldName(i))
                .collect( Collectors.joining(DEL_SEMICOLON) );
    }

    private String getValueByFieldName(String fieldName)  {
        String value = null;
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            value = (String) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    @Override
    public long getDocSize() {
        return 0;
    }
}
