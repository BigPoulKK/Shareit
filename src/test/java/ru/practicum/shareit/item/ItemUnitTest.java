package ru.practicum.shareit.item;


import org.junit.jupiter.api.Assertions;
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
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingByBooker;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static ru.practicum.shareit.booking.Status.REJECTED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ItemUnitTest {
    @Mock
    private ItemRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ItemServiceImpl service;
    private static final Long id = 1L;
    private static final User user = new User();
    private static final Item item = new Item();
    private static final Pageable pageable = PageRequest.of(0, 10);

    @BeforeAll
    static void createUser() {
        user.setId(id);
        user.setName("Vaka");
        user.setEmail("name@mail.ru");
        item.setId(id);
        item.setName("Дрель");
        item.setDescription("Сверла в комплекте");
        item.setAvailable(true);
        item.setUserId(id);
    }

    @Test
    void getUserItemsCollectionInBody() {
        List<Item> list = List.of(item);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findByUserId(id, pageable)).thenReturn(list);


        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));

        List<ItemDto> dtoService = service.getUserItems(id, pageable);
        ItemDto itemDto = dtoService.get(0);
        itemDto.setId(id);

        assertNotNull(dtoService);
        assertEquals(item, ItemMapper.toItem(itemDto, id));
        verify(repository).findByUserId(id, pageable);
        verify(repository).findById(id);
        verify(userRepository).findById(id);
    }

    @Test
    void addNewItem() {
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(repository.save(item)).thenReturn(item);

        ItemDto itemDto = ItemMapper.toItemDto(item);
        ItemDto itemDtoService = service.addNewItem(id, itemDto);

        assertNotNull(itemDtoService);
        assertEquals(itemDto, itemDtoService);
        verify(repository).save(item);
    }

    @Test
    void addItemByIdWhenUserNotFound() {
        Mockito.when(userRepository.findById(2L)).thenThrow(new UserNotFoundException("User not found"));

        ItemDto itemDto = ItemMapper.toItemDto(item);
        assertThrows(UserNotFoundException.class,
                () -> service.addNewItem(2L, itemDto));
    }

    @Test
    void getItem() {
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(item));

        LocalDateTime date = LocalDateTime.now();
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        Mockito.when(bookingRepository.findByItemIdPast(id, date.withNano(0), REJECTED)).thenReturn(List.of(booking));

        booking.setStart(LocalDateTime.now().plusDays(3));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        Mockito.when(bookingRepository.findByItemIdFuture(id, date.withNano(0), REJECTED)).thenReturn(List.of(booking));

        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(Set.of());
        itemDto.setNextBooking(BookingByBooker.builder().bookerId(2L).build());
        itemDto.setLastBooking(BookingByBooker.builder().bookerId(2L).build());

        ItemDto serviceItem = service.getItem(id, id);

        assertNotNull(serviceItem);
        assertEquals(itemDto, serviceItem);
        verify(repository).findById(Mockito.anyLong());
    }

    @Test
    void getItemByIdWhenItemNotFound() {
        Mockito.when(repository.findById(0L)).thenThrow(new ItemNotFoundException("Item not found"));

        assertThrows(ItemNotFoundException.class,
                () -> service.getItem(0L, id));
    }

    @Test
    void updateItem() {
        Item itemUpdate = new Item();
        itemUpdate.setId(id);
        itemUpdate.setName("Отвертка");
        itemUpdate.setDescription("Набор отверток");
        itemUpdate.setAvailable(true);
        itemUpdate.setRequestId(id);
        itemUpdate.setUserId(id);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.save(itemUpdate)).thenReturn(itemUpdate);

        ItemDtoUpdate itemDto = ItemMapper.toItemDtoUpdate(itemUpdate);
        ItemDto itemDtoUpdate = service.updateItem(id, id, itemDto);

        assertEquals(ItemMapper.toItemDto(itemUpdate), itemDtoUpdate);
        verify(repository).save(itemUpdate);
        verify(repository, Mockito.times(1)).findById(id);
    }

    @Test
    void UpdateItemTheDoesNotHaveAccessRights() {
        User userNew = new User();
        user.setId(2L);
        user.setName("VI");
        user.setEmail("nameZ@mail.ru");

        Item itemUpdate = new Item();
        itemUpdate.setId(id);
        itemUpdate.setName("Отвертка");
        itemUpdate.setDescription("Набор отверток");
        itemUpdate.setAvailable(true);
        itemUpdate.setUserId(2L);

        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userNew));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(item));

        ItemDtoUpdate itemDto = ItemMapper.toItemDtoUpdate(itemUpdate);
        final AccessRightsError exception = Assertions.assertThrows(
                AccessRightsError.class,
                () -> service.updateItem(id, 2L, itemDto));

        assertEquals("You don't have rights", exception.getMessage());

        verify(repository, never()).save(itemUpdate);
    }

    @Test
    void checkDeleteUser() {
        Item itemUpdate = new Item();
        itemUpdate.setId(2L);
        itemUpdate.setName("Отвертка");
        itemUpdate.setDescription("Набор отверток");
        itemUpdate.setAvailable(true);
        itemUpdate.setUserId(id);

        List<Item> list = List.of(item, itemUpdate);
        List<ItemDto> itemsDto = List.of(ItemMapper.toItemDto(item));
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(repository.findByUserId(id, pageable)).thenReturn(list);

        service.deleteItem(id, 2L);

        Mockito.when(repository.findByUserId(id, pageable)).thenReturn(List.of(item));

        List<ItemDto> items = service.getUserItems(id, pageable);
        items.get(0).setComments(null);

        assertEquals(itemsDto, items);
        verify(repository).deleteById(2L);
        verify(repository).findById(id);
        verify(repository).findByUserId(id, pageable);
    }

    @Test
    void trappingValidationExceptionItemIfTheNameIsIncorrect() {
        Item itemUpdate = new Item();
        itemUpdate.setId(2L);
        itemUpdate.setDescription("Набор отверток");
        itemUpdate.setAvailable(true);
        itemUpdate.setUserId(id);

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        ItemDto dto = ItemMapper.toItemDto(itemUpdate);
        final ValidationExceptionUser exception = Assertions.assertThrows(
                ValidationExceptionUser.class,
                () -> service.addNewItem(id, dto));

        assertEquals("not all data is entered", exception.getMessage());
        verify(repository, never()).save(itemUpdate);
    }

    @Test
    void search() {
        List<ItemInfo> info = List.of(ItemInfoImpl.builder()
                .id(item.getId())
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .build());
        Mockito.when(repository.findItemsWhereContainsTheText("Дрель", pageable)).thenReturn(info);

        List<ItemInfo> dto = service.search("Дрель", pageable);

        assertEquals(dto.size(), 1);
        verify(repository).findItemsWhereContainsTheText("Дрель", pageable);
    }

    @Test
    void addComment() {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setText("Отличная дрель");

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(item));

        final TheItemHasAlreadyBeenBooked exception = Assertions.assertThrows(
                TheItemHasAlreadyBeenBooked.class,
                () -> service.addComment(id, id, comment));

        assertEquals("You can't book this thing", exception.getMessage());
        verify(commentRepository, never()).save(comment);
    }

}