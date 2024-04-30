package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest({BookingController.class, BookingMapper.class})
public class BookingMockMvcTest {
    private static final String PATH = "/bookings";
    private static final String PATH_WITH_ID = "/bookings/1";
    private static final String PATH_SEARCH = "/bookings/owner";
    private static final Long ID = 1L;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingServiceImpl bookingService;

    @Test
    void createBooking() throws Exception {
        Booking booking = new Booking();
        booking.setId(ID);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12));
        booking.setEnd(LocalDateTime.of(2024, 5, 25, 21, 12));
        booking.setStatus(Status.WAITING);

        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.of(2024, 5, 20, 21, 12))
                .end(LocalDateTime.of(2024, 5, 25, 21, 12))
                .itemId(ID)
                .build();
        when(bookingService.createBooking(bookingDto, ID)).thenReturn(booking);

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile("controller/request/booking.json")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/booking/testBooking.json")));
    }

    @Test
    void getBooking() throws Exception {
        Booking booking = new Booking();
        booking.setId(ID);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12));
        booking.setEnd(LocalDateTime.of(2024, 5, 25, 21, 12));
        booking.setStatus(Status.WAITING);

        when(bookingService.getBooking(ID, ID)).thenReturn(booking);

        mockMvc.perform(MockMvcRequestBuilders.get(PATH_WITH_ID)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/booking/testBooking.json")));
    }

    @Test
    void confirmTheBooking() throws Exception {
        Booking booking = new Booking();
        booking.setId(ID);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12));
        booking.setEnd(LocalDateTime.of(2024, 5, 25, 21, 12));
        booking.setStatus(Status.APPROVED);

        when(bookingService.confirmTheBooking(ID, ID, true)).thenReturn(booking);

        mockMvc.perform(MockMvcRequestBuilders.patch(PATH_WITH_ID)
                        .header("X-Sharer-User-Id", ID)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/booking/bookingApproved.json")));
    }

    @Test
    void getAllBookingByUser() throws Exception {
        Booking booking = new Booking();
        booking.setId(ID);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12));
        booking.setEnd(LocalDateTime.of(2024, 5, 25, 21, 12));
        booking.setStatus(Status.APPROVED);

        Booking bookingUp = new Booking();
        bookingUp.setId(2L);
        bookingUp.setStart(LocalDateTime.of(2024, 6, 20, 21, 12));
        bookingUp.setEnd(LocalDateTime.of(2024, 6, 25, 21, 12));
        bookingUp.setStatus(Status.WAITING);

        when(bookingService.getAllBookingByUser(ID, Status.ALL,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start_time")))).thenReturn(List.of(booking, bookingUp));

        String s = getContentFromFile("controller/response/booking/getAllBookingByUser.json");
        mockMvc.perform(MockMvcRequestBuilders.get(PATH)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/booking/getAllBookingByUser.json")));
    }

    @Test
    void getAllBookingByUserExceptionStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(PATH)
                        .param("state", "NON")
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    void getAllBookingItemsByOwner() throws Exception {
        Booking booking = new Booking();
        booking.setId(ID);
        booking.setStart(LocalDateTime.of(2024, 5, 20, 21, 12));
        booking.setEnd(LocalDateTime.of(2024, 5, 25, 21, 12));
        booking.setStatus(Status.APPROVED);

        Booking bookingUp = new Booking();
        bookingUp.setId(2L);
        bookingUp.setStart(LocalDateTime.of(2024, 6, 20, 21, 12));
        bookingUp.setEnd(LocalDateTime.of(2024, 6, 25, 21, 12));
        bookingUp.setStatus(Status.WAITING);

        when(bookingService.getAllBookingItemsByOwner(ID, Status.ALL,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start_time")))).thenReturn(List.of(booking, bookingUp));

        String s = getContentFromFile("controller/response/booking/getAllBookingByUser.json");
        mockMvc.perform(MockMvcRequestBuilders.get(PATH_SEARCH)
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        getContentFromFile("controller/response/booking/getAllBookingByUser.json")));
    }

    @Test
    void getAllBookingItemsByOwnerExceptionStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(PATH_SEARCH)
                        .param("state", "NON")
                        .header("X-Sharer-User-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
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