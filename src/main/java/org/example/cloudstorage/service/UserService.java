package org.example.cloudstorage.service;

import org.example.cloudstorage.model.User;

public interface UserService {

    User createUser(User user);

    User findByUsername(String username);
}
