package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemJpaTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    private static User userBase;
    private static Item itemBase;
    private static final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void createItem() {
        User user = new User();
        user.setName("Vaka");
        user.setEmail("name@mail.ru");
        userBase = userRepository.save(user);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Сверла в комплекте");
        item.setAvailable(true);
        item.setUserId(userBase.getId());
        itemBase = itemRepository.save(item);
    }

    @Test
    void findByUserId() {
        List<Item> items = itemRepository.findByUserId(userBase.getId(), pageable);

        assertEquals(1, items.size());
        assertEquals(itemBase, items.get(0));
    }

    @Test
    void deleteById() {
        itemRepository.deleteById(itemBase.getId());
        List<Item> items = itemRepository.findAll();

        assertEquals(0, items.size());
    }

    @Test
    void findItemsWhereContainsTheText() {
        List<ItemInfo> items = itemRepository.findItemsWhereContainsTheText("сверла", pageable);

        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @AfterEach
    void deleteUser() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

}