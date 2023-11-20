package com.cereixeira.job.entry.s3.listener;

import com.cereixeira.batch.utils.constants.Step;
import com.cereixeira.batch.utils.exception.StepSkipException;
import com.cereixeira.batch.utils.exception.StepSkipRetryException;
import com.cereixeira.job.entry.s3.dto.InputEntryDTO;
import com.cereixeira.job.entry.s3.dto.OutputEntryDTO;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

@Component
public class SkipListener {

    @OnSkipInRead
    public void skipInRead(Throwable e){
        if (e instanceof StepSkipException || e instanceof StepSkipRetryException){
            ((StepSkipException) e).printErrorLog();
        } else if (e instanceof FlatFileParseException){
            StepSkipException.printErrorLog(e.getMessage(), "FlatFileParse", "", Step.READER);
        } else{
            StepSkipException.printErrorLog(e.getMessage(), "Exception", "", Step.READER);
        }
    }

    @OnSkipInProcess
    public void skipInProcessor(InputEntryDTO inputEntryDTO, Throwable e){
        if (e instanceof StepSkipException || e instanceof StepSkipRetryException) {
            ((StepSkipException) e).setStep(Step.PROCESSOR);
            ((StepSkipException) e).setInformationContext(inputEntryDTO.toString());
            ((StepSkipException) e).printErrorLog();
        }  else{
            StepSkipException.printErrorLog(e.getMessage(), "Exception", inputEntryDTO.toString(), Step.PROCESSOR);
        }
    }

    @OnSkipInWrite
    public void skipInWrite(OutputEntryDTO outputEntryDTO, Throwable e){
        if (e instanceof StepSkipException || e instanceof StepSkipRetryException){
            ((StepSkipException) e).setStep(Step.WRITER);
            ((StepSkipException) e).setInformationContext(outputEntryDTO.toString());
            ((StepSkipException) e).printErrorLog();
        } else{
            StepSkipException.printErrorLog(e.getMessage(), "Exception", outputEntryDTO.toString(), Step.WRITER);
        }
    }
}
