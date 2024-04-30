package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class RequestJpaTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private RequestRepository repository;
    private static User userBase;
    private static ItemRequest itemRequestBase;
    private static final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void createItem() {
        User user = new User();
        user.setName("Vaka");
        user.setEmail("name@mail.ru");
        userBase = userRepository.save(user);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Сверла вместе с дрелью");
        itemRequest.setUser(userBase);
        itemRequest.setCreated(LocalDateTime.now().withNano(0));
        itemRequestBase = repository.save(itemRequest);
    }

    @Test
    void findByUserId() {
        List<ItemRequest> items = repository.findByUserId(userBase.getId());

        assertEquals(1, items.size());
        assertEquals(itemRequestBase, items.get(0));
    }

    @Test
    void findByAllRequests() {
        User user = new User();
        user.setName("Vak");
        user.setEmail("nam@mail.ru");
        User userNew = userRepository.save(user);
        List<ItemRequest> items = repository.findByAllRequests(userNew.getId(), pageable);

        assertEquals(1, items.size());
        assertEquals(itemRequestBase, items.get(0));
    }

    @Test
    void findByAllRequestsForOwner() {
        List<ItemRequest> items = repository.findByAllRequests(userBase.getId(), pageable);

        assertEquals(0, items.size());
    }

    @AfterEach
    void deleteUser() {
        repository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

}