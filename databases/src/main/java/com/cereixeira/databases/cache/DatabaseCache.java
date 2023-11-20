package com.cereixeira.databases.cache;

import com.cereixeira.databases.repository.BlockEntity;
import com.cereixeira.databases.repository.BlockRepository;
import com.cereixeira.databases.repository.ExecutionEntity;
import com.cereixeira.databases.repository.ExecutionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DatabaseCache {
    private Logger logger = LogManager.getLogger(DatabaseCache.class);
    public final static String GET_BLOCK_ENTITY = "getBlockEntity";
    public final static String GET_EXECUTION_ENTITY = "getExecutionEntity";

    @Autowired
    private ExecutionRepository executionRepository;
    @Autowired
    private BlockRepository blockRepository;

    @Cacheable(GET_BLOCK_ENTITY)
    public BlockEntity getBlockEntity(Long idBlock) {
        Optional<BlockEntity> optBlockEntity = blockRepository.findById(idBlock);
        return optBlockEntity.get();
    }

    @Cacheable(GET_EXECUTION_ENTITY)
    public ExecutionEntity getExecutionEntity(String name){
        List<ExecutionEntity> list = executionRepository.findByName(name);
        if(list.isEmpty()){
            throw new RuntimeException("The ExecutionEntity has not been found by name '{"+name+"}'");
        }
        return list.get(0);
    }

}
