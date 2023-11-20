package com.cereixeira.databases.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, Long> {
    List<ExecutionEntity> findByName(String name);
}
