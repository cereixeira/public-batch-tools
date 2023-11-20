package com.cereixeira.job.executeblock.api;

import com.cereixeira.job.executeblock.api.dto.IParamExecuteBlockDTO;

public interface IExecutionBlockJobLauncher {

    void run(IParamExecuteBlockDTO inputParamDTO);
}
