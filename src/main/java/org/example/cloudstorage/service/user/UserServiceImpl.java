package org.example.cloudstorage.service.user;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.user.UserAlreadyExistsException;
import org.example.cloudstorage.exception.user.UserNotFoundException;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public User createUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException();
        }

        return user;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }
}
