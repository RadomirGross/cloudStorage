package com.gross.cloudstorage;

import com.gross.cloudstorage.model.User;
import com.gross.cloudstorage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;


    @Test
    void createUser_persistsToDatabase() {
        User user = new User();
        user.setUsername("user1");
        user.setPassword("password");

        User saved = userRepository.save(user);

        assertThat(userRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("user1");
    }

    @Test
    void duplicateUsername_violatesUniqueConstraint() {
        User u1 = new User();
        u1.setUsername("qqqqq");
        u1.setPassword("qqqqq");
        userRepository.save(u1);

        User u2 = new User();
        u2.setUsername("qqqqq");
        u2.setPassword("zzzzz");

        assertThrows(Exception.class, () -> userRepository.saveAndFlush(u2));
    }

    @Test
    void existsByUsername_returnsTrueWhenPresent_andFalseWhenAbsent() {
        User user = new User();
        user.setUsername("user2");
        user.setPassword("password");
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("user2")).isTrue();
        assertThat(userRepository.existsByUsername("unknown_user")).isFalse();
    }



    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        assertThat(userRepository.findByUsername("nobody")).isNotPresent();
    }

    @Test
    void deleteUser_removesRecord() {
        User user = new User();
        user.setUsername("user3");
        user.setPassword("password");
        User saved = userRepository.save(user);

        userRepository.deleteById(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isNotPresent();
    }
}