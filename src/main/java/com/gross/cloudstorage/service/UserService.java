package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.AuthRequestDto;
import com.gross.cloudstorage.exception.UserValidationException;
import com.gross.cloudstorage.model.User;
import com.gross.cloudstorage.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public User createUser(AuthRequestDto authRequest, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername(authRequest.getUsername());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        return user;
    }

    public void validateUser(String username,String password) {


        if (username == null || username.trim().isEmpty()) {
            throw new UserValidationException("Имя пользователя не может быть пустым");
        }
        if (username.length() < 5) {
            throw new UserValidationException("Длинна имени пользователя не должна быть менее 5 символов");
        }
        if (username.length() > 20) {
            throw new UserValidationException("Длинна имени пользователя не должна быть более 20 символов");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new UserValidationException("Пароль не может быть пустым");
        }
        if (password.length() < 5) {
            throw new UserValidationException("Длинна пароля пользователя не должна быть менее 5 символов");
        }
        if (password.length() > 20) {
            throw new UserValidationException("Длинна пароля пользователя не должна быть более 20 символов");
        }

    }


}
