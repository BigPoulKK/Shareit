package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    @NotNull
    @FutureOrPresent(message = "wrong start date")
    private LocalDateTime start;
    @NotNull
    @Future(message = "wrong end date")
    private LocalDateTime end;
    @NotNull
    private Long itemId;
}