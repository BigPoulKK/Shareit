package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
public class BookingJsonTest {
    @Autowired
    JacksonTester<Booking> json;

    @Test
    void testSerialize() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Vaka");
        user.setEmail("name@mail.ru");
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Сверла в комплекте");
        item.setAvailable(true);
        item.setUserId(1L);
        var dto = new Booking();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2024, 5, 20, 21, 12, 3));
        dto.setEnd(LocalDateTime.of(2024, 5, 21, 21, 12, 3));
        dto.setItem(item);
        dto.setBooker(user);
        dto.setStatus(Status.WAITING);

        var result = json.write(dto);
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("@.start");
        assertThat(result).hasJsonPath("@.end");
        assertThat(result).hasJsonPath("@.item");
        assertThat(result).hasJsonPath("@.booker");
        assertThat(result).hasJsonPath("@.status");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathValue("$.start").isEqualTo("2024-05-20T21:12:03");
        assertThat(result).extractingJsonPathValue("$.end").isEqualTo("2024-05-21T21:12:03");
        assertThat(result).extractingJsonPathValue("$.item.name").isEqualTo(dto.getItem().getName());
        assertThat(result).extractingJsonPathValue("$.booker.name").isEqualTo(dto.getBooker().getName());
        assertThat(result).extractingJsonPathValue("$.status").isEqualTo(dto.getStatus().toString());
    }
}