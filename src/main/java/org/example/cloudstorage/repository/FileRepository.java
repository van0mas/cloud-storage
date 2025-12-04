package org.example.cloudstorage.repository;

import org.example.cloudstorage.model.FileEntity;
import org.example.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByObjectKey(String objectKey);
}
