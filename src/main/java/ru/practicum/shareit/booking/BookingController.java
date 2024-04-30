package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ExceptionEnum;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Pageable;

import java.util.List;
/**
 * TODO Sprint add-bookings.
 */
@RestController
@Validated
@RequestMapping(path = "/bookings")
public class BookingController {

    BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody BookingDto booking) {
        return bookingService.createBooking(booking, userId);
    }

    @GetMapping("/{bookingId}")
    public Booking getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @PatchMapping("/{bookingId}")
    public Booking confirmTheBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId, @RequestParam(name = "approved") boolean approved) {
        return bookingService.confirmTheBooking(userId, bookingId, approved);
    }

    @GetMapping
    public List<Booking> getAllBookingByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(name = "state", defaultValue = "ALL") String state,
                                             @PositiveOrZero  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        try {
            Status stateNew = Status.valueOf(state.toUpperCase());
            Sort sort = Sort.by(Sort.Direction.DESC, "start_time");
            final Pageable pageable = PageRequest.of(from/size, size, sort);
            return bookingService.getAllBookingByUser(userId, stateNew, pageable);
        } catch (RuntimeException e) {
            throw new ExceptionEnum("Unknown state: " + state);
        }
    }

    @GetMapping("/owner")
    public List<Booking> getAllBookingItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @PositiveOrZero  @RequestParam(name = "from", defaultValue = "0") Integer from, @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        try {
            Status stateNew = Status.valueOf(state.toUpperCase());
            Sort sort = Sort.by(Sort.Direction.DESC, "start_time");
            final Pageable pageable = PageRequest.of(from/size, size, sort);
            return bookingService.getAllBookingItemsByOwner(userId, stateNew, pageable);
        } catch (RuntimeException e) {
            throw new ExceptionEnum("Unknown state: " + state);
        }
    }
}