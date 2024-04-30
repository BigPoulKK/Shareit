package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class BookingJpaTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    private static User userItem;
    private static User userBase;
    private static Item itemBase;
    private static Booking bookingBase;
    private static LocalDateTime date = LocalDateTime.now();
    private static final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start_time"));

    @BeforeEach
    void createItem() {
        User userOne = new User();
        userOne.setName("Vaka");
        userOne.setEmail("name@mail.ru");
        userItem = userRepository.save(userOne);

        Item item = new Item();
        item.setName("Дрель");
        item.setId(1L);
        item.setDescription("Сверла в комплекте");
        item.setAvailable(true);
        item.setUserId(userOne.getId());
        itemBase = itemRepository.save(item);

        User user = new User();
        user.setName("Lik");
        user.setEmail("nameLikun@mail.ru");
        userBase = userRepository.save(user);
    }

    @Test
    void findByBookerId() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findByBookerId(userBase.getId(), pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findByItemIdPast() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2023, 5, 20, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2023, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findByItemIdPast(itemBase.getId(), date, Status.REJECTED);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findByItemIdFuture() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findByItemIdFuture(itemBase.getId(), date, Status.REJECTED);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingPast() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2023, 5, 20, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2023, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingPast(userBase.getId(), date, pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingCurrent() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingCurrent(userBase.getId(), date, pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingFuture() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingFuture(userBase.getId(), date, pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingWaiting() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingWaiting(userBase.getId(), Status.WAITING.name(), pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingRejected() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.REJECTED);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingRejected(userBase.getId(), Status.REJECTED.name(), pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingItemsByOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingItemsByOwner(userItem.getId(), pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingPastItemsByOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2023, 5, 20, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2023, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingPastItemsByOwner(userItem.getId(), date, pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingCurrentItemsByOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingCurrentItemsByOwner(userItem.getId(), date, pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingFutureItemsByOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingFutureItemsByOwner(userItem.getId(), date, pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingWaitingItemsByOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.WAITING);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingWaitingItemsByOwner(userItem.getId(), Status.WAITING.name(), pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @Test
    void findBookingRejectedItemsByOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2024, 2, 15, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(itemBase);
        booking.setBooker(userBase);
        booking.setStatus(Status.REJECTED);
        bookingBase = bookingRepository.save(booking);
        List<Booking> bookings = bookingRepository.findBookingRejectedItemsByOwner(userItem.getId(), Status.REJECTED.name(), pageable);

        assertEquals(1, bookings.size());
        assertEquals(bookingBase, bookings.get(0));
        assertEquals(userBase, bookings.get(0).getBooker());
        assertEquals(itemBase, bookings.get(0).getItem());
    }

    @AfterEach
    void deleteUser() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}