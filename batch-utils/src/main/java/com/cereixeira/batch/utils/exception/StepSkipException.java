package com.cereixeira.batch.utils.exception;

import com.cereixeira.batch.utils.constants.PackageConstants;
import com.cereixeira.batch.utils.constants.Step;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class StepSkipException extends RuntimeException{

    protected static final Logger loggerError = LoggerFactory.getLogger(PackageConstants.JOB_ENTRY_ERROR);
    private Step step;
    private String exceptionType;
    private String informationContext;

//    public StepSkipException(String message){
//        super(message);
//    }
    public StepSkipException(String message, String exceptionType){
        super(message);
        this.exceptionType = exceptionType;
    }
    public StepSkipException(String message, String exceptionType, String informationContext){
        super(message);
        this.exceptionType = exceptionType;
        this.informationContext = informationContext;
    }
    public StepSkipException(String message, String exceptionType, String informationContext, Step step){
        super(message);
        this.exceptionType = exceptionType;
        this.informationContext = informationContext;
        this.step = step;
    }
    public void printErrorLog(){
        printErrorLog(getMessage(), exceptionType, informationContext, step);
    }

    public static void printErrorLog(String message, String exceptionType, String informationContext, Step step){
        loggerError.error("{} - {} - {} - {}",
                StringUtils.rightPad(step.name(), 9),
                StringUtils.rightPad(exceptionType, 15),
                message,
                informationContext);
    }

}
