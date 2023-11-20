package com.cereixeira.databases.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

    //List<BlockEntity> findByExecutionAndFileName(ExecutionEntity execution, String filename);

    List<BlockEntity> findByExecutionAndCompleted(ExecutionEntity execution, boolean completed);
    List<BlockEntity> findByExecutionAndRefPath(ExecutionEntity execution, String refPath);

}
