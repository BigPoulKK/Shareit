package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;
import ru.practicum.shareit.exception.ValidationExceptionUser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, UserMapper.class})
public class UserMockMvcTest {

    private static final String PATH = "/users";
    private static final String PATH_WITH_ID = "/users/1";
    private static final Long USER_ID = 1L;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;


    @Test
    void create() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(USER_ID)
                .name("Paul")
                .email("BigPoulk@gmail.com")
                .build();
        when(userService.saveUser(userDto)).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/user.json")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/user/testUser.json")));
    }

    @Test
    void get() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(USER_ID)
                .name("Paul")
                .email("BigPoulk@gmail.com")
                .build();
        when(userService.getUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.get(PATH_WITH_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/user/testUser.json")));
    }

    @Test
    void createBadRequest() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Paul")
                .email(null)
                .build();
        when(userService.saveUser(any())).thenThrow(ValidationExceptionUser.class);

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/userEx.json")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(USER_ID)
                .name("Paul")
                .email("BigPoulk@gmail.com")
                .build();
        when(userService.getUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.delete(PATH_WITH_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void update() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(USER_ID)
                .name("Lara")
                .email("Big@gmail.com")
                .build();

        UserDto userDtoUpdate = UserDto.builder()
                .id(USER_ID)
                .name("Paul")
                .email("BigPoulk@gmail.com")
                .build();
        when(userService.getUser(USER_ID)).thenReturn(userDto);
        when(userService.updateUser(USER_ID, userDtoUpdate)).thenReturn(userDtoUpdate);

        mockMvc.perform(MockMvcRequestBuilders.patch(PATH_WITH_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/user.json")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/user/testUser.json")));
    }

    @Test
    void getAll() throws Exception {
        UserDto userDto1 = UserDto.builder()
                .id(USER_ID)
                .name("Paul")
                .email("BigPoulk@gmail.com")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Polina")
                .email("QUery@gmail.com")
                .build();
        List<UserDto> userDto = List.of(userDto1, userDto2);
        when(userService.getAllUsers()).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.get(PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/user/getAllUsers.json")));
    }


    private String getContentFromFile(String filename) {

        try {
            return Files.readString(ResourceUtils.getFile("classpath:" + filename).toPath(),
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return "";
        }
    }


}