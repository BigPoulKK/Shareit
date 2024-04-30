package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationExceptionUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserUnitTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void getAllUsersCollectionInBody() {
        List<User> response = List.of(new User());
        List<UserDto> userDtos = List.of(UserDto.builder().build());
        Mockito.when(userRepository.findAll()).thenReturn(response);

        List<UserDto> userDtoService = userService.getAllUsers();

        assertNotNull(userDtoService);
        assertEquals(userDtos, userDtoService);
        verify(userRepository).findAll();
    }

    @Test
    void checkSaveUser() {
        UserDto userDto = UserDto.builder()
                .name("name")
                .email("name@mail.ru")
                .build();
        Mockito.when(userRepository.save(UserMapper.toUser(userDto))).thenReturn(UserMapper.toUser(userDto));

        UserDto userDtoService = userService.saveUser(userDto);

        assertNotNull(userDtoService);
        assertEquals(userDto, userDtoService);
        verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void checkGetUser() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("name@mail.ru")
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(UserMapper.toUser(userDto)));

        UserDto userDtoService = userService.getUser(1L);

        assertNotNull(userDtoService);
        assertEquals(userDto, userDtoService);
        verify(userRepository).findById(Mockito.anyLong());
    }

    @Test
    void getUserByIdWhenUserNotFound() {
        Mockito.when(userRepository.findById(0L)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class,
                () -> userService.getUser(0L));
    }

    @Test
    void checkUpdateUser() {
        Long id = 1L;
        UserDto userDto = UserDto.builder()
                .id(id)
                .name("name")
                .email("name@mail.ru")
                .build();

        UserDto userDtoNew = UserDto.builder()
                .id(id)
                .name("Kate")
                .email("kate@mail.ru")
                .build();

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(UserMapper.toUser(userDto)));
        Mockito.when(userRepository.save(UserMapper.toUser(userDtoNew))).thenReturn(UserMapper.toUser(userDtoNew));

        UserDto userDtoUpdate = userService.updateUser(id, userDtoNew);

        assertEquals(userDtoNew, userDtoUpdate);
        verify(userRepository).save(Mockito.any(User.class));
        verify(userRepository).findById(Mockito.anyLong());
    }

    @Test
    void UpdateUserNotFound() {
        Long id = 1L;
        UserDto userDto = UserDto.builder()
                .id(id)
                .name("NAME")
                .email("name@mail.ru")
                .build();

        UserDto userDtoNew = UserDto.builder()
                .id(id)
                .name("Kate")
                .email("kate@mail.ru")
                .build();

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(UserMapper.toUser(userDto)));

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(2L, userDtoNew));
        verify(userRepository, never()).save(UserMapper.toUser(userDtoNew));
    }

    @Test
    void checkDeleteUser() {
        List<UserDto> response = List.of(UserDto.builder().build());
        List<User> users = List.of(new User());
        Long id = 1L;
        UserDto userDto = UserDto.builder()
                .id(id)
                .name("NAME")
                .email("name@mail.ru")
                .build();
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(UserMapper.toUser(userDto)));

        userService.deleteUser(id);

        Mockito.when(userRepository.findAll()).thenReturn(users);

        List<UserDto> userDtos = userService.getAllUsers();

        assertEquals(response, userDtos);
        verify(userRepository).deleteById(id);
        verify(userRepository).findById(id);
        verify(userRepository).findAll();
    }

    @Test
    void trappingValidationExceptionUserIfTheNameIsIncorrect() {
        UserDto userDto = UserDto.builder()
                .name(null)
                .email("name@mail.ru")
                .build();

        final ValidationExceptionUser exception = Assertions.assertThrows(
                ValidationExceptionUser.class,
                () -> userService.saveUser(userDto));

        assertEquals("not all data is entered", exception.getMessage());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }

    @Test
    void trappingValidationExceptionUserIfTheEmailIsNull() {
        UserDto userDto = UserDto.builder()
                .name("name")
                .email(null)
                .build();

        final ValidationExceptionUser exception = Assertions.assertThrows(
                ValidationExceptionUser.class,
                () -> userService.saveUser(userDto));

        assertEquals("not all data is entered", exception.getMessage());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }

    @Test
    void trappingValidationExceptionUserIfTheNameIsNull() {
        UserDto userDto = UserDto.builder()
                .name(null)
                .email("name@mail.ru")
                .build();

        final ValidationExceptionUser exception = Assertions.assertThrows(
                ValidationExceptionUser.class,
                () -> userService.saveUser(userDto));

        assertEquals("not all data is entered", exception.getMessage());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }

    @Test
    void UserErrorEmail() {
        Long id = 1L;
        UserDto userDto = UserDto.builder()
                .id(id)
                .name("NAME")
                .email("name@mail.ru")
                .build();

        User user = new User();
        user.setName("Vaka");
        user.setEmail("name@mail.ru");


        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(UserMapper.toUser(userDto)));
        Mockito.when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException("could not execute statement"));

        final DataIntegrityViolationException exception = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.saveUser(UserMapper.toUserDto(user)));

        assertEquals("could not execute statement", exception.getMessage());
        verify(userRepository, never()).save(UserMapper.toUser(userDto));
    }


}