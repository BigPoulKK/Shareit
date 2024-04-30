package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookingService {

    @Transactional
    Booking createBooking(BookingDto bookingDto, Long userId);

    @Transactional
    Booking getBooking(Long bookingId, Long userId);

    public Booking confirmTheBooking(Long idUser, Long idBooking, boolean confirm);

    @Transactional
    List<Booking> getAllBookingByUser(Long userId, Status state, Pageable pageable);

    List<Booking> getAllBookingItemsByOwner(Long userId, Status state, Pageable pageable);

}