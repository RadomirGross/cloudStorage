package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.AuthRequestDto;
import com.gross.cloudstorage.exception.UserValidationException;
import com.gross.cloudstorage.mapper.UserMapper;
import com.gross.cloudstorage.model.User;
import com.gross.cloudstorage.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User create(AuthRequestDto authRequest) {
        if (exists(authRequest.getUsername())) {
            throw new UserValidationException("Пользователь с таким именем уже существует");
        }
        User user = userMapper.toEntity(authRequest);
        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }


}
