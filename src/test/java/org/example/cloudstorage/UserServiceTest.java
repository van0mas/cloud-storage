package org.example.cloudstorage;

import org.springframework.transaction.annotation.Transactional;
import org.example.cloudstorage.exception.UserAlreadyExistsException;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_shouldSaveUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("password");

        User saved = userService.createUser(user);

        assertNotNull(saved.getId());
        assertEquals("test", saved.getUsername());
    }

    @Test
    void createUser_shouldThrowException_WhenUserAlreadyExists() {
        User user1 = new User();
        user1.setUsername("same");
        user1.setPassword("password");

        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("same"); // повторяем логин
        user2.setPassword("password");

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(user2));
    }
}

