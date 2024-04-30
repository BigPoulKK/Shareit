package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserJpaTest {
    @Autowired
    private UserRepository userRepository;
    private static User userBase;

    @BeforeEach
    void createUser() {
        User user = new User();
        user.setName("name");
        user.setEmail("mail@mail.ru");
        userBase = userRepository.save(user);
    }

    @Test
    void deleteById() {
        userRepository.deleteById(userBase.getId());
        List<User> user = userRepository.findAll();

        assertEquals(0, user.size());
    }

    @AfterEach
    void deleteUser() {
        userRepository.deleteAll();
    }

}