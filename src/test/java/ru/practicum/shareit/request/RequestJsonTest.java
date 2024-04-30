package ru.practicum.shareit.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
public class RequestJsonTest {
    @Autowired
    JacksonTester<ItemRequest> json;

    @Test
    void testSerializeWithNull() throws Exception {
        var dto = new ItemRequest();
        dto.setId(1L);
        dto.setDescription("Набор инструментов");
        dto.setCreated(LocalDateTime.of(2024, 11, 4, 5, 12, 1));

        var result = json.write(dto);
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("@.description");
        assertThat(result).hasJsonPath("@.created");
        assertThat(result).hasJsonPath("@.user");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathValue("@.created").isEqualTo(dto.getCreated().toString());
        assertThat(result).extractingJsonPathValue("$.user").isEqualTo(dto.getUser());
    }

}