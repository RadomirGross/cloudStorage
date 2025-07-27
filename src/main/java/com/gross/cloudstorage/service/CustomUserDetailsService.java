package com.gross.cloudstorage.service;

import com.gross.cloudstorage.model.User;
import com.gross.cloudstorage.repository.UserRepository;
import com.gross.cloudstorage.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserRepository userRepository, UserService userService) {
        this.userService = userService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь с таким именем не найден");
        }
        return new CustomUserDetails(user.get().getUsername(), user.get().getPassword());
    }
}
