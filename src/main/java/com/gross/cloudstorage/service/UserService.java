package com.gross.cloudstorage.service;

import com.gross.cloudstorage.model.User;
import com.gross.cloudstorage.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(User user) {
        return userRepository.save(user);
    }

    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
