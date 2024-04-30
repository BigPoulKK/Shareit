package ru.practicum.shareit.request;

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
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestUnitTest {

    @Mock
    private RequestRepository repository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ItemRequestServiceImpl service;
    private static final Long ID = 1L;
    private static final User user = new User();
    private static List<ItemInfo> info;
    private static final ItemRequest itemRequest = new ItemRequest();
    private static final Pageable pageable = PageRequest.of(0, 10);

    @BeforeAll
    static void createUser() {
        user.setId(ID);
        user.setName("Vaka");
        user.setEmail("name@mail.ru");
        itemRequest.setId(ID);
        itemRequest.setDescription("Сверла вместе с дрелью");
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.now().withNano(0));
        info = List.of(ItemInfoImpl.builder()
                .id(ID)
                .name("Дрель")
                .available(true)
                .description("Сверла в комплекте")
                .requestId(ID)
                .build());
    }

    @Test
    void createRequest() {
        Mockito.when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        Mockito.when(repository.save(itemRequest)).thenReturn(itemRequest);

        ItemRequest itemRequestDto = service.createRequest(ID, itemRequest);

        assertNotNull(itemRequestDto);
        assertEquals(itemRequest, itemRequestDto);
        verify(repository).save(itemRequest);
    }

    @Test
    void createRequestUserNotFound() {
        Mockito.when(userRepository.findById(2L)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class,
                () -> service.createRequest(2L, itemRequest));
    }

    @Test
    void getRequest() {
        itemRequest.setItems(info);

        Mockito.when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        Mockito.when(repository.findById(ID)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.findByRequestId(ID)).thenReturn(info);

        ItemRequest itemRequestDto = service.getRequest(ID, ID);

        assertNotNull(itemRequestDto);
        assertEquals(itemRequest, itemRequestDto);
        verify(repository).findById(ID);
    }

    @Test
    void getRequestUserNotFound() {
        Mockito.when(userRepository.findById(2L)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class,
                () -> service.getRequest(2L, ID));
    }

    @Test
    void getRequestItemRequestNotFound() {
        Mockito.when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        Mockito.when(repository.findById(2L)).thenThrow(new RequestNotFoundException("Request not found"));

        assertThrows(RequestNotFoundException.class,
                () -> service.getRequest(ID, 2L));
    }

    @Test
    void getAllRequestsByUser() {
        List<ItemRequest> list = List.of(itemRequest);
        Mockito.when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        Mockito.when(repository.findByUserId(ID)).thenReturn(list);
        Mockito.when(itemRepository.findByRequestId(ID)).thenReturn(info);

        itemRequest.setItems(info);
        List<ItemRequest> listRequest = service.getAllRequestsByUser(ID);

        assertNotNull(listRequest);
        assertEquals(list, listRequest);
        verify(repository).findByUserId(ID);
        verify(itemRepository).findByRequestId(ID);
        verify(userRepository).findById(ID);
    }

    @Test
    void getAllRequestsByUserUserNotFound() {
        Mockito.when(userRepository.findById(2L)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class,
                () -> service.getAllRequestsByUser(2L));
    }

    @Test
    void getAllRequests() {
        List<ItemRequest> list = List.of(itemRequest);
        Mockito.when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        Mockito.when(repository.findByAllRequests(ID, pageable)).thenReturn(list);
        Mockito.when(itemRepository.findByRequestId(ID)).thenReturn(info);

        itemRequest.setItems(info);
        List<ItemRequest> listRequest = service.getAllRequests(ID, pageable);

        assertNotNull(listRequest);
        assertEquals(list, listRequest);
        verify(repository).findByAllRequests(ID, pageable);
        verify(itemRepository).findByRequestId(ID);
        verify(userRepository).findById(ID);
    }

    @Test
    void getAllRequestsUserNotFound() {
        Mockito.when(userRepository.findById(2L)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class,
                () -> service.getAllRequests(2L, pageable));
    }

}