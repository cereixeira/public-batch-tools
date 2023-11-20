package com.cereixeira.job.entry.api;

import com.cereixeira.job.entry.api.dto.IParamEntryDTO;

public interface IEntryJobLauncher {

    void run(IParamEntryDTO inputParamDTO);
}
