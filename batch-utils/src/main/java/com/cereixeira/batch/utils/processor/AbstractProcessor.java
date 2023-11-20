package com.cereixeira.batch.utils.processor;

import com.cereixeira.databases.repository.BlockEntity;

public abstract class AbstractProcessor {

    protected abstract String getUniqueRef(BlockEntity blockEntity, Object input);
}
