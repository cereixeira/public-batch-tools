package com.cereixeira.job.executeblock.api.dto;

public interface IParamExecuteBlockDTO {
    void setExecuteName(String executionName);
    void setFileSuffix(String fileSuffix);
    void setFolderPath(String folderPath);

    String getExecuteName();
    String getFileSuffix();
    String getFolderPath();
}
