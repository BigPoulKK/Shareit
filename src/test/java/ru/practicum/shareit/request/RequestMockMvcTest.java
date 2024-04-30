package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;
import ru.practicum.shareit.user.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;


@WebMvcTest({ItemRequestController.class})
public class RequestMockMvcTest {
    private static final String PATH = "/requests";
    private static final String PATH_WITH_ID = "/requests/1";
    private static final String PATH_ALL = "/requests/all";
    private static final Long ID = 1L;
    private static final User user = new User();
    private static final User userNew = new User();
    private static final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestServiceImpl requestService;

    @BeforeAll
    static void createUser() {
        user.setId(ID);
        user.setName("Paul");
        user.setEmail("BigPoulk@gmail.com");

        userNew.setId(2L);
        userNew.setName("Lik");
        userNew.setEmail("SmallPoulk@gmail.com");
    }

    @Test
    void create() throws Exception {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(ID);
        itemRequest.setDescription("Дрель, отвертка, пилы");
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.of(2024, 4, 20, 21, 12));
        when(requestService.createRequest(ID, itemRequest)).thenReturn(itemRequest);

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/request.json")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/request/testRequest.json")));
    }

    @Test
    void get() throws Exception {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(ID);
        itemRequest.setDescription("Дрель, отвертка, пилы");
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.of(2024, 4, 20, 21, 12));
        when(requestService.getRequest(ID, ID)).thenReturn(itemRequest);

        mockMvc.perform(MockMvcRequestBuilders.get(PATH_WITH_ID)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/request/testRequest.json")));
    }

    @Test
    void getAllByUser() throws Exception {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(ID);
        itemRequest.setDescription("Чемодан");
        itemRequest.setUser(userNew);
        itemRequest.setCreated(LocalDateTime.of(2024, 4, 20, 21, 12));
        when(requestService.getAllRequestsByUser(2L)).thenReturn(List.of(itemRequest));


        System.out.print(List.of(itemRequest));
        mockMvc.perform(MockMvcRequestBuilders.get(PATH)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/request/getAll.json")));
    }

    @Test
    void getAll() throws Exception {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(ID);
        itemRequest.setDescription("Чемодан");
        itemRequest.setUser(userNew);
        itemRequest.setCreated(LocalDateTime.of(2024, 4, 20, 21, 12));
        when(requestService.getAllRequests(ID, pageable)).thenReturn(List.of(itemRequest));

        mockMvc.perform(MockMvcRequestBuilders.get(PATH_ALL)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/request/getAll.json")));
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