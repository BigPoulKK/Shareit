package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

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
public class BookingQueryTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService service;
    static Long idUser = 0L;
    static Long idItem = 0L;
    static Long idBooking = 0L;

    @BeforeEach
    void addUser() {
        ++idUser;
        ++idItem;
        ++idBooking;
        UserDto userDto = userService.saveUser(UserDto.builder()
                .id(idUser)
                .name("Пётр")
                .email("petr@mail.com")
                .build());
        ItemDto itemDto = itemService.addNewItem(userDto.getId(), ItemDto.builder()
                .id(idItem)
                .name("Дрель")
                .description("Сверла в комплекте")
                .available(true)
                .build());
    }

    @Test
    void createBookingAndGet() {
        UserDto userDto = userService.saveUser(UserDto.builder()
                .id(2L)
                .name("Пёт")
                .email("pet@mail.com")
                .build());
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.of(2024, 5, 20, 21, 12, 3))
                .end(LocalDateTime.of(2024, 5, 21, 21, 12, 3))
                .itemId(idItem)
                .build();
        service.createBooking(bookingDto, userDto.getId());

        TypedQuery<Booking> query = em.createQuery("Select u from Booking u where u.id = :id", Booking.class);
        Booking booking = query
                .setParameter("id", idBooking)
                .getSingleResult();
        ++idUser;
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(booking.getStatus(), equalTo(Status.WAITING));
    }

    @Test
    void confirmTheBooking() {
        UserDto userDto = userService.saveUser(UserDto.builder()
                .id(2L)
                .name("Пёт")
                .email("pet@mail.com")
                .build());
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.of(2024, 5, 20, 21, 12, 3))
                .end(LocalDateTime.of(2024, 5, 21, 21, 12, 3))
                .itemId(idItem)
                .build();
        service.createBooking(bookingDto, userDto.getId());
        service.confirmTheBooking(idUser, idBooking, true);

        TypedQuery<Booking> query = em.createQuery("Select u from Booking u where u.id = :id", Booking.class);
        Booking booking = query
                .setParameter("id", idBooking)
                .getSingleResult();
        ++idUser;
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(booking.getStatus(), equalTo(Status.APPROVED));
    }

    @AfterEach
    void delete() {
        em.flush();
        em.clear();
    }
}