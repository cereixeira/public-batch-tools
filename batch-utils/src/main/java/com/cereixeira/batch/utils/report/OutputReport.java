package com.cereixeira.batch.utils.report;


import io.micrometer.core.lang.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class OutputReport<T extends AbstractOutputDTO> {
    private Logger logger = LogManager.getLogger(OutputReport.class);
    private FileOutputStream fileOutputStream;
    private String outputFile;
    private String header;
    private int countLines = 0;
    private int countOutputFiles = 1;
    private float sumDocsSize = 0;
    // Mb
    private float maxDocsSize = 50;
    // by page
    private int maxLines = 50000;


    @PreDestroy
    public void close() throws IOException {
        if(fileOutputStream != null) {
            fileOutputStream.close();
        }
    }
    public void init(String outputFile, String header, @Nullable String maxDocsSize, @Nullable String maxLines) throws IOException {
        this.outputFile = outputFile;
        this.header = header;
        this.fileOutputStream = getNewFileOutputStream();
        if(maxDocsSize != null){
            this.maxDocsSize = Float.parseFloat(maxDocsSize);
        }
        if(maxLines != null){
            this.maxLines = Integer.parseInt(maxLines);
        }
        logger.debug("#setFileInputStream - Fichero de salida: {}", outputFile);
    }

    public void addLine(T dto){
        try {
            this.fileOutputStream.write(("\n"+dto.getLine()).getBytes(StandardCharsets.ISO_8859_1));
            this.countLines++;
            this.sumDocsSize += ((float)dto.getDocSize())/1024000;

            checkNewFileRequired(dto);

        } catch (IOException e) {
            logger.error("#deleteById - {} - No se ha podido escribir en el fichero salida.");
        }
    }

    protected void checkNewFileRequired(T dto) throws IOException {
        if(countLines == maxLines){
            createNewOutputFile();
            this.countLines = 0;
            this.sumDocsSize = 0;
        } else if(sumDocsSize > maxDocsSize){
            createNewOutputFile();
            this.countLines = 0;
            this.sumDocsSize = 0;
        }
    }
    private void createNewOutputFile() throws IOException {
        this.fileOutputStream.flush();
        this.fileOutputStream.close();
        this.fileOutputStream = getNewFileOutputStream();
    }

    private FileOutputStream getNewFileOutputStream() throws IOException {
        FileOutputStream fis = new FileOutputStream(outputFile+"_"+ (countOutputFiles++)+".csv");
        fis.write(this.header.getBytes(StandardCharsets.ISO_8859_1));
        return fis;
    }

}
