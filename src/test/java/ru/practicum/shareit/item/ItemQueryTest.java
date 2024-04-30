package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemQueryTest {
    private final EntityManager em;
    private final ItemService service;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    static Long idUser;
    static Long idItem;

    @BeforeEach
    void addUser() {
        UserDto userDto = userService.saveUser(UserDto.builder()
                .name("Пётр")
                .email("petr@mail.com")
                .build());
        idUser = userDto.getId();
    }

    @Test
    void getUserItems() {
        List<ItemDto> itemDto = List.of(service.addNewItem(idUser, ItemDto.builder()
                        .name("Дрель")
                        .description("Сверла в комплекте")
                        .available(true)
                        .build()),
                service.addNewItem(idUser, ItemDto.builder()
                        .name("Пылесос")
                        .description("Насадки в комплекте")
                        .available(true)
                        .build()));

        TypedQuery<Item> query = em.createQuery("Select u from Item u", Item.class);
        List<Item> items = query.getResultList();

        assertThat(items, notNullValue());
        assertThat(items.size(), equalTo(itemDto.size()));
    }

    @Test
    void addNewItemAndGet() {
        ItemDto itemDto = ItemDto.builder()
                .name("Мышка")
                .description("Коврик в комплекте")
                .available(true)
                .build();

        ItemDto itemDto1 = service.addNewItem(idUser, itemDto);
        idItem = itemDto1.getId();

        TypedQuery<Item> query = em.createQuery("Select u from Item u where u.id = :id", Item.class);
        Item item = query
                .setParameter("id", itemDto1.getId())
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void updateItem() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Сверла в комплекте")
                .available(true)
                .build();
        ItemDto itemDto1 = service.addNewItem(idUser, itemDto);
        idItem = itemDto1.getId();
        ItemDto updateItem = service.updateItem(idItem, idUser, ItemDtoUpdate.builder().description("Отличная дрель").build());

        TypedQuery<Item> query = em.createQuery("Select u from Item u where u.id = :id", Item.class);
        Item item = query
                .setParameter("id", itemDto1.getId())
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(updateItem.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void deleteItem() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Сверла в комплекте")
                .available(true)
                .build();
        ItemDto itemDto1 = service.addNewItem(idUser, itemDto);
        idItem = itemDto1.getId();
        service.deleteItem(idUser, idItem);
        TypedQuery<Item> query = em.createQuery("Select u from Item u", Item.class);
        List<Item> item = query.getResultList();

        assertThat(item, notNullValue());
        assertThat(item.size(), equalTo(0));
    }

    @Test
    void checkSearch() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Сверла в комплекте")
                .available(true)
                .build();
        ItemDto itemDto1 = service.addNewItem(idUser, itemDto);
        idItem = itemDto1.getId();

        TypedQuery<Item> query = em.createQuery("Select u from Item u where (LOWER(u.name) " +
                "LIKE CONCAT('%', ?1, '%') or LOWER(u.description) LIKE CONCAT('%', ?1, '%'))" +
                "and u.available = true", Item.class);
        List<Item> item = query
                .setParameter(1, "сверла")
                .getResultList();

        assertThat(item, notNullValue());
        assertThat(item.size(), equalTo(1));
        assertThat(item.get(0).getName(), equalTo(itemDto.getName()));
    }

    @Test
    void addComment() {
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Сверла в комплекте");
        item.setAvailable(true);
        item.setUserId(idUser);

        Long id = ++idUser;

        User user = new User();
        user.setId(id);
        user.setName("Vaka");
        user.setEmail("name@mail.ru");

        userService.saveUser(UserMapper.toUserDto(user));
        Item item1 = itemRepository.save(item);
        idItem = item1.getId();

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Отличная дрель");
        service.addComment(idUser, idItem, comment);
        TypedQuery<Comment> query = em.createQuery("Select u from Comment u where u.id = :id", Comment.class);
        Comment com = query
                .setParameter("id", 1L)
                .getSingleResult();
        ++idUser;
        assertThat(com.getId(), notNullValue());
        assertThat(com.getText(), equalTo(comment.getText()));
        assertThat(com.getUser(), equalTo(user));
        assertThat(com.getItem(), equalTo(item));
    }

    @AfterEach
    void delete() {
        em.flush();
        em.clear();
    }
}