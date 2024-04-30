package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserQueryTest {

    private final EntityManager em;
    private final UserService service;

    @Test
    void getAllUsers() {
        List<UserDto> userDto = List.of(service.saveUser(UserDto.builder()
                        .name("Пётр")
                        .email("petr@mail.com")
                        .build()),
                service.saveUser(UserDto.builder()
                        .name("Марина")
                        .email("marina@mail.com")
                        .build()));

        TypedQuery<User> query = em.createQuery("Select u from User u", User.class);
        List<User> user = query.getResultList();

        assertThat(user, notNullValue());
        assertThat(user.size(), equalTo(userDto.size()));
    }

    @Test
    void saveAndGetUser() {
        UserDto userDto = UserDto.builder()
                .name("Пётр")
                .email("some@email.com")
                .build();
        service.saveUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query
                .setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void updateUser() {
        UserDto userDtoFirst = service.saveUser(UserDto.builder()
                .name("Пётр")
                .email("som@email.com")
                .build());
        UserDto userDto = service.updateUser(userDtoFirst.getId(), UserDto.builder().name("Семен").build());

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query
                .setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }


    @Test
    void deleteUser() {
        UserDto userDtoFirst = service.saveUser(UserDto.builder()
                .name("П")
                .email("so@email.com")
                .build());
        service.deleteUser(userDtoFirst.getId());
        TypedQuery<User> query = em.createQuery("Select u from User u", User.class);
        List<User> user = query.getResultList();

        assertThat(user, notNullValue());
        assertThat(user.size(), equalTo(0));
    }

    @AfterEach
    void delete() {
        em.flush();
        em.clear();
    }
}