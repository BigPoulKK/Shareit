package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;
import ru.practicum.shareit.exception.UserNotFoundException;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ItemController.class, ItemMapper.class})
public class ItemMockMvcTest {

    private static final String PATH = "/items";
    private static final String PATH_WITH_ID = "/items/1";
    private static final String PATH_SEARCH = "/items/search";
    private static final String PATH_COMMENT = "/items/1/comment";
    private static final Long ID = 1L;
    private static final Pageable pageable = PageRequest.of(0, 10);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemServiceImpl itemService;


    @Test
    void create() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(ID)
                .name("Набор инструментов")
                .description("Дрель, отвертка, пилы")
                .available(true)
                .build();
        when(itemService.addNewItem(ID, itemDto)).thenReturn(itemDto);

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/item.json")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/item/testItem.json")));
    }

    @Test
    void get() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(ID)
                .name("Набор инструментов")
                .description("Дрель, отвертка, пилы")
                .available(true)
                .build();
        when(itemService.getItem(ID, ID)).thenReturn(itemDto);

        mockMvc.perform(MockMvcRequestBuilders.get(PATH_WITH_ID)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/item/testItem.json")));
    }

    @Test
    void getNotFound() throws Exception {
        when(itemService.getItem(ID, 2L)).thenThrow(UserNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(PATH_WITH_ID)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void update() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(ID)
                .name("Дрель")
                .description("отвертка, пилы")
                .available(false)
                .build();

        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder()
                .id(ID)
                .name("Набор инструментов")
                .description("Дрель, отвертка, пилы")
                .available(true)
                .build();
        when(itemService.getItem(ID, ID)).thenReturn(itemDto);
        when(itemService.updateItem(ID, ID, itemDtoUpdate)).thenReturn(ItemMapper.toItemDto(itemDtoUpdate));

        mockMvc.perform(MockMvcRequestBuilders.patch(PATH_WITH_ID)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/item.json")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/item/testItem.json")));
    }

    @Test
    void getAll() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("отвертка, пилы")
                .available(false)
                .build();

        ItemDto itemDtoUpdate = ItemDto.builder()
                .id(2L)
                .name("Набор инструментов")
                .description("Дрель, отвертка, пилы")
                .available(true)
                .build();
        List<ItemDto> list = List.of(itemDto, itemDtoUpdate);
        when(itemService.getUserItems(ID, pageable)).thenReturn(list);

        mockMvc.perform(MockMvcRequestBuilders.get(PATH)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/item/getAllItems.json")));
    }

    @Test
    void getItemByQueryField() throws Exception {
        ItemInfo itemDto = ItemInfoImpl.builder()
                .id(ID)
                .name("Набор инструментов")
                .description("Дрель, отвертка, пилы")
                .available(true)
                .build();

        when(itemService.search("отвертка", pageable)).thenReturn(List.of(itemDto));

        mockMvc.perform(MockMvcRequestBuilders.get(PATH_SEARCH)
                        .param("text", "отвертка")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/item/testItemInfo.json")));
    }

    @Test
    void getComment() throws Exception {
        Comment comment = new Comment();
        comment.setId(ID);
        comment.setText("Good");
        when(itemService.addComment(ID, ID, comment)).thenReturn(comment);

        mockMvc.perform(MockMvcRequestBuilders.post(PATH_COMMENT)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/comment.json")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/item/testComment.json")));
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