package com.cereixeira.batch.utils.exception;

import com.cereixeira.batch.utils.constants.Step;

public class StepSkipRetryException extends StepSkipException {



//    public StepSkipRetryException(String message) {
//        super(message);
//        count++;
//    }

    public StepSkipRetryException(String message, String exceptionType) {
        super(message, exceptionType);
    }

    public StepSkipRetryException(String message, String exceptionType, String informationContext) {
        super(message, exceptionType, informationContext);
    }

    public StepSkipRetryException(String message, String exceptionType, String informationContext, Step step) {
        super(message, exceptionType, informationContext, step);
    }

}
