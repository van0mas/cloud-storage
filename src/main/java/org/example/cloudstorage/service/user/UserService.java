package org.example.cloudstorage.service.user;

import org.example.cloudstorage.model.User;

public interface UserService {

    User createUser(User user);

    User findByUsername(String username);
}
