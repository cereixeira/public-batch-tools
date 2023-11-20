package com.cereixeira.batch.ingestion;

import com.cereixeira.job.entry.api.IEntryJobLauncher;
import com.cereixeira.job.entry.api.dto.IParamEntryDTO;
import com.cereixeira.job.entry.s3.constants.S3EntryJobBeanName;
import com.cereixeira.job.entry.s3.dto.ParamEntryDTO;
import com.cereixeira.job.executeblock.api.IExecutionBlockJobLauncher;
import com.cereixeira.job.executeblock.api.dto.IParamExecuteBlockDTO;
import com.cereixeira.job.executeblock.s3.constants.S3ExecuteBlockJobBeanName;
import com.cereixeira.job.executeblock.s3.dto.ParamExecuteBlockDTO;
import com.cereixeira.job.report.ReportEntryJobLauncher;
import com.cereixeira.job.report.dto.ParamReportEntryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class S3Ingestion implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(S3Ingestion.class);

    @Value("${job.ingestion.execution.name}")
    private String executionName;
    @Value("${job.ingestion.input.folder.path}")
    private String inputFolderPath;
    @Value("${job.ingestion.file.suffix}")
    private String fileSuffix;
    @Value("${job.ingestion.output.folder.path}")
    private String outputFolderPath;
    @Autowired
    @Qualifier(S3ExecuteBlockJobBeanName.EXECUTION_JOB_LAUNCHER_S3)
    private IExecutionBlockJobLauncher executionBlockJobLauncher;
    @Autowired
    @Qualifier(S3EntryJobBeanName.ENTRY_JOB_LAUNCHER_S3)
    private IEntryJobLauncher entryJobLauncher;
    @Autowired
    private ReportEntryJobLauncher reportEntryJobLauncher;

    private static ExitCodeGenerator exit = new ExitCodeGenerator() {
        @Override
        public int getExitCode() {
            return 0;
        }
    };

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(S3Ingestion.class, args);
        SpringApplication.exit(context, exit);
    }

    @Override
    public void run(ApplicationArguments args) {

        try {
            logger.info("#run - START");

            IParamExecuteBlockDTO paramExeBlock = new ParamExecuteBlockDTO();
            paramExeBlock.setExecuteName(executionName);
            paramExeBlock.setFileSuffix(fileSuffix);
            paramExeBlock.setFolderPath(inputFolderPath);

            executionBlockJobLauncher.run(paramExeBlock);

            IParamEntryDTO paramEntry = new ParamEntryDTO();
            paramEntry.setExecuteName(executionName);

            entryJobLauncher.run(paramEntry);

            ParamReportEntryDTO paramReport = new ParamReportEntryDTO();
            paramReport.setExecuteName(executionName);
            paramReport.setFolderPath(outputFolderPath);

            reportEntryJobLauncher.run(paramReport);
        } catch (Exception e){
            logger.error("#run", e);
        } finally {
            logger.info("#run - END");
        }
    }

}
