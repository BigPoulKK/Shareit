package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestQueryTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemRequestService itemRequestService;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    static Long idUser;
    static Long idRequest;
    static User user;

    @BeforeEach
    void addUser() {
        user = UserMapper.toUser(userService.saveUser(UserDto.builder()
                .name("Пётр")
                .email("petr@mail.com")
                .build()));
        idUser = user.getId();
    }

    @Test
    void createAndGetRequest() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Сверла вместе с дрелью");
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.now().withNano(0));

        ItemRequest itemRequestNew = itemRequestService.createRequest(idUser, itemRequest);

        TypedQuery<ItemRequest> query = em.createQuery("Select u from ItemRequest u where u.id = :id", ItemRequest.class);
        ItemRequest request = query
                .setParameter("id", itemRequestNew.getId())
                .getSingleResult();

        assertThat(request.getId(), notNullValue());
        assertThat(request.getUser(), equalTo(itemRequestNew.getUser()));
        assertThat(request.getDescription(), equalTo(itemRequestNew.getDescription()));
        assertThat(request.getCreated(), equalTo(itemRequestNew.getCreated()));
    }

    @AfterEach
    void delete() {
        em.flush();
        em.clear();
    }
}