package org.example.cloudstorage.repository;

import org.example.cloudstorage.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    @Query("SELECT COALESCE(SUM(f.size), 0) FROM FileMetadata f WHERE f.userId = :userId")
    long getTotalSizeByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);

    Optional<FileMetadata> findByUserIdAndPath(Long userId, String path);
}
