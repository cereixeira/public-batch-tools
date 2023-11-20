package com.cereixeira.databases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntryRepository extends JpaRepository<EntryEntity, Long> {

    List<EntryEntity> findByUniqueRef(String uniqueRef);
    List<EntryEntity> findByUniqueRefAndCompleted(String uniqueRef, boolean completed);
//    List<EntryEntity> findByUniqueHash(String uniqueHash);
//    List<EntryEntity> findByUniqueHashAndCompleted(String uniqueHash, boolean completed);

    Long countByBlockAndCompleted(BlockEntity block, boolean completed);
    Long countByBlockAndCompletedTrue(BlockEntity block);

    //Long countByBlockAndValidTrue(BlockEntity block);
    Long countByBlock(BlockEntity block);

    @Query("SELECT SUM(e.writingDuration) FROM EntryEntity e WHERE e.block.id = :blockId")
    Float sumWritingDurationByBlock(@Param("blockId")Long id);
    @Query("SELECT SUM(e.fileSize) FROM EntryEntity e WHERE e.block.id = :blockId")
    Float sumFileSizesByBlock(@Param("blockId")Long id);
}
