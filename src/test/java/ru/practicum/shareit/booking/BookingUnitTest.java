package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static ru.practicum.shareit.booking.Status.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository repository;
    @InjectMocks
    private BookingServiceImpl service;
    private static final Long id = 1L;
    private static final User user = new User();
    private static final User userItem = new User();
    private static final Item item = new Item();
    private static final Booking booking = new Booking();
    private static final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start_time"));

    @BeforeAll
    static void createUser() {
        userItem.setId(id);
        userItem.setName("Vaka");
        userItem.setEmail("name@mail.ru");
        item.setId(id);
        item.setName("Дрель");
        item.setDescription("Сверла в комплекте");
        item.setAvailable(true);
        item.setUserId(id);
        user.setId(2L);
        user.setName("Vaka");
        user.setEmail("name@mail.ru");
        booking.setId(id);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12, 3));
        booking.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(WAITING);
    }

    @Test
    void createBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        booking.setId(null);
        Mockito.when(repository.save(booking)).thenReturn(booking);

        BookingDto bookingDto = BookingDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(id)
                .build();
        Booking serviceBooking = service.createBooking(bookingDto, user.getId());

        assertNotNull(serviceBooking);
        assertEquals(booking, serviceBooking);
    }

    @Test
    void createBookingUserNotFound() {
        Mockito.when(userRepository.findById(3L)).thenThrow(new UserNotFoundException("User not found"));

        BookingDto bookingDto = BookingDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(id)
                .build();

        assertThrows(UserNotFoundException.class,
                () -> service.createBooking(bookingDto, 3L));
    }

    @Test
    void createBookingItemNotFound() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(3L)).thenThrow(new ItemNotFoundException("Item not found"));

        BookingDto bookingDto = BookingDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(3L)
                .build();

        assertThrows(ItemNotFoundException.class,
                () -> service.createBooking(bookingDto, user.getId()));
    }

    @Test
    void createBookingAccessRightsError() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));


        BookingDto bookingDto = BookingDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(id)
                .build();

        AccessRightsError error = assertThrows(AccessRightsError.class,
                () -> service.createBooking(bookingDto, id));
        assertEquals("you do not have the necessary access rights", error.getMessage());
    }

    @Test
    void createBookingTheItemHasAlreadyBeenBooked() {
        item.setAvailable(false);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));


        BookingDto bookingDto = BookingDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(id)
                .build();

        TheItemHasAlreadyBeenBooked error = assertThrows(TheItemHasAlreadyBeenBooked.class,
                () -> service.createBooking(bookingDto, id));
        assertEquals("You can't book this thing", error.getMessage());
        item.setAvailable(true);
    }

    @Test
    void getBooking() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(booking));

        Booking serviceBooking = service.getBooking(id, id);

        assertNotNull(serviceBooking);
        assertEquals(booking, serviceBooking);
        verify(repository).findById(id);
    }

    @Test
    void getBookingBookingNotFound() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(repository.findById(4L)).thenThrow(new BookingNotFoundException("Booking not found"));

        BookingNotFoundException error = assertThrows(BookingNotFoundException.class,
                () -> service.getBooking(4L, id));
        assertEquals("Booking not found", error.getMessage());
    }

    @Test
    void getBookingAccessRightsError() {
        User noName = new User();
        noName.setId(3L);
        noName.setName("Vi");
        noName.setEmail("nome@mail.ru");
        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.of(noName));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(booking));

        AccessRightsError error = assertThrows(AccessRightsError.class,
                () -> service.getBooking(id, 3L));
        assertEquals("you do not have the necessary access rights", error.getMessage());
    }

    @Test
    void confirmTheBookingWithAPPROVED() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(booking));
        Mockito.when(repository.save(booking)).thenReturn(booking);

        Booking serviceBooking = service.confirmTheBooking(id, id, true);

        assertNotNull(serviceBooking);
        assertEquals(booking.getItem(), serviceBooking.getItem());
        assertEquals(booking.getBooker(), serviceBooking.getBooker());
        assertEquals(Status.APPROVED, serviceBooking.getStatus());
        verify(repository).findById(id);
        verify(repository).save(booking);
    }

    @Test
    void confirmTheBookingWithREJECTED() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(booking));
        Mockito.when(repository.save(booking)).thenReturn(booking);

        Booking serviceBooking = service.confirmTheBooking(id, id, false);

        assertNotNull(serviceBooking);
        assertEquals(booking.getItem(), serviceBooking.getItem());
        assertEquals(booking.getBooker(), serviceBooking.getBooker());
        assertEquals(REJECTED, serviceBooking.getStatus());
        verify(repository).findById(id);
        verify(repository).save(booking);
    }

    @Test
    void confirmTheBookingTheItemHasAlreadyBeenBooked_APPROVED() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        booking.setStatus(Status.APPROVED);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(booking));

        TheItemHasAlreadyBeenBooked error = assertThrows(TheItemHasAlreadyBeenBooked.class,
                () -> service.confirmTheBooking(id, id, true));
        assertEquals("The status has already been confirmed", error.getMessage());
    }

    @Test
    void confirmTheBookingTheItemHasAlreadyBeenBooked_REJECTED() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        booking.setStatus(REJECTED);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(booking));

        TheItemHasAlreadyBeenBooked error = assertThrows(TheItemHasAlreadyBeenBooked.class,
                () -> service.confirmTheBooking(id, id, false));
        assertEquals("The status has already been confirmed", error.getMessage());
    }

    @Test
    void confirmTheBookingUserIdNotEqualsItemUserId() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(booking));
        Mockito.when(repository.save(booking)).thenReturn(booking);

        AccessRightsError error = assertThrows(AccessRightsError.class,
                () -> service.confirmTheBooking(2L, id, false));
        assertEquals("you do not have the necessary access rights", error.getMessage());
    }

    @Test
    void getAllBookingByUser() {
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(repository.findByBookerId(2L, pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingByUser(2L, Status.ALL, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findByBookerId(2L, pageable);
    }

    @Test
    void getAllBookingByUserFindBookingPast() {
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        LocalDateTime date = LocalDateTime.now();
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(repository.findBookingPast(2L, date.withNano(0), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingByUser(2L, Status.PAST, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingPast(2L, date.withNano(0), pageable);
    }

    @Test
    void getAllBookingByUserFindBookingFuture() {
        LocalDateTime date = LocalDateTime.now();
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(repository.findBookingFuture(2L, date.withNano(0), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingByUser(2L, Status.FUTURE, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingFuture(2L, date.withNano(0), pageable);
    }

    @Test
    void getAllBookingByUserFindBookingCurrent() {
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        LocalDateTime date = LocalDateTime.now();
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(repository.findBookingCurrent(2L, date.withNano(0), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingByUser(2L, Status.CURRENT, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingCurrent(2L, date.withNano(0), pageable);
    }

    @Test
    void getAllBookingByUserFindBookingWaiting() {
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(repository.findBookingWaiting(2L, WAITING.name(), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingByUser(2L, WAITING, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingWaiting(2L, WAITING.name(), pageable);
    }

    @Test
    void getAllBookingByUserFindBookingRejected() {
        booking.setStatus(REJECTED);
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        Mockito.when(repository.findBookingRejected(2L, REJECTED.name(), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingByUser(2L, REJECTED, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingRejected(2L, REJECTED.name(), pageable);
        booking.setStatus(WAITING);
    }

    @Test
    void getAllBookingByUserRequestNotFound() {
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.getAllBookingItemsByOwner(id, CANCELED, pageable));
        assertEquals("the request was not found", error.getMessage());
    }

    @Test
    void getAllBookingByUserRequestNotFoundStatusNo() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));

        assertThrows(RuntimeException.class,
                () -> service.getAllBookingByUser(id, Status.valueOf("NO"), pageable));
    }

    @Test
    void getAllBookingItemsByOwner() {
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findBookingItemsByOwner(id, pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingItemsByOwner(id, Status.ALL, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingItemsByOwner(id, pageable);
    }

    @Test
    void getAllBookingItemsByOwnerFindBookingPast() {
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        LocalDateTime date = LocalDateTime.now();
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(repository.findBookingPastItemsByOwner(id, date.withNano(0), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingItemsByOwner(id, Status.PAST, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingPastItemsByOwner(id, date.withNano(0), pageable);
    }

    @Test
    void getAllBookingItemsByOwnerFindBookingFuture() {
        LocalDateTime date = LocalDateTime.now();
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(repository.findBookingFutureItemsByOwner(id, date.withNano(0), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingItemsByOwner(id, Status.FUTURE, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingFutureItemsByOwner(id, date.withNano(0), pageable);
    }

    @Test
    void getAllBookingItemsByOwnerFindBookingCurrent() {
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        LocalDateTime date = LocalDateTime.now();
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(repository.findBookingCurrentItemsByOwner(id, date.withNano(0), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingItemsByOwner(id, Status.CURRENT, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingCurrentItemsByOwner(id, date.withNano(0), pageable);
    }

    @Test
    void getAllBookingItemsByOwnerFindBookingWaiting() {
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(repository.findBookingWaitingItemsByOwner(id, WAITING.name(), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingItemsByOwner(id, WAITING, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingWaitingItemsByOwner(id, WAITING.name(), pageable);
    }

    @Test
    void getAllBookingItemsByOwnerFindBookingRejected() {
        booking.setStatus(REJECTED);
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));
        Mockito.when(repository.findBookingRejectedItemsByOwner(id, REJECTED.name(), pageable)).thenReturn(list);

        List<Booking> serviceBooking = service.getAllBookingItemsByOwner(id, REJECTED, pageable);

        assertNotNull(serviceBooking);
        assertEquals(list, serviceBooking);
        verify(repository).findBookingRejectedItemsByOwner(id, REJECTED.name(), pageable);
        booking.setStatus(WAITING);
    }

    @Test
    void getAllBookingItemsByOwnerRequestNotFound() {
        List<Booking> list = List.of(booking);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(userItem));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.getAllBookingItemsByOwner(id, CANCELED, pageable));
        assertEquals("the request was not found", error.getMessage());
    }

}