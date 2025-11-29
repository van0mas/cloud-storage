package org.example.cloudstorage.repository;

import org.example.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
